package org.gh.afriluck.afriluckussd.controllers;

import com.google.gson.JsonObject;
import org.gh.afriluck.afriluckussd.constants.AppConstants;
import org.gh.afriluck.afriluckussd.dto.RecentTickets;
import org.gh.afriluck.afriluckussd.dto.Transaction;
import org.gh.afriluck.afriluckussd.entities.Game;
import org.gh.afriluck.afriluckussd.mapping.TransactionMapper;
import org.gh.afriluck.afriluckussd.repositories.CustomerSessionRepository;
import org.gh.afriluck.afriluckussd.entities.Session;
import org.gh.afriluck.afriluckussd.repositories.GameRepository;
import org.gh.afriluck.afriluckussd.utils.AfriluckCallHandler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RestController("/")
public class UssdController {

    private static final int FIRST = 1;
    private static final int SECOND = 2;
    private static final int THIRD = 3;
    private static final int FOURTH = 4;
    private static final int FIFTH = 5;
    private final CustomerSessionRepository sessionRepository;
    private final AfriluckCallHandler handler;
    private final TransactionMapper mapper;
    private final GameRepository gameRepository;
    List<Game> games = null;
    Thread.Builder paymentThread = Thread.ofVirtual().name("Payment Thread");

    public UssdController(
            CustomerSessionRepository sessionRepository,
            AfriluckCallHandler handler,
            TransactionMapper mapper,
            GameRepository gameRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.handler = handler;
        this.mapper = mapper;
        this.gameRepository = gameRepository;
    }


    @PostMapping(path = "/ussd")
    public String index(@RequestBody Session session) throws ExecutionException, InterruptedException {

        String message = null;

        session.setTimeStamp(String.valueOf(LocalDateTime.now()));
        Session savedSession = sessionRepository.findBySequenceID(session.getSequenceID());

        //System.out.println("--- Initial Request ---");
        //System.out.println(session.toString());

        if (savedSession == null) {
            session.setPosition(0);
            // session.setSequenceId(new Date().getTime() +session.getSequenceID());
            sessionRepository.save(session);
        } else {
            System.out.println("--- Updating ---");
            updateSession(session, true);
        }

        if (savedSession != null) {
            message = switch (savedSession.getGameType()) {
                case 1 -> megaGameOptions(savedSession.getGameType(), savedSession.getPosition(), savedSession);
                case 2 -> directGameOptions(savedSession.getGameType(), savedSession.getPosition(), savedSession);
                case 3 -> permGameOptions(savedSession.getGameType(), savedSession.getPosition(), savedSession);
                case 4 -> getDrawResults();
                case 5 -> getLastFiveTransactions(savedSession.getMsisdn());
                case 6 -> tnCsMessage();
                case 99 -> contactUsMessage();
                default -> menuResponse(session, 0, AppConstants.WELCOME_MENU_MESSAGE);

            };
        } else {
            System.out.println("--- Initial Menu ---");
            System.out.printf("Session => %s", session);
            message = menuResponse(session, 0, AppConstants.WELCOME_MENU_MESSAGE);
        }
        return message;
    }


    private void updateSession(Session session, boolean increment) {
        Session savedSession = sessionRepository.findBySequenceID(session.getSequenceID());
        savedSession.setData(session.getData());
        if (increment) {
            savedSession.setPosition(savedSession.getPosition() + 1);
        }

        if (savedSession.getPosition() == 1) {
            savedSession.setGameType(Integer.parseInt(session.getData()));
        }
        sessionRepository.save(savedSession);
    }

    private String megaGameOptions(int gameType, int position, Session s) throws ExecutionException, InterruptedException {
        String message = null;
        Game gameDraw;
        AtomicInteger index = new AtomicInteger(1);
        Session savedSession = sessionRepository.findBySequenceID(s.getSequenceID());
        savedSession.setGameType(1);
        savedSession.setCurrentGame(AppConstants.MEGA);
        updateSession(savedSession, false);
        System.out.println(savedSession.toString());
        if (savedSession.getGameType() == FIRST && savedSession.getPosition() == FIRST) {
            message = AppConstants.MEGA_OPTIONS_CHOICE_MESSAGE;
            //updateSession(savedSession, true);
        } else if (savedSession.getGameType() == FIRST && savedSession.getPosition() == SECOND) {
            StringBuilder messageBuilder = new StringBuilder(AppConstants.AMOUNT_TO_STAKE_MESSAGE);

            games = gameRepository.findAll().stream().distinct().filter(game -> !game.getGameDraw().endsWith("A"))
                    .sorted(Comparator.comparing(Game::getGameDraw)).toList();

            games.stream().forEachOrdered(game -> {
                int currentIndex = index.getAndIncrement();
                messageBuilder.append(String.format("%s) %s\n", currentIndex, game.getAmount()));
            });

            message = messageBuilder.toString();
            savedSession.setSelectedNumbers(s.getData());
            s.setGameTypeCode(Integer.parseInt("1"));
            updateSession(savedSession, true);
        } else if (savedSession.getGameType() == FIRST && savedSession.getPosition() == FOURTH) {
            int amount = 0;
            amount = switch (s.getData()) {
                case "1" -> 5;
                case "2" -> 10;
                case "3" -> 20;
                default -> 0;
            };
            String ticketInfo = """
                    Tck info:
                    Lucky 70 M Mega Jackpot
                    Your numbers: %s
                    \s
                    1) to pay %s GHS on momo.
                    \s
                    2) to apply coupon code.
                    \s
                    0) to cancel.
                    \s""";
            message = String.format(ticketInfo, s.getSelectedNumbers(), amount);
            int finalAmount = amount;
            CompletableFuture<Game> matchAsync = CompletableFuture.supplyAsync(()
                    -> games.stream().filter(game -> game.getAmount() == Double.parseDouble(String.valueOf(finalAmount))).findFirst().get());
            gameDraw = matchAsync.get();
            savedSession.setGameTypeId(gameDraw.getGameDraw());
            savedSession.setAmount(Double.parseDouble(gameDraw.getAmount().toString()));
            savedSession.setGameId(gameDraw.getGameId());
            savedSession.setBetTypeCode("1");
            updateSession(s, true);
        } else {
            gameDraw = new Game();
            message = AppConstants.PAYMENT_INIT_MESSAGE;
            Runnable paymentTask = () -> {
                Transaction t = mapper.mapTransactionFromSession(s, gameDraw);
                System.out.println(t.toString());
                ResponseEntity<String> response = handler.client()
                        .post()
                        .uri("/api/V1/place-bet")
                        .body(t)
                        .contentType(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .toEntity(String.class);
                System.out.println(response.getBody());
            };
            Thread task = paymentThread.start(paymentTask);
            System.out.println(task.threadId());
        }
        return message;
    }

    private String directGameOptions(int gameType, int position, Session s) throws ExecutionException, InterruptedException {
        System.out.printf("Position %s Game %s", position, gameType);
        String message = null;
        Session savedSession = sessionRepository.findBySequenceID(s.getSequenceID());
        Optional<Game> currentGameDraw = gameRepository.findAll().stream().filter(game -> game.getGameDraw().endsWith("A")).findFirst();
        final Game gameDraw = currentGameDraw.get();
        savedSession.setGameId(gameDraw.getGameId());
        savedSession.setGameTypeId(gameDraw.getGameDraw());
        savedSession.setBetTypeCode(AppConstants.DIRECT);
        AtomicInteger index = new AtomicInteger(1);
        List<String> directGames = AppConstants.directGames;
        updateSession(savedSession, false);
        if (savedSession.getGameType()  == SECOND && savedSession.getPosition()  == FIRST) {
            StringBuilder builder = new StringBuilder();
            directGames.stream().forEachOrdered(game -> {
                int currentIndex = index.getAndIncrement();
                builder.append(String.format("%s) %s\n", currentIndex, game.toString()));
            });
            message = builder.toString();
        } else if (savedSession.getGameType()  == SECOND && savedSession.getPosition()  == SECOND) {
            String currentGame = directGames.get(Integer.parseInt(s.getData()) - 1).toString();
            message = """
                    %s
                    Choose a number between 1 and 57
                    99. More info
                    """;
            message = String.format(message, currentGame);
            //s.setGameType(Integer.parseInt(s.getData()));
            s.setGameTypeCode(Integer.parseInt(s.getData()));
            s.setCurrentGame(currentGame);
            updateSession(s, false);
        } else if (gameType == savedSession.getGameType()  && savedSession.getPosition()  == THIRD) {
            message = "Type Amount to Start (1 - 20)";
            savedSession.setSelectedNumbers(s.getData());
            updateSession(savedSession, false);
        } else if (gameType == SECOND && position == FOURTH) {
            String ticketInfo = """
                    Tck info:
                    Lucky 70 M %s
                    Your No: %s
                    \s
                    1) to pay %s GHS on momo.
                    \s
                    2) to apply coupon code.
                    \s
                    0) to cancel.
                    \s""";
            //String directGameName = s.getCurrentGame();
            s.setAmount(Double.parseDouble(s.getData()));
            //s.setCurrentGame(directGameName);
            message = String.format(ticketInfo, s.getCurrentGame(), s.getSelectedNumbers(), s.getAmount());
            updateSession(s, false);
        } else if (savedSession.getGameType()  == SECOND && savedSession.getPosition()  == FIFTH) {
            savedSession.setCurrentGame("direct");
            updateSession(s, true);
            message = AppConstants.PAYMENT_INIT_MESSAGE;
            Runnable paymentTask = () -> {
                Transaction t = mapper.mapTransactionFromSession(s, gameDraw);
                System.out.println(t.toString());
                ResponseEntity<String> response = handler.client()
                        .post()
                        .uri("/api/V1/place-bet")
                        .body(t)
                        .contentType(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .toEntity(String.class);
                System.out.println(response.getBody());
            };
            Thread task = paymentThread.start(paymentTask);
            System.out.println(task.threadId());
        }
        return message;
    }

    public String permGameOptions(int gameType, int position, Session s) {
        String message = null;
        List<String> permGames = AppConstants.permGames;
        AtomicReference<Integer> index = new AtomicReference<>(0);
        Session savedSession = sessionRepository.findBySequenceID(s.getSequenceID());
        savedSession.setCurrentGame(AppConstants.PERM);
        updateSession(savedSession, false);
        if (savedSession.getGameType() == THIRD && savedSession.getPosition() == FIRST) {
            StringBuilder builder = new StringBuilder();
            permGames.stream().forEachOrdered(game -> {
                int currentIndex = index.updateAndGet(v -> v + 1);
                builder.append(String.format("%s) %s\n", currentIndex, game.toString()));
            });
            message = builder.toString();
        }else if (savedSession.getGameType() == THIRD && savedSession.getPosition() == SECOND) {
            savedSession.setGameTypeCode(Integer.parseInt(s.getData()));
            System.out.println("Data"+ s.getData());
            int min = 0;
            int max = 0;
            message = """
                    Choose 3 or not more than 10 numbers\n
                    between 1 & 57 separated by space\n
                    99. More info
                    """;
        }else if (savedSession.getGameType() == THIRD && savedSession.getPosition() == THIRD) {
            message = """
                    Type Amount to Start (1 - 20):
                    """;
        }else if (savedSession.getGameType() == THIRD && savedSession.getPosition() == FOURTH) {
            String ticketInfo = """
                    Tck info:
                    Lucky 70 M %s
                    Your No: %s
                    \s
                    1) to pay %s GHS on momo.
                    \s
                    2) to apply coupon code.
                    \s
                    0) to cancel.
                    \s""";
            s.setAmount(Double.parseDouble(s.getData()));
            message = String.format(ticketInfo, s.getCurrentGame(), s.getSelectedNumbers(), s.getAmount());
            updateSession(s, false);
        }else if (gameType == THIRD && position == FIFTH) {
            message = AppConstants.PAYMENT_INIT_MESSAGE;
        }
        return message;
    }

    private String getDrawResults() {
        ResponseEntity<String> response = handler.client()
                .get()
                .uri("/api/V1/draw-results")
                .retrieve()
                .toEntity(String.class);
        return response.getBody();
    }

    private String getLastFiveTransactions(String msisdn) {
        ResponseEntity<RecentTickets> response = handler.client()
                .get()
                .uri("/api/V1/recent-tickets")
                //.body(String.format("{\"msisdn\":\"%s\"}", msisdn))
                //.contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(RecentTickets.class);
        return response.getBody().ticket;
    }


    private String tnCsMessage() {
        return """
                                TnCs
                You can read here: http://www.afriluck.com/#/
                page-details/terms-and-conditions
                """;
    }

    private String contactUsMessage() {
        return """
                Contact us:
                0303957964
                0303958006
                """;
    }

    public String menuResponse(Session session, int continueFlag, String message) {
        JsonObject json = new JsonObject();
        json.addProperty("msisdn", session.getMsisdn());
        json.addProperty("sequenceID", session.getSequenceID());
        json.addProperty("timestamp", session.getTimeStamp());
        json.addProperty("message", message);
        json.addProperty("continueFlag", continueFlag);
        return json.getAsString();
    }
}

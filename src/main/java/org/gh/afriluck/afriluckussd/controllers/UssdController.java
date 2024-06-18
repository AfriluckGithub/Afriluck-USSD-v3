package org.gh.afriluck.afriluckussd.controllers;

import org.gh.afriluck.afriluckussd.constants.AppConstants;
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

        if (savedSession == null) {
            session.setPosition(0);
            // session.setSequenceId(new Date().getTime() +session.getSequenceID());
            sessionRepository.save(session);
        } else {
            System.out.println("--- Updating initial session ---");
            updateSession(session, true);
            System.out.println(savedSession.toString());
        }

        if (savedSession != null) {
            message = switch (savedSession.getGameType()) {
                case 1 -> megaGameOptions(savedSession.getGameType(), savedSession.getPosition(), savedSession);
                case 2 -> directGameOptions(savedSession.getGameType(), savedSession.getPosition(), savedSession);
                case 3 -> permGameOptions(savedSession.getGameType(), savedSession.getPosition(), savedSession);
                case 4 -> "4";
                case 5 -> tnCsMessage();
                case 99 -> contactUsMessage();
                default -> AppConstants.WELCOME_MENU_MESSAGE;

            };
        } else {
            message = AppConstants.WELCOME_MENU_MESSAGE;
        }
        return message;
    }


    private void updateSession(Session session, boolean increment) {
        Session savedSession = sessionRepository.findBySequenceID(session.getSequenceID());
        savedSession.setData(session.getData());
        if (increment) {
            savedSession.setPosition(savedSession.getPosition() + 1);
        }
        int amount = 0;
        if (savedSession.getPosition() == 1) {
            savedSession.setGameType(Integer.parseInt(session.getData()));
        } else if (savedSession.getGameType() == 1 && savedSession.getPosition() == 2) {
            savedSession.setSelectedNumbers(session.getData());
        } else if (savedSession.getGameType() == 1 && savedSession.getPosition() == 3) {
            amount = switch (session.getData()) {
                case "1" -> 5;
                case "2" -> 10;
                case "3" -> 20;
                default -> 0;
            };
            savedSession.setAmount((double) amount);
        } else if (savedSession.getGameType() == 2 && savedSession.getPosition() == 2) {
            savedSession.setGameTypeId(session.getData());
        } else if (savedSession.getGameType() == 2 && savedSession.getPosition() == 3) {
            savedSession.setSelectedNumbers(session.getData());
        } else if (savedSession.getGameType() == 2 && savedSession.getPosition() == 4) {
            // savedSession.setAmount((double) Integer.parseInt(session.getData()));
        }
        sessionRepository.save(savedSession);
    }

    private String megaGameOptions(int gameType, int position, Session s) throws ExecutionException, InterruptedException {
        String message = null;
        Game gameDraw;
        AtomicInteger index = new AtomicInteger(1);

        if (gameType == FIRST && position == FIRST) {
            gameDraw = new Game();
            message = AppConstants.MEGA_OPTIONS_CHOICE_MESSAGE;
        } else if (gameType == FIRST && position == SECOND) {
            gameDraw = new Game();
            StringBuilder messageBuilder = new StringBuilder(AppConstants.AMOUNT_TO_STAKE_MESSAGE);

            CompletableFuture<List<Game>> gameAsync = CompletableFuture.supplyAsync(()
                    -> gameRepository.findAll().stream().distinct().filter(game -> !game.getGameDraw().endsWith("A"))
                    .sorted(Comparator.comparing(Game::getGameDraw)).toList());

            games = gameAsync.get();

            games.stream().forEachOrdered(game -> {
                int currentIndex = index.getAndIncrement();
                messageBuilder.append(String.format("%s) %s\n", currentIndex, game.getAmount()));
            });

            message = messageBuilder.toString();
        } else if (gameType == FIRST && position == THIRD) {
            Session savedSession = sessionRepository.findBySequenceID(s.getSequenceID());
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
            message = String.format(ticketInfo, s.getSelectedNumbers(), s.getAmount());
            CompletableFuture<Game> matchAsync = CompletableFuture.supplyAsync(()
                    -> games.stream().filter(game -> game.getAmount() == Double.parseDouble(savedSession.getAmount().toString())).findFirst().get());
            gameDraw = matchAsync.get();
            s.setGameTypeId(gameDraw.getGameDraw());
            System.out.println("Game ID" + gameDraw.getGameId());
            s.setAmount(Double.parseDouble(gameDraw.getAmount().toString()));
            s.setGameId(gameDraw.getGameId());
            s.setBetTypeCode(s.getBetTypeCode());
            //s.setGameType("mega");
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
        Optional<Game> currentGameDraw = gameRepository.findAll().stream().filter(game -> game.getGameDraw().endsWith("A")).findFirst();
        final Game gameDraw = currentGameDraw.get();
        s.setGameId(gameDraw.getGameId());
        s.setGameTypeId(gameDraw.getGameDraw());
        s.setBetTypeCode("direct");
        AtomicInteger index = new AtomicInteger(1);
        List<String> directGames = AppConstants.directGames;
        if (gameType == SECOND && position == FIRST) {
            StringBuilder builder = new StringBuilder();
            directGames.stream().forEachOrdered(game -> {
                int currentIndex = index.getAndIncrement();
                builder.append(String.format("%s) %s\n", currentIndex, game.toString()));
            });
            message = builder.toString();
        } else if (gameType == SECOND && position == SECOND) {
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
        } else if (gameType == SECOND && position == THIRD) {
            message = "Type Amount to Start (1 - 20)";
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
        } else if (gameType == SECOND && position == FIFTH) {
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
        if (gameType == THIRD && position == FIRST) {
            StringBuilder builder = new StringBuilder();
            permGames.stream().forEachOrdered(game -> {
                int currentIndex = index.updateAndGet(v -> v + 1);
                builder.append(String.format("%s) %s\n", currentIndex, game.toString()));
            });
            message = builder.toString();
        }else if (gameType == THIRD && position == SECOND) {
            message = """
                    Choose 3 or not more than 10 numbers\n
                    between 1 & 57 separated by space\n
                    99. More info
                    """;
        }else if (gameType == THIRD && position == THIRD) {
            message = """
                    Type Amount to Start (1 - 20):
                    """;
        }else if (gameType == THIRD && position == FOURTH) {
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

    private String getGameType(String gameTypeId) {
        return switch (gameTypeId) {
            case "1" -> "Direct-1(Match first no.)";
            case "2" -> "Direct-2(2 # to win)";
            case "3" -> "Direct-3(3 # to win)";
            case "4" -> "Direct-4(4 # to win)";
            case "5" -> "Direct-5(5 # to win)";
            case "6" -> "Direct-6(6 # to win)";
            default -> throw new IllegalStateException("Unexpected value: " + gameTypeId);
        };
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
}

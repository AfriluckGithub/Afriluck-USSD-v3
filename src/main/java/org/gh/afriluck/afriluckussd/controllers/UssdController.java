package org.gh.afriluck.afriluckussd.controllers;

import com.google.gson.JsonObject;
import org.gh.afriluck.afriluckussd.constants.AppConstants;
import org.gh.afriluck.afriluckussd.dto.Pair;
import org.gh.afriluck.afriluckussd.dto.RecentTickets;
import org.gh.afriluck.afriluckussd.dto.Transaction;
import org.gh.afriluck.afriluckussd.entities.Game;
import org.gh.afriluck.afriluckussd.mapping.TransactionMapper;
import org.gh.afriluck.afriluckussd.repositories.CustomerSessionRepository;
import org.gh.afriluck.afriluckussd.entities.Session;
import org.gh.afriluck.afriluckussd.repositories.GameRepository;
import org.gh.afriluck.afriluckussd.utils.AfriluckCallHandler;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController("/")
public class UssdController {

    private static final int FIRST = 1;
    private static final int SECOND = 2;
    private static final int THIRD = 3;
    private static final int FOURTH = 4;
    private static final int FIFTH = 5;
    private static final int ZERO = 0;
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

    private static List<Integer> extractNumbers(String input) {
        List<Integer> numbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            numbers.add(Integer.parseInt(matcher.group()));
        }

        return numbers;
    }

    private static Set<Integer> findRepeatedNumbers(List<Integer> numbers) {
        Set<Integer> seenNumbers = new HashSet<>();
        Set<Integer> repeatedNumbers = new HashSet<>();

        for (Integer number : numbers) {
            if (!seenNumbers.add(number)) {
                repeatedNumbers.add(number);
            }
        }

        return repeatedNumbers;
    }

    public static boolean isBetween(int number, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum value cannot be greater than maximum value");
        }
        return number >= min && number <= max;
    }

    public static boolean anyNumberExceedsLimit(String numbersString, String delimiter, int limit) {
        // Split the string into an array of number strings
        String[] numberStrings = numbersString.trim().split("\\s+");

        // Iterate through the number strings
        for (String numberString : numberStrings) {
            // Trim any whitespace and parse the number
            int number = Integer.parseInt(numberString.trim());

            // Check if the number exceeds the limit
            if (number > limit) {
                return true;
            }
        }

        return false;
    }

    public static String[] splitNumbers(String numbersString) {
        // Trim the input string and split by one or more spaces
        return numbersString.trim().split("\\s+");
    }

    public static boolean containsAnyLetters(String input) {
        // Define the regex pattern to match any letter
        String regex = "[a-zA-Z]";

        // Compile the regex into a pattern
        Pattern pattern = Pattern.compile(regex);

        // Match the pattern against the input string
        Matcher matcher = pattern.matcher(input);

        // Return whether the input contains any letters
        return matcher.find();
    }

    @PostMapping(path = "/ussd")
    public String index(@RequestBody Session session) throws ExecutionException, InterruptedException {

        String message = null;

        SimpleDateFormat formatter = new SimpleDateFormat(AppConstants.GLOBAL_DATE_FORMAT);
        String timeStamp = formatter.format(new Date());

        session.setTimeStamp(timeStamp);
        Session savedSession = sessionRepository.findBySequenceID(session.getSequenceID());

        //System.out.println("--- Initial Request ---");
        //System.out.println(session.toString());

        if (savedSession == null) {
            session.setPosition(0);
            //session.setSequenceId(new Date().getTime() + session.getSequenceID());
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
                case 4 -> banker(savedSession, "Banker");
                case 5 -> account(savedSession);
                case 6 -> tnCsMessage(savedSession);
                case 99 -> contactUsMessage(savedSession);
                case 0 -> menuResponse(session, 0, AppConstants.WELCOME_MENU_MESSAGE);
                default -> silentDelete(savedSession);

            };
        } else {
            System.out.println("--- Initial Menu ---");
            message = menuResponse(session, 0, AppConstants.WELCOME_MENU_MESSAGE);
            System.out.printf("Session => %s", session.getMessage());
        }
        return message;
    }

    private String account(Session savedSession) {
        String message = null;
        int continueFlag = 0;
        if (savedSession.getGameType() == FIFTH && savedSession.getPosition() == FIRST) {
            message = AppConstants.ACCOUNT_MENU_MESSAGE;
            updateSession(savedSession, false);
        } else if (savedSession.getGameType() == FIFTH && savedSession.getPosition() == SECOND) {
            String response = null;
            String json = null;
            JSONObject oj = null;
            switch (savedSession.getData()) {
                case "0":
                    deleteSession(savedSession);
                    continueFlag = 0;
                    break;
                case "3":
                    continueFlag = 1;
                    response = getDrawResults(savedSession);
                    json = menuResponse(savedSession, continueFlag, response);
                    oj = new JSONObject(json);
                    message = oj.get("message").toString();
                    System.out.println(message);
                    break;
                case "4":
                    continueFlag = 1;
                    response = getLastFiveTransactions(savedSession, savedSession.getMsisdn()).ticket;
                    json = menuResponse(savedSession, continueFlag, response);
                    oj = new JSONObject(json);
                    message = oj.get("message").toString();
                    System.out.println(message);
                    break;
                default:
                    deleteSession(savedSession);
                    break;
            }
            return menuResponse(savedSession, continueFlag, message);
        }
        return menuResponse(savedSession, continueFlag, message);
    }

    private String banker(Session s, String title) {
        String message = null;
        int continueFlag = 0;
        Session savedSession = sessionRepository.findBySequenceID(s.getSequenceID());
        Optional<Game> currentGameDraw = gameRepository.findAll().stream().filter(game -> game.getGameDraw().endsWith("A")).findFirst();
        final Game gameDraw = currentGameDraw.get();
        savedSession.setGameId(gameDraw.getGameId());
        savedSession.setGameTypeId(gameDraw.getGameDraw());
        boolean containsLetters = containsAnyLetters(s.getData());
        if (!containsLetters) {
            if (savedSession.getGameType() == FOURTH && savedSession.getPosition() == FIRST) {
                message = """
                        %s
                        Choose one number between 1 and 57
                        """;
                message = String.format(message, "Banker");
            } else if (savedSession.getGameType() == FOURTH && savedSession.getPosition() == SECOND) {
                String[] selectedNumbers = splitNumbers(s.getData());
                int len = selectedNumbers.length;
                boolean exceeds = anyNumberExceedsLimit(s.getData(), ",", 57);
                boolean containsZero = s.getData().contains("0");
                if (len > 1 || exceeds || containsZero) {
                    message = exceeds ? AppConstants.EXCEEDS_NUMBER_LIMIT_MESSAGE : AppConstants.INVALID_TRAN_MESSAGE;
                    deleteSession(savedSession);
                } else {
                    message = """
                            Type amount to Start (1 - 20):
                            """;
                    savedSession.setCurrentGame("banker");
                    savedSession.setSelectedNumbers(s.getData());
                    updateSession(savedSession, false);
                }
            } else if (savedSession.getGameType() == FOURTH && savedSession.getPosition() == THIRD) {
                int amount = Integer.parseInt(s.getData());
                if (amount > 20 || amount < 1) {
                    deleteSession(savedSession);
                    message = "Amount should be between 1GHS and 20GHS \n 0 Back";
                } else {
                    savedSession.setAmount(Double.valueOf(s.getData()));
                    savedSession.setGameType(4);
                    // updateSession(savedSession, false);
                    String total = calculateAmountBankerAPI(savedSession, "banker");
                    String ticketInfo = """
                            Tck info:
                            --
                            Lucky 70 M %s
                            Your No: %s
                            \s
                            1) to pay %s GHS on momo.
                            \s
                            2) to apply coupon code.
                            \s
                            0) to cancel.
                            \s""";
                    message = String.format(ticketInfo, s.getCurrentGame(), s.getSelectedNumbers(), total);
                    savedSession.setAmount(Double.valueOf(total));
                    updateSession(s, false);
                }
            } else {
                if (s.getData().equals("0")) {
                    continueFlag = 0;
                    deleteSession(savedSession);
                    savedSession.setData("0");
                    savedSession.setMsisdn(s.getMsisdn());
                    sessionRepository.save(savedSession);
                    return menuResponse(savedSession, continueFlag, AppConstants.WELCOME_MENU_MESSAGE);
                } else {
                    updateSession(savedSession, false);
                    continueFlag = 1;
                    message = AppConstants.PAYMENT_INIT_MESSAGE;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSessionBanker(s);
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
            }
        } else {
            deleteSession(savedSession);
            message = "Input must only contain numbers \n 0) Back";
        }
        return menuResponse(savedSession, continueFlag, message);
    }

    private void updateSession(Session session, boolean increment) {
        Session savedSession = sessionRepository.findBySequenceID(session.getSequenceID());
        try {
            savedSession.setData(session.getData());

            if (increment) {
                savedSession.setPosition(savedSession.getPosition() + 1);
            }

            if (savedSession.getPosition() == 1) {
                savedSession.setGameType(Integer.parseInt(session.getData()));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println(savedSession);
        sessionRepository.save(savedSession);
    }

    private void deleteSession(Session session) {
        sessionRepository.delete(session);
    }

    private String megaGameOptions(int gameType, int position, Session s) throws ExecutionException, InterruptedException {
        String message = null;
        Game gameDraw;
        int continueFlag = 0;
        AtomicInteger index = new AtomicInteger(1);
        Session savedSession = sessionRepository.findBySequenceID(s.getSequenceID());
        savedSession.setGameType(1);
        savedSession.setCurrentGame(AppConstants.MEGA);
        updateSession(savedSession, false);
        System.out.println(savedSession.toString());
        boolean containsLetters = containsAnyLetters(s.getData());
        if (!containsLetters) {
            if (savedSession.getGameType() == FIRST && savedSession.getPosition() == FIRST) {
                message = AppConstants.MEGA_OPTIONS_CHOICE_MESSAGE;
                //updateSession(savedSession, true);
            } else if (savedSession.getGameType() == FIRST && savedSession.getPosition() == SECOND) {

                List<Integer> numbers = extractNumbers(savedSession.getData());
                Set<Integer> repeatedNumbers = findRepeatedNumbers(numbers);
                boolean exceeds = anyNumberExceedsLimit(s.getData(), ",", 57);

                System.out.println(repeatedNumbers);
                String[] selectedNumbers = splitNumbers(s.getData());
                int len = selectedNumbers.length;
                boolean containsZero = s.getData().contains("0");

                if (len == AppConstants.MAX_MEGA && !exceeds && !containsZero) {
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
                } else {
                    message = exceeds ? AppConstants.EXCEEDS_NUMBER_LIMIT_MESSAGE : AppConstants.MEGA_VALIDATION_MESSAGE;
                    deleteSession(savedSession);
                }

                if (!repeatedNumbers.isEmpty()) {
                    message = exceeds ? AppConstants.EXCEEDS_NUMBER_LIMIT_MESSAGE : AppConstants.MEGA_VALIDATION_MESSAGE;
                    deleteSession(savedSession);
                }
            } else if (savedSession.getGameType() == FIRST && savedSession.getPosition() == FOURTH) {
                int amount = 0;
                amount = switch (s.getData()) {
                    case "1" -> 5;
                    case "2" -> 10;
                    case "3" -> 20;
                    default -> 0;
                };
                if (amount > 20 || amount < 1) {
                    deleteSession(savedSession);
                    message = "Amount should be between 1GHS and 20GHS \n 0 Back";
                } else {
                    String ticketInfo = """
                            Tck info:
                            --
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
                }
            } else {
                if (s.getData().equals("0")) {
                    continueFlag = 0;
                    deleteSession(savedSession);
                    savedSession.setData("0");
                    savedSession.setMsisdn(s.getMsisdn());
                    sessionRepository.save(savedSession);
                    return menuResponse(savedSession, continueFlag, AppConstants.WELCOME_MENU_MESSAGE);
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
                    continueFlag = 1;
                }
            }
        } else {
            deleteSession(savedSession);
            message = "Input must only contain numbers \n 0) Back";
        }
        return menuResponse(savedSession, continueFlag, message);
    }

    private String directGameOptions(int gameType, int position, Session s) throws ExecutionException, InterruptedException {
        String message = null;
        int continueFlag = 0;
        Session savedSession = sessionRepository.findBySequenceID(s.getSequenceID());
        Optional<Game> currentGameDraw = gameRepository.findAll().stream().filter(game -> game.getGameDraw().endsWith("A")).findFirst();
        final Game gameDraw = currentGameDraw.get();
        savedSession.setGameId(gameDraw.getGameId());
        savedSession.setGameTypeId(gameDraw.getGameDraw());
        savedSession.setBetTypeCode(AppConstants.DIRECT);
        AtomicInteger index = new AtomicInteger(1);
        List<String> directGames = AppConstants.DIRECT_GAMES;
        updateSession(savedSession, false);
        boolean containsLetters = containsAnyLetters(s.getData());
        if (!containsLetters) {
            if (savedSession.getGameType() == SECOND && savedSession.getPosition() == FIRST) {
                StringBuilder builder = new StringBuilder();
                directGames.stream().forEachOrdered(game -> {
                    int currentIndex = index.getAndIncrement();
                    builder.append(String.format("%s) %s\n", currentIndex, game.toString()));
                });
                message = builder.toString();
            } else if (savedSession.getGameType() == SECOND && savedSession.getPosition() == SECOND) {
                String currentGame = directGames.get(Integer.parseInt(s.getData()) - 1).toString();
                int currentMax = Integer.parseInt(s.getData());
                savedSession.setMax(currentMax);
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
            } else if (gameType == savedSession.getGameType() && savedSession.getPosition() == THIRD) {
                String[] selectedNumbers = splitNumbers(s.getData());
                int len = selectedNumbers.length;
                boolean exceeds = anyNumberExceedsLimit(s.getData(), ",", 57);
                boolean containsZero = s.getData().contains("0");

                if (savedSession.getMax() < len || savedSession.getMax() > len || exceeds || containsZero) {
                    message = exceeds ? AppConstants.EXCEEDS_NUMBER_LIMIT_MESSAGE : AppConstants.INVALID_TRAN_MESSAGE;
                    deleteSession(savedSession);
                } else {
                    message = "Type amount to Start (1 - 20)";
                    savedSession.setSelectedNumbers(s.getData());
                    updateSession(savedSession, false);
                }
            } else if (gameType == SECOND && position == FOURTH) {
                int amount = Integer.parseInt(s.getData());
                if (amount > 20 || amount < 1) {
                    deleteSession(savedSession);
                    message = "Amount should be between 1GHS and 20GHS \n 0 Back";
                } else {
                    String ticketInfo = """
                            Tck info:
                            --
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
                    //s.setCurrentGame(directGameName);
                    message = String.format(ticketInfo, s.getCurrentGame(), s.getSelectedNumbers(), s.getAmount());
                    updateSession(s, false);
                }
            } else if (savedSession.getGameType() == SECOND && savedSession.getPosition() == FIFTH) {
                if (s.getData().equals("0")) {
                    continueFlag = 0;
                    deleteSession(savedSession);
                    savedSession.setData("0");
                    savedSession.setMsisdn(s.getMsisdn());
                    sessionRepository.save(savedSession);
                    return menuResponse(savedSession, continueFlag, AppConstants.WELCOME_MENU_MESSAGE);
                } else {
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
                    continueFlag = 1;
                }
            }
        } else {
            deleteSession(savedSession);
            message = "Input must only contain numbers \n 0) Back";
        }
        return menuResponse(savedSession, continueFlag, message);
    }

    public String permGameOptions(int gameType, int position, Session s) {
        int continueFlag = 0;
        String message = null;
        int codeType = 0;
        List<String> permGames = AppConstants.PERM_GAMES;
        AtomicReference<Integer> index = new AtomicReference<>(0);
        Optional<Game> currentGameDraw = gameRepository.findAll().stream().filter(game -> game.getGameDraw().endsWith("A")).findFirst();
        final Game gameDraw = currentGameDraw.get();
        Session savedSession = sessionRepository.findBySequenceID(s.getSequenceID());
        savedSession.setGameId(gameDraw.getGameId());
        savedSession.setGameTypeId(gameDraw.getGameDraw());
        //savedSession.setBetTypeCode(AppConstants.PERM);
        updateSession(savedSession, false);
        boolean containsLetters = containsAnyLetters(s.getData());
        if (!containsLetters) {
            if (savedSession.getGameType() == THIRD && savedSession.getPosition() == FIRST) {
                StringBuilder builder = new StringBuilder();
                permGames.stream().forEachOrdered(game -> {
                    int currentIndex = index.updateAndGet(v -> v + 1);
                    builder.append(String.format("%s) %s\n", currentIndex, game.toString()));
                });
                message = builder.toString();
            } else if (savedSession.getGameType() == THIRD && savedSession.getPosition() == SECOND) {
                String currentGame = permGames.get(Integer.parseInt(s.getData()) - 1).toString();
                //System.out.printf("Number => %s", s.getData());
                codeType = switch (s.getData()) {
                    case "1" -> 2;
                    case "2" -> 3;
                    case "3" -> 4;
                    case "4" -> 5;
                    case "5" -> 6;
                    case "6" -> 7;
                    default -> throw new IllegalStateException("Unexpected value: " + s.getData());
                };
                savedSession.setCurrentGame(currentGame);
                savedSession.setBetTypeCode(String.valueOf(codeType));
                savedSession.setGameTypeCode(codeType);
                System.out.printf("Bet Code Type => %s\n", codeType);
                List<Pair<Integer, Integer>> ranges = AppConstants.ranges;
                Pair<Integer, Integer> currentPair = ranges.get(Integer.parseInt(s.getData()) - 1);
                savedSession.setMax(currentPair.getValue());
                savedSession.setMin(currentPair.getKey());

                String template = s.getData() == "" ? "Wrong input" : AppConstants.RANGE_CHOICE_TEMPLATE;

                message = switch (savedSession.getData()) {
                    case "1" -> String.format(template, ranges.get(0).getKey(), ranges.get(0).getValue());
                    case "2" -> String.format(template, ranges.get(1).getKey(), ranges.get(1).getValue());
                    case "3" -> String.format(template, ranges.get(2).getKey(), ranges.get(2).getValue());
                    case "4" -> String.format(template, ranges.get(3).getKey(), ranges.get(3).getValue());
                    case "5" -> String.format(template, ranges.get(4).getKey(), ranges.get(4).getValue());
                    default -> "";
                };
                updateSession(savedSession, false);
            } else if (savedSession.getGameType() == THIRD && savedSession.getPosition() == THIRD) {
                savedSession.setSelectedNumbers(s.getData());
                String[] selectedNumbers = splitNumbers(s.getData());
                int len = selectedNumbers.length;
                boolean exceeds = anyNumberExceedsLimit(s.getData(), ",", 57);
                boolean containsZero = s.getData().contains("0");

                // System.out.printf("Len => %s Min => %s Max => %s Exceeds => %s\n", len, savedSession.getMin(), savedSession.getMax(), exceeds);

                if (!isBetween(len, savedSession.getMin(), savedSession.getMax()) || exceeds || containsZero) {
                    message = exceeds ? AppConstants.EXCEEDS_NUMBER_LIMIT_MESSAGE : AppConstants.INVALID_TRAN_MESSAGE;
                    deleteSession(savedSession);
                } else {
                    message = """
                            Type amount to Start (1 - 20):
                            """;
                    updateSession(savedSession, false);
                }
            } else if (savedSession.getGameType() == THIRD && savedSession.getPosition() == FOURTH) {
                int amount = Integer.parseInt(s.getData());
                if (amount > 20 || amount < 1) {
                    deleteSession(savedSession);
                    message = "Amount should be between 1GHS and 20GHS \n 0 Back";
                } else {
                    savedSession.setAmount(Double.parseDouble(s.getData()));
                    String total = calculateAmountPermAPI(savedSession, "perm");
                    String ticketInfo = """
                            Tck info:
                            --
                            Lucky 70 M %s
                            Your No: %s
                            \s
                            1) to pay %s GHS on momo.
                            \s
                            2) to apply coupon code.
                            \s
                            0) to cancel.
                            \s""";
                    message = String.format(ticketInfo, s.getCurrentGame(), s.getSelectedNumbers(), total);
                    savedSession.setAmount(Double.valueOf(total));
                    updateSession(s, false);
                }
            } else if (gameType == THIRD && position == FIFTH) {
                if (s.getData().equals("0")) {
                    continueFlag = 0;
                    deleteSession(savedSession);
                    savedSession.setData("0");
                    savedSession.setMsisdn(s.getMsisdn());
                    sessionRepository.save(savedSession);
                    return menuResponse(savedSession, continueFlag, AppConstants.WELCOME_MENU_MESSAGE);
                } else {
                    savedSession.setBetTypeCode(AppConstants.PERM);
                    updateSession(savedSession, false);
                    continueFlag = 1;
                    message = AppConstants.PAYMENT_INIT_MESSAGE;
                    System.out.printf("Perm session => ", savedSession.toString());
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSessionPerm(s);
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
                    System.out.println(task.getName());
                }
            }
        } else {
            deleteSession(savedSession);
            message = "Input must only contain numbers \n 0) Back";
        }
        return menuResponse(savedSession, continueFlag, message);
    }

    private String getDrawResults(Session session) {
        ResponseEntity<String> response = handler.client()
                .get()
                .uri("/api/V1/draw-results")
                .retrieve()
                .toEntity(String.class);
        return response.getBody();
    }

    private RecentTickets getLastFiveTransactions(Session session, String msisdn) {
        ResponseEntity<RecentTickets> response = handler.client()
                .get()
                .uri("/api/V1/recent-tickets")
                //.body(String.format("{\"msisdn\":\"%s\"}", msisdn))
                //.contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(RecentTickets.class);
        return response.getBody();
    }

    private String calculateAmountAPI(Session session, String type) {
        String body = String.format("{\"amount\":\"%s\",\"selected_numbers\":\"%s\",\"bet_type_code\":\"%s\",\"bet_type\":\"%s\"}"
                , session.getAmount(), session.getSelectedNumbers(), session.getGameType(), type);
        System.out.println(body);
        ResponseEntity<String> response = handler.client()
                .post()
                .uri("/api/V1/request-bet-amount")
                .body(body)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);
        String json = response.getBody();
        JSONObject object = new JSONObject(json);
        String total = object.get("total").toString();
        return total;
    }

    private String calculateAmountPermAPI(Session session, String type) {
        String body = String.format("{\"amount\":\"%s\",\"selected_numbers\":\"%s\",\"bet_type_code\":\"%s\",\"bet_type\":\"%s\"}"
                , session.getAmount(), session.getSelectedNumbers(), session.getBetTypeCode(), type);
        System.out.println(body);
        ResponseEntity<String> response = handler.client()
                .post()
                .uri("/api/V1/request-bet-amount")
                .body(body)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);
        String json = response.getBody();
        JSONObject object = new JSONObject(json);
        String total = object.get("total").toString();
        return total;
    }

    private String calculateAmountBankerAPI(Session session, String type) {
        String body = String.format("{\"amount\":\"%s\",\"selected_numbers\":\"%s\",\"bet_type_code\":\"%s\",\"bet_type\":\"%s\"}"
                , session.getAmount(), session.getSelectedNumbers(), 2, type);
        System.out.println(body);
        ResponseEntity<String> response = handler.client()
                .post()
                .uri("/api/V1/request-bet-amount")
                .body(body)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);
        String json = response.getBody();
        JSONObject object = new JSONObject(json);
        String total = object.get("total").toString();
        return total;
    }

    public String applyDiscount(Session savedSession) {
        try {

        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    private String tnCsMessage(Session session) {
        return menuResponse(session, 1, """
                                TnCs
                You can read here: http://www.afriluck.com/#/
                page-details/terms-and-conditions
                """);
    }

    private String contactUsMessage(Session session) {
        return menuResponse(session, 1, """
                Contact us:
                0303957964
                0303958006
                """);
    }

    public String menuResponse(Session session, int continueFlag, String message) {
        JsonObject json = new JsonObject();
        json.addProperty("msisdn", session.getMsisdn());
        json.addProperty("sequenceID", session.getSequenceID());
        json.addProperty("timestamp", session.getTimeStamp());
        json.addProperty("message", message);
        json.addProperty("continueFlag", continueFlag);
        return json.toString();
    }

    public String silentDelete(Session savedSession) {
        deleteSession(savedSession);
        return menuResponse(savedSession, 0, "Enter a valid menu option\n 0) Back");
    }
}

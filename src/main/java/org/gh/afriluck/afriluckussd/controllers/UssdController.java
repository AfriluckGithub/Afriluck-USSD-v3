package org.gh.afriluck.afriluckussd.controllers;

import com.google.gson.JsonObject;
import org.gh.afriluck.afriluckussd.constants.AppConstants;
import org.gh.afriluck.afriluckussd.dto.*;
import org.gh.afriluck.afriluckussd.entities.Game;
import org.gh.afriluck.afriluckussd.mapping.TransactionMapper;
import org.gh.afriluck.afriluckussd.repositories.CustomerSessionRepository;
import org.gh.afriluck.afriluckussd.entities.Session;
import org.gh.afriluck.afriluckussd.repositories.GameRepository;
import org.gh.afriluck.afriluckussd.utils.AfriluckCallHandler;
import org.gh.afriluck.afriluckussd.utils.ValidationUtils;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
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
    private static final int SIX = 6;
    private static final int SEVEN = 7;
    private static final int ZERO = 0;
    private final CustomerSessionRepository sessionRepository;
    private final AfriluckCallHandler handler;
    private final TransactionMapper mapper;
    private final GameRepository gameRepository;
    List<Game> games = null;
    Thread.Builder paymentThread = Thread.ofVirtual().name("Payment Thread");
    Thread.Builder sessionThread = Thread.ofVirtual().name("Session Thread");

    /**
     * @param sessionRepository
     * @param handler
     * @param mapper
     * @param gameRepository
     * @apiNote Autowiring of required dependencies
     * @since 2024-06-01
     */
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

    /**
     * @param session
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @apiNote A controller that serves the ussd application
     */
    @PostMapping(path = "/ussd")
    public String index(@RequestBody Session session) throws ExecutionException, InterruptedException {

        String message = null;
        SimpleDateFormat formatter = new SimpleDateFormat(AppConstants.GLOBAL_DATE_FORMAT);
        String timeStamp = formatter.format(new Date());

        session.setTimeStamp(timeStamp);
        Session savedSession = sessionRepository.findBySequenceID(session.getSequenceID());

        if (savedSession == null) {
            session.setPosition(0);
            //session.setSequenceId(new Date().getTime() + session.getSequenceID());
            sessionRepository.save(session);
        } else {
            System.out.println("--- Updating ---");
            updateSession(session, true);
        }

        if (ValidationUtils.isBetweenGameTime()) {
            message = menuResponse(session, 1, AppConstants.GAME_CLOSED_MESSAGE);
        } else {
            Session s = sessionRepository.findBySequenceID(session.getSequenceID());
            if (s.isReset()) {
                s.setNextStep(SECOND);
                s.setPosition(SECOND);
                s.setGameType(s.getGameType());
            }
            if (s.getNextStep() == ZERO) {
                s.setNextStep(FIRST);
                String dayOfWeekInWords = getDayOfWeekInWords();
                updateSession(s, false);
                message = menuResponse(session, 0, ValidationUtils.isEveningGameTime() ? String.format(AppConstants.WELCOME_MENU_MESSAGE_NEW, dayOfWeekInWords, dayOfWeekInWords.equals("Sunday") ? 6 : 7) : String.format(AppConstants.WELCOME_MENU_MESSAGE_NEW_EVENING, dayOfWeekInWords, dayOfWeekInWords.equals("Sunday") ? 6 : 7));
            } else if (s.getNextStep() == FIRST) {
                try {
                    if (s.getNextStep() == FIRST && s.isSecondStep() == false) {
                        s.setGameType(Integer.valueOf(s.getData()));
                        updateSession(s, false);
                    }

                    message = switch (s.getGameType()) {
                        case 1 -> anopaGameOptions(s);
                        case 2 -> eveningGameOptions(s);
                        case 5 -> account(s);
                        case 6 -> tnCsMessage(s);
                        case 99 -> contactUsMessage(s);
                        case null -> "Invalid value entered\n 0. Back";
                        default -> throw new IllegalStateException("Unexpected value: " + s.getData());
                    };
                } catch (Exception e) {
                    message = menuResponse(session, 1, "Invalid value entered");
                    e.printStackTrace();
                }
            } else if (s.getNextStep() == SECOND) {
                String dayOfWeekInWords = getDayOfWeekInWords();
                if (s.getPosition() == SECOND && !s.isReset()) {
                    s.setGameType(Integer.valueOf(s.getData()));
                    updateSession(s, false);
                }

                if (s.isBackPressed()) {
                    s.setGameType(Integer.parseInt(s.getData()));
                    s.setBackPressed(false);
                    s.setPosition(SECOND);
                    updateSession(s, false);
                }

                message = !s.isMorning() ? switch (s.getGameType()) {
                    case 1 -> megaGameOptions(s.getGameType(), s.getPosition(), s);
                    case 2 -> directGameOptions(s.getGameType(), s.getPosition(), s);
                    case 3 -> permGameOptions(s.getGameType(), s.getPosition(), s);
                    case 4 -> banker(s, "Banker");
                    case null -> "Invalid value entered\n 0. Back";
                    case 0 -> backOption(session, s);
                    default -> silentDelete(s);
                } : switch (s.getGameType()) {
                    case 2 -> directGameOptions(s.getGameType(), s.getPosition(), s);
                    case 3 -> permGameOptions(s.getGameType(), s.getPosition(), s);
                    case 4 -> banker(s, "Banker");
                    case null -> "Invalid value entered\n 0. Back";
                    case 0 -> backOption(session, s);
                    default -> silentDelete(s);
                }
                ;
            }
        }
        return message;
    }

    private String backOption(Session session, Session savedSession) {
        String dayOfWeekInWords = getDayOfWeekInWords();
        //deleteSession(savedSession);
        savedSession.setNextStep(FIRST);
        savedSession.setPosition(FIRST);
        savedSession.setSequenceId(session.getSequenceID());
        savedSession.setMsisdn(session.getMsisdn());
        savedSession.setReset(false);
        savedSession.setBackPressed(true);
        updateSession(savedSession, false);
        return menuResponse(session, 0, savedSession.isMorning() ? String.format(AppConstants.WELCOME_MENU_MESSAGE_NEW_EVENING, dayOfWeekInWords, dayOfWeekInWords.equals("Sunday") ? 6 : 7)
                : String.format(AppConstants.WELCOME_MENU_MESSAGE_NEW, dayOfWeekInWords, dayOfWeekInWords.equals("Sunday") ? 6 : 7));
    }

    private String anopaGameOptions(Session session) {
        session.setNextStep(SECOND);
        session.setMorning(true);
        updateSession(session, false);
        return menuResponse(session, 0, AppConstants.WELCOME_MENU_MESSAGE_MORNING);
    }

    private String eveningGameOptions(Session session) {
        session.setNextStep(SECOND);
        session.setMorning(false);
        updateSession(session, false);
        return menuResponse(session, 0, AppConstants.WELCOME_MENU_MESSAGE);
    }

    /**
     * @param savedSession
     * @return
     */
    private String account(Session savedSession) {
        String message = null;
        int continueFlag = 0;
        savedSession.setGameType(5);
        savedSession.setSecondStep(true);
        updateSession(savedSession, false);
        if (savedSession.isSecondStep() && savedSession.getPosition() == FIRST) {
            message = AppConstants.ACCOUNT_MENU_MESSAGE;
            updateSession(savedSession, true);
        } else if (savedSession.isSecondStep() && savedSession.getPosition() == THIRD) {
            String response = null;
            String json = null;
            JSONObject oj = null;
            switch (savedSession.getData()) {
                case "0":
                    deleteSession(savedSession);
                    continueFlag = 0;
                    return menuResponse(savedSession, continueFlag, ValidationUtils.isEveningGameTime() ? String.format(AppConstants.WELCOME_MENU_MESSAGE_NEW_EVENING, getDayOfWeekInWords(), getDayOfWeekInWords().equals("Sunday") ? 6 : 7) : String.format(AppConstants.WELCOME_MENU_MESSAGE_NEW, getDayOfWeekInWords(), getDayOfWeekInWords().equals("Sunday") ? 6 : 7));
                case "1":
                    continueFlag = 1;
                    response = getDrawResults(savedSession);
                    json = menuResponse(savedSession, continueFlag, response);
                    oj = new JSONObject(json);
                    message = oj.get("message").toString();
                    System.out.println(message);
                    break;
                case "2":
                    continueFlag = 1;
                    response = getLastFiveTransactions(savedSession, savedSession.getMsisdn());
                    json = menuResponse(savedSession, continueFlag, response);
                    oj = new JSONObject(json);
                    message = oj.get("message").toString()
                            .replace("{", "")
                            .replace("}", "")
                            .replace("\"", "")
                            .replace("]", "")
                            .replace("[", "");
                    break;
                case "3":
                    continueFlag = 0;
                    message = "1) Deposit\n 2) Balance Enquiry\n";
                    break;
                default:
                    message = "Invalid input\n 0) Back";
                    continueFlag = 0;
                    deleteSession(savedSession);
                    break;
            }
            return menuResponse(savedSession, continueFlag, message);
        } else if (savedSession.isSecondStep() && savedSession.getPosition() == FOURTH) {
            if (savedSession.getData().equals("1")) {
                continueFlag = 0;
                message = "Enter amount to deposit\n";
            } else {
                try {
                    CustomerBalanceDto balance = getCustomerBalance(savedSession.msisdn);
                    message = String.format("Your current balance is %s GHS", balance.balance);
                    continueFlag = 1;
                } catch (Exception e) {
                    String response = e.getMessage();
                    continueFlag = 1;
                    int colonIndex = response.indexOf(":");
                    String messageAfterColon = response.substring(colonIndex + 1).trim()
                            .replace("{", "")
                            .replace("}", "")
                            .replace("\"error\"", "")
                            .replace("\":\"", "")
                            .replace(":", "")
                            .replace("\"\"", "");
                    //JSONObject oj = new JSONObject(messageAfterColon);
                    message = messageAfterColon;
                    System.out.println(messageAfterColon);

                }
            }
        } else if (savedSession.isSecondStep() && savedSession.getPosition() == FIFTH) {
            CustomerDepositResponseDto depositResponse = customerDeposit(savedSession.getMsisdn(), savedSession.getData(), savedSession.getNetwork());
            message = depositResponse.success;
            continueFlag = 1;
        }
        return menuResponse(savedSession, continueFlag, message);
    }

    private String banker(Session s, String title) throws InterruptedException {
        String message = null;
        int continueFlag = 0;
        Session savedSession = sessionRepository.findBySequenceID(s.getSequenceID());
        //List<Game> currentGameDraw = gameRepository.findAll().stream().filter(game -> game.getGameDraw().endsWith("A")).sorted(Comparator.comparing(Game::getGameName)).toList();
        List<Game> currentGameDraw = gameRepository.findAll().stream().filter(game -> game.getGameTypeId() == 15).sorted(Comparator.comparing(Game::getGameName)).toList();
        final Game gameDraw = s.isMorning() ? currentGameDraw.get(0) : currentGameDraw.get(1);
        savedSession.setGameId(gameDraw.getGameId());
        savedSession.setGameTypeId(gameDraw.getGameDraw());
        boolean containsLetters = s.getPosition() != 6 ? ValidationUtils.containsAnyLetters(s.getData()) : false;
        if (!containsLetters) {
            if (savedSession.getGameType() == FOURTH && savedSession.getPosition() == SECOND) {
                message = """
                        %s
                        Choose one number between 1 and 57
                        """;
                message = String.format(message, "Banker");
            } else if (savedSession.getGameType() == FOURTH && savedSession.getPosition() == THIRD) {
                String input = ValidationUtils.removeSpecialCharacters(s.getData());
                String[] selectedNumbers = ValidationUtils.splitNumbers(input);
                int len = selectedNumbers.length;
                boolean exceeds = ValidationUtils.anyNumberExceedsLimit(input, ",", 57);
                boolean containsZero = ValidationUtils.containsSingularZero(input);
                if (len > 1 || exceeds || containsZero) {
                    message = exceeds ? AppConstants.EXCEEDS_NUMBER_LIMIT_MESSAGE : AppConstants.INVALID_TRAN_MESSAGE;
                    deleteSession(savedSession);
                } else {
                    message = """
                            Type amount to Start (1 - 20):
                            """;
                    savedSession.setCurrentGame(gameDraw.getGameName());
                    savedSession.setSelectedNumbers(s.getData());
                    updateSession(savedSession, false);
                }
            } else if (savedSession.getGameType() == FOURTH && savedSession.getPosition() == FOURTH) {
                Number amount = ValidationUtils.parseNumber(s.getData());
                boolean isDecimal = ValidationUtils.isDecimal(amount.doubleValue());
                if (!isDecimal) {
                    if (amount.intValue() > 20 || amount.intValue() < 1) {
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
                                1) to pay %s GHS.
                                \s
                                2) to apply coupon code.
                                \s
                                0) to cancel.
                                \s""";
                        message = String.format(ticketInfo, s.getCurrentGame(), s.getSelectedNumbers(), total);
                        savedSession.setAmount(Double.valueOf(total));
                        updateSession(savedSession, false);
                    }
                } else {
                    deleteSession(savedSession);
                    message = "Invalid amount. Enter a round figure.\n 0) Back";
                    continueFlag = 0;
                }
            } else if (savedSession.getGameType() == FOURTH && savedSession.getPosition() == SEVEN) {
                message = "Select payment method\n1) Mobile Money\n2) Afriluck Wallet";
            } else if (savedSession.getGameType() == FOURTH && savedSession.getPosition() == 8) {
                if (savedSession.getData().equals("1")) {
                    updateSession(savedSession, false);
                    continueFlag = 1;
                    message = AppConstants.PAYMENT_INIT_MESSAGE;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSessionBanker(savedSession, false);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());
                        System.out.println("--- Running Payment ---");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("--- Deleting Session ---");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
                } else if (savedSession.getData().equals("2")) {
                    updateSession(savedSession, false);
                    continueFlag = 1;
                    message = AppConstants.PAYMENT_INIT_MESSAGE_WALLET;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSessionBanker(savedSession, true);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());
                        System.out.println("--- Running Payment ---");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("--- Deleting Session ---");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
                }
            } else {
                String choice = s.getData();
                if (choice.equals("0")) {
                    return ticketCancelledMessage(savedSession);
                } else if (choice.equals("2") && savedSession.getPosition() == 5) {
                    message = AppConstants.DISCOUNT_PROMPT_MESSAGE;
                } else if (choice.equals("1") && savedSession.getPosition() == 5) {
                    message = "Select payment method\n1) Mobile Money\n2) Afriluck Wallet";
                } else if (savedSession.getPosition() == 6) {
                    // Payment with MOMO
                    if (savedSession.getData().equals("1") && savedSession.getPosition() == 6) {
                        updateSession(savedSession, false);
                        continueFlag = 1;
                        message = AppConstants.PAYMENT_INIT_MESSAGE;
                        Runnable paymentTask = () -> {
                            Transaction t = mapper.mapTransactionFromSessionBanker(savedSession, false);
                            System.out.println(t.toString());
                            ResponseEntity<String> response = handler.client()
                                    .post()
                                    .uri("/api/V1/place-bet")
                                    .body(t)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .retrieve()
                                    .toEntity(String.class);
                            System.out.println(response.getBody());
                            System.out.println("--- Running Payment ---");
                        };
                        Runnable sessionTask = () -> {
                            sessionRepository.deleteById(savedSession.getId());
                            System.out.println("--- Deleting Session ---");
                        };
                        paymentThread.start(paymentTask).join();
                        sessionThread.start(sessionTask);
                    } else if (savedSession.getData().equals("2") && savedSession.getPosition() == 6) {
                        updateSession(savedSession, false);
                        continueFlag = 1;
                        message = AppConstants.PAYMENT_INIT_MESSAGE_WALLET;
                        Runnable paymentTask = () -> {
                            Transaction t = mapper.mapTransactionFromSessionBanker(savedSession, false);
                            System.out.println(t.toString());
                            ResponseEntity<String> response = handler.client()
                                    .post()
                                    .uri("/api/V1/place-bet")
                                    .body(t)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .retrieve()
                                    .toEntity(String.class);
                            System.out.println(response.getBody());
                            System.out.println("--- Running Payment ---");
                        };
                        Runnable sessionTask = () -> {
                            sessionRepository.deleteById(savedSession.getId());
                            System.out.println("--- Deleting Session ---");
                        };
                        paymentThread.start(paymentTask).join();
                        sessionThread.start(sessionTask);
                    } else {
                        DiscountResponse response = applyCoupon(s.getAmount(), s.getData());
                        System.out.printf("Discount => ", response);
                        message = discountMessage(response);
                        if (response.getValid()) {
                            savedSession.setDiscountedAmount(response.getAmount());
                            updateSession(savedSession, false);
                        }
                    }
                } else {
                    updateSession(savedSession, false);
                    continueFlag = 1;
                    message = AppConstants.PAYMENT_INIT_MESSAGE;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSessionBanker(savedSession, false);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());
                        System.out.println("--- Running Payment ---");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("--- Deleting Session ---");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
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

            if (savedSession.getPosition() == 2 && !savedSession.isReset()) {
                savedSession.setGameType(Integer.parseInt(session.getData()));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println(savedSession);
        sessionRepository.save(savedSession);
    }

    private void increasePosition(Session savedSession) {
        savedSession.setPosition(savedSession.getPosition() + 1);
    }

    private void saveUSSDSession(Session session) {
        Session s = sessionRepository.findBySequenceID(session.getSequenceID());
        sessionRepository.save(s);
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
        boolean containsLetters = savedSession.getPosition() != 8 ? ValidationUtils.containsAnyLetters(s.getData()) : false;
        if (!containsLetters) {
            if (savedSession.getGameType() == FIRST && savedSession.getPosition() == SECOND) {
                message = AppConstants.MEGA_OPTIONS_CHOICE_MESSAGE;
            } else if (savedSession.getGameType() == FIRST && savedSession.getPosition() == THIRD) {

                String input = ValidationUtils.removeSpecialCharacters(s.getData());
                List<Integer> numbers = ValidationUtils.extractNumbers(input);
                Set<Integer> repeatedNumbers = ValidationUtils.findRepeatedNumbers(numbers);
                boolean exceeds = ValidationUtils.anyNumberExceedsLimit(input, ",", 57);

                System.out.println(repeatedNumbers);
                String[] selectedNumbers = ValidationUtils.splitNumbers(input);
                int len = selectedNumbers.length;
                boolean containsZero = ValidationUtils.containsSingularZero(input);

                if (len == AppConstants.MAX_MEGA && !exceeds && !containsZero) {
                    StringBuilder messageBuilder = new StringBuilder(AppConstants.AMOUNT_TO_STAKE_MESSAGE);

                    games = gameRepository.findAll().stream().distinct().filter(game -> game.getGameTypeId() == 1)
                            .sorted(Comparator.comparing(Game::getAmount)).toList();

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
            } else if (savedSession.getGameType() == FIRST && savedSession.getPosition() == FIFTH) {
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
                    int finalAmount = amount;
                    CompletableFuture<Game> matchAsync = CompletableFuture.supplyAsync(()
                            -> games.stream().filter(game -> game.getAmount() == Double.parseDouble(String.valueOf(finalAmount))).findFirst().get());
                    gameDraw = matchAsync.get();
                    String ticketInfo = """
                            Tck info:
                            --
                            %s
                            Your numbers: %s
                            \s
                            1) to pay %s GHS.
                            \s
                            2) to apply coupon code.
                            \s
                            0) to cancel.
                            \s""";
                    message = String.format(ticketInfo, gameDraw.getGameName(), s.getSelectedNumbers(), amount);
                    savedSession.setGameTypeId(gameDraw.getGameDraw());
                    savedSession.setAmount(Double.parseDouble(gameDraw.getAmount().toString()));
                    savedSession.setGameId(gameDraw.getGameId());
                    savedSession.setBetTypeCode("1");
                    updateSession(savedSession, true);
                }
            } else {

                String choice = s.getData();

                if (choice.equals("0")) {
                    return ticketCancelledMessage(savedSession);
                } else if (choice.equals("2") && savedSession.getPosition() == 7) {
                    message = AppConstants.DISCOUNT_PROMPT_MESSAGE;
                } else if (choice.equals("1") && savedSession.getPosition() == 7) {
                    message = "Select payment method\n1) Mobile Money\n2) Afriluck Wallet";
                } else if (choice.equals("1") && savedSession.getPosition() == 8) {
                    gameDraw = new Game();
                    message = AppConstants.PAYMENT_INIT_MESSAGE;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSession(s, gameDraw, false);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());

                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Payment Thread running...");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Session Thread running...");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
                    continueFlag = 1;
                } else if (choice.equals("2") && savedSession.getPosition() == 8) {
                    gameDraw = new Game();
                    message = AppConstants.PAYMENT_INIT_MESSAGE_WALLET;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSession(s, gameDraw, true);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());

                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Payment Thread running...");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Session Thread running...");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
                    continueFlag = 1;
                } else if (savedSession.getPosition() == 8) {
                    DiscountResponse response = applyCoupon(s.getAmount(), s.getData());
                    System.out.printf("Discount => ", response);
                    message = discountMessage(response);
                    if (response.getValid()) {
                        savedSession.setDiscountedAmount(response.getAmount());
                        updateSession(savedSession, false);
                    }
                } else if (savedSession.getPosition() == 9) {
                    message = "Select payment method\n1) Mobile Money\n2) Afriluck Wallet";
                } else if (savedSession.getPosition() == 10) {
                    if (savedSession.getData().equals("1")) {
                        gameDraw = new Game();
                        updateSession(savedSession, false);
                        continueFlag = 1;
                        message = AppConstants.PAYMENT_INIT_MESSAGE;
                        Runnable paymentTask = () -> {
                            Transaction t = mapper.mapTransactionFromSession(s, gameDraw, false);
                            System.out.println(t.toString());
                            ResponseEntity<String> response = handler.client()
                                    .post()
                                    .uri("/api/V1/place-bet")
                                    .body(t)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .retrieve()
                                    .toEntity(String.class);
                            System.out.println(response.getBody());
                            System.out.println("--- Running Payment ---");
                        };
                        Runnable sessionTask = () -> {
                            sessionRepository.deleteById(savedSession.getId());
                            System.out.println("--- Deleting Session ---");
                        };
                        paymentThread.start(paymentTask).join();
                        sessionThread.start(sessionTask);
                    } else if (savedSession.getData().equals("2")) {
                        gameDraw = new Game();
                        updateSession(savedSession, false);
                        continueFlag = 1;
                        message = AppConstants.PAYMENT_INIT_MESSAGE_WALLET;
                        Runnable paymentTask = () -> {
                            Transaction t = mapper.mapTransactionFromSession(s, gameDraw, true);
                            System.out.println(t.toString());
                            ResponseEntity<String> response = handler.client()
                                    .post()
                                    .uri("/api/V1/place-bet")
                                    .body(t)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .retrieve()
                                    .toEntity(String.class);
                            System.out.println(response.getBody());
                            System.out.println("--- Running Payment ---");
                        };
                        Runnable sessionTask = () -> {
                            sessionRepository.deleteById(savedSession.getId());
                            System.out.println("--- Deleting Session ---");
                        };
                        paymentThread.start(paymentTask).join();
                        sessionThread.start(sessionTask);
                    }
                } else {
                    gameDraw = new Game();
                    message = AppConstants.PAYMENT_INIT_MESSAGE;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSession(s, gameDraw, false);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());

                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Payment Thread running...");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Session Thread running...");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
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
        System.err.printf("\nDIRECT SESSION => %s\n", savedSession);
        System.err.printf("\nSESSION SESSION => %s\n", savedSession);
        List<Game> currentGameDraw = gameRepository.findAll().stream().filter(game -> game.getGameTypeId() == 15).sorted(Comparator.comparing(Game::getGameName)).toList();
        Game gameDraw = savedSession.isMorning() ? currentGameDraw.get(0) : currentGameDraw.get(1);
        //System.out.printf("Game Name ----> %s", gameDraw.getGameName());
        savedSession.setGameId(gameDraw.getGameId());
        savedSession.setGameTypeId(gameDraw.getGameDraw());
        savedSession.setBetTypeCode(AppConstants.DIRECT);
        AtomicInteger index = new AtomicInteger(1);
        List<String> directGames = savedSession.isMorning() ? AppConstants.DIRECT_GAMES_MORNING : AppConstants.DIRECT_GAMES;
        updateSession(savedSession, false);
        boolean containsLetters = savedSession.getPosition() != 7 ? ValidationUtils.containsAnyLetters(s.getData()) : false;
        if (!containsLetters) {
            if (savedSession.getGameType() == SECOND && savedSession.getPosition() == SECOND) {
                StringBuilder builder = new StringBuilder();
                directGames.stream().forEachOrdered(game -> {
                    int currentIndex = index.getAndIncrement();
                    builder.append(String.format("%s) %s\n", currentIndex, game.toString()));
                });
                message = builder.toString();
            } else if (savedSession.getGameType() == SECOND && savedSession.getPosition() == THIRD) {
                try {
                    String currentGame = directGames.get(ValidationUtils.parseNumber(s.getData()).intValue() - 1).toString();
                    int currentMax = ValidationUtils.parseNumber(s.getData()).intValue();
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
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println(e);
                    deleteSession(savedSession);
                    return menuResponse(savedSession, 0, "Invalid input \n 0) Back");
                }
            } else if (gameType == savedSession.getGameType() && savedSession.getPosition() == FOURTH) {
                String input = ValidationUtils.removeSpecialCharacters(s.getData());
                String[] selectedNumbers = ValidationUtils.splitNumbers(input);
                int len = selectedNumbers.length;
                boolean exceeds = ValidationUtils.anyNumberExceedsLimit(input, ",", 57);
                boolean containsZero = ValidationUtils.containsSingularZero(input);
                List<Integer> numbers = ValidationUtils.extractNumbers(input);
                Set<Integer> repeatedNumbers = ValidationUtils.findRepeatedNumbers(numbers);


                if (savedSession.getMax() < len || savedSession.getMax() > len || exceeds || containsZero || !repeatedNumbers.isEmpty()) {
                    message = exceeds ? AppConstants.EXCEEDS_NUMBER_LIMIT_MESSAGE : AppConstants.INVALID_TRAN_MESSAGE;
                    if (!repeatedNumbers.isEmpty()) {
                        message = "Duplicate numbers entered\n 0) Back";
                    }
                    deleteSession(savedSession);
                } else {
                    message = "Type amount to Start (1 - 20)";
                    savedSession.setSelectedNumbers(s.getData());
                    updateSession(savedSession, false);
                }
            } else if (gameType == SECOND && position == FIFTH) {
                Number amount = ValidationUtils.parseNumber(s.getData());
                boolean isDecimal = ValidationUtils.isDecimal(amount.doubleValue());
                if (!isDecimal) {
                    if (amount.intValue() > 20 || amount.intValue() < 1) {
                        deleteSession(savedSession);
                        message = "Amount should be between 1GHS and 20GHS \n 0 Back";
                    } else {
                        String ticketInfo = """
                                Tck info:
                                --
                                %s
                                Your No: %s
                                \s
                                1) to pay %s GHS.
                                \s
                                2) to apply coupon code.
                                \s
                                0) to cancel.
                                \s""";
                        s.setAmount(Double.parseDouble(s.getData()));
                        //s.setCurrentGame(directGameName);
                        message = String.format(ticketInfo, gameDraw.getGameName(), s.getSelectedNumbers(), s.getAmount());
                        updateSession(s, false);
                    }
                } else {
                    deleteSession(savedSession);
                    message = "Invalid amount. Enter a round figure.\n 0) Back";
                    continueFlag = 0;
                }
            } else if (savedSession.getGameType() == SECOND && savedSession.getPosition() == SIX) {
                String choice = s.getData();
                if (choice.equals("0")) {
                    return ticketCancelledMessage(savedSession);
                } else if (choice.equals("2") && savedSession.getPosition() == 6) {
                    message = AppConstants.DISCOUNT_PROMPT_MESSAGE;
                } else if (choice.equals("1") && savedSession.getPosition() == 6) {
                    message = "Select payment method\n1) Mobile Money\n2) Afriluck Wallet";
                } else {
                    savedSession.setCurrentGame("direct");
                    updateSession(s, true);
                    message = AppConstants.PAYMENT_INIT_MESSAGE;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSession(s, gameDraw, false);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());

                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Payment Thread running...");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Session Thread running...");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
                    continueFlag = 1;
                }
            } else if (savedSession.getPosition() == SEVEN) {
                if (savedSession.getData().equals("1")) {
                    // Pay with MOMO
                    message = AppConstants.PAYMENT_INIT_MESSAGE;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSession(s, gameDraw, false);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());

                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Payment Thread running...");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Session Thread running...");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
                    continueFlag = 1;
                } else if (savedSession.getData().equals("2")) {
                    // Pay with Wallet
                    message = AppConstants.PAYMENT_INIT_MESSAGE_WALLET;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSession(s, gameDraw, true);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());

                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Payment Thread running...");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Session Thread running...");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
                    continueFlag = 1;
                } else {
                    DiscountResponse response = applyCoupon(s.getAmount(), s.getData());
                    System.out.printf("Discount => ", response);
                    message = discountMessage(response);
                    if (response.getValid()) {
                        savedSession.setDiscountedAmount(response.getAmount());
                        updateSession(savedSession, false);
                    }
                }
            } else if (savedSession.getPosition() == 8 && savedSession.getData().equals("0")) {
                return ticketCancelledMessage(savedSession);
            } else if (savedSession.getPosition() == 8 && savedSession.getData().equals("1")) {
                message = "Select payment method\n1) Mobile Money\n2) Afriluck Wallet";
            } else if (savedSession.getPosition() == 9) {
                if (savedSession.getData().equals("1")) {
                    message = AppConstants.PAYMENT_INIT_MESSAGE;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSession(s, gameDraw, false);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());

                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Payment Thread running...");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Session Thread running...");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
                    continueFlag = 1;
                } else if (savedSession.getData().equals("2")) {
                    message = AppConstants.PAYMENT_INIT_MESSAGE_WALLET;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSession(s, gameDraw, true);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());

                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Payment Thread running...");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Session Thread running...");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
                    continueFlag = 1;
                }
            } else {
                savedSession.setCurrentGame("direct");
                updateSession(s, true);
                message = AppConstants.PAYMENT_INIT_MESSAGE;
                Runnable paymentTask = () -> {
                    Transaction t = mapper.mapTransactionFromSession(savedSession, gameDraw, false);
                    System.out.println(t.toString());
                    ResponseEntity<String> response = handler.client()
                            .post()
                            .uri("/api/V1/place-bet")
                            .body(t)
                            .contentType(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .toEntity(String.class);
                    System.out.println(response.getBody());
                    System.out.println("Payment Thread running...");
                };

                Runnable sessionTask = () -> {
                    sessionRepository.deleteById(savedSession.getId());
                    System.out.println("Session Thread running...");
                };
                paymentThread.start(paymentTask).join();
                sessionThread.start(sessionTask);

                continueFlag = 1;
            }
        } else {
            deleteSession(savedSession);
            message = "Input must only contain numbers \n 0) Back";
        }
        return menuResponse(savedSession, continueFlag, message);
    }

    public String permGameOptions(int gameType, int position, Session s) throws InterruptedException {
        int continueFlag = 0;
        String message = null;
        int codeType = 0;
        List<String> permGames = s.isMorning() ? AppConstants.PERM_GAMES_MORNING : AppConstants.PERM_GAMES;
        AtomicReference<Integer> index = new AtomicReference<>(0);
        List<Game> currentGameDraw = gameRepository.findAll().stream().filter(game -> game.getGameTypeId() == 15).sorted(Comparator.comparing(Game::getGameName)).toList();
        ;
        final Game gameDraw = s.isMorning() ? currentGameDraw.get(0) : currentGameDraw.get(1);
        Session savedSession = sessionRepository.findBySequenceID(s.getSequenceID());
        savedSession.setGameId(gameDraw.getGameId());
        savedSession.setGameTypeId(gameDraw.getGameDraw());
        //savedSession.setBetTypeCode(AppConstants.PERM);
        updateSession(savedSession, false);
        boolean containsLetters = savedSession.getPosition() != 7 ? ValidationUtils.containsAnyLetters(s.getData()) : false;
        if (!containsLetters) {
            if (savedSession.getGameType() == THIRD && savedSession.getPosition() == SECOND) {
                StringBuilder builder = new StringBuilder();
                permGames.stream().forEachOrdered(game -> {
                    int currentIndex = index.updateAndGet(v -> v + 1);
                    builder.append(String.format("%s) %s\n", currentIndex, game.toString()));
                });
                message = builder.toString();
            } else if (savedSession.getGameType() == THIRD && savedSession.getPosition() == THIRD) {
                String currentGame = null;
                try {
                    currentGame = permGames.get(ValidationUtils.parseNumber(s.getData()).intValue() - 1).toString();
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println(e);
                    deleteSession(savedSession);
                    return menuResponse(savedSession, 0, "Invalid input \n 0) Back");
                }
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
            } else if (savedSession.getGameType() == THIRD && savedSession.getPosition() == FOURTH) {
                String input = ValidationUtils.removeSpecialCharacters(s.getData());
                savedSession.setSelectedNumbers(input);
                String[] selectedNumbers = ValidationUtils.splitNumbers(input);
                int len = selectedNumbers.length;
                boolean exceeds = ValidationUtils.anyNumberExceedsLimit(input, ",", 57);
                boolean containsZero = ValidationUtils.containsSingularZero(input);
                List<Integer> numbers = ValidationUtils.extractNumbers(input);
                Set<Integer> repeatedNumbers = ValidationUtils.findRepeatedNumbers(numbers);

                // System.out.printf("Len => %s Min => %s Max => %s Exceeds => %s\n", len, savedSession.getMin(), savedSession.getMax(), exceeds);

                if (!ValidationUtils.isBetween(len, savedSession.getMin(), savedSession.getMax()) || exceeds || containsZero || !repeatedNumbers.isEmpty()) {
                    message = exceeds ? AppConstants.EXCEEDS_NUMBER_LIMIT_MESSAGE : AppConstants.INVALID_TRAN_MESSAGE;
                    if (!repeatedNumbers.isEmpty()) {
                        message = "Duplicate numbers entered\n 0) Back";
                    }
                    deleteSession(savedSession);
                } else {
                    message = """
                            Type amount to Start (1 - 20):
                            """;
                    updateSession(savedSession, false);
                }
            } else if (savedSession.getGameType() == THIRD && savedSession.getPosition() == FIFTH) {
                Number amount = ValidationUtils.parseNumber(s.getData());
                boolean isDecimal = ValidationUtils.isDecimal(amount.doubleValue());
                if (!isDecimal) {
                    if (amount.intValue() > 20 || amount.intValue() < 1) {
                        deleteSession(savedSession);
                        message = "Amount should be between 1GHS and 20GHS \n 0 Back";
                    } else {
                        savedSession.setAmount(Double.parseDouble(s.getData()));
                        String total = calculateAmountPermAPI(savedSession, "perm");
                        String ticketInfo = """
                                Tck info:
                                --
                                %s
                                Your No: %s
                                \s
                                1) to pay %s GHS.
                                \s
                                2) to apply coupon code.
                                \s
                                0) to cancel.
                                \s""";
                        message = String.format(ticketInfo, gameDraw.getGameName(), s.getSelectedNumbers(), total);
                        savedSession.setAmount(Double.valueOf(total));
                        updateSession(s, false);
                    }
                } else {
                    deleteSession(savedSession);
                    message = "Invalid amount. Enter a round figure.\n 0) Back";
                    continueFlag = 0;
                }
            } else if (gameType == THIRD && position == SIX) {
                String choice = s.getData();
                if (choice.equals("0")) {
                    return ticketCancelledMessage(savedSession);
                } else if (choice.equals("2") && savedSession.getPosition() == 6) {
                    message = AppConstants.DISCOUNT_PROMPT_MESSAGE;
                } else if (choice.equals("1") && savedSession.getPosition() == 6) {
                    message = "Select payment method\n1) Mobile Money\n2) Afriluck Wallet";
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

                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Payment Thread running...");
                    };

                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Session Thread running...");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
                }
            } else if (savedSession.getPosition() == SEVEN) {
                if (savedSession.getData().equals("1")) {
                    // Payment from MOMO
                    savedSession.setCurrentGame("direct");
                    updateSession(s, true);
                    message = AppConstants.PAYMENT_INIT_MESSAGE;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSession(savedSession, gameDraw, false);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());

                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Payment Thread running...");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Session Thread running...");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
                    continueFlag = 1;
                } else if (savedSession.getData().equals("2")) {
                    // Payment from Wallet
                    savedSession.setCurrentGame("direct");
                    updateSession(s, true);
                    message = AppConstants.PAYMENT_INIT_MESSAGE_WALLET;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSession(savedSession, gameDraw, true);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());

                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Payment Thread running...");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Session Thread running...");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
                    continueFlag = 1;
                } else {
                    DiscountResponse response = applyCoupon(s.getAmount(), s.getData());
                    System.out.printf("Discount => ", response);
                    message = discountMessage(response);
                    if (response.getValid()) {
                        savedSession.setDiscountedAmount(response.getAmount());
                        updateSession(savedSession, false);
                    }
                }
            } else if (savedSession.getPosition() == 8 && savedSession.getData().equals("0")) {
                return ticketCancelledMessage(savedSession);
            } else if (savedSession.getPosition() == 8 && savedSession.getData().equals("1")) {
                message = "Select payment method\n1) Mobile Money\n2) Afriluck Wallet";
            } else if (savedSession.getPosition() == 9) {
                if (savedSession.getData().equals("1")) {
                    message = AppConstants.PAYMENT_INIT_MESSAGE;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSession(s, gameDraw, false);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());

                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Payment Thread running...");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Session Thread running...");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
                    continueFlag = 1;
                } else if (savedSession.getData().equals("2")) {
                    message = AppConstants.PAYMENT_INIT_MESSAGE_WALLET;
                    Runnable paymentTask = () -> {
                        Transaction t = mapper.mapTransactionFromSession(s, gameDraw, true);
                        System.out.println(t.toString());
                        ResponseEntity<String> response = handler.client()
                                .post()
                                .uri("/api/V1/place-bet")
                                .body(t)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .toEntity(String.class);
                        System.out.println(response.getBody());

                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Payment Thread running...");
                    };
                    Runnable sessionTask = () -> {
                        sessionRepository.deleteById(savedSession.getId());
                        System.out.println("Session Thread running...");
                    };
                    paymentThread.start(paymentTask).join();
                    sessionThread.start(sessionTask);
                    continueFlag = 1;
                }
            } else {
                savedSession.setCurrentGame("direct");
                updateSession(s, true);
                message = AppConstants.PAYMENT_INIT_MESSAGE;
                Runnable paymentTask = () -> {
                    Transaction t = mapper.mapTransactionFromSession(savedSession, gameDraw, false);
                    System.out.println(t.toString());
                    ResponseEntity<String> response = handler.client()
                            .post()
                            .uri("/api/V1/place-bet")
                            .body(t)
                            .contentType(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .toEntity(String.class);
                    System.out.println(response.getBody());

                    sessionRepository.deleteById(savedSession.getId());
                    System.out.println("Payment Thread running...");
                };
                Runnable sessionTask = () -> {
                    sessionRepository.deleteById(savedSession.getId());
                    System.out.println("Session Thread running...");
                };
                paymentThread.start(paymentTask).join();
                sessionThread.start(sessionTask);
                continueFlag = 1;
            }
        } else {
            deleteSession(savedSession);
            message = "Input must only contain numbers \n 0) Back";
        }
        return menuResponse(savedSession, continueFlag, message);
    }

    @Async
    private String getDrawResults(Session session) {
        ResponseEntity<String> response = handler.client()
                .get()
                .uri("/api/V1/draw-results")
                .retrieve()
                .toEntity(String.class);
        return response.getBody();
    }

    @Async
    private String getLastFiveTransactions(Session session, String msisdn) {
        ResponseEntity<String> response = handler.client()
                .get()
                .uri("/api/V1/recent-tickets?msisdn=" + msisdn)
                //.body(String.format("{\"msisdn\":\"%s\"}", msisdn))
                //.contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);
        return response.getBody();
    }

    private CustomerBalanceDto getCustomerBalance(String msisdn) {
        ResponseEntity<CustomerBalanceDto> response = handler.client()
                .get()
                .uri("/api/V1/account/check-balance?msisdn=" + msisdn)
                .retrieve()
                .toEntity(CustomerBalanceDto.class);
        return response.getBody();
    }

    private CustomerDepositResponseDto customerDeposit(String msisdn, String amount, String channel) {
        String body = String.format("{\"msisdn\":\"%s\",\"amount\":\"%s\",\"channel\":\"%s\"}", msisdn, amount, channel);
        ResponseEntity<CustomerDepositResponseDto> response = handler.client()
                .post()
                .uri("/api/V1/account/deposit")
                .body(body)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(CustomerDepositResponseDto.class);
        return response.getBody();
    }

    @Async
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

    @Async
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

    public String ticketCancelledMessage(Session savedSession) {
        deleteSession(savedSession);
        return menuResponse(savedSession, 0, "Ticket cancelled by user\n 0) Back");
    }

    @Async
    public DiscountResponse applyCoupon(double amount, String coupon) {
        ResponseEntity<DiscountResponse> response = handler.client()
                .get()
                .uri(String.format("/api/V1/calculate-discount?amount=%s&code=%s", amount, coupon))
                .retrieve()
                .toEntity(DiscountResponse.class);
        return response.getBody();
    }

    public String discountMessage(DiscountResponse response) {
        String message = null;
        String ticketInfo;
        if (response.getValid()) {
            ticketInfo = """
                    Coupon applied. New amount to pay: %s GHS.\n
                    Enter 1 to proceed with payment or 0 to cancel.
                    """;
        } else {
            ticketInfo = """
                    Invalid coupon code. Amount to pay: %s GHS.\n
                    Enter 1 to proceed with payment or 0 to cancel.
                    """;
        }
        message = String.format(ticketInfo, response.getAmount());
        return message;
    }

    public String getDayOfWeekInWords() {
        LocalDate currentDate = ValidationUtils.isCurrentGameTime() ? LocalDate.now().plusDays(1) : LocalDate.now();
        DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
        return dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }
}

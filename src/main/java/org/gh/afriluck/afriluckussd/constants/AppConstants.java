package org.gh.afriluck.afriluckussd.constants;

import org.gh.afriluck.afriluckussd.dto.Pair;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class AppConstants {
    public static final String GLOBAL_DATE_FORMAT = "yyyy/MM/dd hh:mm.ss";
    public static final int FIRST = 1;
    public static final int SECOND = 2;
    public static final int THIRD = 3;

    public static final String WELCOME_MENU_MESSAGE = "Welcome to Afriluck-NLA\n1. Mega Jackpot\n2. Direct Game\n3. Perm Game\n4. Banker\n 0. Back".trim();
    public static final String WELCOME_MENU_MESSAGE_NEW_EVENING = "Welcome to Afriluck-NLA\n--\n\n1. Anopa Bosuo (10:00 am)\n2. 6/57 Super %s (%s:00 pm)\n\n4. Deposit to wallet\n5. Account\n6. TnCs\n99. Contact Us".trim();
    public static final String WELCOME_MENU_MESSAGE_NEW = "Welcome to Afriluck-NLA\n--\n\n1. 6/57 Super %s (%s:%s pm)\n\n4. Deposit to wallet\n5. Account\n6. TnCs\n99. Contact Us".trim();
    public static final String WELCOME_MENU_MESSAGE_NEW_SUNDAY = "Welcome to Afriluck-NLA\n--\n\n1. 6/57 Super %s (5:30 pm)\n\n5. Account\n6. TnCs\n99. Contact Us".trim();
    public static final String WELCOME_MENU_MESSAGE_MORNING = "Welcome to Afriluck-NLA\n2. Direct Game\n3. Perm Game\n4. Banker\n 0. Back".trim();
    public static final String ACCOUNT_MENU_MESSAGE = "1) Last Draw Results\n2) Recent Transactions\n3) Wallet \n0. Back";
    public static final String BANKER_MENU_MESSAGE = "1) Banker";
    public static final String MEGA_VALIDATION_MESSAGE = "Numbers must be a total of 6 starting from 1 to 57.\n 0) Back";
    public static final String DISCOUNT_PROMPT_MESSAGE = "Enter your coupon code:";
    public static final String GAME_CLOSED_MESSAGE = "Game is closed for now. Try again later at 7:45 PM";
    public static final String DISCOUNT_VALID_MESSAGE = """
            Invalid coupon code. Amount to pay: %s GHS.\n
            Enter 1 to proceed with payment or 0 to cancel.
            """;
    public static final String DISCOUNT_INVALID_MESSAGE = """
            Invalid coupon code. Amount to pay: %s GHS.\n
            Enter 1 to proceed with payment or 0 to cancel.
            """;
    public static final String MEGA_OPTIONS_CHOICE_MESSAGE = "Choose 6 numbers between 1 and 57 separated by space";
    public static final String AMOUNT_TO_STAKE_MESSAGE = "Choose stake amount\n";
    public static final String MEGA = "mega";
    public static final String DIRECT = "direct";
    public static final String PERM = "perm";
    public static final String BANKER = "banker";
    public static final Integer MAX_MEGA = 6;
    public static final String INVALID_TRAN_MESSAGE = "Numbers selected do not match game type\n 0) Back";
    public static final String EXCEEDS_NUMBER_LIMIT_MESSAGE = "Numbers must be between 1 and 57\n 0) Back";
    public static final List<org.gh.afriluck.afriluckussd.dto.Pair<Integer, Integer>> ranges = List.of(
            new Pair<>(3, 15),
            new Pair<>(4, 10),
            new Pair<>(5, 8),
            new Pair<>(6, 8),
            new Pair<>(7, 8)
    );
    public static final String RANGE_CHOICE_TEMPLATE = """
            Choose %s or not more than %s numbers
            between 1 and 57 separated by space
            99. More info
            """;
    public static List<String> DIRECT_GAMES = List.of(
            "Direct-1(Match first no.)",
            "Direct-2(2 # to win)",
            "Direct-3(3 # to win)",
            "Direct-4(4 # to win)",
            "Direct-5(5 # to win)",
            "Direct-6(6 # to win)");

    public static List<String> DIRECT_GAMES_MORNING = List.of(
            "Direct-1(Match first no.)",
            "Direct-2(2 # to win)",
            "Direct-3(3 # to win)",
            "Direct-4(4 # to win)"
            //"Direct-5(5 # to win)",
            //"Direct-6(6 # to win)"
    );

    public static List<String> PERM_GAMES = List.of(
            "Perm-2(2 nos. to win)",
            "Perm-3(3 nos. to win)",
            "Perm-4(4 nos. to win)",
            "Perm-5(5 nos. to win)",
            "Perm-6(6 nos. to win)"
    );

    public static List<String> PERM_GAMES_MORNING = List.of(
            "Perm-2(2 nos. to win)",
            "Perm-3(3 nos. to win)",
            "Perm-4(4 nos. to win)"
            //"Perm-5(5 nos. to win)",
            //"Perm-6(6 nos. to win)"
    );
    public static String PAYMENT_INIT_MESSAGE = "Payment request initiated.\nApprove to complete ticket purchase";
    public static String PAYMENT_INIT_MESSAGE_WALLET = "Payment request initiated.";
    public static String SUNDAY = "Sunday";
}

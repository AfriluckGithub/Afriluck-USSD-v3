package org.gh.afriluck.afriluckussd.constants;

import org.gh.afriluck.afriluckussd.dto.Pair;

import java.util.*;

public class AppConstants {
    public static final int FIRST = 1;
    public static final int SECOND = 2;
    public static final int THIRD = 3;

    public static final String WELCOME_MENU_MESSAGE = "Welcome to Afriluck NLA\n1. Mega Jackpot\n2. Direct Game\n3. Perm Game\n4. Banker\n5. Account\n6. TnCs\n99. Contact Us".trim();
    public static final String ACCOUNT_MENU_MESSAGE  = "1) Deposit\n2) Balance\n3) Last Draw Results\n4) Recent Transactions";

    public static final String MEGA_OPTIONS_CHOICE_MESSAGE = "Choose 6 numbers between 1 and 57 separated by space";
    public static final String AMOUNT_TO_STAKE_MESSAGE = "Choose stake amount\n";
    public static final String MEGA = "MEGA";
    public static final String DIRECT = "DIRECT";
    public static final String PERM = "PERM";
    public static final Integer MAX_MEGA = 6;

    public static List<String> DIRECT_GAMES = List.of(
            "Direct-1(Match first no.)",
            "Direct-2(2 # to win)",
            "Direct-3(3 # to win)",
            "Direct-4(4 # to win)",
            "Direct-5(5 # to win)",
            "Direct-6(6 # to win)");

    public static List<String> PERM_GAMES = List.of(
            "Perm-2(2 nos. to win)",
            "Perm-3(3 nos. to win)",
            "Perm-4(4 nos. to win)",
            "Perm-5(5 nos. to win)",
            "Perm-6(6 nos. to win)"
    );

    public static final List<org.gh.afriluck.afriluckussd.dto.Pair<Integer, Integer>> ranges = List.of(
            new Pair<>(3, 15),
            new Pair<>(4, 10),
            new Pair<>(5, 8),
            new Pair<>(6, 8),
            new Pair<>(7, 8)
    );

    public static final String RANGE_CHOICE_TEMPLATE = """
            Choose %s or not more than %s numbers
            between 1 & 57 separated by space
            99. More info
            """;

    public static String PAYMENT_INIT_MESSAGE = "Payment request initiated.\nApprove to complete ticket purchase";
}

package org.gh.afriluck.afriluckussd.constants;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static List<String> directGames = List.of(
            "Direct-1(Match first no.)",
            "Direct-2(2 # to win)",
            "Direct-3(3 # to win)",
            "Direct-4(4 # to win)",
            "Direct-5(5 # to win)",
            "Direct-6(6 # to win)");

    public static List<String> permGames = List.of(
            "Perm-2(2 nos. to win)",
            "Perm-3(3 nos. to win)",
            "Perm-4(4 nos. to win)",
            "Perm-5(5 nos. to win)",
            "Perm-6(6 nos. to win)"
    );

    public static final List<AbstractMap.SimpleEntry<Integer, Integer>> permRanges = List.of(
            new AbstractMap.SimpleEntry<>(3, 15),
            new AbstractMap.SimpleEntry<>(4, 10),
            new AbstractMap.SimpleEntry<>(5, 8),
            new AbstractMap.SimpleEntry<>(6, 8),
            new AbstractMap.SimpleEntry<>(7,8)
    );

    public static String PAYMENT_INIT_MESSAGE = "Payment request initiated.\nApprove to complete ticket purchase";
}

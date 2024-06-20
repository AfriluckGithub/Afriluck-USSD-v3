package org.gh.afriluck.afriluckussd.constants;

import java.util.List;

public class AppConstants {
    public static final int FIRST = 1;
    public static final int SECOND = 2;
    public static final int THIRD = 3;

    public static final String WELCOME_MENU_MESSAGE = "Welcome to Afriluck NLA\n 1. Mega Jackpot \n2. Direct Game(Classic)\n3. Perm Game(Classic)\n4. Last Draw Results\n5. Recent Transactions\n6. TnCs \n99. Contact Us".trim();

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

    public static String PAYMENT_INIT_MESSAGE = "Payment request initiated.\nApprove to complete ticket purchase";
}

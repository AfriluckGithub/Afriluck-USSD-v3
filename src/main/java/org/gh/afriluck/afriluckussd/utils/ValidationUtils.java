package org.gh.afriluck.afriluckussd.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtils {

    public static Number parseNumber(String str) throws NumberFormatException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ex) {
                throw new NumberFormatException("Input string is not a valid number: " + str);
            }
        }
    }

    public static boolean isDecimal(double number) {
        return number % 1 != 0;
    }

    public static boolean containsAnyLetters(String input) {
        try {
            String regex = "[a-zA-Z]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);
            return matcher.find();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String removeSpecialCharacters(String input) {
        try {
            String regex = "[^a-zA-Z0-9\\s]";
            return input.replaceAll(regex, " ");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] splitNumbers(String numbersString) {
        try {
            return numbersString.trim().split("[\\s\\W]+");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean containsSingularZero(String input) {
        try {
            String regex = "[\\s\\W]+";
            String[] parts = input.trim().split(regex);
            for (String part : parts) {
                if (part.equals("0")) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static boolean isBetween(int number, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum value cannot be greater than maximum value");
        }
        return number >= min && number <= max;
    }

    public static boolean anyNumberExceedsLimit(String numbersString, String delimiter, int limit) {
        try {
            String[] numberStrings = numbersString.trim().split("[\\s\\W]+");
            System.out.println(numbersString);
            for (String numberString : numberStrings) {
                Number number = parseNumber(numberString.trim());
                System.out.printf("Number => %s Limit => %s\n", number, limit);
                if (number.intValue() > limit) {
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static List<Integer> extractNumbers(String input) {
        List<Integer> numbers = new ArrayList<>();
        //Pattern pattern = Pattern.compile("\\d+");
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            numbers.add(parseNumber(matcher.group()).intValue());
        }

        return numbers;
    }

    public static Set<Integer> findRepeatedNumbers(List<Integer> numbers) {
        Set<Integer> seenNumbers = new HashSet<>();
        Set<Integer> repeatedNumbers = new HashSet<>();

        for (Integer number : numbers) {
            if (!seenNumbers.add(number)) {
                repeatedNumbers.add(number);
            }
        }

        return repeatedNumbers;
    }


    public static boolean containsAny(String mainString, String[] numbers) {
        for (String number : numbers) {
            if (mainString.contains(number)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBetweenGameTime() {
        int startHour = 19;
        int startMinute = 3;
        int endHour = 19;
        int endMinute = 45;

        int dayOfWeekNumber = LocalDate.now().getDayOfWeek().getValue();

        // 0 represents Sunday
        if (dayOfWeekNumber == 7) {
            startHour = 17;
            startMinute = 33;
            endHour = 19;
            endMinute = 45;
        }
        int currentTime = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();
        int startTime = startHour * 60 + startMinute;
        int endTime = endHour * 60 + endMinute;
        return currentTime >= startTime && currentTime <= endTime;
    }


//    public static boolean isEveningGameTime() {
//        int startHour = 10;
//        int startMinute = 0;
//        int endHour = 19;
//        int endMinute = 3;
//        int currentTime = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();
//        int startTime = startHour * 60 + startMinute;
//        int endTime = endHour * 60 + endMinute;
//        return currentTime >= startTime && currentTime <= endTime;
//    }

//    public static boolean isAfternoonGameTime() {
//        int startHour = 19;
//        int startMinute = 45;
//        int endHour = 15;
//        int endMinute = 0;
//
//        int currentTime = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();
//        int startTime = startHour * 60 + startMinute;
//        int endTime = endHour * 60 + endMinute;
//
//        if (startTime > endTime) {
//            return (currentTime >= startTime) || (currentTime <= endTime);
//        } else {
//            return currentTime >= startTime && currentTime <= endTime;
//        }
//    }

    public static boolean isEveningGameTime() {
        LocalTime startTime = LocalTime.of(19, 45); // 7:45 PM
        LocalTime endTime = LocalTime.of(10, 0);   // 10:00 AM
        LocalTime currentTime = LocalTime.now();
        if (currentTime.isAfter(startTime) || currentTime.isBefore(endTime)) {
            return true;
        }
        return false;
    }

    public static boolean isAfternoonGameTime() {
        LocalTime startTime = LocalTime.of(19, 45); // 7:45 PM
        LocalTime endTime = LocalTime.of(13, 30);   // 1:30 PM
        LocalTime currentTime = LocalTime.now();
        if (currentTime.isAfter(startTime) || currentTime.isBefore(endTime)) {
            return true;
        }
        return false;
    }

    public static boolean isCurrentGameTime() {
        int startHour = 19;
        int startMinute = 45;
        int endHour = 24;
        int endMinute = 59;
        int currentTime = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();
        int startTime = startHour * 60 + startMinute;
        int endTime = endHour * 60 + endMinute;
        return currentTime >= startTime && currentTime <= endTime;
    }

    public static boolean isDayTime() {
        LocalTime startTime = LocalTime.of(23, 59);
        LocalTime endTime = LocalTime.of(7, 45);
        LocalTime currentTime = LocalTime.now();
        if (currentTime.isAfter(startTime) || currentTime.isBefore(endTime)) {
            return true;
        }
        return false;
    }


    public static boolean currentGamePeriod() {
        LocalTime startTime = LocalTime.of(0, 0);
        LocalTime endTime = LocalTime.of(19, 45);
        LocalTime currentTime = LocalTime.now();
        boolean isWithinRange = !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
        if (isWithinRange) {
            return true;
        }
        return false;
    }
}

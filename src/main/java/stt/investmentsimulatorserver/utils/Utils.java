package stt.investmentsimulatorserver.utils;

import java.util.List;

public class Utils {
    public static boolean containsKorean(String keyword) {
        return keyword.matches(".*[가-힣].*");
    }

    public static boolean containsEnglishAndNumber(String keyword) {
        return keyword.matches(".*[a-zA-Z].*") && keyword.matches(".*[0-9].*");
    }

    public static <T> List<T> limitList(List<T> list, int limit) {
        if (list.size() < limit) {
            return list;
        }

        return list.subList(0, limit);
    }

    public static double floorMoney(double money, boolean isDollar) {
        if (isDollar) {
            return Math.floor(money * 100) / 100.0;
        }
        return Math.floor(money);
    }

    public static double roundAmount(double amount) {
        return Math.round(amount * 10) / 10.0;
    }
}

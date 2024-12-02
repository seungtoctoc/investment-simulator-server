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
}

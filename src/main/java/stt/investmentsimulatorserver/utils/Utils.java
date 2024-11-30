package stt.investmentsimulatorserver.utils;

public class Utils {
    public static boolean isEnglishOnly(String keyword) {
        return keyword.matches("^[a-zA-Z]+$");
    }
}

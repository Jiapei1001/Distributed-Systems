package servlet;

public class ServerUtil {

    public static boolean isNumeric(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }

        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }
}

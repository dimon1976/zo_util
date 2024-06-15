package by.demon.zoom.util;

public class StringUtil {

    private StringUtil() {
        // Приватный конструктор, чтобы предотвратить создание экземпляров класса
    }

    public static String cleanAndReplace(String str, String replacement) {
        if (str == null) {
            return "";
        }
        // Удаление и замена символов
        String cleanedStr = str.replaceAll("[^\\d.,-]", "").trim();
        return cleanedStr.replaceAll(",", replacement);
    }
}

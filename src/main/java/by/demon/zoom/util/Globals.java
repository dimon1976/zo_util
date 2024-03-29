package by.demon.zoom.util;

import javax.servlet.http.HttpServletRequest;

public class Globals {

    public static final String SUFFIX_XLSX = ".xlsx";
    public static final String VLOOK_RESULT = "vlook_result" + System.currentTimeMillis() + SUFFIX_XLSX;
    public static final String SHEET_NAME = "Data";
    public static final String TEMP_PATH = "C:/temp";

    public static String getBasePath(HttpServletRequest request) {
        String path = request.getContextPath();
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
    }
}

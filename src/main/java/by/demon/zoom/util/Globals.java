package by.demon.zoom.util;

import javax.servlet.http.HttpServletRequest;

public class Globals {

    public static final String SUFFIX_XLSX = ".xlsx";
    public static final String SUFFIX_CSV = ".csv";
    public static final String SHEET_NAME = "Data";
    public static final String TEMP_PATH = "uploadedFiles";

    public static String getBasePath(HttpServletRequest request) {
        String path = request.getContextPath();
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
    }
}

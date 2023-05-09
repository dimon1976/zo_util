package by.demon.zoom.util;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

public class Globals {

    public static final String SUFFIX_XLS = ".xls";
    public static final String SUFFIX_XLSX = ".xlsx";
    public static final String EXPORT_2007 = "export2007_" + System.currentTimeMillis() + SUFFIX_XLSX;
    public static final String VLOOK_RESULT = "vlook_result" + System.currentTimeMillis() + SUFFIX_XLSX;
    public static final String EXPORT_PRODUCT = "product" + System.currentTimeMillis() + SUFFIX_XLS;
    public static final String SHEET_NAME = "Data";

    public static String getBasePath(HttpServletRequest request) {
        String path = request.getContextPath();
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
    }
}

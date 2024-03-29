package by.demon.zoom.util;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class ClassLoaderUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(ClassLoaderUtil.class);
    // class path
    @Getter
    private static String classPath;
    // loader
    @Getter
    private static ClassLoader loader = Thread.currentThread().getContextClassLoader();

    private ClassLoaderUtil() {
    }

    //
    // get class path
    //
    static {
        if (loader == null) {
            LOG.info("using system class loader!");
            loader = ClassLoader.getSystemClassLoader();
        }
        try {
            URL url = loader.getResource("");
            // get class path
            assert url != null;
            File f = new File(url.toURI());
            classPath = f.getAbsolutePath();
            classPath = URLDecoder.decode(classPath, StandardCharsets.UTF_8);
            if (classPath.contains(".jar!")) {
                LOG.warn("using config file inline jar!" + classPath);
                classPath = System.getProperty("user.dir");
                //
                addCurrentWorkingDir2Classpath(classPath);
            }
        } catch (Exception e) {
            LOG.warn("cannot get classpath using getResource(), now using user.dir");
            classPath = System.getProperty("user.dir");
            //
            addCurrentWorkingDir2Classpath(classPath);
        }
        LOG.info("classpath: {}", classPath);
    }

    /**
     * only support 1.7 or higher
     * <a href="http://stackoverflow.com/questions/252893/how-do">http://stackoverflow.com/questions/252893/how-do</a>
     * -you-change-the-classpath-within-java
     */
    private static void addCurrentWorkingDir2Classpath(String path2Added) {
        // Add the conf dir to the classpath
        // Chain the current thread classloader
        URLClassLoader urlClassLoader;
        try {
            urlClassLoader = new URLClassLoader(new URL[]{new File(path2Added).toURI().toURL()}, loader);
            // Replace the thread classloader - assumes
            // you have permissions to do so
            Thread.currentThread().setContextClassLoader(urlClassLoader);
        } catch (Exception e) {
            LOG.warn(e.toString());
        }
    }

}

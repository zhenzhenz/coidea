package sse.tongji.coidea.util;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyLogger {
    public static final String ANSI_RESET = "\033[0m ";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private Class<?> objClass;

    public static MyLogger getLogger(Class<?> objClass) {
        return new MyLogger(objClass);
    }

    private MyLogger(Class<?> objClass) {
        this.objClass = objClass;
    }

    public void debug(String msg) {
        System.out.println(ANSI_BLUE + getTimeStr() + " [DEBUG] "  + objClass.getSimpleName()
                + "(" + getLineNumber() + ") "
                + ": "
                + msg + ANSI_RESET);
    }

    public void debug(String pattern, Object... args) {
        debug(MessageFormat.format(pattern, args));
    }

    public void info(String msg) {
        System.out.println(ANSI_GREEN + getTimeStr() + " [INFO] "  + objClass.getSimpleName()
                + "(" + getLineNumber() + ") "
                + ": "
                + msg + ANSI_RESET);
    }

    public void info(String pattern, Object... args) {
        info(MessageFormat.format(pattern, args));
    }

    public void warn(String msg) {
        System.out.println(ANSI_YELLOW + getTimeStr() + " [WARNING] "  + objClass.getSimpleName() + ": "
                + msg + ANSI_RESET);
    }

    public void warn(String pattern, Object... args) {
        warn(MessageFormat.format(pattern, args));
    }

    public void error(String msg) {
        System.err.println(getTimeStr() + " [ERROR] "  + objClass.getSimpleName() + ": "
                + msg);
    }

    public void error(String pattern, Object... args) {
        error(MessageFormat.format(pattern, args));
    }

    public void error(String type, Exception e) {
        error(type + " " + e.getMessage());
    }

    public void purpled(String msg) {
        System.out.println(ANSI_PURPLE + getTimeStr() + " [INFO] "  + objClass.getSimpleName() + ": "
                + msg + ANSI_RESET);
    }

    public void purpled(String pattern, Object... args) {
        info(MessageFormat.format(pattern, args));
    }

    private String getTimeStr() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Date date = new Date();
        return simpleDateFormat.format(date);
    }

    private int getLineNumber() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[3];
        return e.getLineNumber();
    }
}

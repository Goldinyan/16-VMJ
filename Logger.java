public class Logger {

    public enum LogLevel {
        INFO,
        WARNING,
        ERROR,
        DEBUG,
        SUCCESS,
        FAILED
    }
    
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String BLINK = "\u001B[5m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String CYAN = "\u001B[36m";
    public static final String GRAY = "\u001B[90m";
    
    public static void log(LogLevel level, String message) {
        String logLevel = switch (level) {
            case INFO -> GREEN;
            case WARNING -> CYAN;
            case ERROR -> RED;
            case DEBUG -> CYAN;
            case SUCCESS -> GREEN;
            case FAILED -> RED;
            default -> GRAY;
        };
        System.out.println(logLevel + "[" + level + "] " + message + RESET);
    }
}

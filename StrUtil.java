public final class StrUtil {

    private StrUtil() {} 

    public static boolean equals(String input, String target) {
        if (input.equals(target)) return true;
        if (target == null) return false;
        
        int len = input.length();
        if (len != target.length()) return false;

        for (int i = 0; i < len; i++) {
            if (input.charAt(i) != target.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean is(String input, String a) {
        return equals(input, a);
    }

    public static boolean is(String input, String a, String b) {
        return equals(input, a) || equals(input, b);
    }

    public static boolean is(String input, String a, String b, String c) {
        return equals(input, a) || equals(input, b) || equals(input, c);
    }

    public static boolean is(String input, String a, String b, String c, String d) {
        return equals(input, a) || equals(input, b) || equals(input, c) || equals(input, d);
    }
}
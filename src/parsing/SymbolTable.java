package parsing;

import java.util.Arrays;



public class SymbolTable {
    private static final int CAPACITY = 64; // 2 potenz for fast modulo & mask
    private static final int MASK = CAPACITY - 1;

    private static final String[] names = new String[CAPACITY];
    private static final int[] offsets = new int[CAPACITY];

    public static boolean add(String name, int offset) {
        int index = name.hashCode() & MASK; // bc cap is 2 potenz we can do '&' 

        while (names[index] != null) {
            if (names[index].equals(name)) {
                offsets[index] = offset; // Overwrite
                return false;
            }
            index = (index + 1) & MASK; // Linear probing -> next free space
        }

        names[index] = name;
        offsets[index] = offset;
        return true;
    }

    public static boolean contains(String name) {
        return getOffset(name) != -1;
    }

    public static int getOffset(String name) {
        int index = name.hashCode() & MASK;

        while (names[index] != null) {
            if (names[index].equals(name)) {
                return offsets[index];
            }
            index = (index + 1) & MASK;
        }

        return -1;
    }

    public static void clear() {
        Arrays.fill(names, null);
        Arrays.fill(offsets, 0);
    }
}
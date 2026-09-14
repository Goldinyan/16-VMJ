
public class Assembler {
    public static final int PARSE_ERROR = Integer.MAX_VALUE;

    public static final int MAX_FUNCS = 40;

    public static String[] funcMap = new String[MAX_FUNCS];
    public static int[] funcOffset = new int[MAX_FUNCS];
    public static int funcCount = 0;
    public static String currentFuncName = null;

    private static int byteOffset = 0;
    private static int currentLine = 0;

    public enum Instruction {
        NOP,
        LOADI,
        LOADHI,
        MOV,
        LOADM,
        STOREM,
        ADD,
        SUB,
        MUL,
        DIV,
        AND,
        OR,
        CMP,
        JMP,
        JE,
        JNE,
        CALL,
        RET,
        PUSH,
        POP,
        IRET,
        SYS,
        HALT
    }

    public enum ParseResult {
        SUCCESS,
        SYNTAX_ERROR, // Falsche Formate, fehlende Argumente
        INVALID_REGISTER, // Register nicht im Bereich R0-R15
        INVALID_IMMEDIATE, // Zahlenwert passt nicht in Byte/Short
        SYMBOL_NOT_FOUND, // Label existiert im Symbol Table nicht
        GENERIC_ERROR // Fallback
    }

    public int isFuncDecl(String[] parts) {
        int spaces = 0;
        for (String part : parts) {
            if (part.equals(" ")) {
                spaces++;
            } else {
                break;
            }
        }

        if (spaces == 0 && !InstructionContains(parts[0])) {
            return 1;
        }

        if (spaces == 2 && InstructionContains(parts[2])) {
            return 0;
        }

        Logger.log(Logger.LogLevel.ERROR, "WRONT FORMATTING IN LINE: " + currentLine);
        return PARSE_ERROR;

    }

    public void reset() {
        byteOffset = 0;
        currentLine = 0;
    }

    private boolean InstructionContains(String reg) {
        try {
            Instruction.valueOf(reg);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private int parseFuncDecl(String[] parts) {
        if (parts.length > 1) {
            Logger.log(Logger.LogLevel.ERROR, "ERROR IN FUNCTION DECLARATION IN LINE: " + currentLine);
            return PARSE_ERROR;
        }
        String funcName = parts[0].substring(0, parts[0].length() - 1);

        if (funcCount >= MAX_FUNCS) {
            return 0;
        }

        if (funcMapContains(funcName)) {
            Logger.log(Logger.LogLevel.ERROR, "DUPLICATE FUNCTION NAME IN LINE: " + currentLine);
            return 0;
        }

        funcMap[funcCount] = funcName;
        funcOffset[funcCount] = byteOffset;

        funcCount++;

        return 1;
    }

    private boolean funcMapContains(String name) {
        for (String func : funcMap) {
            if (func.equals(name)) {
                return true;
            }
        }

        return false;
    }

    public byte[] parse(String[] instructions) {

        byte[] bytes = new byte[instructions.length * 2];

        for (String instruction : instructions) {
            String[] parts = instruction.split(" ");
            currentLine++;

            int funcDecl = isFuncDecl(parts);

            if (funcDecl == PARSE_ERROR) {
                return null;
            }

            if (isFuncDecl(parts) == 1) {
                int parsingSuceed = parseFuncDecl(parts);
                if (parsingSuceed == PARSE_ERROR || parsingSuceed == 0) {
                    return null;
                }
            }

            Instruction opcode = Instruction.valueOf(parts[0]);

            switch (opcode) {
                case NOP -> {
                    bytes[byteOffset++] = 0x00;
                    bytes[byteOffset++] = 0x00;
                }
                case LOADI -> {
                    int regId = getRegId(parts[1]);
                    String rawValue = parts[2].trim();
                    int value = getValue(rawValue);

                    if (regId == -1 || value == PARSE_ERROR) {
                        return null;
                    }

                    // 0x10 | bc opcode is 0x1RXX
                    bytes[byteOffset++] = (byte) (0x10 | regId);

                    // second byte is value
                    bytes[byteOffset++] = (byte) value;
                }
                case LOADHI -> {
                    int regId = getRegId(parts[1]);
                    String rawValue = parts[2].trim();
                    int value = getValue(rawValue);

                    if (regId == -1 || value == PARSE_ERROR) {
                        return null;
                    }

                    bytes[byteOffset++] = (byte) (0x20 | regId);
                    bytes[byteOffset++] = (byte) value;
                }
                case MOV -> {
                    int regId1 = getRegId(parts[1]);
                    int regId2 = getRegId(parts[2]);

                    if (regId1 == -1 || regId2 == -1) {
                        return null;
                    }

                    bytes[byteOffset++] = (byte) (0x30 | regId1);
                    bytes[byteOffset++] = (byte) (regId2 << 4);
                }
                case LOADM -> {
                    int regId = getRegId(parts[1]);
                    int memAddRegId = getMemAddReg(parts[2]);

                    if (regId == -1 || memAddRegId == -1) {
                        return null;
                    }

                    bytes[byteOffset++] = (byte) (0x40 | regId);
                    bytes[byteOffset++] = (byte) (memAddRegId << 4);
                }
                case STOREM -> {

                    int memAddRegId = getMemAddReg(parts[1]);
                    int regId = getRegId(parts[2]);

                    if (regId == -1 || memAddRegId == -1) {
                        return null;
                    }

                    bytes[byteOffset++] = (byte) (0x50 | memAddRegId);
                    bytes[byteOffset++] = (byte) (regId << 4);
                }
                case ADD -> {
                    int regId1 = getRegId(parts[1]);
                    int regId2 = getRegId(parts[2]);
                    int regId3 = getRegId(parts[3]);

                    if (regId1 == -1 || regId2 == -1 || regId3 == -1) {
                        return null;
                    }

                    bytes[byteOffset++] = (byte) (0x60 | regId1);
                    bytes[byteOffset++] = (byte) ((regId2 << 4) | regId3);
                }
                case SUB -> {
                    int regId1 = getRegId(parts[1]);
                    int regId2 = getRegId(parts[2]);
                    int regId3 = getRegId(parts[3]);

                    if (regId1 == -1 || regId2 == -1 || regId3 == -1) {
                        return null;
                    }

                    bytes[byteOffset++] = (byte) (0x70 | regId1);
                    bytes[byteOffset++] = (byte) ((regId2 << 4) | regId3);
                }
                case MUL -> {
                    int regId1 = getRegId(parts[1]);
                    int regId2 = getRegId(parts[2]);
                    int regId3 = getRegId(parts[3]);

                    if (regId1 == -1 || regId2 == -1 || regId3 == -1) {
                        return null;
                    }

                    bytes[byteOffset++] = (byte) (0x80 | regId1);
                    bytes[byteOffset++] = (byte) ((regId2 << 4) | regId3);
                }
                case DIV -> {
                    int regId1 = getRegId(parts[1]);
                    int regId2 = getRegId(parts[2]);
                    int regId3 = getRegId(parts[3]);

                    if (regId1 == -1 || regId2 == -1 || regId3 == -1) {
                        return null;
                    }

                    bytes[byteOffset++] = (byte) (0x90 | regId1);
                    bytes[byteOffset++] = (byte) ((regId2 << 4) | regId3);
                }
                case AND -> {
                    int regId1 = getRegId(parts[1]);
                    int regId2 = getRegId(parts[2]);
                    int regId3 = getRegId(parts[3]);

                    if (regId1 == -1 || regId2 == -1 || regId3 == -1) {
                        return null;
                    }

                    bytes[byteOffset++] = (byte) (0xA0 | regId1);
                    bytes[byteOffset++] = (byte) ((regId2 << 4) | regId3);
                }
                case OR -> {
                    int regId1 = getRegId(parts[1]);
                    int regId2 = getRegId(parts[2]);
                    int regId3 = getRegId(parts[3]);

                    if (regId1 == -1 || regId2 == -1 || regId3 == -1) {
                        return null;
                    }

                    bytes[byteOffset++] = (byte) (0xB0 | regId1);
                    bytes[byteOffset++] = (byte) ((regId2 << 4) | regId3);
                }
                case CMP -> {
                    int regId1 = getRegId(parts[1]);
                    int regId2 = getRegId(parts[2]);

                    if (regId1 == -1 || regId2 == -1) {
                        return null;
                    }

                    bytes[byteOffset++] = (byte) (0xC0 | regId1);
                    bytes[byteOffset++] = (byte) (regId2 << 4);
                }
                case JMP -> {
                    int regId = getRegId(parts[1]);

                    if (regId == -1) {
                        return null;
                    }

                    bytes[byteOffset++] = (byte) (0xD0 | regId);
                    bytes[byteOffset++] = (byte) 0x00;
                }
                case JE -> {
                    int regId = getRegId(parts[1]);

                    if (regId == -1) {
                        return null;
                    }

                    // second half of first byte is to distingush between JE and JNE so watch out
                    bytes[byteOffset++] = (byte) (0xE0);
                    bytes[byteOffset++] = (byte) (regId << 4);
                }
                case JNE -> {
                    int regId = getRegId(parts[1]);

                    if (regId == -1) {
                        return null;
                    }

                    // second half of first byte is to distingush between JE and JNE so watch out
                    bytes[byteOffset++] = (byte) (0xE1);
                    bytes[byteOffset++] = (byte) (regId << 4);
                }
                case CALL -> {
                    int regId = getRegId(parts[1]);

                    if (regId == -1) {
                        return null;
                    }

                    // same here sec half of first byte to distingush
                    bytes[byteOffset++] = (byte) (0xF0);
                    bytes[byteOffset++] = (byte) (regId << 4);
                }
                case RET -> {
                    bytes[byteOffset++] = (byte) 0xF1;
                    bytes[byteOffset++] = (byte) 0x00;
                }
                case PUSH -> {
                    int regId = getRegId(parts[1]);

                    if (regId == -1) {
                        return null;
                    }

                    // same here sec half of first byte to distingush
                    bytes[byteOffset++] = (byte) (0xF2);
                    bytes[byteOffset++] = (byte) (regId << 4);
                }
                case POP -> {
                    int regId = getRegId(parts[1]);

                    if (regId == -1) {
                        return null;
                    }

                    // same here sec half of first byte to distingush
                    bytes[byteOffset++] = (byte) (0xF3);
                    bytes[byteOffset++] = (byte) (regId << 4);
                }
                case IRET -> {
                    bytes[byteOffset++] = (byte) 0xF4;
                    bytes[byteOffset++] = (byte) 0x00;
                }
                case SYS -> {
                    bytes[byteOffset++] = (byte) 0xF5;
                    bytes[byteOffset++] = (byte) 0x00;
                }
                case HALT -> {
                    bytes[byteOffset++] = (byte) 0xFF;
                    bytes[byteOffset++] = (byte) 0xFF;
                }
            }
        }
        return bytes;
    }

    private int getValue(String input) {
        try {
            int val = Integer.decode(input.trim());
            if (val < -128 || val > 255) {
                return PARSE_ERROR;
            }
            return val & 0xFF;
        } catch (NumberFormatException e) {
            Logger.log(Logger.LogLevel.ERROR, "INVALID VALUE: Line " + currentLine);
            return PARSE_ERROR;
        }

    }

    private int getRegId(String reg) {
        int len = reg.length();
        if (reg.charAt(len - 1) != ',') {
            Logger.log(Logger.LogLevel.ERROR, "INVALID SYNTAX MISSING COMMA: Line " + currentLine);
            return 1;
        }
        reg = reg.substring(0, len - 1);
        int regId = Integer.parseInt(reg.substring(1));

        if (!reg.startsWith("R") || len > 3 || regId < 0 || regId > 15) {
            Logger.log(Logger.LogLevel.ERROR, "INVALID REGISTER: Line " + currentLine);
            return -1;
        }

        return regId;
    }

    private int getMemAddReg(String reg) {
        int len = reg.length();
        int regId = Integer.parseInt(reg.substring(2, 3));

        if (!reg.startsWith("[") || !reg.endsWith("]") || len > 5 || regId < 0 || regId > 15) {
            Logger.log(Logger.LogLevel.ERROR, "INVALID MEMORY ADDRESS: Line " + currentLine);
            return -1;
        }

        return regId;
    }
}

public class Assembler {
    public static final int PARSE_ERROR = Integer.MIN_VALUE;

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

    public byte[] parse(String[] instructions) {

        byte[] bytes = new byte[instructions.length * 2];
        int i = 0;

        for (String instruction : instructions) {
            String[] parts = instruction.split(" ");
            Instruction opcode = Instruction.valueOf(parts[0]);

            switch (opcode) {
                case NOP -> {
                    bytes[i++] = 0x00;
                    bytes[i++] = 0x00;
                }
                case LOADI -> {
                    int regId = getRegId(parts[1], i);
                    String rawValue = parts[2].trim();
                    int value = getValue(rawValue, i);

                    if (regId == -1 || value == PARSE_ERROR) {
                        return null;
                    }

                    // 0x10 | bc opcode is 0x1RXX
                    bytes[i++] = (byte) (0x10 | regId);

                    // second byte is value
                    bytes[i++] = (byte) value;
                }
                case LOADHI -> {
                    int regId = getRegId(parts[1], i);
                    String rawValue = parts[2].trim();
                    int value = getValue(rawValue, i);

                    if (regId == -1 || value == PARSE_ERROR) {
                        return null;
                    }

                    bytes[i++] = (byte) (0x20 | regId);
                    bytes[i++] = (byte) value;
                }
                case MOV -> {
                    int regId1 = getRegId(parts[1], i);
                    int regId2 = getRegId(parts[2], i);

                    if (regId1 == -1 || regId2 == -1) {
                        return null;
                    }

                    bytes[i++] = (byte) (0x30 | regId1);
                    bytes[i++] = (byte) (regId2 << 4);
                }
                case LOADM -> {
                    int regId = getRegId(parts[1], i);
                    int memAddRegId = getMemAddReg(parts[2], i);

                    if (regId == -1 || memAddRegId == -1) {
                        return null;
                    }

                    bytes[i++] = (byte) (0x40 | regId);
                    bytes[i++] = (byte) (memAddRegId << 4);
                }
                case STOREM -> {

                    int memAddRegId = getMemAddReg(parts[1], i);
                    int regId = getRegId(parts[2], i);

                    if (regId == -1 || memAddRegId == -1) {
                        return null;
                    }

                    bytes[i++] = (byte) (0x50 | memAddRegId);
                    bytes[i++] = (byte) (regId << 4);
                }
                case ADD -> {
                    int regId1 = getRegId(parts[1], i);
                    int regId2 = getRegId(parts[2], i);
                    int regId3 = getRegId(parts[3], i);

                    if (regId1 == -1 || regId2 == -1 || regId3 == -1) {
                        return null;
                    }

                    bytes[i++] = (byte) (0x60 | regId1);
                    bytes[i++] = (byte) ((regId2 << 4) | regId3);
                }
                case SUB -> {
                    int regId1 = getRegId(parts[1], i);
                    int regId2 = getRegId(parts[2], i);
                    int regId3 = getRegId(parts[3], i);

                    if (regId1 == -1 || regId2 == -1 || regId3 == -1) {
                        return null;
                    }

                    bytes[i++] = (byte) (0x70 | regId1);
                    bytes[i++] = (byte) ((regId2 << 4) | regId3);
                }
                case MUL -> {
                    int regId1 = getRegId(parts[1], i);
                    int regId2 = getRegId(parts[2], i);
                    int regId3 = getRegId(parts[3], i);

                    if (regId1 == -1 || regId2 == -1 || regId3 == -1) {
                        return null;
                    }

                    bytes[i++] = (byte) (0x80 | regId1);
                    bytes[i++] = (byte) ((regId2 << 4) | regId3);
                }
                case DIV -> {
                    int regId1 = getRegId(parts[1], i);
                    int regId2 = getRegId(parts[2], i);
                    int regId3 = getRegId(parts[3], i);

                    if (regId1 == -1 || regId2 == -1 || regId3 == -1) {
                        return null;
                    }

                    bytes[i++] = (byte) (0x90 | regId1);
                    bytes[i++] = (byte) ((regId2 << 4) | regId3);
                }
                case AND -> {
                    int regId1 = getRegId(parts[1], i);
                    int regId2 = getRegId(parts[2], i);
                    int regId3 = getRegId(parts[3], i);

                    if (regId1 == -1 || regId2 == -1 || regId3 == -1) {
                        return null;
                    }

                    bytes[i++] = (byte) (0xA0 | regId1);
                    bytes[i++] = (byte) ((regId2 << 4) | regId3);
                }
                case OR -> {
                    int regId1 = getRegId(parts[1], i);
                    int regId2 = getRegId(parts[2], i);
                    int regId3 = getRegId(parts[3], i);

                    if (regId1 == -1 || regId2 == -1 || regId3 == -1) {
                        return null;
                    }

                    bytes[i++] = (byte) (0xB0 | regId1);
                    bytes[i++] = (byte) ((regId2 << 4) | regId3);
                }
                case CMP -> {
                    int regId1 = getRegId(parts[1], i);
                    int regId2 = getRegId(parts[2], i);

                    if (regId1 == -1 || regId2 == -1) {
                        return null;
                    }

                    bytes[i++] = (byte) (0xC0 | regId1);
                    bytes[i++] = (byte) (regId2 << 4);
                }
                case JMP -> {
                    int regId = getRegId(parts[1], i);

                    if (regId == -1) {
                        return null;
                    }

                    bytes[i++] = (byte) (0xD0 | regId);
                    bytes[i++] = (byte) 0x00;
                }
                case JE -> {
                    int regId = getRegId(parts[1], i);

                    if (regId == -1) {
                        return null;
                    }

                    // second half of first byte is to distingush between JE and JNE so watch out
                    bytes[i++] = (byte) (0xE0);
                    bytes[i++] = (byte) (regId << 4);
                }
                case JNE -> {
                    int regId = getRegId(parts[1], i);

                    if (regId == -1) {
                        return null;
                    }

                    // second half of first byte is to distingush between JE and JNE so watch out
                    bytes[i++] = (byte) (0xE1);
                    bytes[i++] = (byte) (regId << 4);
                }
                case CALL -> {
                    int regId = getRegId(parts[1], i);

                    if (regId == -1) {
                        return null;
                    }

                    // same here sec half of first byte to distingush
                    bytes[i++] = (byte) (0xF0);
                    bytes[i++] = (byte) (regId << 4);
                }
                case RET -> {
                    bytes[i++] = (byte) 0xF1;
                    bytes[i++] = (byte) 0x00;
                }
                case PUSH -> {
                    int regId = getRegId(parts[1], i);

                    if (regId == -1) {
                        return null;
                    }

                    // same here sec half of first byte to distingush
                    bytes[i++] = (byte) (0xF2);
                    bytes[i++] = (byte) (regId << 4);
                }
                case POP -> {
                    int regId = getRegId(parts[1], i);

                    if (regId == -1) {
                        return null;
                    }

                    // same here sec half of first byte to distingush
                    bytes[i++] = (byte) (0xF3);
                    bytes[i++] = (byte) (regId << 4);
                }
                case IRET -> {
                    bytes[i++] = (byte) 0xF4;
                    bytes[i++] = (byte) 0x00;
                }
                case SYS -> {
                    bytes[i++] = (byte) 0xF5;
                    bytes[i++] = (byte) 0x00;
                }
                case HALT -> {
                    bytes[i++] = (byte) 0xFF;
                    bytes[i++] = (byte) 0xFF;
                }
            }
        }
        return bytes;
    }

    private int getValue(String input, int i) {
        try {
            int val = Integer.decode(input.trim());
            if (val < -128 || val > 255) {
                return PARSE_ERROR;
            }
            return val & 0xFF;
        } catch (NumberFormatException e) {
            Logger.log(Logger.LogLevel.ERROR, "INVALID VALUE: Line " + i / 2);
            return PARSE_ERROR;
        }

    }

    private int getRegId(String reg, int i) {
        int len = reg.length();
        if(reg.charAt(len - 1) != ',') {
            Logger.log(Logger.LogLevel.ERROR, "INVALID SYNTAX MISSING COMMA: Line " + i / 2);
            return 1;
        }
        reg = reg.substring(0, len - 1);
        int regId = Integer.parseInt(reg.substring(1));

        if (!reg.startsWith("R") || len > 3 || regId < 0 || regId > 15) {
            Logger.log(Logger.LogLevel.ERROR, "INVALID REGISTER: Line " + i / 2);
            return -1;
        }

        return regId;
    }

    private int getMemAddReg(String reg, int i) {
        int len = reg.length();
        int regId = Integer.parseInt(reg.substring(2, 3));

        if (!reg.startsWith("[") || !reg.endsWith("]") || len > 5 || regId < 0 || regId > 15) {
            Logger.log(Logger.LogLevel.ERROR, "INVALID MEMORY ADDRESS: Line " + i / 2);
            return -1;
        }

        return regId;
    }
}

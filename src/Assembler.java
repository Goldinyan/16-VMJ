
import parsing.ParseError;
import parsing.ParseResult;
import parsing.SymbolTable;
import util.Logger;

public class Assembler {
    private final ParseResult parseResult = new ParseResult();

    public static final int MAX_FUNCS = 40;

    public static String currentFuncName = null;

    private static int currentOffset = 0;
    private static int currentLine = 0;
    private static int byteIndex = 0;

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

    /*
     * public boolean isFuncDecl(String instruction) {
     * 
     * if (instruction == null) {
     * this.parseResult.setError(ParseError.SYNTAX_ERROR, currentLine);
     * return false;
     * }
     * 
     * int spaces = 0;
     * while (spaces < instruction.length() &&
     * Character.isWhitespace(instruction.charAt(spaces))) {
     * spaces++;
     * }
     * 
     * if (spaces == 0 && !containsInstruction(instruction.split(" ")[0])) {
     * this.parseResult.setData(1); // is a func decl
     * return true;
     * }
     * 
     * if (spaces == 2 && containsInstruction(instruction.split(" ")[1])) {
     * this.parseResult.setData(0); // not a func decl
     * return true;
     * }
     * 
     * this.parseResult.setError(ParseError.SYNTAX_ERROR, currentLine);
     * return false;
     * }
     */

    /*
     * private boolean containsInstruction(String name) {
     * 
     * if (name == null)
     * return false;
     * for (Instruction instr : Instruction.values()) {
     * if (instr.name().equals(name)) {
     * return true;
     * }
     * }
     * return false;
     * }
     */
    
    public byte[] assemble(String[] lines) {

        // SYMBOL TABLE
        SymbolTable.clear();

        currentOffset = 0;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue; // kommis & spaces
            }

            if (trimmed.endsWith(":")) {
                String funcName = trimmed.substring(0, trimmed.length() - 1);
                SymbolTable.add(funcName, currentOffset);
                continue;
            }

            currentOffset += 2;
        }

        int maxCodeSize = 0x2000 - 0x0200;
        byte[] bytes = new byte[maxCodeSize];
        byteIndex = 0;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.endsWith(":")) {
                continue; // func decl dont create 2 bytes code
            }
            String[] parts = trimmed.split("\\s+");
            Instruction opcode = Instruction.valueOf(parts[0]);

            switch (opcode) {
                case NOP -> {
                    bytes[byteIndex++] = 0x00;
                    bytes[byteIndex++] = 0x00;
                }
                case LOADI -> {
                    int regId;
                    int immVal;

                    if (!getRegId(parts[1])) {
                        parseResult.logError();
                        return null;
                    }
                    regId = parseResult.getData();

                    if (!getValue(parts[2].trim())) {
                        parseResult.logError();
                        return null;
                    }
                    immVal = parseResult.getData();

                    bytes[byteIndex++] = (byte) (0x10 | regId);
                    bytes[byteIndex++] = (byte) immVal;
                }
                case LOADHI -> {
                    int regId;
                    int immVal;

                    if (!getRegId(parts[1])) {
                        parseResult.logError();
                        return null;
                    }
                    regId = parseResult.getData();

                    if (!getValue(parts[2].trim())) {
                        parseResult.logError();
                        return null;
                    }
                    immVal = parseResult.getData();

                    bytes[byteIndex++] = (byte) (0x20 | regId);
                    bytes[byteIndex++] = (byte) immVal;
                }
                case MOV -> {
                    int regId1;
                    int regId2;

                    if (!getRegId(parts[1])) {
                        parseResult.logError();
                        return null;
                    }
                    regId1 = parseResult.getData();

                    if (!getRegId(parts[2])) {
                        parseResult.logError();
                        return null;
                    }
                    regId2 = parseResult.getData();

                    bytes[byteIndex++] = (byte) (0x30 | regId1);
                    bytes[byteIndex++] = (byte) (regId2 << 4);
                }
                case LOADM -> {
                    int regId;
                    int memAddRegId;

                    if (!getRegId(parts[1])) {
                        parseResult.logError();
                        return null;
                    }
                    regId = parseResult.getData();

                    if (!getMemAddReg(parts[2])) {
                        parseResult.logError();
                        return null;
                    }
                    memAddRegId = parseResult.getData();

                    bytes[byteIndex++] = (byte) (0x40 | regId);
                    bytes[byteIndex++] = (byte) (memAddRegId << 4);
                }
                case STOREM -> {
                    int memAddRegId;
                    int regId;

                    if (!getMemAddReg(parts[1])) {
                        parseResult.logError();
                        return null;
                    }
                    memAddRegId = parseResult.getData();

                    if (!getRegId(parts[2])) {
                        parseResult.logError();
                        return null;
                    }
                    regId = parseResult.getData();

                    bytes[byteIndex++] = (byte) (0x50 | memAddRegId);
                    bytes[byteIndex++] = (byte) (regId << 4);
                }
                case ADD -> {
                    int regId1;
                    int regId2;
                    int regId3;

                    if (!getRegId(parts[1])) {
                        parseResult.logError();
                        return null;
                    }
                    regId1 = parseResult.getData();

                    if (!getRegId(parts[2])) {
                        parseResult.logError();
                        return null;
                    }
                    regId2 = parseResult.getData();

                    if (!getRegId(parts[3])) {
                        parseResult.logError();
                        return null;
                    }
                    regId3 = parseResult.getData();

                    bytes[byteIndex++] = (byte) (0x60 | regId1);
                    bytes[byteIndex++] = (byte) ((regId2 << 4) | regId3);
                }
                case SUB -> {
                    int regId1;
                    int regId2;
                    int regId3;

                    if (!getRegId(parts[1])) {
                        parseResult.logError();
                        return null;
                    }
                    regId1 = parseResult.getData();

                    if (!getRegId(parts[2])) {
                        parseResult.logError();
                        return null;
                    }
                    regId2 = parseResult.getData();

                    if (!getRegId(parts[3])) {
                        parseResult.logError();
                        return null;
                    }
                    regId3 = parseResult.getData();

                    bytes[byteIndex++] = (byte) (0x70 | regId1);
                    bytes[byteIndex++] = (byte) ((regId2 << 4) | regId3);
                }
                case MUL -> {
                    int regId1;
                    int regId2;
                    int regId3;

                    if (!getRegId(parts[1])) {
                        parseResult.logError();
                        return null;
                    }
                    regId1 = parseResult.getData();

                    if (!getRegId(parts[2])) {
                        parseResult.logError();
                        return null;
                    }
                    regId2 = parseResult.getData();

                    if (!getRegId(parts[3])) {
                        parseResult.logError();
                        return null;
                    }
                    regId3 = parseResult.getData();

                    bytes[byteIndex++] = (byte) (0x80 | regId1);
                    bytes[byteIndex++] = (byte) ((regId2 << 4) | regId3);
                }
                case DIV -> {
                    int regId1;
                    int regId2;
                    int regId3;

                    if (!getRegId(parts[1])) {
                        parseResult.logError();
                        return null;
                    }
                    regId1 = parseResult.getData();

                    if (!getRegId(parts[2])) {
                        parseResult.logError();
                        return null;
                    }
                    regId2 = parseResult.getData();

                    if (!getRegId(parts[3])) {
                        parseResult.logError();
                        return null;
                    }
                    regId3 = parseResult.getData();

                    bytes[byteIndex++] = (byte) (0x90 | regId1);
                    bytes[byteIndex++] = (byte) ((regId2 << 4) | regId3);
                }
                case AND -> {
                    int regId1;
                    int regId2;
                    int regId3;

                    if (!getRegId(parts[1])) {
                        parseResult.logError();
                        return null;
                    }
                    regId1 = parseResult.getData();

                    if (!getRegId(parts[2])) {
                        parseResult.logError();
                        return null;
                    }
                    regId2 = parseResult.getData();

                    if (!getRegId(parts[3])) {
                        parseResult.logError();
                        return null;
                    }
                    regId3 = parseResult.getData();

                    bytes[byteIndex++] = (byte) (0xA0 | regId1);
                    bytes[byteIndex++] = (byte) ((regId2 << 4) | regId3);
                }
                case OR -> {
                    int regId1;
                    int regId2;
                    int regId3;

                    if (!getRegId(parts[1])) {
                        parseResult.logError();
                        return null;
                    }
                    regId1 = parseResult.getData();

                    if (!getRegId(parts[2])) {
                        parseResult.logError();
                        return null;
                    }
                    regId2 = parseResult.getData();

                    if (!getRegId(parts[3])) {
                        parseResult.logError();
                        return null;
                    }
                    regId3 = parseResult.getData();

                    bytes[byteIndex++] = (byte) (0xB0 | regId1);
                    bytes[byteIndex++] = (byte) ((regId2 << 4) | regId3);
                }
                case CMP -> {
                    int regId1;
                    int regId2;

                    if (!getRegId(parts[1])) {
                        parseResult.logError();
                        return null;
                    }
                    regId1 = parseResult.getData();

                    if (!getRegId(parts[2])) {
                        parseResult.logError();
                        return null;
                    }
                    regId2 = parseResult.getData();

                    bytes[byteIndex++] = (byte) (0xC0 | regId1);
                    bytes[byteIndex++] = (byte) (regId2 << 4);
                }
                case JMP -> {
                    String targetFunc = parts[1];
                    int targetOffset = SymbolTable.getOffset(targetFunc);

                    if (targetOffset == -1) {
                        Logger.logParseError(ParseError.SYMBOL_NOT_FOUND, currentLine);
                        return null;
                    }

                    int target = targetOffset + 0x0200; // 0x0206

                    // load address in 2 steps into R0
                    bytes[byteIndex++] = 0x10 | 15; // LOADI Opcode
                    bytes[byteIndex++] = (byte) (target & 0xFF);

                    bytes[byteIndex++] = 0x20 | 15; // LOADHI Opcode
                    bytes[byteIndex++] = (byte) ((target >> 8) & 0xFF);

                    bytes[byteIndex++] = (byte) 0xD0;
                    bytes[byteIndex++] = (byte) (15 << 4);

                }
                case JE -> {
                    String targetFunc = parts[1];
                    int targetOffset = SymbolTable.getOffset(targetFunc);

                    if (targetOffset == -1) {
                        Logger.logParseError(ParseError.SYMBOL_NOT_FOUND, currentLine);
                        return null;
                    }

                    int target = targetOffset + 0x0200; // 0x0206

                    // load address in 2 steps into R0
                    bytes[byteIndex++] = 0x10 | 15; // LOADI Opcode
                    bytes[byteIndex++] = (byte) (target & 0xFF);

                    bytes[byteIndex++] = 0x20 | 15; // LOADHI Opcode
                    bytes[byteIndex++] = (byte) ((target >> 8) & 0xFF);

                    bytes[byteIndex++] = (byte) 0xE0;
                    bytes[byteIndex++] = (byte) ((byte) 15 << 4);
                }
                case JNE -> {
                    String targetFunc = parts[1];
                    int targetOffset = SymbolTable.getOffset(targetFunc);

                    if (targetOffset == -1) {
                        Logger.logParseError(ParseError.SYMBOL_NOT_FOUND, currentLine);
                        return null;
                    }

                    int target = targetOffset + 0x0200; // 0x0206

                    // load address in 2 steps into R0
                    bytes[byteIndex++] = 0x10 | 15; // LOADI Opcode
                    bytes[byteIndex++] = (byte) (target & 0xFF);

                    bytes[byteIndex++] = 0x20 | 15; // LOADHI Opcode
                    bytes[byteIndex++] = (byte) ((target >> 8) & 0xFF);

                    bytes[byteIndex++] = (byte) 0xE1;
                    bytes[byteIndex++] = (byte) ((byte) 15 << 4);
                }
                case CALL -> {
                    String targetFunc = parts[1];
                    int targetOffset = SymbolTable.getOffset(targetFunc);

                    if (targetOffset == -1) {
                        Logger.logParseError(ParseError.SYMBOL_NOT_FOUND, currentLine);
                        return null;
                    }

                    int target = targetOffset + 0x0200; // 0x0206

                    // load address in 2 steps into R0
                    bytes[byteIndex++] = 0x10 | 15; // LOADI Opcode
                    bytes[byteIndex++] = (byte) (target & 0xFF);

                    bytes[byteIndex++] = 0x20 | 15; // LOADHI Opcode
                    bytes[byteIndex++] = (byte) ((target >> 8) & 0xFF);

                    bytes[byteIndex++] = (byte) 0xF0;
                    bytes[byteIndex++] = (byte) ((byte) 15 << 4);
                }
                case RET -> {
                    bytes[byteIndex++] = (byte) 0xF1;
                    bytes[byteIndex++] = (byte) 0x00;
                }
                case PUSH -> {
                    int regId;

                    if (!getRegId(parts[1])) {
                        parseResult.logError();
                        return null;
                    }
                    regId = parseResult.getData();

                    bytes[byteIndex++] = (byte) 0xF2;
                    bytes[byteIndex++] = (byte) (regId << 4);
                }
                case POP -> {
                    int regId;

                    if (!getRegId(parts[1])) {
                        parseResult.logError();
                        return null;
                    }
                    regId = parseResult.getData();

                    bytes[byteIndex++] = (byte) 0xF3;
                    bytes[byteIndex++] = (byte) (regId << 4);
                }
                case IRET -> {
                    bytes[byteIndex++] = (byte) 0xF4;
                    bytes[byteIndex++] = (byte) 0x00;
                }
                case SYS -> {
                    bytes[byteIndex++] = (byte) 0xF5;
                    bytes[byteIndex++] = (byte) 0x00;
                }
                case HALT -> {
                    bytes[byteIndex++] = (byte) 0xFF;
                    bytes[byteIndex++] = (byte) 0xFF;
                }
            }

            currentLine++;
        }
        return bytes;
    }

    public int getByteCount() {
        return byteIndex;
    }

    private boolean getValue(String input) {
        try {
            int val = Integer.decode(input.trim());
            if (val < -128 || val > 255) {
                this.parseResult.setError(ParseError.INVALID_IMMEDIATE, currentLine);
                return false;
            }
            this.parseResult.setData(val & 0xFF);
        } catch (NumberFormatException e) {
            this.parseResult.setError(ParseError.INVALID_IMMEDIATE, currentLine);
            return false;
        }

        return true;
    }

    private boolean getRegId(String reg) {
        int len = reg.length();
        if (reg.charAt(len - 1) != ',') {
            this.parseResult.setError(ParseError.SYNTAX_ERROR, currentLine);
            return false;
        }

        reg = reg.substring(0, len - 1);
        int regId = Integer.parseInt(reg.substring(1));

        if (!reg.startsWith("R") || len > 3 || regId < 0 || regId > 15) {
            this.parseResult.setError(ParseError.INVALID_REGISTER, currentLine);
            return false;
        }

        this.parseResult.setData(regId);
        return true;
    }

    private boolean getMemAddReg(String reg) {
        int len = reg.length();
        int regId = Integer.parseInt(reg.substring(2, 3));

        if (!reg.startsWith("[") || !reg.endsWith("]") || len > 5 || regId < 0 || regId > 15) {
            this.parseResult.setError(ParseError.INVALID_REGISTER, currentLine);
            return false;
        }

        this.parseResult.setData(regId);
        return true;
    }
}

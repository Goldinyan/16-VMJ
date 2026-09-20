


public class CPU {
    public static byte[] ram = new byte[65536];

    private final byte[] R = new byte[16];
    private int I;
    private int PC = 0x0200; // Program Counter
    private int SP = 0x0000; // Stack Pointer
    private int ZF = 0; // Zero Flag
    private int OF = 0; // Overflow Flag


    public void execute()
    {
        int instruction = readWord(PC); 
        PC += 2;

        int prefix = (instruction & 0xF000) >> 12;
        
    }

    public void loadROM(byte[] rom) {
        System.arraycopy(rom, 0, ram, 0x0200, rom.length);
    }

    
    public void writeWord(int address, int value)
    {
        ram[address] = (byte) (value & 0xFF);
        ram[address + 1] = (byte) ((value >> 8) & 0xFF);
    }

    public int readWord(int address)
    {
        return (ram[address] & 0xFF) | ((ram[address + 1] & 0xFF) << 8);
    }
}




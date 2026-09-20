import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import util.Logger;

public class VM {
    private final CPU cpu = new CPU();
    private final Assembler assembler = new Assembler();

    private boolean loaded = false;
    private boolean running = false;
    private final Scanner scanner = new Scanner(System.in);

    public VM() {
        Logger.log(Logger.LogLevel.INFO, "Loading...");
        Logger.log(Logger.LogLevel.INFO, "Custom 16-bit Virtual Machine");
        Logger.log(Logger.LogLevel.INFO, "By: 0x0520");
        Logger.log(Logger.LogLevel.INFO, "Version: 0.1.4");
        Logger.log(Logger.LogLevel.INFO, "Release Date: 12/09/2026");
    }

    public void start() {
        String input;
        while (!running) {
            System.out
                    .print(Logger.CYAN + Logger.BOLD + ">> " + Logger.RESET + Logger.BLINK + "█" + Logger.RESET + "\b");

            input = scanner.nextLine();
            parseInput(input);
        }

        // byte[] rom = assembler.generateBytes(code);

    }

    private void parseInput(String input) {
        if (input == null || input.isBlank())
            return;

        // parts cmd in 2 parts after first whitespace, idk how tho lol
        String[] parts = input.trim().split("\\s+", 2);
        String command = parts[0];

        switch (command) {
            case "help" -> printHelp();
            case "exit" -> System.exit(0);
            case "run" -> run();
            case "load" -> {
                if (parts.length < 2) {
                    Logger.log(Logger.LogLevel.ERROR, "Usage: load <filename>");
                } else {
                    loadFile(parts[1]);
                }
            }
            default -> Logger.log(Logger.LogLevel.ERROR, "Invalid command: " + command);
        }
    }

    private String[] getFile(String filename) {

        if (!filename.endsWith(".casm")) {
            Logger.log(Logger.LogLevel.ERROR, "Invalid file extension: " + filename);
            Logger.log(Logger.LogLevel.ERROR, "File extension must be .casm");
            return null;
        }

        File file = new File(filename);
        if (!file.exists()) {
            Logger.log(Logger.LogLevel.ERROR, "File not found: " + file.getAbsolutePath());
            return null;
        }

        try {
            List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
            loaded = true;
            return lines.toArray(String[]::new);
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.ERROR, "Error reading file: " + e.getMessage());
            return null;
        }

    }

    private void loadFile(String filename) {
        String[] instructions = getFile(filename);
        if (instructions == null) {
            return;
        }

        loaded = true;

        byte[] rom = assembler.assemble(instructions);
        int byteCount = assembler.getByteCount();

        if (rom != null) {
            System.out.print("ROM Bytes [Hex]: ");
            for (int i = 0; i < byteCount; i++) {
                System.out.format("%02X ", rom[i] & 0xFF);
                if ((i + 1) % 2 == 0) {
                    System.out.print("| ");
                }
            }
            System.out.println();
        }

    }

    private void run() {
        if (!loaded)
            Logger.log(Logger.LogLevel.FAILED, "No program loaded");
        else {
            running = true;
            cpu.execute();
        }

    }

    private void printHelp() {
        System.out.println(Logger.CYAN + Logger.BOLD + "Available Commands:" + Logger.RESET + "\n");
        System.out.println(Logger.CYAN + Logger.GREEN + "help                Show this help message" + Logger.RESET);
        System.out.println(Logger.CYAN + "load <filename>     Load a file into the ROM" + Logger.RESET + "\n");
        System.out.println(
                Logger.CYAN + Logger.GREEN + "run                 Run the loaded program" + Logger.RESET + "\n");
        System.out.println(
                Logger.CYAN + Logger.GREEN + "exit                Exit the virtual machine" + Logger.RESET + "\n");
    }
}

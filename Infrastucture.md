
# INFRASTRUCTURE

## CUSTOM 16-BIT VM & ASSEMBLER

VIRTUAL MEMORY (RAM):

- 64 KB (65.536 as byte[])
- Memory-Layout.md
- Logic:
  - readByte / writeByte (8 bit)
  - readWork / writeWord (16 Bit = 2 * 8 bit)

## CPU / REGISTERS

- Data Registers (16 Bit, 0 - 65.535)
  - R0 - R15 (General Purpose)
         !NOTE: Two-Step Assembler needs one reg for mem address of loop, so if no one is empty,
         we push one to the stack, which is not used in the loop, and after we finish we load it back
  - Special Purpose Register (SPR)
    - PC (Program Counter) - current instruction in ram
    - SP (Stack Pointer)   - top of stack
    - FLAGS (Status Registers)
      - Zero Flag (Z) - 1 if result was 0
      - Carry / Overflow Flag (C) - 1 if Overflow
  - Fetch-Decode-Execute Loop
    - FETCH: GEt 16 bit at PC, after PC += 2, bc ram is byte
    - DECODE: Bitmask to cut opcode and register out
    - EXECUTE: switch opcode for logic

## ASSEMBLER (Two-Pass Parser & Two-Pass Assembler)

- Pass 1: Symbol Table Generator
  - line for line
  - labels like 'LOOP' search and save address 
  - count byte size and instruction count

- Pass 2: Code Generator
  - parses ISA into bytes
  - labels like 'LOOP' into real addresses out of Pass 1
  - return binary byte file

## MEMORY LAYOUT

0x0000 - 0x00FF : Reserved / Vector Table (z. B. System-Flags)
0x0100 - 0x01FF : Call Stack (für Unterprogramme)
0x0200 - 0x7FFF : Programm-Code & Statische Daten
0x8000 - 0xFFFF : Freier Arbeitsspeicher (Heap / User Data)

## ISA - Instruction Set Architecture (16-Bit fixed Width)

Base Formats:
  R-Format: [ Opcode: 4 Bit ][ Reg Dest: 4 Bit ][ Reg Src1: 4 Bit ][ Reg Src2: 4 Bit ]
  I-Format: [ Opcode: 4 Bit ][ Reg Dest: 4 Bit ][ Immediate Value: 8 Bit ]
  J-Format: [ Opcode: 4 Bit ][ Sub-Op: 4 Bit   ][ Reg Target: 4 Bit ][ Unused: 4 Bit ]

Addressing Modes:
  R2   -> Direct Register Value
  [R2] -> Memory Reference (Uses value in R2 as RAM Address)

1.  NOP     - 0x0000 - NOP               - Does nothing
2.  LOADI   - 0x1RXX - LOAD R1, 0x42     - Loads 8bit value XX into register R
3.  MOV     - 0x2R12 - MOV R1, R2        - Copies value from R2 into R1 (uses 12 bits)
4.  LOADM   - 0x3R12 - LOADM R1, [R2]    - Reads value out of ram address in R2 and saves in R1
5.  STOREM  - 0x4R12 - STOREM [R1], R2   - Writes value from R2 into ram address in R1
6.  ADD     - 0x5123 - ADD R1, R2, R3    - R1 = R2 + R3 -> sets Z-Flag if 0
7.  SUB     - 0x6123 - SUB R1, R2, R3    - R1 = R2 - R3 -> sets Z-Flag if 0
8.  MUL     - 0x7123 - MUL R1, R2, R3    - R1 = R2 * R3
9.  DIV     - 0x8123 - DIV R1, R2, R3    - R1 = R2 / R3
10. AND     - 0x9123 - AND R1, R2, R3    - Bitwise AND: R1 = R2 & R3
11. OR      - 0xA123 - OR R1, R2, R3     - Bitwise OR: R1 = R2 | R3
12. CMP     - 0xB012 - CMP R1, R2        - Compares R1 with R2 -> sets Z-Flag if R1 == R2
13. JMP     - 0xC0R1 - JMP R1            - Unconditional jump to address in R1
14. JE      - 0xD0R1 - JE R1             - Jump to address in R1 if Z-Flag == 1
15. JNE     - 0xE0R1 - JNE R1            - Jump to address in R1 if Z-Flag == 0
16. PUSH    - 0xF100 - PUSH R1           - Decrements SP by 2 and writes R1 to [SP]
17. POP     - 0xF200 - POP R1            - Reads [SP] into R1 and increments SP by 2
18. HALT    - 0xFFFF - HALT              - Stops the CPU execution loop


EXAMPLE:

; setup initial values and load memory
main:
  LOADI r1, 0x05       ; r1 = 5
  LOADI r2, 0x03       ; r2 = 3
  MOV r3, r1           ; r3 = 5 (copy from r1)

; arithmetic & bitwise operations
  ADD r4, r1, r2       ; r4 = 5 + 3 = 8
  SUB r5, r1, r2       ; r5 = 5 - 3 = 2
  MUL r6, r1, r2       ; r6 = 5 * 3 = 15
  DIV r7, r2, r1       ; r7 = 3 / 5 = 0
  AND r8, r1, r2       ; r8 = 5 & 3 = 1 // 0101 & 0011 = 0001
  OR r9, r1, r2        ; r9 = 5 | 3 = 7 // 0101 | 0011 = 0111

; stack usage
  PUSH r4              ; push 8 onto stack
  POP r10              ; pop 8 into r10

; memory operations (ram)
  LOADI r11, 0x80      ; r11 = 0x80 (ram address)
  STOREM [r11], r4     ; ram[0x80] = 8
  LOADM r12, [r11]     ; r12 = ram[0x80] = 8

; comparison, conditional jump & loop
  LOADI r13, 0x00      ; r13 = 0 (counter)
  LOADI r14, 0x01      ; r14 = 1 (increment)

; init loop reg
  LOADI r15, 0x1A      ; assumes loop is adress 0x1A in RAm
                       ; Normally the "two part assembler" does this work, he looks after
                       ; the label 'loop:' finds byte adress in ram and files it in

LOOP:
  ADD r13, r13, r14    ; r13 = r13 + 1
  CMP r13, r2          ; compare r13 with 3
  JNE r15              ; jump to loop address in r15 if r13 != 3
  JE r15               ; dummy jump check if equal

; termination
  NOP                  ; do nothing
  HALT                 ; stop execution
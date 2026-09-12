# INFRASTRUCTURE

## CUSTOM 16-BIT VM & ASSEMBLER

VIRTUAL MEMORY (RAM):

- 64 KB (65.536 as byte[])
- Logic:
  - readByte / writeByte (8 bit)
  - readWork / writeWord (16 Bit = 2 * 8 bit)

## CPU / REGISTERS

- Data Registers (16 Bit, 0 - 65.535)
  - R0 - R15 (General Purpose)
   !NOTE: Convention for SYSCALLS: R0 = Service Mode ID, R1 = Service Argument
   !NOTE: Two-Step Assembler needs one reg for mem address of loop, so if no one is empty,
   we push one to the stack, which is not used in the loop, and after we finish we load it back
  - Special Purpose Register (SPR)
    - PC (Program Counter) - current instruction in ram
    - SP (Stack Pointer)   - top of stack
    - FLAGS (Status Registers)
      - Zero Flag (Z) - 1 if result was 0
      - Carry / Overflow Flag (C) - 1 if Overflow
  - Fetch-Decode-Execute Loop
    - FETCH: GEt 16 bit at PC, after PC += 2, bc ram is byte-addressable
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

0x0000 - 0x0200 : Reserved / Vector Table (z. B. System-Flags)
0x0200 - 0x2000 : Programm-Code & .data
0x2000 - 0xFFFF : Heap & Stack | Heap grows up & Stack starts up and goes down
                : if meet -> Stack Overflow | Out of Memory (Error)

0x0000 - 0x00FF : (256 Bytes) is the interface between VM-Hardware and Software
                : If we get an unexpected event, e.g: (Runtime-Error, Inputs, Timer)
                : the hardware has to know, on which ram-address the current
                : instruction is. Without a const table in ram the address would 
                : have to be hardcoded in the cpu
                :
                : If we get an interrupt, so the signal, the cpu loop will stop.
                : The cpu stops current procress, save the PC on the stack, and goes
                : on to the special route -> (Interrupt Handler).
                : There will be a memory address to a handler in the code which will
                : be crafted by the assembler, after it goes back to the old PC
                :
                : EXAMPLE
                :
                : DIV R1, R2, R3 (R3 = 0) -> ALU fails and returns Interrupt ID 0
                : CPU calculates the Table Offset: ID 0 x 2 = Ram address = 0x0000
                : CPU reads word at 0x0000 (0x0500)
                : CPU puts current PC on the stack `PUSH PC`, to get back later
                : CPU overwrites PC with the address 0x0500
                : next fetch cpu runs instruction 0x0500 in which the assembler
                : placed error handling before hand
                :
                : at the end of the handler it runs IRET: pop SP back from stack
                : and loads it into SP
                :
                :
                : Error Handling by the assembler
                : reserves 0x0000 - 0x000F in RAM
                : at the end of the code segmente he writes const standart routines
                : e.g: default_div_zero_handler
                : writes the address of these routines into the beginning of the ram
                : at 0x0000
                :
                :
                :
                : SIMPLER
                :
                :
                : Interrupt Cycle:
                : 1. Hardware/ALU detects fault (e.g. DIV by 0 -> ID 0)
                : 2. CPU pushes current PC to stack (`PUSH PC`)
                : 3. CPU loads 16-bit address from RAM[ID * 2] into PC
                : 4. CPU executes handler code
                : 5. Handler ends with `IRET` -> pops PC from stack, resumes main execution



## ISA - Instruction Set Architecture (16-Bit fixed Width)

Base Formats:
  R-Format: [ Opcode: 4 Bit ][ Reg Dest: 4 Bit ][ Reg Src1: 4 Bit ][ Reg Src2: 4 Bit ]
  J-Format: [ Opcode: 4 Bit ][ Sub-Op:   4 Bit ][ Reg Target: 4 Bit ][ Unused: 4 Bit ]
  I-Format: [ Opcode: 4 Bit ][ Reg Dest: 4 Bit ][ Immediate Value: 8 Bit             ]


Addressing Modes:
  R2   -> Direct Register Value
  [R2] -> Memory Reference (Uses value in R2 as RAM Address)

1.  NOP     - 0x0000 - NOP               - Does nothing
2.  LOADI   - 0x1RXX - LOADI R1, 0x42    - Loads 8-bit value XX into lower byte of R1 (0x00XX)
3.  LOADHI  - 0x2RXX - LOADHI R1, 0x80   - Loads 8-bit value XX into upper byte of R1 (0xXX00), keeps lower byte
4.  MOV     - 0x3R12 - MOV R1, R2        - Copies value from R2 into R1 
5.  LOADM   - 0x4R12 - LOADM R1, [R2]    - Reads value out of ram address in R2 and saves in R1
6.  STOREM  - 0x5R12 - STOREM [R1], R2   - Writes value from R2 into ram address in R1
7.  ADD     - 0x6123 - ADD R1, R2, R3    - R1 = R2 + R3 -> sets Z-Flag if 0
8.  SUB     - 0x7123 - SUB R1, R2, R3    - R1 = R2 - R3 -> sets Z-Flag if 0
9.  MUL     - 0x8123 - MUL R1, R2, R3    - R1 = R2 * R3
10. DIV     - 0x9123 - DIV R1, R2, R3    - R1 = R2 / R3 (triggers Interrupt ID 0 if R3 == 0)
11. AND     - 0xA123 - AND R1, R2, R3    - Bitwise AND: R1 = R2 & R3
12. OR      - 0xB123 - OR R1, R2, R3     - Bitwise OR: R1 = R2 | R3
13. CMP     - 0xCR12 - CMP R1, R2        - Compares R1 with R2 -> sets Z-Flag if R1 == R2

14. JMP     - 0xDR10 - JMP R1            - Unconditional jump to address in R1

! NOTE: second half of first byte is to distingush bewtween JE and JNE so watch out
15. JE      - 0xE0R1 - JE R1             - Jump to address in R1 if Z-Flag == 1
16. JNE     - 0xE1R1 - JNE R1            - Jump to address in R1 if Z-Flag == 0

17. CALL    - 0xF0R1 - CALL R1           - Decrements SP by 2, writes current PC to [SP], sets PC = R1
18. RET     - 0xF100 - RET               - Pops return address from stack into PC (SP += 2)
19. PUSH    - 0xF2R1 - PUSH R1           - Decrements SP by 2 and writes R1 to [SP]

20. POP     - 0xF3R1 - POP R1            - Reads [SP] into R1 and increments SP by 2
21. IRET    - 0xF400 - IRET              - Pops return address from stack into PC (SP += 2) after Interrupt
22. SYS     - 0xF500 - SYS               - Triggers System Call:
                                         - Reads R0 for Mode ID:
                                           Mode 1: Print R1 as Char (ASCII)
                                           Mode 2: Print R1 as Integer Number
                                         - Example:
                                           LOADI  R0, 1    ; Mode 1
                                           LOADI  R1, 'A'  ; Char 'A'
                                           SYS             ; Prints 'A'
23. HALT    - 0xFFFF - HALT              - Stops the CPU execution loop

                                      


EXAMPLE FOR FUNCTION:

main:
    LOADI R1, 0x05        
    LOADI R2, 0x03        
    LOADI R3, add_func    ; address of the func
    CALL R3               ; saves pc on stack and jumps to add_func
    
    ; after func we are here
    MOV R4, R1            
    HALT

add_func:
    ADD R1, R1, R2        ; R1 = R1 + R2 (5 + 3 = 8)
    RET                   ; pops old address from stack
                          ; into PC, goes back to main

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
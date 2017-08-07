/*******************************************************************************
  Snes9x - Portable Super Nintendo Entertainment System (TM) emulator.
 
  (c) Copyright 1996 - 2002 Gary Henderson (gary.henderson@ntlworld.com) and
                            Jerremy Koot (jkoot@snes9x.com)

  (c) Copyright 2001 - 2004 John Weidman (jweidman@slip.net)

  (c) Copyright 2002 - 2004 Brad Jorsch (anomie@users.sourceforge.net),
                            funkyass (funkyass@spam.shaw.ca),
                            Joel Yliluoma (http://iki.fi/bisqwit/)
                            Kris Bleakley (codeviolation@hotmail.com),
                            Matthew Kendora,
                            Nach (n-a-c-h@users.sourceforge.net),
                            Peter Bortas (peter@bortas.org) and
                            zones (kasumitokoduck@yahoo.com)

  C4 x86 assembler and some C emulation code
  (c) Copyright 2000 - 2003 zsKnight (zsknight@zsnes.com),
                            _Demo_ (_demo_@zsnes.com), and Nach

  C4 C++ code
  (c) Copyright 2003 Brad Jorsch

  DSP-1 emulator code
  (c) Copyright 1998 - 2004 Ivar (ivar@snes9x.com), _Demo_, Gary Henderson,
                            John Weidman, neviksti (neviksti@hotmail.com),
                            Kris Bleakley, Andreas Naive

  DSP-2 emulator code
  (c) Copyright 2003 Kris Bleakley, John Weidman, neviksti, Matthew Kendora, and
                     Lord Nightmare (lord_nightmare@users.sourceforge.net

  OBC1 emulator code
  (c) Copyright 2001 - 2004 zsKnight, pagefault (pagefault@zsnes.com) and
                            Kris Bleakley
  Ported from x86 assembler to C by sanmaiwashi

  SPC7110 and RTC C++ emulator code
  (c) Copyright 2002 Matthew Kendora with research by
                     zsKnight, John Weidman, and Dark Force

  S-DD1 C emulator code
  (c) Copyright 2003 Brad Jorsch with research by
                     Andreas Naive and John Weidman
 
  S-RTC C emulator code
  (c) Copyright 2001 John Weidman
  
  ST010 C++ emulator code
  (c) Copyright 2003 Feather, Kris Bleakley, John Weidman and Matthew Kendora

  Super FX x86 assembler emulator code 
  (c) Copyright 1998 - 2003 zsKnight, _Demo_, and pagefault 

  Super FX C emulator code 
  (c) Copyright 1997 - 1999 Ivar, Gary Henderson and John Weidman


  SH assembler code partly based on x86 assembler code
  (c) Copyright 2002 - 2004 Marcus Comstedt (marcus@mc.pp.se) 

 
  Specific ports contains the works of other authors. See headers in
  individual files.
 
  Snes9x homepage: http://www.snes9x.com
 
  Permission to use, copy, modify and distribute Snes9x in both binary and
  source form, for non-commercial purposes, is hereby granted without fee,
  providing that this license information and copyright notice appear with
  all copies and any derived work.
 
  This software is provided 'as-is', without any express or implied
  warranty. In no event shall the authors be held liable for any damages
  arising from the use of this software.
 
  Snes9x is freeware for PERSONAL USE only. Commercial users should
  seek permission of the copyright holders first. Commercial use includes
  charging money for Snes9x or software derived from Snes9x.
 
  The copyright holders request that bug fixes and improvements to the code
  should be forwarded to them so everyone can benefit from the modifications
  in future versions.
 
  Super NES and Super Nintendo Entertainment System are trademarks of
  Nintendo Co., Limited and its subsidiary companies.
*******************************************************************************/
#if defined(__GCC)
#define S9xTraceMessage _S9xTraceMessage
#define S9xGetPPU _S9xGetPPU
#define S9xSetPPU _S9xSetPPU
#define S9xSetDSP _S9xSetDSP
#define S9xGetDSP _S9xGetDSP
#define S9xGetCPU _S9xGetCPU
#define S9xSetCPU _S9xSetCPU
#define S9xSyncSpeed _S9xSyncSpeed
#define S9xTrace _S9xTrace
#define S9xDoHBlankProcessing _S9xDoHBlankProcessing
#define S9xTraceAPU _S9xTraceAPU
#define S9xGetWord _S9xGetWord
#define memset _memset
#define S9xDoHDMA _S9xDoHDMA
#define S9xStartHDMA _S9xStartHDMA
#define S9xEndScreenRefresh _S9xEndScreenRefresh
#define S9xStartScreenRefresh _S9xStartScreenRefresh
#define S9xSetAPUControl _S9xSetAPUControl
#define S9xSetAPUDSP _S9xSetAPUDSP
#define S9xGetAPUDSP _S9xGetAPUDSP
#define S9xTextMode _S9xTextMode
#define printf _printf
#define S9xSetAPUTimer _S9xSetAPUTimer
#define S9xAPUOPrint _S9xAPUOPrint
#define S9xSuperFXExec _S9xSuperFXExec
#define S9xGenerateSound _S9xGenerateSound
#define S9xUpdateJoypads _S9xUpdateJoypads
#define S9xMainLoop _S9xMainLoop
#define RenderLine _RenderLine
#define S9xSetIRQ _S9xSetIRQ
#define S9xClearIRQ _S9xClearIRQ
#define SA1 _SA1
#define CPU _CPU
#define ICPU _ICPU
#define Settings _Settings
#define PPU _PPU
#define IPPU _IPPU
#define Registers _Registers
#define Memory _Memory
#define APU _APU
#define IAPU _IAPU
#define APURegisters _APURegisters
#define S9xBreakpoint _S9xBreakpoint
#define missing _missing
#define S9xApuOpcodes _S9xApuOpcodes
#define S9xAPUCycles _S9xAPUCycles
#define S9xMessage _S9xMessage
#define String _String
#define sprintf _sprintf
#define S9xSA1ExecuteDuringSleep _S9xSA1ExecuteDuringSleep

#define A1 _A1
#define A2 _A2
#define A3 _A3
#define A4 _A4
#define Work8 _Work8
#define Work16 _Work16
#define Work32 _Work32
#define Int8 _Int8
#define Int16 _Int16
#define Int32 _Int32
#define S9xE1M1X1 _S9xE1M1X1
#define S9xE0M1X1 _S9xE0M1X1
#define S9xE0M1X0 _S9xE0M1X0
#define S9xE0M0X1 _S9xE0M0X1
#define S9xE0M0X0 _S9xE0M0X0
#define S9xOpcodesM1X1 _S9xOpcodesM1X1
#define S9xOpcodesM1X0 _S9xOpcodesM1X0
#define S9xOpcodesM0X1 _S9xOpcodesM0X1
#define S9xOpcodesM0X0 _S9xOpcodesM0X0
#define APUROM _APUROM
#define W1 _W1
#define W2 _W2
#define W3 _W3
#define W4 _W4
#define ebx_save _ebx_save
#endif

#define SA1REG r1
#include "offsets.h"

#define Carry		1
#define Zero		2
#define IRQ		4
#define Decimal		8
#define IndexFlag	16
#define MemoryFlag	32
#define Overflow	64
#define Negative	128
#define Emulation	256

#define DEBUG_MODE_FLAG	    (1 << 0)
#define TRACE_FLAG	    (1 << 1)
#define SINGLE_STEP_FLAG    (1 << 2)
#define BREAK_FLAG	    (1 << 3)
#define SCAN_KEYS_FLAG	    (1 << 4)
#define SAVE_SNAPSHOT_FLAG  (1 << 5)
#define DELAYED_NMI_FLAG    (1 << 6)
#define NMI_FLAG	    (1 << 7)
#define PROCESS_SOUND_FLAG  (1 << 8)
#define FRAME_ADVANCE_FLAG  (1 << 9)
#define DELAYED_NMI_FLAG2   (1 << 10)
#define IRQ_PENDING_FLAG    (1 << 11)

#define MEMMAP_BLOCK_SIZE (0x1000)
#define MEMMAP_NUM_BLOCKS (0x1000000 / MEMMAP_BLOCK_SIZE)
#define MEMMAP_BLOCKS_PER_BANK (0x10000 / MEMMAP_BLOCK_SIZE)
#define MEMMAP_SHIFT 12
#define MEMMAP_MASK (MEMMAP_BLOCK_SIZE - 1)

#define CYCLES r10
#define PC r9
#ifdef SPC700_C
#define APUPC APUPCS
#else
#error No register allocated for APUPC
#endif

#define FLAGS r6
#define FLAGS16 r6

#define A r8

#define LOAD_CYCLES mov.l Cycles, CYCLES
#define SAVE_CYCLES mov.l CYCLES, Cycles

.macro SETUP_PREDEF
	mova .misc,r0
        mov.l @r0+,r11
        mov.l @r0+,r12
        mov.l @r0+,r13
	mov r0,r14
.endm

.macro MISC
 	.align 2
.misc:
	.long _Registers
	.long _CPU
	.long MainAsmLoop

	.long 0
  	.long S9xGetByte_r
  	.long S9xSetByte_r
  	.long S9xGetWord_r
  	.long S9xSetWord_r
  	.long S9xOpcode_IRQ_r
  	.long S9xOpcode_NMI_r
	.long S9xSetPCBase_r
	.long _ICPU
#ifdef ARMAPU
  	.long 0
	.long 0
#else
  	.long _IAPU
  	.long _APU
#endif
  	.long _SA1
#ifdef ARMAPU
  	.long 0
#else
	.long _APURegisters
#endif
	.long _Settings
	.long _IPPU
  	.long _PPU
.endm

.macro LOAD_REGISTERS K
	LOAD_CYCLES
	mov.l PCS, PC
#ifndef SPC700_C
	mov.l IAPU, r1
	mov.l APUPCS, APUPC
#endif
	mov.w PP, r0
        mov r0, FLAGS16
        mov.w AA, r0
        extu.w r0, A
        S9xUnpackStatus \K
.endm

.macro STORE_REGISTERS K
        S9xPackStatus \K
	SAVE_CYCLES
	mov.l PC, PCS
#ifndef SPC700_C
	mov.l IAPU, r1
	mov.l APUPC, APUPCS
#endif
	mov FLAGS16, r0
        mov.w r0,PP
	mov A,r0
        mov.w r0,AA
.endm

.macro PUSH_REGISTERS
  	mov.l r6,@-r15
	mov.l r7,@-r15
.endm

.macro POP_REGISTERS
	mov.l @r15+,r7
  	mov.l @r15+,r6
.endm


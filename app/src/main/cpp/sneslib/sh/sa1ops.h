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
.macro Adc8 K
	mov #Decimal,r1
	tst r1, FLAGS
	bf/s .ADC8Decimal\K
	extu.b A,r2
	mov #Carry,r1
	tst r1, FLAGS
	bt/s .ADC8nocarry\K
	add r0,r2
	add #1,r2
.ADC8nocarry\K:	
	mov r2,r3
.ADC8Binary\K:
	xor r0,r3
	xor A,r0
	mov #Carry|Overflow,r1
	not r0,r0
	or r1,FLAGS
	and r3,r0
	exts.b r0,r0
	cmp/pz r0
	bf/s .ADC8setovf\K
	extu.b A,r3
	add #-Overflow,FLAGS
.ADC8setovf\K:
	sub r3,A
	extu.b r2,r3
	shlr8 r2
	tst r2,r2
	bf/s .ADC8setcarry\K
	add r3,A
	add #-Carry,FLAGS
.ADC8setcarry\K:
	SetZNjmp A

.ADC8Decimal\K:
	GetCarry
	mov #15,r3
	and r3,r2
	and r0,r3
	addc r3,r2
	mov #9,r3
	cmp/gt r3,r2
	bf/s .ADC8less10\K
	mov #-16,r3
	add #6,r2
.ADC8less10\K:
	and A,r3
	extu.b r3,r3
	add r3,r2
	mov #-16,r3
	and r0,r3
	add r3,r2
	mov #0x60,r3
	add #0x39,r3
	cmp/gt r3,r2
	bf/s .ADC8Binary\K
	mov r2,r3
	add #96,r2
	bra .ADC8Binary\K
	mov r2,r3
.endm

.macro Adc16 K
	mov #Decimal,r1
	tst r1, FLAGS
	bf/s .ADC16Decimal\K
	extu.w A,r2
	mov #Carry,r1
	tst r1, FLAGS
	bt/s .ADC16nocarry\K
	add r0,r2
	add #1,r2
.ADC16nocarry\K:	
	mov r2,r3
.ADC16Binary\K:
	xor r0,r3
	xor A,r0
	mov #Carry|Overflow,r1
	not r0,r0
	or r1,FLAGS
	and r3,r0
	exts.w r0,r0
	cmp/pz r0
	bf/s .ADC16setovf\K
	extu.w r2,A
	add #-Overflow,FLAGS
.ADC16setovf\K:
	shlr16 r2
	tst r2,r2
	bf .ADC16setcarry\K
	add #-Carry,FLAGS
.ADC16setcarry\K:
	Set16ZNjmp A

.ADC16Decimal\K:
	GetCarry
	mov #15,r4
	and r4,r2
	mov r0,r3
	and r4,r3
	addc r3,r2
	mov #9,r3
	cmp/gt r3,r2
	bf/s .ADC16less10\K
	shll2 r4
	add #6,r2
.ADC16less10\K:
	shll2 r4
	mov r0,r3
	and r4,r3
	add r3,r2
	mov A,r3
	and r4,r3
	add r3,r2
	mov #0x60,r3
	add #0x39,r3
	cmp/gt r3,r2
	bf/s .ADC16less100\K
	shll2 r4
	add #96,r2
.ADC16less100\K:
	shll2 r4
	mov r0,r3
	and r4,r3
	add r3,r2
	mov A,r3
	and r4,r3
	add r3,r2
	mov.w .x999\K,r3
	cmp/gt r3,r2
	bf/s .ADC16less1000\K
	shll2 r4
	mov.w .n1536\K,r3
	add r3,r2
.ADC16less1000\K:
	shll2 r4
	mov r0,r3
	and r4,r3
	add r3,r2
	mov A,r3
	and r4,r3
	add r3,r2
	mov.w .x9999\K,r3
	extu.w r3,r3
	cmp/gt r3,r2
	bf/s .ADC16Binary\K
	mov r2,r3
	mov.w .n24576\K,r3
	add r3,r2
	bra .ADC16Binary\K
	mov r2,r3
.x999\K:
	.word 0x999
.x9999\K:
	.word 0x9999
.n1536\K:	
	.word 1536
.n24576\K:	
	.word 24576
.endm

.macro Sbc8 K
	mov #Decimal,r1
	tst r1, FLAGS
	bf/s .SBC8Decimal\K
	extu.b A,r2
	mov #Carry,r1
	tst r1, FLAGS
	bf/s .SBC8carry\K
	sub r0,r2
	add #-1,r2
.SBC8carry\K:	
	mov r2,r3
.SBC8Binary\K:
	xor A,r0
	xor A,r3
	mov #Carry|Overflow,r1
	and r3,r0
	or r1,FLAGS
	exts.b r0,r0
	cmp/pz r0
	bf/s .SBC8setovf\K
	extu.b A,r3
	add #-Overflow,FLAGS
.SBC8setovf\K:
	sub r3,A
	extu.b r2,r3
	cmp/pz r2
	bt/s .SBC8setcarry\K
	add r3,A
	add #-Carry,FLAGS
.SBC8setcarry\K:
	SetZNjmp A

.SBC8Decimal\K:	
	GetNotCarry
	mov #15,r3
	and r3,r2
	and r0,r3
	subc r3,r2
	cmp/pz r2
	bt/s .SBC8gt01\K
	mov #-16,r3
	add #-6,r2
.SBC8gt01\K:
	and A,r3
	extu.b r3,r3
	add r3,r2
	mov #-16,r3
	and r0,r3
	sub r3,r2
	cmp/pz r2
	bt/s .SBC8Binary\K
	mov r2,r3
	add #-96,r2
	bra .SBC8Binary\K
	mov r2,r3
.endm

.macro Sbc16 K
	mov #Decimal,r1
	tst r1, FLAGS
	bf/s .SBC16Decimal\K
	extu.w A,r2
	mov #Carry,r1
	tst r1, FLAGS
	bf/s .SBC16carry\K
	sub r0,r2
	add #-1,r2
.SBC16carry\K:	
	mov r2,r3
.SBC16Binary\K:
	xor A,r0
	xor A,r3
	mov #Carry|Overflow,r1
	and r3,r0
	or r1,FLAGS
	exts.w r0,r0
	cmp/pz r0
	bf/s .SBC16setovf\K
	extu.w r2,A
	add #-Overflow,FLAGS
.SBC16setovf\K:
	cmp/pz r2
	bt .SBC16setcarry\K
	add #-Carry,FLAGS
.SBC16setcarry\K:
	Set16ZNjmp A

.SBC16Decimal\K:	
	GetNotCarry
	mov #15,r4
	and r4,r2
	mov r0,r3
	and r4,r3
	subc r3,r2
	cmp/pz r2
	bt/s .SBC16gt01\K
	shll2 r4
	add #-6,r2
.SBC16gt01\K:
	shll2 r4
	mov r0,r3
	and r4,r3
	sub r3,r2
	mov A,r3
	and r4,r3
	add r3,r2
	cmp/pz r2
	bt/s .SBC16gt02\K
	shll2 r4
	add #-96,r2
.SBC16gt02\K:
	shll2 r4
	mov r0,r3
	and r4,r3
	sub r3,r2
	mov A,r3
	and r4,r3
	add r3,r2
	cmp/pz r2
	bt/s .SBC16gt03\K
	shll2 r4
	mov.w .n1536\K,r3
	sub r3,r2
.SBC16gt03\K:
	shll2 r4
	mov r0,r3
	and r4,r3
	sub r3,r2
	mov A,r3
	and r4,r3
	add r3,r2
	cmp/pz r2
	bt/s .SBC16Binary\K
	mov r2,r3
	mov.w .n24576\K,r3
	extu.w r3,r3
	sub r3,r2
	bra .SBC16Binary\K
	mov r2,r3
.n1536\K:	
	.word 1536
.n24576\K:	
	.word 24576
.endm

.macro Ora8 K
	or r0,A
	SetZNjmp A
.endm

.macro Ora16 K
	or r0,A
	Set16ZNjmp A
.endm

.macro Eor8 K
	xor r0,A
	SetZNjmp A
.endm

.macro Eor16 K
	xor r0,A
	Set16ZNjmp A
.endm

.macro And8 K
	mov #-128,r2
	shll r2
	or r2,r0
	and r0,A
	SetZNjmp A
.endm

.macro And16 K
	and r0,A
	Set16ZNjmp A
.endm

.macro Lda8 K
	extu.b A,r2
	sub r2,A
	add r0,A
	SetZNjmp A
.endm

.macro Lda16 K
	mov r0,A
	Set16ZNjmp A
.endm

.macro Ldx8 K
	mov.b r0,XL
	SetZNjmp r0
.endm

.macro Ldx16 K
	mov.w r0,XX
	Set16ZNjmp r0
.endm

.macro Ldy8 K
	mov.b r0,YL
	SetZNjmp r0
.endm

.macro Ldy16 K
	mov.w r0,YY
	Set16ZNjmp r0
.endm

.macro Cmp8 K
	extu.b A,r2
	sub r0,r2
	mov #Carry,r1
	cmp/pz r2
	bt/s .Cmp8Carry\K
	or r1,FLAGS
	add #-Carry,FLAGS
.Cmp8Carry\K:	
	SetZNjmp r2
.endm

.macro Cmp16 K
	extu.w A,r2
	sub r0,r2
	mov #Carry,r1
	cmp/pz r2
	bt/s .Cmp16Carry\K
	or r1,FLAGS
	add #-Carry,FLAGS
.Cmp16Carry\K:	
	Set16ZNjmp r2
.endm

.macro Cpx8 K
	mov r0,r1
	mov.b XL,r0
	extu.b r0,r0
	sub r1,r0
	mov #Carry,r2
	cmp/pz r0
	bt/s .Cpx8Carry\K
	or r2,FLAGS
	add #-Carry,FLAGS
.Cpx8Carry\K:	
	SetZNjmp r0
.endm

.macro Cpx16 K
	mov r0,r1
	mov.w XX,r0
	extu.w r0,r0
	sub r1,r0
	mov #Carry,r2
	cmp/pz r0
	bt/s .Cpx16Carry\K
	or r2,FLAGS
	add #-Carry,FLAGS
.Cpx16Carry\K:	
	Set16ZNjmp r0
.endm

.macro Cpy8 K
	mov r0,r1
	mov.b YL,r0
	extu.b r0,r0
	sub r1,r0
	mov #Carry,r2
	cmp/pz r0
	bt/s .Cpy8Carry\K
	or r2,FLAGS
	add #-Carry,FLAGS
.Cpy8Carry\K:	
	SetZNjmp r0
.endm

.macro Cpy16 K
	mov r0,r1
	mov.w YY,r0
	extu.w r0,r0
	sub r1,r0
	mov #Carry,r2
	cmp/pz r0
	bt/s .Cpy16Carry\K
	or r2,FLAGS
	add #-Carry,FLAGS
.Cpy16Carry\K:	
	Set16ZNjmp r0
.endm

.macro Asl8 K
	mov.l S9xSA1GetByte,r3
	jsr @r3
	nop
	mov #-128,r2
	mov #Carry,r1
	tst r2,r0
	bf/s .Asl8Carry\K
	or r1,FLAGS
	add #-Carry,FLAGS
.Asl8Carry\K:	
	shll r0
	mov.l S9xSA1SetByte,r3
	SetZNlink r0
.endm

.macro Asl16 K
	mov.l S9xSA1GetWord,r3
	jsr @r3
	nop
	mov #1,r2
	shll r0
	shll16 r2
	mov #Carry,r1
	tst r2,r0
	bf/s .Asl16Carry\K
	or r1,FLAGS
	add #-Carry,FLAGS
.Asl16Carry\K:	
	mov.l S9xSA1SetWord,r3
	Set16ZNlink r0
.endm

.macro Lsr8 K
	mov.l S9xSA1GetByte,r3
	jsr @r3
	nop
	tst #1,r0
	mov #Carry,r1
	bf/s .Lsr8Carry\K
	or r1,FLAGS
	add #-Carry,FLAGS
.Lsr8Carry\K:	
	shlr r0
	mov.l S9xSA1SetByte,r3
	SetZNlink r0
.endm

.macro Lsr16 K
	mov.l S9xSA1GetWord,r3
	jsr @r3
	nop
	tst #1,r0
	mov #Carry,r1
	bf/s .Lsr16Carry\K
	or r1,FLAGS
	add #-Carry,FLAGS
.Lsr16Carry\K:	
	shlr r0
	mov.l S9xSA1SetWord,r3
	Set16ZNlink r0
.endm

.macro Rol8 K
	mov.l S9xSA1GetByte,r3
	jsr @r3
	nop
	GetCarry
	mov #1,r2
	rotcl r0
	or r2,FLAGS
	shll8 r2
	tst r2,r0
	bf/s .Rol8Carry\K
	mov.l S9xSA1SetByte,r3
	add #-Carry,FLAGS
.Rol8Carry\K:	
	SetZNlink r0
.endm

.macro Rol16 K
	mov.l S9xSA1GetWord,r3
	jsr @r3
	nop
	GetCarry
	mov #1,r2
	rotcl r0
	or r2,FLAGS
	shll16 r2
	tst r2,r0
	bf/s .Rol16Carry\K
	mov.l S9xSA1SetWord,r3
	add #-Carry,FLAGS
.Rol16Carry\K:	
	Set16ZNlink r0
.endm

.macro Ror8 K
	mov.l S9xSA1GetByte,r3
	jsr @r3
	nop
	mov #1,r1
	and FLAGS,r1
	shll8 r1
	or r1,r0
	shlr r0
	mov #Carry,r1
	bt/s .Ror8Carry\K
	or r1,FLAGS
	add #-Carry,FLAGS
.Ror8Carry\K:	
	mov.l S9xSA1SetByte,r3
	SetZNlink r0
.endm

.macro Ror16 K
	mov.l S9xSA1GetWord,r3
	jsr @r3
	nop
	mov #1,r1
	and FLAGS,r1
	shll16 r1
	or r1,r0
	shlr r0
	mov #Carry,r1
	bt/s .Ror16Carry\K
	or r1,FLAGS
	add #-Carry,FLAGS
.Ror16Carry\K:	
	mov.l S9xSA1SetWord,r3
	Set16ZNlink r0
.endm

.macro Bit8 K
	mov.l S9xSA1GetByte,r3
	jsr @r3
	nop
	exts.b r0,r7
	mov #0x40,r2
	shll16 r7
	tst r2,r0
	bf/s .Bit8Ovf\K
	or r2,FLAGS
	sub r2,FLAGS
.Bit8Ovf\K:
	and A,r0
	jmp @r13
	or r0,r7
.endm

.macro Bit16 K
	mov.l S9xSA1GetWord,r3
	jsr @r3
	nop
	mov #0x40,r2
	exts.w r0,r7
	shll8 r2
	sub r0,r7
	tst r2,r0
	shlr8 r2
	bf/s .Bit16Ovf\K
	or r2,FLAGS
	sub r2,FLAGS
.Bit16Ovf\K:
	and A,r0
	jmp @r13
	or r0,r7
.endm

.macro Dec8 K
	mov.l S9xSA1GetByte,r3
	jsr @r3
	nop
	add #-1,r0
	mov.l S9xSA1SetByte,r3
	SetZNlink r0
.endm

.macro Dec16 K
	mov.l S9xSA1GetWord,r3
	jsr @r3
	nop
	add #-1,r0
	mov.l S9xSA1SetWord,r3
	Set16ZNlink r0
.endm

.macro Inc8 K
	mov.l S9xSA1GetByte,r3
	jsr @r3
	nop
	add #1,r0
	mov.l S9xSA1SetByte,r3
	SetZNlink r0
.endm

.macro Inc16 K
	mov.l S9xSA1GetWord,r3
	jsr @r3
	nop
	add #1,r0
	mov.l S9xSA1SetWord,r3
	Set16ZNlink r0
.endm

.macro Sta8 K
	mov.l S9xSA1SetByte,r3
	mov A,r0
	Xlink
.endm

.macro Sta16 K
	mov.l S9xSA1SetWord,r3
	mov A,r0
	Xlink
.endm

.macro Stx8 K
	mov.l S9xSA1SetByte,r3
	mov.b XL,r0
	Xlink
.endm

.macro Stx16 K
	mov.l S9xSA1SetWord,r3
	mov.w XX,r0
	Xlink
.endm

.macro Sty8 K
	mov.l S9xSA1SetByte,r3
	mov.b YL,r0
	Xlink
.endm

.macro Sty16 K
	mov.l S9xSA1SetWord,r3
	mov.w YY,r0
	Xlink
.endm

.macro Stz8 K
	mov.l S9xSA1SetByte,r3
	mov #0,r0
	Xlink
.endm

.macro Stz16 K
	mov.l S9xSA1SetWord,r3
	mov #0,r0
	Xlink
.endm

.macro Trb8 K
	mov.l S9xSA1GetByte,r3
	jsr @r3
	nop
	extu.w r7,r2
	sub r2,r7
	extu.b A,r2
	and r0,r2
	or r2,r7
	sub r2,r0
	mov.l S9xSA1SetByte,r3
	Xlink
.endm

.macro Trb16 K
	mov.l S9xSA1GetWord,r3
	jsr @r3
	nop
	extu.w r7,r2
	sub r2,r7
	extu.w A,r2
	and r0,r2
	or r2,r7
	sub r2,r0
	mov.l S9xSA1SetWord,r3
	Xlink
.endm

.macro Tsb8 K
	mov.l S9xSA1GetByte,r3
	jsr @r3
	nop
	extu.w r7,r2
	sub r2,r7
	extu.b A,r2
	and r0,r2
	or r2,r7
	or A,r0
	mov.l S9xSA1SetByte,r3
	Xlink
.endm

.macro Tsb16 K
	mov.l S9xSA1GetWord,r3
	jsr @r3
	nop
	extu.w r7,r2
	sub r2,r7
	extu.w A,r2
	and r0,r2
	or r2,r7
	or A,r0
	mov.l S9xSA1SetWord,r3
	Xlink
.endm

.macro CheckForIrqjmp K
	mov #IRQ,r0
	tst r0,FLAGS
	bf .CheckForIrqS9xExit\K
	mov.l SA1,r1
	mov.b SA1IRQActive,r0
	tst r0,r0
	bt .CheckForIrqS9xExit\K
	mov.l S9xSA1Opcode_IRQ,r3
	Xlink
.CheckForIrqS9xExit\K:
	jmp @r13
	nop
.endm

.macro S9xSA1FixCycles K
	mov FLAGS16,r0
	mov #Emulation/4,r1
	shll2 r1
	tst r1,r0
	bt .S9xFixCyclesNoEmulation\K
	mov.l .S9xSA1OpcodesM1X1\K,r0
	bra .S9xFixCyclesS9xExit\K
	mov.l r0,SA1Opcodes

.S9xFixCyclesNoEmulation\K:	
	tst #MemoryFlag,r0
	bt .S9xFixCyclesNoMemory\K
	tst #IndexFlag,r0
	bt .S9xFixCyclesNoIndex\K
	mov.l .S9xSA1OpcodesM1X1\K,r0
	bra .S9xFixCyclesS9xExit\K
	mov.l r0,SA1Opcodes

.S9xFixCyclesNoIndex\K:
	mov.l .S9xSA1OpcodesM1X0\K,r0
	bra .S9xFixCyclesS9xExit\K
	mov.l r0,SA1Opcodes

.S9xFixCyclesNoMemory\K:
	tst #IndexFlag,r0
	bt .S9xFixCyclesNoIndex2\K
	mov.l .S9xSA1OpcodesM0X1\K,r0
	bra .S9xFixCyclesS9xExit\K
	mov.l r0,SA1Opcodes
.S9xFixCyclesNoIndex2\K:
	mov.l .S9xSA1OpcodesM0X0\K,r0
	bra .S9xFixCyclesS9xExit\K
	mov.l r0,SA1Opcodes

	.align 2

.S9xSA1OpcodesM0X0\K:
	.long	_S9xSA1OpcodesM0X0
.S9xSA1OpcodesM0X1\K:
	.long	_S9xSA1OpcodesM0X1
.S9xSA1OpcodesM1X0\K:
	.long	_S9xSA1OpcodesM1X0
.S9xSA1OpcodesM1X1\K:
	.long	_S9xSA1OpcodesM1X1

.S9xFixCyclesS9xExit\K:
.endm

.macro S9xSA1UnpackStatus K
	exts.b FLAGS,r7
	mov #Zero,r0
	shll16 r7
	tst r0,FLAGS
	rotcl r7
.endm

.macro S9xSA1PackStatus K
	cmp/pz r7
	mov #(Negative|Zero)/2,r2
	add r2,r2
	bf/s .PackStatusNeg\K
	or r2,FLAGS
	add #-Negative,FLAGS
.PackStatusNeg\K:
	extu.w r7,r2
	tst r2,r2
	bt .PackStatusZero\K
	add #-Zero,FLAGS
.PackStatusZero\K:
.endm

.macro CPUShutdown K
//	cmpb $0, Shutdown
//	jz .NoShutdown\K
	cmpl PC, SA1WaitAddress
	jnz .NoShutdown\K
	cmpl $1, SA1WaitCounter
	jl .NotYet\K
	movb $0, SA1Executing
	jp .NoShutdown\K
.NotYet\K:
	incl SA1WaitCounter
.NoShutdown\K:
.endm

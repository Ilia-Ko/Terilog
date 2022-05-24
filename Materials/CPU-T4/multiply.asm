; a bit more complex programme:
; takes two numbers and multiplies them
; each number is one tryte in size
; so that the result is two trytes in width

.data
	; numbers themselves
	num_a:	dt	+294
	num_b:	dt	-317
	num_c1:	dt	0	; low tryte of result
	num_c2:	dt	0	; high tryte of result
	; auxilary variables
	neg_a:	dt	0	; num_a * -1
	aux:	dt	0	; tmp
	var1:	dt	0	; low tryte of temporary result
	var2:	dt	0	; high tryte of temporary result
	mask:	dt	1	; mask for extracting digits from num_b
	cnt1:	dt	0	; global counter
	cnt2:	dt	0	; temporary counter

.code
	; initialize
	msk [num_a], 0xFF → rz
	mov rz → [neg_a]
	; prepare r0 for multiplication
l0:	mov [mask] → rz
	msk [num_b], rz → r0
	mov r0 → [aux]
	msk [aux], 0 → rz	; instead of cmp
	jlg ll, lg
	jmp l4 				; skip everything because of 0
ll:	mov [neg_a] → r0
	jmp l1
lg:	mov [num_a] → r0
l1: mov r0 → [var1]
	mov 0 → [var2]
	; shift pair var2:var1 left cnt1 times
	mov [cnt1] → rz
	mov rz → [cnt2]
l2:	add [cnt2], -1 → rz
	mov rz → [cnt2]
	msk [cnt2], 0 → rz	; instead of cmp
	jl l3
	mov [var2] → rz
	add [var2], rz → r0
	add [var2], r0 → r1
	mov r1 → [var2]
	mov [var1] → rz
	add [var1], rz → r0
	add [var2], 0 → r1
	mov r1 → [var2]
	add [var1], r0 → rz
	add [var2], 0 → r1
	mov rz → [var1]
	mov r1 → [var2]
	jmp l2
	; add pair var2:var1 to pair num_c2:num_c1
l3:	mov [var1] → rz
	add [num_c1], rz → r0
	mov r0 → [num_c1]
	mov [var2] → rz
	add [num_c2], rz → r0
	mov r0 → [num_c2]
	; shift mask and repeat everything again cnt1 times
l4:	mov [mask] → rz
	add [mask], rz → r0
	add [mask], r0 → r1
	mov r1 → [mask]
	add [cnt1], 1 → rz
	mov rz → [cnt1]
	msk [cnt1], 6 → rz	; instead of cmp
	jl l0
	; show numbers and halt
	msk [num_a], 0 → r1
	mov [num_c2] → r0
	mov [num_c1] → rz
	finish

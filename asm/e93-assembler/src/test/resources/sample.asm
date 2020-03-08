AND, $r5, $r0   -- init r5 to 0
AND, $r6, $r0   -- init r6 to 0
ORI, $r6, 0x64  -- set r6 to 0x64
ADDI, $r5, 0x1  -- increment r5
SW, $r5, $r6    -- store r5 into memory[0x64]
LW, $r5, $r6    -- load value at r6 into r5
J, 0x03         -- jump to increment line

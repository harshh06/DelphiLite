program Test12;
{ Test 12: continue — skip i = 3 and i = 6 in a for loop }

var
  i: Integer;

begin
  for i := 1 to 8 do
  begin
    if i = 3 then
      continue;
    if i = 6 then
      continue;
    writeln(i);
  end;
end.

{ Expected output:
  1
  2
  4
  5
  7
  8
}

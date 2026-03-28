program TestContinue;

{ Test 12: continue inside for — skip even numbers }

var
  i: Integer;

begin
  for i := 1 to 6 do
  begin
    if i = 2 then
      continue;
    if i = 4 then
      continue;
    writeln(i);
  end;
end.

program TestBreak;

{ Test 11: break inside while }

var
  i: Integer;

begin
  i := 0;
  while i < 100 do
  begin
    if i = 3 then
      break;
    writeln(i);
    i := i + 1;
  end;
  writeln(i);
end.

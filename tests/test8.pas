program Test8;
{ Test 8: while-do loop — count from 1 to 5 }

var
  i: Integer;

begin
  i := 1;
  while i <= 5 do
  begin
    writeln(i);
    i := i + 1;
  end;
end.

{ Expected output:
  1
  2
  3
  4
  5
}

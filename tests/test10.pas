program Test10;
{ Test 10: for-downto loop — count from 5 down to 1 }

var
  i: Integer;

begin
  for i := 5 downto 1 do
    writeln(i);
end.

{ Expected output:
  5
  4
  3
  2
  1
}

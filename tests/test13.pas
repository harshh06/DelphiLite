program Test13;
{ Test 13: if-then-else, boolean/comparison operations }

var
  x: Integer;
  flag: Boolean;

begin
  x := 10;

  { Simple if-then }
  if x > 5 then
    writeln('x is greater than 5');

  { if-then-else }
  if x = 10 then
    writeln('x equals 10')
  else
    writeln('x does not equal 10');

  { Nested if }
  if x > 0 then
  begin
    if x < 20 then
      writeln('x is between 0 and 20');
  end;

  { Boolean with and/or }
  if (x > 5) and (x < 20) then
    writeln('and works');

  if (x < 5) or (x > 5) then
    writeln('or works');

  { not }
  if not (x = 0) then
    writeln('not works');

  { false branch }
  if x > 100 then
    writeln('wrong')
  else
    writeln('else branch works');
end.

{ Expected output:
  x is greater than 5
  x equals 10
  x is between 0 and 20
  and works
  or works
  not works
  else branch works
}

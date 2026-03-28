program TestWhileLoop;

{ Test 8: while-do loop — basic counter }

var
  i: Integer;

begin
  i := 0;
  while i < 5 do
  begin
    writeln(i);
    i := i + 1;
  end;
end.

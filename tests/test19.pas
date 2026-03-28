program TestNestedWhileIf;

{ Test 19: Nested while + if }

var
  i, j: Integer;

begin
  i := 1;
  while i <= 3 do
  begin
    j := 1;
    while j <= 3 do
    begin
      if i = j then
        writeln(i);
      j := j + 1;
    end;
    i := i + 1;
  end;
end.

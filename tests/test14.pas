program TestStandaloneFunc;

{ Test 14: Standalone function returning a value }

function Square(n: Integer): Integer;
begin
  Square := n * n;
end;

var
  result: Integer;

begin
  result := Square(7);
  writeln(result);
end.

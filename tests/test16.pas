program TestStaticScope;

{ Test 16: Function cannot see caller's local — demonstrates static scoping }
{ The function reads global 'x', not the caller's local 'x' }

function ReadX: Integer;
begin
  ReadX := x;
end;

var
  x: Integer;

begin
  x := 10;
  writeln(ReadX());
end.

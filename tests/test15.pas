program TestLocalShadow;

{ Test 15: Local variable in function shadows global }

function GetX: Integer;
var
  x: Integer;
begin
  x := 99;
  GetX := x;
end;

var
  x: Integer;

begin
  x := 1;
  writeln(GetX());
  writeln(x);
end.

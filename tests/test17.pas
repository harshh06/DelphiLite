program TestProcParams;

{ Test 17: Standalone procedure with formal parameters }

procedure PrintSum(a: Integer; b: Integer);
var
  s: Integer;
begin
  s := a + b;
  writeln(s);
end;

begin
  PrintSum(3, 7);
  PrintSum(100, 200);
end.

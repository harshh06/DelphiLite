program TestScopeViolation;

{ Test 22: Static scoping — function CANNOT see caller's local variable }
{ This should throw: Undefined variable: callerLocal }

procedure Outer;
var
  callerLocal: Integer;
begin
  callerLocal := 999;
  Inner;
end;

procedure Inner;
begin
  writeln(callerLocal);
end;

begin
  Outer;
end.

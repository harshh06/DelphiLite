program TestStandaloneProc;

{ Test 13: Standalone procedure with no parameters }

procedure SayHello;
begin
  writeln('Hello from procedure!');
end;

begin
  SayHello;
end.

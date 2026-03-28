program TestIfElse;

{ Test 21: if-then-else with both branches }

var
  x: Integer;

begin
  x := 15;
  if x > 10 then
    writeln('x is greater than 10')
  else
    writeln('x is 10 or less');
  if x = 20 then
    writeln('x is 20')
  else
    writeln('x is not 20');
end.

program TestCombined;

{ Test 20: Loops + procedures + recursion }

function Factorial(n: Integer): Integer;
begin
  if n <= 1 then
    Factorial := 1
  else
    Factorial := n * Factorial(n - 1);
end;

var
  result: Integer;

begin
  result := Factorial(5);
  writeln(result);
  result := Factorial(0);
  writeln(result);
end.

program TestConstFold;

{ Test 18: Constant propagation — run with --show-const-fold to see folding }
{ Examples from requirements: 2*(10+11) -> 42, v+2*3 -> v+6 }

var
  x, v: Integer;

begin
  x := 3 + 4;
  writeln(x);
  x := 10 * 5;
  writeln(x);
  x := 2 * (10 + 11);
  writeln(x);
  v := 1;
  x := v + 2 * 3;
  writeln(x);
end.

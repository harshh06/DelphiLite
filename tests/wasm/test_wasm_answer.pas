program TestWasmAnswer;

function Answer: Integer;
begin
  Answer := 42;
end;

begin
  { Intentionally no writeln/readln for wasm target. }
end.

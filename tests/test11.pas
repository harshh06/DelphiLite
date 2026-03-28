program Test11;
{ Test 11: break — exit loop early when i reaches 5 }

var
  i: Integer;

begin
  i := 1;
  while i <= 10 do
  begin
    if i = 5 then
      break;
    writeln(i);
    i := i + 1;
  end;
  writeln('Done');
end.

{ Expected output:
  1
  2
  3
  4
  Done
}

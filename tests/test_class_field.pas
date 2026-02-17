program TestFieldAccess;

type
  TBox = class
  public
    FValue: Integer;
    constructor Create;
  end;

constructor TBox.Create;
begin
  FValue := 0;
end;

var
  b: TBox;

begin
  b := TBox.Create;
  b.FValue := 42;
  writeln(b.FValue);
  b.Destroy;
end.
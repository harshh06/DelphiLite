program Test5;
{ Test 5: readln + writeln with class }

type
  TBox = class
  private
    FValue: Integer;
  public
    constructor Create;
    procedure SetValue(v: Integer);
    function GetValue: Integer;
  end;

constructor TBox.Create;
begin
  FValue := 0;
end;

procedure TBox.SetValue(v: Integer);
begin
  FValue := v;
end;

function TBox.GetValue: Integer;
begin
  GetValue := FValue;
end;

var
  b: TBox;
  x: Integer;

begin
  b := TBox.Create;
  readln(x);
  b.SetValue(x);
  writeln(b.GetValue);
end.

{ Expected: reads integer from stdin, prints it back }

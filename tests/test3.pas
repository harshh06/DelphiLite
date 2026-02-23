program Test3;
{ Test 3: Encapsulation - accessing private field from outside should error }

type
  TSecret = class
  private
    FValue: Integer;
  public
    constructor Create;
    function GetValue: Integer;
  end;

constructor TSecret.Create;
begin
  FValue := 42;
end;

function TSecret.GetValue: Integer;
begin
  GetValue := FValue;
end;

var
  s: TSecret;

begin
  s := TSecret.Create;
  writeln(s.GetValue);
  writeln(s.FValue);
end.

{ Expected output:
  42
  RuntimeException: Cannot access private member 'FValue' from outside class 'TSecret'
}

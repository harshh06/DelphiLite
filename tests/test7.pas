program Test7;
{ Test 7: Interface implementation }

type
  IPrintable = interface
    procedure Print;
  end;

  TNumber = class(IPrintable)
  private
    FValue: Integer;
  public
    constructor Create(v: Integer);
    procedure Print;
    function GetValue: Integer;
  end;

constructor TNumber.Create(v: Integer);
begin
  FValue := v;
end;

procedure TNumber.Print;
begin
  writeln(FValue);
end;

function TNumber.GetValue: Integer;
begin
  GetValue := FValue;
end;

var
  n1, n2: TNumber;

begin
  n1 := TNumber.Create(10);
  n2 := TNumber.Create(20);
  n1.Print;
  n2.Print;
  writeln(n1.GetValue + n2.GetValue);
end.

{ Expected output:
  10
  20
  30
}

program TestSelf;

type
  TCalculator = class
  private
    FValue: Integer;
  public
    constructor Create;
    procedure SetValue;
    function Double: Integer;
    destructor Destroy;
  end;

constructor TCalculator.Create;
begin
  FValue := 7;
end;

procedure TCalculator.SetValue;
begin
  FValue := 10;
end;

function TCalculator.Double: Integer;
begin
  Double := FValue + FValue;
end;

destructor TCalculator.Destroy;
begin
end;

var
  calc: TCalculator;

begin
  calc := TCalculator.Create;
  writeln(calc.Double);
  calc.SetValue;
  writeln(calc.Double);
  calc.Destroy;
end.
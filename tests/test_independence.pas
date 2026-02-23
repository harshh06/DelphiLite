program TestIndependence;

type
  TCounter = class
  private
    FCount: Integer;
  public
    constructor Create;
    procedure Increment;
    procedure Add;
    function GetCount: Integer;
    destructor Destroy;
  end;

constructor TCounter.Create;
begin
  FCount := 0;
end;

procedure TCounter.Increment;
begin
  FCount := FCount + 1;
end;

procedure TCounter.Add;
begin
  FCount := FCount + 5;
end;

function TCounter.GetCount: Integer;
begin
  GetCount := FCount;
end;

destructor TCounter.Destroy;
begin
end;

var
  counter1, counter2: TCounter;

begin
  counter1 := TCounter.Create;
  counter2 := TCounter.Create;

  counter1.Increment;
  counter1.Increment;
  counter1.Increment;
  counter1.Add;

  counter2.Increment;

  writeln(counter1.GetCount);
  writeln(counter2.GetCount);

  counter1.Destroy;
  counter2.Destroy;
end.
program Test2;
{ Test 2: Two objects of the same class with independent state }

type
  TCounter = class
  private
    FCount: Integer;
  public
    constructor Create;
    procedure Increment;
    procedure Add(value: Integer);
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

procedure TCounter.Add(value: Integer);
begin
  FCount := FCount + value;
end;

function TCounter.GetCount: Integer;
begin
  GetCount := FCount;
end;

destructor TCounter.Destroy;
begin
end;

var
  c1, c2: TCounter;

begin
  c1 := TCounter.Create;
  c2 := TCounter.Create;
  c1.Increment;
  c1.Increment;
  c1.Add(3);
  c2.Increment;
  writeln(c1.GetCount);
  writeln(c2.GetCount);
  c1.Destroy;
  c2.Destroy;
end.

{ Expected output:
  5
  1
}

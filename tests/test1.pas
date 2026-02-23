program Test1;
{ Test 1: Basic class with private field, constructor, methods, destructor }

type
  TCounter = class
  private
    FCount: Integer;
  public
    constructor Create;
    procedure Increment;
    function GetCount: Integer;
    destructor Destroy;
  end;

constructor TCounter.Create;
begin
  FCount := 0;
  writeln('Counter created');
end;

procedure TCounter.Increment;
begin
  FCount := FCount + 1;
end;

function TCounter.GetCount: Integer;
begin
  GetCount := FCount;
end;

destructor TCounter.Destroy;
begin
  writeln('Counter destroyed');
end;

var
  c: TCounter;

begin
  c := TCounter.Create;
  c.Increment;
  c.Increment;
  c.Increment;
  writeln(c.GetCount);
  c.Destroy;
end.

{ Expected output:
  Counter created
  3
  Counter destroyed
}

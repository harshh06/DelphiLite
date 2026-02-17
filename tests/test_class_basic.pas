program TestClassBasic;

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
end;

var
  counter: TCounter;

begin
  counter := TCounter.Create;
  counter.Increment;
  counter.Increment;
  counter.Increment;
  writeln(counter.GetCount);
  counter.Destroy;
end.
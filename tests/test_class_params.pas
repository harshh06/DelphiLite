program TestClassParams;

type
  TGreeter = class
  private
    FName: String;
  public
    constructor CreateWithName(name: String);
    procedure Greet;
    destructor Destroy;
  end;

constructor TGreeter.CreateWithName(name: String);
begin
  FName := name;
end;

procedure TGreeter.Greet;
begin
  writeln(FName);
end;

destructor TGreeter.Destroy;
begin
end;

var
  g: TGreeter;

begin
  g := TGreeter.CreateWithName('World');
  g.Greet;
  g.Destroy;
end.
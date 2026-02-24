program Test4;
{ Test 4: Constructor with parameters }

type
  TGreeter = class
  private
    FName: String;
    FAge: Integer;
  public
    constructor Create(name: String; age: Integer);
    procedure Greet;
    function GetAge: Integer;
    destructor Destroy;
  end;

constructor TGreeter.Create(name: String; age: Integer);
begin
  FName := name;
  FAge := age;
end;

procedure TGreeter.Greet;
begin
  writeln('Hello ' + FName);
end;

function TGreeter.GetAge: Integer;
begin
  GetAge := FAge;
end;

destructor TGreeter.Destroy;
begin
end;

var
  g: TGreeter;

begin
  g := TGreeter.Create('Alice', 25);
  g.Greet;
  writeln(g.GetAge);
  g.Destroy;
end.

{ Expected output:
  Hello Alice
  25
}

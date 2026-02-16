program TestProtected;

type
  TBase = class
  private
    FId: Integer;
  protected
    FName: String;
  public
    constructor Create;
    destructor Destroy;
  end;

constructor TBase.Create;
begin
  FId := 1;
  FName := 'base';
end;

destructor TBase.Destroy;
begin
end;

var
  obj: TBase;

begin
  obj := TBase.Create;
  writeln(obj.FName);
  obj.Destroy;
end.
program TestEncapsulation;

type
  TSecret = class
  private
    FPassword: Integer;
  public
    constructor Create;
    procedure SetPassword;
    function GetPassword: Integer;
    destructor Destroy;
  end;

constructor TSecret.Create;
begin
  FPassword := 12345;
end;

procedure TSecret.SetPassword;
begin
  FPassword := 99999;
end;

function TSecret.GetPassword: Integer;
begin
  GetPassword := FPassword;
end;

destructor TSecret.Destroy;
begin
end;

var
  secret: TSecret;

begin
  secret := TSecret.Create;
  writeln(secret.GetPassword);
  secret.SetPassword;
  writeln(secret.GetPassword);
  secret.Destroy;
end.
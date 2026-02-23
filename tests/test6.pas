program Test6;
{ Test 6: Inheritance - parent/child classes }

type
  TAnimal = class
  private
    FName: String;
  public
    constructor Create(name: String);
    procedure Speak;
    procedure PrintName;
  end;

  TDog = class(TAnimal)
  public
    procedure Speak;
    procedure Bark;
  end;

  TCat = class(TAnimal)
  public
    procedure Speak;
  end;

constructor TAnimal.Create(name: String);
begin
  FName := name;
  writeln('Animal created');
end;

procedure TAnimal.Speak;
begin
  writeln('Some animal sound');
end;

procedure TAnimal.PrintName;
begin
  writeln(FName);
end;

procedure TDog.Speak;
begin
  writeln('Woof!');
end;

procedure TDog.Bark;
begin
  writeln('Bark bark!');
end;

procedure TCat.Speak;
begin
  writeln('Meow!');
end;

var
  dog: TDog;
  cat: TCat;

begin
  dog := TDog.Create('Rex');
  cat := TCat.Create('Whiskers');
  dog.PrintName;
  dog.Speak;
  dog.Bark;
  cat.PrintName;
  cat.Speak;
end.

{ Expected output:
  Animal created
  Animal created
  Rex
  Woof!
  Bark bark!
  Whiskers
  Meow!
}

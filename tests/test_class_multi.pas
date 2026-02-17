program TestMultiClass;

type
  TPoint = class
  public
    FX: Integer;
    FY: Integer;
    constructor Create(x: Integer; y: Integer);
  end;

  TRect = class
  private
    FWidth: Integer;
    FHeight: Integer;
  public
    constructor Create(w: Integer; h: Integer);
    function Area: Integer;
  end;

constructor TPoint.Create(x: Integer; y: Integer);
begin
  FX := x;
  FY := y;
end;

constructor TRect.Create(w: Integer; h: Integer);
begin
  FWidth := w;
  FHeight := h;
end;

function TRect.Area: Integer;
begin
  Area := FWidth * FHeight;
end;

var
  p: TPoint;
  r: TRect;

begin
  p := TPoint.Create(10, 20);
  r := TRect.Create(5, 3);
  writeln(p.FX);
  writeln(r.Area);
end.
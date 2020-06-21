package obscurum.environment.foreground;

import obscurum.display.Display;

public class EmptyTile extends ForegroundTile {
    public EmptyTile() {
        super("Empty Tile", Display.SPACE, Display.BLACK, 1, 0, true, false);
    }
}

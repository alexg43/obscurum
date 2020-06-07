package obscurum.environment.background;

import java.awt.Color;

import lombok.Getter;
import lombok.NonNull;
import obscurum.display.Display;
import obscurum.display.asciiPanel.AsciiPanel;
import obscurum.environment.Tile;

@Getter
public class BackgroundTile extends Tile {
    private final Color backgroundColour;

    protected BackgroundTile(String name, char glyph, Color glyphColour, @NonNull Color backgroundColour) {
        super(name, glyph, glyphColour);
        this.backgroundColour = backgroundColour;
    }

    public static BackgroundTile createFloor() {
        return new BackgroundTile("Floor", (char)250, AsciiPanel.red, AsciiPanel.black);
    }

    public static BackgroundTile createGrass() {
        return new BackgroundTile("Grass", '.', AsciiPanel.green, Display.BLACK);
    }
}

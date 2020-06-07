package obscurum.environment;

import java.awt.Color;

import lombok.Getter;
import lombok.NonNull;
import obscurum.GameMain;

@Getter
public abstract class Tile {
    protected String name;
    protected char glyph;
    protected Color foregroundColour;

    public Tile(String name, char glyph, Color foregroundColour) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        } else if (glyph >= GameMain.NUM_OF_GLYPHS) {
            throw new IllegalArgumentException(String.format("Glyph must be in range 0 - %d.", GameMain.NUM_OF_GLYPHS));
        }

        this.name = name;
        this.glyph = glyph;
        setForegroundColour(foregroundColour);
    }

    public void setForegroundColour(@NonNull Color foregroundColour) {
        this.foregroundColour = foregroundColour;
    }

    public boolean isOfType(@NonNull Tile tile) {
        return this.getClass().equals(tile.getClass());
    }
}

package obscurum.environment;

import java.awt.Color;

import lombok.Getter;
import lombok.NonNull;
import obscurum.GameMain;

@Getter
public abstract class Tile {
    protected String name;
    protected char glyph;
    protected Color glyphColour;

    public Tile(String name, char glyph, Color glyphColour) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        } else if (glyph >= GameMain.NUM_OF_GLYPHS) {
            throw new IllegalArgumentException(String.format("Glyph must be in range 0 - %d.", GameMain.NUM_OF_GLYPHS));
        }

        this.name = name;
        this.glyph = glyph;
        setGlyphColour(glyphColour);
    }

    public void setGlyphColour(@NonNull Color glyphColour) {
        this.glyphColour = glyphColour;
    }

    public boolean isOfType(@NonNull Tile tile) {
        return this.getClass().equals(tile.getClass());
    }
}

package obscurum.environment.background;

import java.awt.Color;

import lombok.Getter;
import lombok.NonNull;
import obscurum.environment.Tile;

@Getter
public abstract class BackgroundTile extends Tile {
    private Color backgroundColour;

    public BackgroundTile(String name, char glyph, Color glyphColour, Color backgroundColour) {
        super(name, glyph, glyphColour);
        setBackgroundColour(backgroundColour);
    }

    public void setBackgroundColour(@NonNull Color backgroundColour) {
        this.backgroundColour = backgroundColour;
    }
}

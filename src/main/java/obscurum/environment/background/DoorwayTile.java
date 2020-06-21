package obscurum.environment.background;

import lombok.NonNull;

import java.awt.*;

public class DoorwayTile extends BackgroundTile {
    public DoorwayTile(String name, char glyph, Color glyphColour, @NonNull Color backgroundColour) {
        super(name, glyph, glyphColour, backgroundColour);
    }
}

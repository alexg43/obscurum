package obscurum.environment.background;

import java.awt.Color;

import lombok.Getter;
import lombok.NonNull;
import obscurum.display.Display;
import obscurum.environment.Tile;

@Getter
public class BackgroundTile extends Tile {
    private final Color backgroundColour;

    public BackgroundTile(String name, char glyph, Color glyphColour, @NonNull Color backgroundColour) {
        super(name, glyph, glyphColour);
        this.backgroundColour = backgroundColour;
    }

    public static BackgroundTile createFloor() {
        return new BackgroundTile("Floor", (char)250, Display.RED, Display.BLACK);
    }

    public static BackgroundTile createGrass() {
        return new BackgroundTile("Grass", '.', Display.GREEN, Display.BLACK);
    }

    public static BackgroundTile createUpwardDoorway(BackgroundTile colourTemplate) {
        return new BackgroundTile("Upward Doorway", Display.D_ARROW, colourTemplate.glyphColour.brighter(), colourTemplate.backgroundColour);
    }

    public static BackgroundTile createDownwardDoorway(BackgroundTile colourTemplate) {
        return new BackgroundTile("Downward Doorway", Display.U_ARROW, colourTemplate.glyphColour.brighter(), colourTemplate.backgroundColour);
    }

    public static BackgroundTile createExitPortal(BackgroundTile colourTemplate) {
        return new BackgroundTile("Exit Portal", (char) 233, Display.YELLOW, colourTemplate.backgroundColour);
    }
}

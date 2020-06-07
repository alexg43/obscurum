package obscurum.environment.background.traps;

import obscurum.creatures.Creature;
import obscurum.environment.background.BackgroundTile;

public abstract class Trap extends BackgroundTile {
    BackgroundTile sourceTile;

    public Trap(BackgroundTile sourceTile) {
        super("Trap", sourceTile.getGlyph(), sourceTile.getGlyphColour().brighter(), sourceTile.getBackgroundColour());
        this.sourceTile = sourceTile;
    }

    public BackgroundTile getSourceTile() {
        return sourceTile;
    }

    public abstract void trigger(Creature target);
}

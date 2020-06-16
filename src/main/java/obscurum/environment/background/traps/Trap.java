package obscurum.environment.background.traps;

import obscurum.creatures.Creature;
import obscurum.environment.background.BackgroundTile;

public abstract class Trap extends BackgroundTile {
    BackgroundTile replacementTile;

    public Trap(BackgroundTile replacementTile) {
        super("Trap", replacementTile.getGlyph(), replacementTile.getGlyphColour().brighter(), replacementTile.getBackgroundColour());
        this.replacementTile = replacementTile;
    }

    public BackgroundTile getReplacementTile() {
        return replacementTile;
    }

    public abstract void trigger(Creature target);
}

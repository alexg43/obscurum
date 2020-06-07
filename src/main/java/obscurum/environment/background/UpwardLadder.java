package obscurum.environment.background;

import obscurum.display.Display;

/**
 * This models an upward ladder, which connects two levels.
 * @author Alex Ghita
 */
public class UpwardLadder extends BackgroundTile {
  /**
   * Default constructor, with default colours.
   */
  public UpwardLadder() {
    super("Upward Ladder", Display.U_ARROW, Display.WHITE, Display.BLACK);
  }

  /**
   * Class constructor specifying the tile's appearance, i.e. an upward arrow
   * coloured like the given tile.
   * @param b the tile from which the ladder should take its colours
   */
  public UpwardLadder(BackgroundTile b) {
    super("Upward Ladder", Display.U_ARROW, b.getGlyphColour().brighter(),
        b.getBackgroundColour());
  }
}

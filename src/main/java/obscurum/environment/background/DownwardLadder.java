package obscurum.environment.background;

import obscurum.display.Display;

/**
 * This models a downward ladder, which connects two levels.
 * @author Alex Ghita
 */
public class DownwardLadder extends BackgroundTile {
  /**
   * Default constructor, with default colours.
   */
  public DownwardLadder() {
    super("Downward Ladder", Display.D_ARROW, Display.WHITE, Display.BLACK);
  }

  /**
   * Class constructor specifying the tile's appearance, i.e. a downward arrow
   * coloured like the given tile.
   * @param b the tile from which the ladder should take its colours
   */
  public DownwardLadder(BackgroundTile b) {
    super("Downward Ladder", Display.D_ARROW,
        b.getGlyphColour().brighter(), b.getBackgroundColour());
  }
}

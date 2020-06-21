package obscurum.environment.level_generators;

import obscurum.environment.Level;
import obscurum.environment.background.BackgroundTile;
import obscurum.environment.foreground.ForegroundTile;
import obscurum.placeholders.BackgroundLevelBound;
import obscurum.placeholders.ForegroundLevelBound;

/**
 * This outlines the necessary fields and behaviours of a level builder,
 * i.e. a pair of dimensions and types of tiles to be used, as well as a
 * method for building the actual level.
 * @author Alex Ghita
 */
public abstract class Builder {
  protected int width;
  protected int height;
  protected Level level;
  protected ForegroundTile foregroundType;
  protected BackgroundTile backgroundType;
  protected boolean hasNext;
  protected boolean hasPrevious;

  /**
   * Level builder constructor specifying the level's dimensions, and the
   * types of tiles that will be used in building it.
   * @param width
   * @param height
   * @param foregroundType
   * @param backgroundType
   */
  public Builder(int width, int height, ForegroundTile foregroundType,
      BackgroundTile backgroundType, boolean hasNext,
      boolean hasPrevious) {
    // Check for illegal arguments.
    if (width < 31) {
      throw new IllegalArgumentException("Width " + width +
          " must be at least 31");
    }
    if (height < 31) {
      throw new IllegalArgumentException("Height " + height +
          " must be at least 31");
    }
    if (foregroundType == null ||
        foregroundType.isOfType(new ForegroundLevelBound())) {
      throw new IllegalArgumentException(
          "Foreground type cannot be null.");
    }
    if (backgroundType == null ||
        backgroundType.isOfType(new BackgroundLevelBound())) {
      throw new IllegalArgumentException(
          "Background type cannot be null.");
    }

    this.width = width;
    this.height = height;
    this.foregroundType = foregroundType;
    this.backgroundType = backgroundType;
    this.hasNext = hasNext;
    this.hasPrevious = hasPrevious;
    level = new Level(new ForegroundTile[width][height],
        new BackgroundTile[width][height]);
  }

  /**
   * Builds a level in a certain way.
   * @return
   */
  public abstract Level build();

  /**
   * Fills the entire board with the default foreground and background tiles.
   */
  protected void initialiseBoard() {
    initialiseBoard(foregroundType, backgroundType);
  }

  /**
   * Fills the entire board with the specified foreground and background
   * tiles.
   * @param f
   * @param b
   */
  protected void initialiseBoard(ForegroundTile f, BackgroundTile b) {
    // Check for illegal arguments.
    if (f == null || f.isOfType(new ForegroundLevelBound())) {
      throw new IllegalArgumentException("Foreground tile cannot be null.");
    }
    if (b == null || f.isOfType(new BackgroundLevelBound())) {
      throw new IllegalArgumentException("Background tile cannot be null.");
    }

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        level.setForegroundTile(x, y, f);
        level.setBackgroundTile(x, y, b);
      }
    }
  }

  /**
   * Places ladders, i.e. connectors to a possible level above, and a
   * possible level below.
   */
  protected abstract void placeLadders();
}

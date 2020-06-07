package obscurum.environment;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import obscurum.creatures.Creature;
import obscurum.creatures.Spawner;
import obscurum.creatures.ai.CorpseAI;
import obscurum.environment.background.BackgroundTile;
import obscurum.environment.background.DownwardLadder;
import obscurum.environment.background.UpwardLadder;
import obscurum.environment.background.traps.Trap;
import obscurum.environment.foreground.EmptyTile;
import obscurum.environment.foreground.ForegroundTile;
import obscurum.environment.foreground.Torch;
import obscurum.placeholders.BackgroundLevelBound;
import obscurum.placeholders.ForegroundLevelBound;
import obscurum.placeholders.NullColour;
import obscurum.placeholders.NullCreature;
import obscurum.placeholders.NullLevel;

/**
 * This models a game level, i.e. a 2D board of tiles.
 * @author Alex Ghita
 */
public class Level {
  private ForegroundTile[][] foreground;
  private BackgroundTile[][] background;
  private int width;
  private int height;
  private Level next;
  private Level previous;
  private Point nextLocation;
  private Point previousLocation;
  private ArrayList<Creature> creatures;
  public ArrayList<Torch> torches;
  public ArrayList<Spawner> spawners;

  /**
   * Class constructor specifying the foreground and background board layers.
   * @param foreground
   * @param background
   */
  public Level(ForegroundTile[][] foreground, BackgroundTile[][] background) {
    // Check for illegal arguments.
    if (foreground == null) {
      throw new IllegalArgumentException("Foreground cannot be null.");
    }
    if (background == null) {
      throw new IllegalArgumentException("Background cannot be null.");
    }
    if (foreground.length != background.length) {
      throw new IllegalArgumentException(
          "Foreground and background must have the same number of rows.");
    }
    for (int i = 0; i < foreground.length; i++) {
      if (background[i].length != background[0].length) {
        throw new IllegalArgumentException("Background row " + i + " length " +
            background[i].length + " should be " + background[0].length + ".");
      }
      if (foreground[i].length != foreground[0].length) {
        throw new IllegalArgumentException("Foreground row " + i + " length " +
            foreground[i].length + " should be " + foreground[0].length + ".");
      }
      if (foreground[i].length != background[i].length) {
        throw new IllegalArgumentException(
            "Row " + i + " length is not the same in the two boards.");
      }
    }

    this.foreground = foreground;
    this.background = background;
    this.width = background.length;
    this.height = background[0].length;
    // Set placeholders for next and previous levels if the level is not null.
    if (width > 1) {
      next = new NullLevel();
      previous = new NullLevel();
      nextLocation = null;
      previousLocation = null;
    }
    creatures = new ArrayList<Creature>();
    torches = new ArrayList<Torch>();
    spawners = new ArrayList<Spawner>();
  }

  /**
   * Gets the board width.
   * @return
   */
  public int getWidth() {
    return width;
  }

  /**
   * Gets the board height.
   * @return
   */
  public int getHeight() {
    return height;
  }

  /**
   * Gets the foreground tile at the given coordinates.
   * @param x
   * @param y
   * @return the requested tile, or a placeholder tile if the coordinates are
   *         out of bounds
   */
  public ForegroundTile getForegroundTile(int x, int y) {
    return getForegroundTile(new Point(x, y));
  }

  /**
   * Gets the foreground tile at the given point.
   * @param p
   * @return the requested tile, or a placeholder tile if the point is out of
   *         bounds
   */
  public ForegroundTile getForegroundTile(Point p) {
    if (!isInBounds(p)) {
      return new ForegroundLevelBound();
    }
    return foreground[p.x][p.y];
  }

  /**
   * Gets the background tile at the given coordinates.
   * @param x
   * @param y
   * @return the requested tile, or a placeholder tile if the coordinates are
   *         out of bounds
   */
  public BackgroundTile getBackgroundTile(int x, int y) {
    return getBackgroundTile(new Point(x, y));
  }

  /**
   * Gets the background tile at the given point.
   * @param p
   * @return the requested tile, or a placeholder tile if the point is out of
   *         bounds
   */
  public BackgroundTile getBackgroundTile(Point p) {
    if (!isInBounds(p)) {
      return new BackgroundLevelBound();
    }
    return background[p.x][p.y];
  }

  /**
   * Gets the previous level.
   * @return
   */
  public Level getPrevious() {
    return previous;
  }

  /**
   * Gets the next level.
   * @return
   */
  public Level getNext() {
    return next;
  }

  /**
   * Gets the location of the entrance to the next level.
   * @return
   */
  public Point getNextLocation() {
    return nextLocation;
  }

  /**
   * Gets the location of the entrance to the previous level.
   * @return
   */
  public Point getPreviousLocation() {
    return previousLocation;
  }

  /**
   * Gets the glyph to be displayed at the given coordinates. Returns the
   * foreground tile's glyph if it is non-empty, or the background tile's glyph
   * otherwise.
   * @param x
   * @param y
   * @return the requested character, or the null character if the coordinates
   *         are out of bounds;
   */
  public char getDisplayGlyph(int x, int y) {
    return getDisplayGlyph(new Point(x, y));
  }

  /**
   * Gets the glyph to be displayed at the given point. Returns the foreground
   * tile's glyph if it is non-empty, or the background tile's glyph otherwise.
   * @param p
   * @return the requested character, or the null character if the point is out
   *         of bounds
   */
  public char getDisplayGlyph(Point p) {
    if (!isInBounds(p)) {
      return 0;
    }
    if (foreground[p.x][p.y] instanceof EmptyTile) {
      return background[p.x][p.y].getGlyph();
    }
    return foreground[p.x][p.y].getGlyph();
  }

  /**
   * Gets the foreground colour to be displayed at the given coordinates.
   * Returns the foreground tile's foreground colour if it is non-empty, or the
   * background tile's foreground colour otherwise.
   * @param x
   * @param y
   * @return the requested colour, or a placeholder colour if the coordinates
   *         are out of bounds
   */
  public Color getDisplayForegroundColour(int x, int y) {
    return getDisplayForegroundColour(new Point(x, y));
  }

  /**
   * Gets the foreground colour to be displayed at the given point. Returns the
   * foreground tile's foreground colour if it is non-empty, or the background
   * tile's foreground colour otherwise.
   * @param p
   * @return the requested colour, or a placeholder colour if the point is out
   *         of bounds
   */
  public Color getDisplayForegroundColour(Point p) {
    if (!isInBounds(p)) {
      return new NullColour();
    }
    if (foreground[p.x][p.y] instanceof EmptyTile) {
      return background[p.x][p.y].getGlyphColour();
    }
    return foreground[p.x][p.y].getGlyphColour();
  }

  /**
   * Gets the background colour to be displayed at the given coordinates. This
   * will always be the background tile's background colour.
   * @param x
   * @param y
   * @return the requested colour, or a placeholder colour if the coordinates
   *         are out of bounds
   */
  public Color getDisplayBackgroundColour(int x, int y) {
    return getDisplayBackgroundColour(new Point(x, y));
  }

  /**
   * Gets the background colour to be displayed at the given point. This will
   * always be the background tile's background colour.
   * @param p
   * @return the requested colour, or a placeholder colour if the point is out
   *         of bounds
   */
  public Color getDisplayBackgroundColour(Point p) {
    if (!isInBounds(p)) {
      return new NullColour();
    }
    return background[p.x][p.y].getBackgroundColour();
  }

  /**
   * Gets the creature found at the given coordinates.
   * @param x
   * @param y
   * @return
   */
  public Creature getCreature(int x, int y) {
    return getCreature(new Point(x, y));
  }

  /**
   * Gets the creature found at the given point.
   * @param p
   * @return
   */
  public Creature getCreature(Point p) {
    for (Creature c : creatures) {
      if (c.getLocation().equals(p)) {
        return c;
      }
    }
    return new NullCreature();
  }

  /**
   * Updates all creatures found in the level.
   */
  public void updateCreatures() {
    ArrayList<Creature> toRemove = new ArrayList<Creature>();
    for (Creature c : creatures) {
      c.update();
      if (!c.isAlive() && ((CorpseAI)c.getAI()).getTurnsLeft() == 0) {
        toRemove.add(c);
      }
    }
    for (Creature c : toRemove) {
      remove(c);
    }
  }

  public void updateSpawners() {
    for (Spawner s : spawners) {
      s.update();
    }
  }

  public void powerUpCreatures() {
    for (Creature c : creatures) {
      c.powerUp();
    }
    for (Spawner s : spawners) {
      s.powerUp();
    }
  }

  /**
   * Adds a creature to the list of creatures.
   * @param c
   */
  public void add(Creature c) {
    if (c == null || c instanceof NullCreature) {
      throw new IllegalArgumentException("Creature cannot be null.");
    }
    creatures.add(c);
  }

  /**
   * Removes a creature from the list of creatures, if found.
   * @param c
   */
  public void remove(Creature c) {
    for (int i = 0; i < creatures.size(); i++) {
      if (creatures.get(i).getLocation().equals(c.getLocation())) {
        setForegroundTile(creatures.get(i).getLocation(), new EmptyTile());
        creatures.remove(i);
        return;
      }
    }
    // The creature should have been found, so throw an error.
    throw new IllegalArgumentException("Creature to be removed not found.");
  }

  /**
   * Sets the foreground tile at the given coordinates.
   * @param x
   * @param y
   * @param f
   */
  public void setForegroundTile(int x, int y, ForegroundTile f) {
    setForegroundTile(new Point(x, y), f);
  }

  /**
   * Sets the foreground tile at the given point.
   * @param p
   * @param f
   */
  public void setForegroundTile(Point p, ForegroundTile f) {
    // Check for illegal arguments.
    checkForIllegalLocation(p);
    if (f == null || f instanceof ForegroundLevelBound) {
      throw new IllegalArgumentException("Foreground tile cannot be null.");
    }

    foreground[p.x][p.y] = f;
  }

  /**
   * Sets the background tile at the given coordinates.
   * @param x
   * @param y
   * @param b
   */
  public void setBackgroundTile(int x, int y, BackgroundTile b) {
    setBackgroundTile(new Point(x, y), b);
  }

  /**
   * Sets the background tile at the given point.
   * @param p
   * @param b
   */
  public void setBackgroundTile(Point p, BackgroundTile b) {
    // Check for illegal arguments.
    checkForIllegalLocation(p);
    if (b == null || b instanceof BackgroundLevelBound) {
      throw new IllegalArgumentException("Background tile cannot be null.");
    }

    background[p.x][p.y] = b;
  }

  /**
   * Sets the foreground and background tiles at the given coordinates.
   * @param x
   * @param y
   * @param f
   * @param b
   */
  public void setTile(int x, int y, ForegroundTile f, BackgroundTile b) {
    setTile(new Point(x, y), f, b);
  }

  /**
   * Sets the foreground and background tiles at the given point.
   * @param p
   * @param f
   * @param b
   */
  public void setTile(Point p, ForegroundTile f, BackgroundTile b) {
    setForegroundTile(p, f);
    setBackgroundTile(p, b);
  }

  /**
   * Sets the next level.
   * @param next
   */
  public void setNext(Level next) {
    // Check for illegal arguments.
    if (next == null || next instanceof NullLevel) {
      throw new IllegalArgumentException("Next level cannot be null.");
    }

    this.next = next;
  }

  /**
   * Sets the previous level.
   * @param previous
   */
  public void setPrevious(Level previous) {
    // Check for illegal arguments.
    if (previous == null || previous instanceof NullLevel) {
      throw new IllegalArgumentException("Previous level cannot be null.");
    }

    this.previous = previous;
  }

  /**
   * Sets the location of the entrance to the next level.
   * @param nextLocation
   */
  public void setNextLocation(Point nextLocation) {
    checkForIllegalLocation(nextLocation);

    this.nextLocation = nextLocation;
  }

  /**
   * Sets the location of the entrance to the previous level.
   * @param previousLocation
   */
  public void setPreviousLocation(Point previousLocation) {
    checkForIllegalLocation(previousLocation);

    this.previousLocation = previousLocation;
  }

  /**
   * Gets the location of a random tile which has its foreground of type
   * EmptyTile.
   * @return a point, or null if there is no empty tile
   */
  public Point getRandomEmptyLocation() {
    ArrayList<Point> emptyLocations = new ArrayList<Point>();

    // Get all empty locations.
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if (isForegroundOfType(x, y, new EmptyTile()) &&
            !isBackgroundOfType(x, y, new DownwardLadder()) &&
            !isBackgroundOfType(x, y, new UpwardLadder())) {
          emptyLocations.add(new Point(x, y));
        }
      }
    }

    // If there are no empty locations, return null.
    if (emptyLocations.isEmpty()) {
      return null;
    }
    // Pick a random empty location and return it.
    return emptyLocations.get((int)(Math.random() * emptyLocations.size()));
  }

  /**
   * Checks whether the given coordinates are in the board bounds.
   * @param x
   * @param y
   * @return
   */
  public boolean isInBounds(int x, int y) {
    return isInBounds(new Point(x, y));
  }

  /**
   * Checks whether the given point is in the board bounds.
   * @param p
   * @return
   */
  public boolean isInBounds(Point p) {
    if (p.x < 0 || p.x >= width || p.y < 0 || p.y >= height) {
      return false;
    }
    return true;
  }

  /**
   * Checks whether the foreground tile at the given coordinates is of the
   * given type.
   * @param x
   * @param y
   * @param f
   * @return
   */
  public boolean isForegroundOfType(int x, int y, ForegroundTile f) {
    return isForegroundOfType(new Point(x, y), f);
  }

  /**
   * Checks whether the foreground tile at the given point is of the given
   * type.
   * @param p
   * @param f
   * @return
   */
  public boolean isForegroundOfType(Point p, ForegroundTile f) {
    // Check for illegal arguments.
    checkForIllegalLocation(p);

    return foreground[p.x][p.y].getClass().equals(f.getClass());
  }

  /**
   * Checks whether the background tile at the given coordinates is of the
   * given type.
   * @param x
   * @param y
   * @param b
   * @return
   */
  public boolean isBackgroundOfType(int x, int y, BackgroundTile b) {
    return isBackgroundOfType(new Point(x, y), b);
  }

  /**
   * Checks whether the background tile at the given point is of the given
   * type.
   * @param p
   * @param b
   * @return
   */
  public boolean isBackgroundOfType(Point p, BackgroundTile b) {
    // Check for illegal arguments.
    checkForIllegalLocation(p);

    return background[p.x][p.y].getClass().equals(b.getClass());
  }

  /**
   * Checks whether the foreground tile at the given coordinates is a creature.
   * @param x
   * @param y
   * @return
   */
  public boolean isCreature(int x, int y) {
    return isCreature(new Point(x, y));
  }

  /**
   * Checks whether the foreground tile at the given point is a creature.
   * @param p
   * @return
   */
  public boolean isCreature(Point p) {
    // Check for illegal arguments.
    checkForIllegalLocation(p);

    if (foreground[p.x][p.y] instanceof Creature) {
      return true;
    }
    return false;
  }

  public boolean isIlluminated(Point p) {
    for (Torch t : torches) {
      if (t.canSee(p)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Counts how many foreground tiles adjacent to the tile at the given
   * coordinates are of the given type. Adjacent tiles are those that "share
   * a side" with the given tile, i.e. the one above, the one below, the one
   * to the right, and the one to left.
   * @param x
   * @param y
   * @param f
   * @return
   */
  public int countAdjacentForegroundTiles(int x, int y, ForegroundTile f) {
    return countAdjacentForegroundTiles(new Point(x, y), f);
  }

  /**
   * Counts how many foreground tiles adjacent to the tile at the given
   * point are of the given type. Adjacent tiles are those that "share a side"
   * with the given tile, i.e. the one above, the one below, the one to the
   * right, and the one to left.
   * @param p
   * @param f
   * @return
   */
  public int countAdjacentForegroundTiles(Point p, ForegroundTile f) {
    return countNearbyTiles(p, f, new BackgroundLevelBound(), false);
  }

  /**
   * Counts how many background tiles adjacent to the tile at the given
   * coordinates are of the given type. Adjacent tiles are those that "share
   * a side" with the given tile, i.e. the one above, the one below, the one
   * to the right, and the one to left.
   * @param x
   * @param y
   * @param b
   * @return
   */
  public int countAdjacentBackgroundTiles(int x, int y, BackgroundTile b) {
    return countAdjacentBackgroundTiles(new Point(x, y), b);
  }

  /**
   * Counts how many background tiles adjacent to the tile at the given
   * point are of the given type. Adjacent tiles are those that "share a side"
   * with the given tile, i.e. the one above, the one below, the one to the
   * right, and the one to left.
   * @param p
   * @param b
   * @return
   */
  public int countAdjacentBackgroundTiles(Point p, BackgroundTile b) {
    return countNearbyTiles(p, new ForegroundLevelBound(), b, false);
  }

  /**
   * Counts how many tiles adjacent to the tile at the given coordinates have
   * both foreground and background tiles of the given types. Adjacent tiles
   * are those that "share a side" with the given tile, i.e. the one above,
   * the one below, the one to the right, and the one to left.
   * @param x
   * @param y
   * @param f
   * @param b
   * @return
   */
  public int countAdjacentTiles(int x, int y, ForegroundTile f,
      BackgroundTile b) {
    return countAdjacentTiles(new Point(x, y), f, b);
  }

  /**
   * Counts how many tiles adjacent to the tile at the given point have both
   * foreground and background tiles of the given types. Adjacent tiles are
   * those that "share a side" with the given tile, i.e. the one above, the one
   * below, the one to the right, and the one to left.
   * @param p
   * @param f
   * @param b
   * @return
   */
  public int countAdjacentTiles(Point p, ForegroundTile f, BackgroundTile b) {
    return countNearbyTiles(p, f, b, false);
  }

  /**
   * Counts how many foreground tiles surrounding the tile at the given
   * coordinates, i.e. the eight tiles around it, are of the given type.
   * @param x
   * @param y
   * @param f
   * @return
   */
  public int countSurroundingForegroundTiles(int x, int y, ForegroundTile f) {
    return countSurroundingForegroundTiles(new Point(x, y), f);
  }

  /**
   * Counts how many foreground tiles surrounding the tile at the given
   * point, i.e. the eight tiles around it, are of the given type.
   * @param p
   * @param f
   * @return
   */
  public int countSurroundingForegroundTiles(Point p, ForegroundTile f) {
    return countNearbyTiles(p, f, new BackgroundLevelBound(), true);
  }

  /**
   * Counts how many background tiles surrounding the tile at the given
   * coordinates, i.e. the eight tiles around it, are of the given type.
   * @param x
   * @param y
   * @param b
   * @return
   */
  public int countSurroundingBackgroundTiles(int x, int y, BackgroundTile b) {
    return countSurroundingBackgroundTiles(new Point(x, y), b);
  }

  /**
   * Counts how many background tiles surrounding the tile at the given
   * point, i.e. the eight tiles around it, are of the given type.
   * @param p
   * @param b
   * @return
   */
  public int countSurroundingBackgroundTiles(Point p, BackgroundTile b) {
    return countNearbyTiles(p, new ForegroundLevelBound(), b, true);
  }

  /**
   * Counts how many tiles surrounding the tile at the given coordinates, i.e.
   * the eight tiles around it, have both foreground and background tiles of
   * the given types.
   * @param x
   * @param y
   * @param f
   * @param b
   * @return
   */
  public int countSurroundingTiles(int x, int y, ForegroundTile f,
      BackgroundTile b) {
    return countSurroundingTiles(new Point(x, y), f, b);
  }

  /**
   * Counts how many tiles surrounding the tile at the given point, i.e. the
   * eight tiles around it, have both foreground and background tiles of the
   * given types.
   * @param p
   * @param f
   * @param b
   * @return
   */
  public int countSurroundingTiles(Point p, ForegroundTile f,
      BackgroundTile b) {
    return countNearbyTiles(p, f, b, true);
  }

  public void triggerTrap(Point p, Creature c) {
    ((Trap)(getBackgroundTile(p))).trigger(c);
    setBackgroundTile(p, ((Trap)(getBackgroundTile(p))).getSourceTile());
  }

  /**
   * Counts how many tiles near the tile at the given coordinates have both
   * foreground and background tiles of the given types.
   * @param x
   * @param y
   * @param f if the placeholder type ForegroundLevelBound is used, then
   *          foreground type will not be used as a counting criterion
   * @param b if the placeholder type BackgroundLevelBound is used, then
   *          background type will not be used as a counting criterion
   * @param corners if true, the method will take into account all eight tiles
   *                surrounding the given tile; otherwise, it will ignore the
   *                four corner tiles
   * @return
   */
  private int countNearbyTiles(int x, int y, ForegroundTile f,
      BackgroundTile b, boolean corners) {
    return countNearbyTiles(new Point(x, y), f, b, corners);
  }

  /**
   * Counts how many tiles near the tile at the given point have both
   * foreground and background tiles of the given types.
   * @param p
   * @param f if the placeholder type ForegroundLevelBound is used, then
   *          foreground type will not be used as a counting criterion
   * @param b if the placeholder type BackgroundLevelBound is used, then
   *          background type will not be used as a counting criterion
   * @param corners if true, the method will take into account all eight tiles
   *                surrounding the given tile; otherwise, it will ignore the
   *                four corner tiles
   * @return
   */
  private int countNearbyTiles(Point p, ForegroundTile f, BackgroundTile b,
      boolean corners) {
    // Check for illegal arguments.
    checkForIllegalLocation(p);

    int count = 0;

    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        if (i == 0 && j == 0 || !corners && i != 0 && j != 0 ||
            !isInBounds(p.x + i, p.y + j)) {
          continue;
        }
        if ((f instanceof ForegroundLevelBound ||
            foreground[p.x + i][p.y + j].getClass().equals(f.getClass())) &&
            (b instanceof BackgroundLevelBound ||
            background[p.x + i][p.y + j].getClass().equals(b.getClass()))) {
          count++;
        }
      }
    }

    return count;
  }

  /**
   * Checks whether the given point is within the level bounds, and throws an
   * exception if it is not.
   * @param p
   */
  private void checkForIllegalLocation(Point p) {
    if (!isInBounds(p)) {
      throw new IllegalArgumentException(
          "Location (" + p.x + ", " + p.y + ") must be between (0, 0) and (" +
          width + ", " + height + ").");
    }
  }
}

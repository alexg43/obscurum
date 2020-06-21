package obscurum.environment.level_generators;

import java.awt.Point;
import java.lang.Math;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import obscurum.environment.Level;
import obscurum.environment.background.BackgroundTile;
import obscurum.environment.foreground.EmptyTile;
import obscurum.environment.foreground.ForegroundTile;
import obscurum.placeholders.ConnectorBackground;
import obscurum.placeholders.PathBackground;

/**
 * This generates a dungeon made of rooms of various sizes connected by
 * corridors.
 * Based on Bob Nystrom's procedural dungeon generation algorithm description
 * at http://journal.stuffwithstuff.com/2014/12/21/rooms-and-mazes/.
 * @author Alex Ghita
 */
public class DungeonBuilder extends Builder {
  /**
   * Class constructor specifying the level size and foreground and background
   * tile types, i.e. what tile to be used to fill unaccessible areas (e.g.
   * walls) and what tile to be used as a background for free tiles,
   * respectively (e.g. floors), and whether the level is linked to others.
   * @param width
   * @param height
   * @param foregroundType
   * @param backgroundType
   * @param hasNext
   * @param hasPrevious
   */
  public DungeonBuilder(int width, int height, ForegroundTile foregroundType,
      BackgroundTile backgroundType, boolean hasNext, boolean hasPrevious) {
    super(width, height, foregroundType, backgroundType, hasNext, hasPrevious);
  }

  /**
   * Builds a dungeon with default parameters.
   * @return
   */
  public Level build() {
    return build(5000, 5, 13, 0.1, -1);
  }

  /**
   * Builds a dungeon with the specified parameters.
   * @param attempts how many room generation attempts to make
   * @param minRoomSize
   * @param maxRoomSize
   * @param carveChance the chance to carve an additional connection between
   *                    a room and a path such that a loop is created; given as
   *                    a number between 0 and 1.
   * @param maxTrims the number of dead ends to trim from paths; if negative,
   *                 all dead ends will be trimmed
   * @return the resulting dungeon
   */
  public Level build(int attempts, int minRoomSize, int maxRoomSize,
      double carveChance, int maxTrims) {
    // Check for illegal arguments.
    int minSize = Math.min(width, height);
    if (attempts < 0) {
      throw new IllegalArgumentException("Number of attemps " + attempts +
          " cannot be negative.");
    }
    if (carveChance < 0 || carveChance > 1) {
      throw new IllegalArgumentException("Carve chance " + carveChance +
          " must be between 0 and 1.");
    }
    if (minRoomSize < 3 || minRoomSize >= minSize) {
      throw new IllegalArgumentException("Minimum room size " + minRoomSize +
          " must be between 3 and " + minSize + ".");
    }
    if (maxRoomSize < 7 || maxRoomSize >= minSize) {
      throw new IllegalArgumentException("Maximum room size " + maxRoomSize +
          " must be between 7 and " + minSize + ".");
    }
    if (minRoomSize % 2 == 0) {
      throw new IllegalArgumentException("Minimum room size " + minRoomSize +
          " must be odd.");
    }
    if (maxRoomSize % 2 == 0) {
      throw new IllegalArgumentException("Maximum room size " + maxRoomSize +
          " size must be odd.");
    }
    if (minRoomSize > maxRoomSize) {
      throw new IllegalArgumentException("Minimum room size " + minRoomSize +
          " must not be greater than the maximum room size " + maxRoomSize +
          ".");
    }

    initialiseBoard(foregroundType, backgroundType);
    makeRooms(attempts, minRoomSize, maxRoomSize);
    makePaths();
    makeConnections(carveChance);
    trimDeadEnds(maxTrims);
    cleanUp();
    placeLadders();
    return level;
  }

  /**
   * Attempts to generate a set number of rooms of random sizes within a
   * certain range and place them in random locations on the board. If any such
   * room can be placed on the board, it will be carved on it, i.e. the
   * corresponding area will be cleared of foreground tiles.
   * @param attempts the number of attempts to generate rooms
   * @param minRoomSize
   * @param maxRoomSize
   */
  private void makeRooms(int attempts, int minRoomSize, int maxRoomSize) {
    int roomWidth;
    int roomHeight;
    int topLeftX;
    int topLeftY;

    for (int i = 0; i < attempts; i++) {
      roomWidth = getRandomOddNumber(minRoomSize, maxRoomSize, true);
      roomHeight = getRandomOddNumber(minRoomSize, maxRoomSize, true);
      topLeftX = getRandomOddNumber(1, width - roomWidth - 1, false);
      topLeftY = getRandomOddNumber(1, height - roomHeight - 1, false);
      if (canPlaceRoom(roomWidth, roomHeight, topLeftX, topLeftY)) {
        carveRoom(roomWidth, roomHeight, topLeftX, topLeftY);
      }
    }
  }

  /**
   * Goes through all odd coordinates and attempts to start carving paths from
   * tiles that have not yet been carved and which have no carved neighbours.
   */
  private void makePaths() {
    for (int x = 1; x < width; x += 2) {
      for (int y = 1; y < height; y += 2) {
        if (level.countAdjacentForegroundTiles(x, y, foregroundType) == 4) {
          carvePath(new Point(x, y));
        }
      }
    }
  }

  /**
   * This connects the dungeon by starting in a random room and applying a
   * flood fill to determine where to carve connections between rooms and
   * paths. It will also carve additional connections, such that the dungeon
   * will contain loops.
   * @param carveChance chance to carve a connector that does not lead to a new
   *                    area, i.e. chance to create a loop in the maze
   */
  private void makeConnections(double carveChance) {
    boolean expanded;
    Point current;
    Point start = new Point();
    Point test;
    ArrayList<Point> connectors = new ArrayList<Point>();
    Queue<Point> fringe = new LinkedList<Point>();
    boolean[][] reached = new boolean[width][height];

    // Mark the connectors on the board.
    markConnectors();
    // Pick a starting point.
    do {
      start.x = (int)(Math.random() * width);
      start.y = (int)(Math.random() * height);
    } while (!level.isForegroundOfType(start, new EmptyTile()) ||
        !level.isBackgroundOfType(start, backgroundType));
    fringe.add(start);
    reached[start.x][start.y] = true;

    do {
      expanded = false;
      // Flood-fill as much as possible without carving new connections.
      while (!fringe.isEmpty()) {
        current = fringe.remove();
        for (int x = -1; x <= 1; x++) {
          for (int y = -1; y <= 1; y++) {
            if (x == 0 && y == 0 || x != 0 && y != 0) {
              continue;
            }
            test = new Point(current.x + x, current.y + y);
            if (!level.isInBounds(test) ||
                level.isForegroundOfType(test, foregroundType)) {
              continue;
            }
            // Add any connectors reached to a separate list.
            if (level.isBackgroundOfType(test, new ConnectorBackground()) &&
                !reached[test.x][test.y]) {
              connectors.add(test);
              reached[test.x][test.y] = true;
            // And all other tiles to the fringe.
            } else if (!reached[test.x][test.y]) {
              fringe.add(test);
              reached[test.x][test.y] = true;
            }
          }
        }
      }
      /**
       * Carve connections until a new area is discovered, or until there are
       * no more connectors.
       */
      while (!expanded && !connectors.isEmpty()) {
        int index = (int)(Math.random() * connectors.size());
        current = connectors.remove(index);
        // Ensure that no two adjacent connectors are carved.
        if (level.countAdjacentForegroundTiles(current, foregroundType) +
            level.countAdjacentBackgroundTiles(current,
            new ConnectorBackground()) < 2) {
          level.setForegroundTile(current, foregroundType);
          level.setBackgroundTile(current, backgroundType);
          continue;
        }
        // Search for a new tile around the carved connector.
        for (int x = -1; x <= 1; x++) {
          for (int y = -1; y <= 1; y++) {
            if (x == 0 && y == 0 || x != 0 && y != 0) {
              continue;
            }
            test = new Point(current.x + x, current.y + y);
            if (!level.isInBounds(test) || reached[test.x][test.y] ||
                level.isForegroundOfType(test, foregroundType)) {
              continue;
            }
            // Add the new tile to the fringe and resume the flood fill.
            if (!reached[test.x][test.y]) {
              expanded = true;
              fringe.add(test);
              level.setBackgroundTile(current, new PathBackground());
              break;
            }
          }
          if (expanded) {
            break;
          }
        }
        /**
         * If no new area has been found, take all currently-discovered
         * connectors and carve them with a small probability, or fill them
         * back in.
         */
        if (!expanded) {
          if (Math.random() >= 1.0 - carveChance) {
            level.setBackgroundTile(current, new PathBackground());
          } else {
            level.setForegroundTile(current, foregroundType);
            level.setBackgroundTile(current, backgroundType);
          }
        }
      }
    } while (!connectors.isEmpty());
  }

  /**
   * Fills a set number of dead ends from the board paths, or all of them if
   * the parameter is negative.
   * @param maxTrims how many dead end tiles to trim from paths; if negative,
   *                 trim all dead ends
   */
  private void trimDeadEnds(int maxTrims) {
    int trims = 0;
    Point current;
    Point test;
    Queue<Point> deadEnds = new LinkedList<Point>();

    // Add all dead ends to a queue.
    for (int x = 1; x < width - 1; x++) {
      for (int y = 1; y < height - 1; y++) {
        if (level.isBackgroundOfType(x, y, new PathBackground()) &&
            level.countAdjacentForegroundTiles(x, y, new EmptyTile()) <= 1) {
          deadEnds.add(new Point(x, y));
        }
      }
    }

    /**
     * While the number of desired trims has not been achieved, take the next
     * dead end from the queue, fill it and add the new nearby deadend to the
     * queue.
     */
    while (!deadEnds.isEmpty() && (maxTrims < 0 || trims < maxTrims)) {
      current = deadEnds.remove();
      level.setTile(current, foregroundType, backgroundType);
      trims++;

      for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
          if (x == 0 && y == 0 || x != 0 && y != 0) {
            continue;
          }
          test = new Point(current.x + x, current.y + y);
          if (level.isInBounds(test) &&
              level.isBackgroundOfType(test, new PathBackground()) &&
              level.countAdjacentForegroundTiles(test, new EmptyTile()) <= 1) {
            deadEnds.add(test);
          }
        }
      }
    }
  }

  /**
   * Turns all path tiles back into normal, carved tiles.
   */
  private void cleanUp() {
    for (int x = 1; x < width - 1; x++) {
      for (int y = 1; y < height - 1; y++) {
        if (level.isBackgroundOfType(x, y, new PathBackground())) {
          level.setBackgroundTile(x, y, backgroundType);
        }
      }
    }
  }

  /**
   * Creates a room of the given size at the given location.
   * @param roomWidth
   * @param roomHeight
   * @param topLeftX
   * @param topLeftY
   */
  private void carveRoom(int roomWidth, int roomHeight, int topLeftX,
      int topLeftY) {
    for (int x = topLeftX; x < topLeftX + roomWidth; x++) {
      for (int y = topLeftY; y < topLeftY + roomHeight; y++) {
        level.setForegroundTile(x, y, new EmptyTile());
        level.setBackgroundTile(x, y, backgroundType);
      }
    }
  }

  /**
   * Randomly generates a non-looping maze from the given starting point.
   * @param start
   */
  private void carvePath(Point start) {
    int index;
    Point test;
    Point current;
    Point opposite;
    // Store the tiles to be expanded.
    ArrayList<Point> frontier = new ArrayList<Point>();
    // Store the tiles from which the frontier tiles have been discovered.
    ArrayList<Point> parents = new ArrayList<Point>();

    // Mark the starting point as a path and carve the foreground.
    level.setTile(start, new EmptyTile(), new PathBackground());

    // Add nearby tiles that have not been carved yet to the frontier.
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        if (x == 0 && y == 0 || x != 0 && y != 0) {
          continue;
        }
        test = new Point(start.x + x, start.y + y);
        if (level.isInBounds(test) &&
            level.isForegroundOfType(test, foregroundType)) {
          frontier.add(test);
          parents.add(new Point(start.x, start.y));
        }
      }
    }

    while (!frontier.isEmpty()) {
      // Take a random tile from the frontier and its parent.
      index = (int)(Math.random() * frontier.size());
      current = frontier.remove(index);
      opposite = parents.remove(index);
      // Compute the opposite of the parent with respect to the child tile.
      opposite.x += 2 * (current.x - opposite.x);
      opposite.y += 2 * (current.y - opposite.y);
      if (!level.isInBounds(opposite)) {
        continue;
      }

      // If neither tile has been carved, add them both to the path.
      if (level.isForegroundOfType(current, foregroundType) &&
          level.countSurroundingTiles(current, new EmptyTile(),
          backgroundType) == 0) {
        if (level.isForegroundOfType(opposite, foregroundType) &&
            level.countSurroundingTiles(opposite, new EmptyTile(),
            backgroundType) == 0) {
          level.setTile(current, new EmptyTile(), new PathBackground());
          level.setTile(opposite, new EmptyTile(), new PathBackground());

          // Add more tiles close to the opposite to the frontier.
          for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
              if (x == 0 && y == 0 || x != 0 && y != 0) {
                continue;
              }
              test = new Point(opposite.x + x, opposite.y + y);
              if (level.isInBounds(test) &&
                  level.isForegroundOfType(test, foregroundType)) {
                frontier.add(test);
                parents.add(new Point(opposite.x, opposite.y));
              }
            }
          }
        }
      }
    }
  }

  /**
   * Marks all connectors on the map. A tile is a connector if it links on a
   * horizontal or vertical line two rooms or a room and a path.
   */
  private void markConnectors() {
    ArrayList<Point> connectors = new ArrayList<Point>();

    // Check each non-carved tile to see if it is a connector.
    for (int x = 1; x < width - 1; x++) {
      for (int y = 1; y < height - 1; y++) {
        if (!level.isForegroundOfType(x, y, foregroundType)) {
          continue;
        }
        if (!level.isForegroundOfType(x - 1, y, foregroundType) &&
            !level.isForegroundOfType(x + 1, y, foregroundType) &&
            !(level.isBackgroundOfType(x - 1, y, new PathBackground()) &&
            level.isBackgroundOfType(x + 1, y, new PathBackground())) ||
            !level.isForegroundOfType(x, y - 1, foregroundType) &&
            !level.isForegroundOfType(x, y + 1, foregroundType) &&
            !(level.isBackgroundOfType(x, y - 1, new PathBackground()) &&
            level.isBackgroundOfType(x, y + 1, new PathBackground()))) {
          connectors.add(new Point(x, y));
        }
      }
    }
    // Mark the connectors.
    for (Point p : connectors) {
      level.setTile(p, new EmptyTile(), new ConnectorBackground());
    }
  }

  /**
   * Tests whether a room can be placed at a specific location, i.e. if there
   * is no other room overlapping its designated area.
   * @param roomWidth
   * @param roomHeight
   * @param topLeftX
   * @param topLeftY
   * @return true if the room can be placed, false otherwise
   */
  private boolean canPlaceRoom(int roomWidth, int roomHeight, int topLeftX,
      int topLeftY) {
    // Ensure the room and its walls do not overlap with any other room.
    for (int x = topLeftX - 1; x <= topLeftX + roomWidth; x++) {
      for (int y = topLeftY - 1; y <= topLeftY + roomHeight; y++) {
        if (level.isForegroundOfType(x, y, new EmptyTile())) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Tests whether a point is inside a room and not on its margins.
   * @param p
   * @return
   */
  private boolean isInRoom(Point p) {
    if (level.countSurroundingTiles(p, new EmptyTile(), backgroundType) == 8) {
      return true;
    }
    return false;
  }

  /**
   * Returns a random odd number between the given bounds. Assumes that minVal
   * is not greater than maxVal.
   * @param minVal
   * @param maxVal
   * @param add whether to add or subtract 1 if the result is not odd
   * @return
   */
  private int getRandomOddNumber(int minVal, int maxVal, boolean add) {
    int num;
    int diff;
    diff = add ? 1 : -1;
    num = minVal + (int)(Math.random() * (maxVal - minVal + 1));
    num = num % 2 == 0 ? num + diff : num;
    return num;
  }

  /**
   * Places the downward and upward ladders which connect the current level to
   * the next and the previous levels. Ladders are placed randomly inside
   * rooms, and only if the level is connected to other levels.
   */
  protected void placeLadders() {
    Point ladderLocation;
    if (hasNext) {
      do {
        ladderLocation = level.getRandomEmptyLocation();
      } while (!isInRoom(ladderLocation));
      level.setBackgroundTile(ladderLocation, BackgroundTile.createDownwardDoorway(level.getBackgroundTile(ladderLocation)));
      level.setNextLocation(ladderLocation);
    }
    if (hasPrevious) {
      do {
        ladderLocation = level.getRandomEmptyLocation();
      } while (!isInRoom(ladderLocation));
      level.setBackgroundTile(ladderLocation, BackgroundTile.createUpwardDoorway(level.getBackgroundTile(ladderLocation)));
      level.setPreviousLocation(ladderLocation);
    }
  }
}

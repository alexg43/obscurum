package obscurum.creatures.ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import obscurum.creatures.Creature;
import obscurum.util.Line;
import obscurum.creatures.abilities.Spell;
import obscurum.environment.Level;
import obscurum.environment.foreground.EmptyTile;
import obscurum.environment.foreground.ForegroundTile;
import obscurum.placeholders.NullCreature;
import obscurum.util.PathTileData;

/**
 * This models a creature's behaviour.
 * @author Alex Ghita
 */
public abstract class CreatureAI {
  public static final int SIMPLE = 0;
  public static final int LEARNING = 1;
  public static final int OMNISCIENT = 2;
  protected String name;
  protected Creature creature;
  protected int knowledgeType;
  protected ArrayList<ForegroundTile> transparentTiles;

  /**
   * Class constructor specifying the creature to which this AI is assigned to.
   * @param creature
   */
  public CreatureAI(String name, Creature creature, int knowledgeType,
      ArrayList<ForegroundTile> transparentTiles) {
    // Check for illegal arguments.
    if (creature == null || creature instanceof NullCreature) {
      throw new IllegalArgumentException("Creature cannot be null.");
    }
    if (knowledgeType < 0 || knowledgeType > 2) {
      throw new IllegalArgumentException("Knowledge type " + knowledgeType +
          " must be 0, 1, or 2.");
    }
    if (transparentTiles == null) {
      throw new IllegalArgumentException(
          "The list of transparent tiles cannot be null.");
    }

    this.name = name;
    this.creature = creature;
    this.knowledgeType = knowledgeType;
    this.transparentTiles = transparentTiles;
    this.creature.setAI(this);

    // Uncomment to have enemies coloured by their AI. Used for debugging.

    // if (!creature.getName().equals("Treasure Chest") &&
    //     !creature.getName().equals("Spawner")) {
    //   if (name == "Aggressive") {
    //     creature.setForegroundColour(Display.CYAN);
    //   } else if (name == "Hit And Run") {
    //     creature.setForegroundColour(Display.BLUE);
    //   } else if (name == "Idle") {
    //     creature.setForegroundColour(Display.BRIGHT_BLACK);
    //   } else if (name == "Passive") {
    //     creature.setForegroundColour(Display.GREEN);
    //   } else if (name == "Random Walk") {
    //     creature.setForegroundColour(Display.YELLOW);
    //   } else if (name == "Teleport") {
    //     creature.setForegroundColour(Display.MAGENTA);
    //   }
    // }
  }

  public String getName() {
    return name;
  }

  public int getKnowledgeType() {
    return knowledgeType;
  }

  public void setKnowledgeType(int knowledgeType) {
    this.knowledgeType = knowledgeType;
  }

  public boolean canSee(Point target) {
    return canSee(target, creature.getAttributes()[Creature.LINE_OF_SIGHT]);
  }

  // Search in a square around the creature which tiles can be seen.
  public ArrayList<Point> getSeenPoints() {
    ArrayList<Point> seenPoints = new ArrayList<Point>();
    if (knowledgeType == OMNISCIENT) {
      for (int x = 0; x < creature.getLevel().getWidth(); x++) {
        for (int y = 0; y < creature.getLevel().getHeight(); y++) {
          seenPoints.add(new Point(x, y));
        }
      }
      return seenPoints;
    }
    int lineOfSight = creature.getAttributes()[Creature.LINE_OF_SIGHT];
    Point source = creature.getLocation();
    Point topLeft = new Point(source.x - lineOfSight, source.y - lineOfSight);
    Point bottomRight = new Point(source.x + lineOfSight,
        source.y + lineOfSight);

    for (int x = topLeft.x; x <= bottomRight.x; x++) {
      for (int y = topLeft.y; y <= bottomRight.y; y++) {
        if (x < 0 || x >= creature.getLevel().getWidth() ||
            y < 0 || y >= creature.getLevel().getHeight()) {
          continue;
        }
        if (canSee(new Point(x, y))) {
          seenPoints.add(new Point(x, y));
        }
      }
    }
    return seenPoints;
  }

  // -1 = infinite
  public boolean canSee(Point target, int maxDistance) {
    Point source = creature.getLocation();
    int distance = (source.x - target.x) * (source.x - target.x) +
        (source.y - target.y) * (source.y - target.y);
    Line sightRay;
    List<Point> linePoints;

    if (distance > maxDistance * maxDistance && maxDistance != -1) {
      return false;
    }
    sightRay = new Line(creature.getLocation(), target);
    sightRay.plotLine();
    linePoints = sightRay.getPointsOnLine();
    for (Point p : linePoints) {
      if (creature.getLevel().getForegroundTile(p).isOpaque() &&
          !p.equals(target) && !p.equals(source)) {
        return false;
      }
    }
    if (knowledgeType == LEARNING) {
      ForegroundTile fg = creature.getLevel().getForegroundTile(target);
      fg = fg instanceof Creature && !fg.getName().equals("Treasure Chest") &&
          !fg.getName().equals("Spawner") ? new EmptyTile() : fg;
      creature.getExploration().setTile(target, fg,
          creature.getLevel().getBackgroundTile(target));
    }
    return true;
  }

  /**
   * Decides what the creature should do each turn.
   */
  public void onUpdate() {
    creature.setAttackTrajectory(null);
    if (creature.getCombatCooldown() == 0) {
      creature.regenerate();
    } else {
      creature.decrementCombatCooldown();
    }
    for (Spell s : creature.getSpells()) {
      s.refresh();
    }
    if (!canSee(creature.getTarget().getLocation())) {
      creature.setTarget(new NullCreature());
    }
  }

  public void moveInRandomDirection() {
    int x;
    int y;

    do {
      x = 1 - (int)(Math.random() * 3);
      y = 1 - (int)(Math.random() * 3);
    } while (x == 0 && y == 0 || x != 0 && y != 0);
    creature.moveBy(x, y);
  }

  /**
   * Decides what the creature should do when it is about to enter a new
   * location.
   * @param p
   */
  public abstract void onEnter(Point p);

  protected ArrayList<Point> getShortestPathTo(Point targetLocation) {
    ArrayList<Point> path = new ArrayList<Point>();
    Level level = creature.getLevel();
    Point startLocation = creature.getLocation();
    PriorityQueue<PathTileData> frontier = new PriorityQueue<PathTileData>(
        level.getWidth() * level.getHeight(), Comparator.comparingInt(PathTileData::getPriority));
    int[][] costs = new int[level.getWidth()][level.getHeight()];

    frontier.add(new PathTileData(startLocation, 0));
    costs[startLocation.x][startLocation.y] = 1;

    PathTileData currentSquare;
    Point currentLocation;
    Point testLocation;
    while (!frontier.isEmpty()) {
      currentSquare = frontier.remove();
      currentLocation = currentSquare.getLocation();

      if (currentLocation.equals(targetLocation)) {
        break;
      }

      for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
          if (x != 0 && y != 0 || x == 0 && y == 0) {
            continue;
          }

          testLocation = new Point(currentLocation.x + x,
              currentLocation.y + y);

          if (!level.isInBounds(testLocation) ||
              !level.isForegroundOfType(testLocation, new EmptyTile()) &&
              !(level.getForegroundTile(testLocation) instanceof Creature)) {
            continue;
          }

          if (costs[testLocation.x][testLocation.y] == 0 ||
              costs[testLocation.x][testLocation.y] >
              costs[currentLocation.x][currentLocation.y] + 1) {
            costs[testLocation.x][testLocation.y] =
                costs[currentLocation.x][currentLocation.y] + 1;
            int priority = costs[testLocation.x][testLocation.y] +
                Math.abs(testLocation.x - targetLocation.x) +
                Math.abs(testLocation.y - targetLocation.y);
            frontier.add(new PathTileData(testLocation, priority));
          }
        }
      }
    }

    // Retrace path.
    currentLocation = targetLocation;
    path.add(currentLocation);

    while (costs[currentLocation.x][currentLocation.y] > 1) {
      ArrayList<Point> nextPoints = new ArrayList<Point>();
      for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
          if (x != 0 && y != 0 || x == 0 && y == 0) {
            continue;
          }
          testLocation = new Point(currentLocation.x + x,
              currentLocation.y + y);
          if (!level.isInBounds(testLocation)) {
            continue;
          }
          if (costs[testLocation.x][testLocation.y] ==
              costs[currentLocation.x][currentLocation.y] - 1) {
            nextPoints.add(testLocation);
          }
        }
      }
      currentLocation = nextPoints.get(
          (int)(Math.random() * nextPoints.size()));
      path.add(currentLocation);
    }

    return path;
  }
}

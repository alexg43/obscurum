package obscurum.creatures.ai;

import java.awt.Point;
import java.lang.Math;
import java.util.ArrayList;
import obscurum.creatures.Creature;
import obscurum.environment.foreground.EmptyTile;
import obscurum.environment.foreground.ForegroundTile;

/**
 * This models an AI which runs away for a few turns after attacking its
 * target.
 * @author Alex Ghita
 */
public class HitAndRunAI extends CreatureAI{
  private int hitMode; // 0 = yes, 0<x<4 = no
  private int previousHealth;
  private boolean hitWhileRunning;

  public HitAndRunAI(Creature creature, int knowledgeType,
      ArrayList<ForegroundTile> transparentTiles) {
    super("Hit And Run", creature, knowledgeType, transparentTiles);
    previousHealth = creature.getCurrentHealth();
    hitMode = 0;
    hitWhileRunning = false;
  }

  @Override
  public void onEnter(Point p) {
    if (creature.getLevel().isForegroundOfType(p, new EmptyTile())) {
      creature.move(p);
    }
  }

  @Override
  public void onUpdate() {
    super.onUpdate();

    ArrayList<Point> seenPoints = getSeenPoints();
    boolean foundPlayer = false;

    for (Point p : seenPoints) {
      // Check if it's the player.
      if (creature.getLevel().getForegroundTile(p).getName().equals(
          "Player")) {
        creature.setTarget((Creature)
            (creature.getLevel().getForegroundTile(p)));
        foundPlayer = true;
        break;
      }
    }
    if (foundPlayer) {
      Point targetLocation = creature.getTarget().getLocation();

      if (hitMode == 0 || creature.getCurrentHealth() < previousHealth) {
        if (canSee(targetLocation, creature.getAttackRange())) {
          creature.attackTarget();
          hitMode = 1;
        } else {
          ArrayList<Point> steps = getShortestPathTo(targetLocation);

          if (steps.size() > 1) {
            Point nextStep = steps.get(steps.size() - 2);
            creature.moveBy(nextStep.x - creature.getLocation().x,
                nextStep.y - creature.getLocation().y);
          }
        }
      } else {
        ArrayList<Point> steps = getShortestPathTo(targetLocation);

        if (steps.size() > 1) {
          Point nextStep = steps.get(steps.size() - 2);
          ArrayList<Point> options = new ArrayList<Point>();

          for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
              if (x != 0 && y != 0 || x == 0 && y == 0) {
                continue;
              }
              Point test = new Point(creature.getLocation().x + x,
                  creature.getLocation().y + y);
              if (!creature.getLevel().isInBounds(test) ||
                  test.equals(nextStep) ||
                  !creature.getLevel().isForegroundOfType(test,
                  new EmptyTile())) {
                continue;
              }
              options.add(test);
            }
          }
          if (!options.isEmpty()) {
            nextStep = options.get((int)(Math.random() * options.size()));

            creature.moveBy(nextStep.x - creature.getLocation().x,
                nextStep.y - creature.getLocation().y);
          }
        }
        hitMode = (hitMode + 1) % 4;
      }
    } else {
      moveInRandomDirection();
      hitMode = 0;
    }
    previousHealth = creature.getCurrentHealth();
  }
}

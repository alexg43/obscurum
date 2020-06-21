package obscurum.creatures.ai;

import java.awt.Point;
import java.util.ArrayList;
import obscurum.creatures.Creature;
import obscurum.creatures.Player;
import obscurum.environment.Level;
import obscurum.environment.background.ExitPortal;
import obscurum.environment.background.traps.Trap;
import obscurum.environment.foreground.EmptyTile;
import obscurum.environment.foreground.ForegroundTile;
import obscurum.screens.RangedAttackAimScreen;
import obscurum.screens.LootScreen;

/**
 * This models the player AI, which acts based on user input.
 * @author Alex Ghita
 */
public class PlayerAI extends CreatureAI {
  public PlayerAI(Player creature, int knowledgeType,
      ArrayList<ForegroundTile> transparentTiles) {
    super("Player", creature, knowledgeType, transparentTiles);
  }

  @Override
  public void onEnter(Point p) {
    if (creature.getLevel().isForegroundOfType(p, new EmptyTile())) {
      if (creature.getLevel().isBackgroundOfType(p, "Downward Doorway")) {
        ((Player)creature).setCurrentLevel(((Player)creature).getCurrentLevel()
            + 1);
        switchLevels(creature.getLevel(), creature.getLevel().getNext(),
            creature.getLevel().getNext().getPreviousLocation());
      } else if (creature.getLevel().isBackgroundOfType(p, "Upward Doorway")) {
        ((Player)creature).setCurrentLevel(((Player)creature).getCurrentLevel()
            - 1);
        switchLevels(creature.getLevel(), creature.getLevel().getPrevious(),
            creature.getLevel().getPrevious().getNextLocation());
      } else if (creature.getLevel().isBackgroundOfType(p, new ExitPortal())) {
        creature.move(p);
        ((Player)creature).setWin();
      } else if (creature.getLevel().getBackgroundTile(p) instanceof Trap) {
        creature.move(p);
        creature.getLevel().triggerTrap(p, creature);
      } else {
        creature.move(p);
      }
    } else if (creature.getLevel().isCreature(p)) {
      Creature c = (Creature)creature.getLevel().getForegroundTile(p);
      ((Player)creature).setTarget(c);
      if (c.isAlive() && !c.getName().equals("Treasure Chest")) {
        creature.attackTarget();
      } else {
        ((Player)creature).setSubScreen(
            new LootScreen(((Player)creature).getWorld(), (Player)creature,
                ((Player)creature).getTarget()));
        ((Player)creature).setInSubScreen(true);
      }
    }
  }

  public void aim(int maxRange) {
    ((Player)creature).setSubScreen(
        new RangedAttackAimScreen(((Player)creature).getWorld(),
        (Player)creature, maxRange));
    ((Player)creature).setInSubScreen(true);
  }

  @Override
  public void onUpdate() {
    super.onUpdate();

    while (creature.getAttributes()[Creature.INVENTORY_SIZE] >
        creature.getInventory().getSize()) {
      creature.getInventory().increaseSize();
    }
  }

  /**
   * Transport the player from one level to another.
   * @param current
   * @param next
   * @param p the location where the player will be placed in the next level.
   */
  private void switchLevels(Level current, Level next, Point p) {
    creature.getLevel().setForegroundTile(creature.getLocation(),
        new EmptyTile());
    creature.getLevel().remove(creature);
    creature.setLevel(next);
    creature.addExploration(next);
    creature.setLocation(p);
    creature.getLevel().add(creature);
    creature.getLevel().setForegroundTile(p, creature);
  }
}

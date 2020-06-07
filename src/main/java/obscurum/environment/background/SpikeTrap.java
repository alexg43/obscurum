package obscurum.environment.background;

import obscurum.creatures.Creature;
import obscurum.creatures.Player;

/**
 * This models a spike trap which takes the appearance of a given source
 * background tile (usually the one used to create the level) and deals damage
 * to the player when stepping on it.
 * @author Alex Ghita
 */
public class SpikeTrap extends Trap {
  public SpikeTrap() {
    super();
  }

  public SpikeTrap(BackgroundTile sourceTile) {
    super(sourceTile);
  }

  @Override
  public void trigger(Creature target) {
    if (target.isInvulnerable()) {
      return;
    }
    int damage = Math.min(target.getCurrentHealth(),
        (int)(target.getMaxHealth() * 0.05));
    target.setCurrentHealth(target.getCurrentHealth() - damage);
    ((Player)target).addMessageToCombatLog("You've walked into a spike trap! You've taken " + damage + " damage.");
    target.resetCombatCooldown();
  }
}

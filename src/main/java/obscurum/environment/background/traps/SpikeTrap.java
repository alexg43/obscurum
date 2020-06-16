package obscurum.environment.background.traps;

import obscurum.creatures.Creature;
import obscurum.creatures.Player;
import obscurum.environment.background.BackgroundTile;

public class SpikeTrap extends Trap {
    public SpikeTrap(BackgroundTile sourceTile) {
        super(sourceTile);
    }

    @Override
    public void trigger(Creature target) {
        int damage = Math.min(target.getCurrentHealth(), (int)(target.getMaxHealth() * 0.05));
        target.dealDamage(damage);
        target.resetCombatCooldown();

        if (target instanceof Player) {
            ((Player) target).addMessageToCombatLog("You've walked into a spike trap! You've taken " + damage + " damage.");
        }
    }
}

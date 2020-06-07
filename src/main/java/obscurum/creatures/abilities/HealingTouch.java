package obscurum.creatures.abilities;

import obscurum.creatures.Creature;

/**
 * This models a "Healing Touch" spells, which restores a number of health
 * points to the target creature.
 * @author Alex Ghita
 */
public class HealingTouch extends Spell {
  private static final int HEALING_INCREASE = 4;
  private static final int MANA_COST_INCREASE = 3;
  private int healing;
  private int healedLife;

  public HealingTouch() {
    super("Healing Touch", 1, 50, 5, 5, 10, Spell.ANYTIME_SPELL);
    healing = 5;
  }

  public String getEffectMessage(Creature c) {
    String message = "You've healed " + healedLife + " health points. " +
        c.getName() + " now has " + c.getCurrentHealth() + " health.";
    return message;
  }

  public String getDescription() {
    return "Cast on a target creature to heal it for " + healing +
        " (+ Spell Power) health.";
  }

  public String getLevelUpDescription() {
    return "+" + HEALING_INCREASE + " Healing, +" + MANA_COST_INCREASE +
        " Mana Cost.";
  }

  protected void improveSpell() {
    healing += HEALING_INCREASE;
    manaCost += MANA_COST_INCREASE;
  }

  protected void affect(Creature c, int casterSpellPower) {
    healedLife = Math.min(healing + casterSpellPower,
        c.getMaxHealth() - c.getCurrentHealth());
    c.setCurrentHealth(Math.min(c.getMaxHealth(), c.getCurrentHealth() + healedLife));
  }
}

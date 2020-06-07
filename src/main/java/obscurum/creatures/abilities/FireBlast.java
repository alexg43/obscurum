package obscurum.creatures.abilities;

import obscurum.creatures.Creature;

/**
 * This models a "Fire Blast" spells, which deals damage to the target
 * creature.
 * @author Alex Ghita
 */
public class FireBlast extends Spell {
  private static final int DAMAGE_INCREASE = 2;
  private static final int MANA_COST_INCREASE = 1;
  private int damage;
  private int dealtDamage;

  public FireBlast() {
    super("Fire Blast", 1, 50, 10, 5, 10, Spell.COMBAT_SPELL);
    damage = 3;
  }

  public String getEffectMessage(Creature c) {
    String message = "You've dealt " + dealtDamage + " damage. " + c.getName();
    if (c.getCurrentHealth() == 0) {
      message += " died.";
    } else {
      message += " has " + c.getCurrentHealth() + " health left.";
    }
    return message;
  }

  public String getDescription() {
    return "Cast on a target creature to deal " + damage +
        " (+ Spell Power) damage to it.";
  }

  public String getLevelUpDescription() {
    return "+" + DAMAGE_INCREASE + " Damage, +" + MANA_COST_INCREASE +
        " Mana Cost.";
  }

  protected void improveSpell() {
    damage += DAMAGE_INCREASE;
    manaCost += MANA_COST_INCREASE;
  }

  protected void affect(Creature c, int casterSpellPower) {
    dealtDamage = Math.min(c.getCurrentHealth(), damage + casterSpellPower);
    c.setCurrentHealth(Math.max(0, c.getCurrentHealth() - dealtDamage));
  }
}

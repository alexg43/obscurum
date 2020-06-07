package obscurum.creatures;

import java.awt.Point;
import java.lang.Math;

import obscurum.display.Display;
import obscurum.environment.Level;
import obscurum.items.Item;
import obscurum.items.RottenFlesh;
import obscurum.items.SpellTome;
import obscurum.items.weapons.Sword;
import obscurum.items.weapons.Bow;

/**
 * This models a zombie, a basic enemy with melee attacks.
 * @author Alex Ghita
 */
public class Zombie extends Creature {
  public Zombie(Level level, Point location, int powerLevel, int lineOfSight,
      int health, int armour, int strength, int agility, int stamina,
      int spirit, int intellect) {
    super("Zombie", 'z', Display.MAGENTA, level, location, powerLevel,
        lineOfSight, 5, 1, health, 0, armour, strength, agility, stamina,
        spirit, intellect);
  }

  @Override
  public void generateInventory() {
    int weaponLevel = (int)(Math.max(1, attributes[POWER_LEVEL] * 0.75));
    LootTableEntry[] lootTables = {
        new LootTableEntry(((Item)new RottenFlesh()), 0.7, 1, 10, false),
        new LootTableEntry(((Item)new Sword(weaponLevel)), 0.1, 1, 1, true),
        new LootTableEntry(((Item)new Bow(weaponLevel)), 0.1, 1, 1, true),
        new LootTableEntry(((Item)new SpellTome()), 0.1, 1, 1, true)};
    LootTable lootTable = new LootTable(lootTables);
    lootTable.fillInventory(inventory);
  }

  @Override
  public void powerUp() {
    currentHealth += 50;
    armour += 50;
    attributes[Creature.POWER_LEVEL] += 10;
    for (int i = Creature.STRENGTH; i <= Creature.INTELLECT; i++) {
      attributes[i] += 35;
    }
    computeSecondaryAttributes();
  }
}

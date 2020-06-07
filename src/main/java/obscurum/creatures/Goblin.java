package obscurum.creatures;

import java.awt.Point;
import java.lang.Math;

import obscurum.display.Display;
import obscurum.environment.Level;
import obscurum.items.Item;
import obscurum.items.Rags;
import obscurum.items.SpellTome;
import obscurum.items.armour.*;
import obscurum.items.weapons.Sword;
import obscurum.items.weapons.Bow;

/**
 * This models a goblin, i.e. a creature with a ranged attack.
 * @author Alex Ghita
 */
public class Goblin extends Creature {
  public Goblin(Level level, Point location, int powerLevel, int lineOfSight,
      int health, int armour, int strength, int agility, int stamina,
      int spirit, int intellect) {
    super("Goblin", 'g', Display.BRIGHT_GREEN, level, location, powerLevel,
        lineOfSight, 5, 5, health, 1, armour, strength, agility, stamina,
        spirit, intellect);
  }

  @Override
  public void generateInventory() {
    int itemLevel = Math.max(1, attributes[POWER_LEVEL]);
    LootTableEntry[] lootTables = {
        new LootTableEntry(((Item)new Rags()), 0.45, 1, 10, false),
        new LootTableEntry(((Item)new Sword(itemLevel)), 0.05, 1, 1, true),
        new LootTableEntry(((Item)new Bow(itemLevel)), 0.05, 1, 1, true),
        new LootTableEntry(((Item)new SpellTome()), 0.1, 1, 1, true),
        new LootTableEntry(((Item)new IronBoots(itemLevel)), 0.05, 1, 1, true),
        new LootTableEntry(((Item)new IronChestplate(itemLevel)), 0.05, 1, 1,
        true),
        new LootTableEntry(((Item)new IronHelmet(itemLevel)), 0.05, 1, 1, true),
        new LootTableEntry(((Item)new IronLeggings(itemLevel)), 0.05, 1, 1,
        true),
        new LootTableEntry(((Item)new LeatherGloves(itemLevel)), 0.05, 1, 1,
        true)};
    LootTable lootTable = new LootTable(lootTables);
    lootTable.fillInventory(inventory);
  }

  @Override
  public void powerUp() {
    currentHealth += 80;
    armour += 10;
    attributes[Creature.POWER_LEVEL] += 10;
    attributes[Creature.LINE_OF_SIGHT] += 1;
    for (int i = Creature.STRENGTH; i <= Creature.INTELLECT; i++) {
      attributes[i] += 22;
    }
    computeSecondaryAttributes();
  }
}

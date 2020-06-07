package obscurum.creatures;

import java.awt.Point;
import java.util.ArrayList;

import obscurum.display.asciiPanel.AsciiPanel;
import obscurum.environment.Level;
import obscurum.items.ConsumableItem;
import obscurum.items.Equipment;
import obscurum.items.Inventory;
import obscurum.items.InventorySlot;
import obscurum.items.Item;
import obscurum.items.armour.*;
import obscurum.placeholders.NullEquipment;
import obscurum.screens.SubScreen;

/**
 * This models a player character.
 * @author Alex Ghita
 */
public class Player extends Creature {
  public static final int MAX_RANGE = 7; // for ranged weapons
  private boolean hasAmulet;
  private boolean hasWon;
  private boolean inSubScreen;
  private SubScreen subScreen;
  private Point screenLocation;
  private ArrayList<String> combatLog;
  private int experience;
  private int experienceToLevel;
  private int attributePoints;
  private Level[] world;
  private int enemiesKilled;
  private int deepestLevel;
  private int currentLevel;
  private boolean spawnedExit;
  private boolean godMode;

  /**
   * The class constructor. Unlike other creatures, the inventory is generated
   * here, since it is necessary throughout the game, and not when the player
   * has died.
   */
  public Player(Level level, Point location, int inventorySize,
      int lineOfSight, int health, int mana, int armour, int strength,
      int agility, int stamina, int spirit, int intellect) {
    super("Player", '@', AsciiPanel.brightWhite, level, location, 1,
        lineOfSight, inventorySize, MAX_RANGE, health, mana, armour, strength,
        agility, stamina, spirit, intellect);
    inSubScreen = false;
    combatLog = new ArrayList<String>();
    experience = 0;
    experienceToLevel = 10;
    attributePoints = 0;
    hasAmulet = false;
    spawnedExit = false;
    godMode = false;
    enemiesKilled = 0;
    deepestLevel = currentLevel = 1;
    generateInventory(inventorySize);
  }

  public boolean hasSpawnedExit() {
    return spawnedExit;
  }

  public void setSpawnedExit(boolean spawnedExit) {
    this.spawnedExit = spawnedExit;
  }

  public void setToGodMode() {
    if (godMode == false) {
      setInvulnerable(true);
      addMessageToCombatLog("God mode enabled.");
    } else {
      setInvulnerable(false);
      addMessageToCombatLog("God mode disabled.");
    }
    godMode = !godMode;
  }

  public int getEnemiesKilled() {
    return enemiesKilled;
  }

  public int getCurrentLevel() {
    return currentLevel;
  }

  public void setCurrentLevel(int currentLevel) {
    this.currentLevel = currentLevel;
    if (currentLevel > deepestLevel) {
      setDeepestLevel(currentLevel);
    }
  }

  public int getDeepestLevel() {
    return deepestLevel;
  }

  public void setDeepestLevel(int deepestLevel) {
    this.deepestLevel = deepestLevel;
  }

  public Point getScreenLocation() {
    return screenLocation;
  }

  public int getExperience() {
    return experience;
  }

  public int getExperienceToLevel() {
    return experienceToLevel;
  }

  public Level[] getWorld() {
    return world;
  }

  public SubScreen getSubScreen() {
    return subScreen;
  }

  public ArrayList<String> getCombatLog() {
    return combatLog;
  }

  public int getAttributePoints() {
    return attributePoints;
  }

  public void setInSubScreen(boolean inSubScreen) {
    this.inSubScreen = inSubScreen;
  }

  public void setSubScreen(SubScreen subScreen) {
    this.subScreen = subScreen;
  }

  public void setScreenLocation(Point screenLocation) {
    this.screenLocation = screenLocation;
  }

  public void setWorld(Level[] world) {
    this.world = world;
  }

  public void setAttributePoints(int attributePoints) {
    this.attributePoints = attributePoints;
  }

  public void levelUp() {
    experience -= experienceToLevel;
    attributes[Creature.POWER_LEVEL]++;
    baseAttributes[Creature.POWER_LEVEL]++;
    attributePoints++;
    experienceToLevel += experienceToLevel * 0.5;
    addMessageToCombatLog("Congratulations! You've reached level " +
        attributes[Creature.POWER_LEVEL] +
        ". You have received one attribute point.");
  }

  public void addExperience(int experience) {
    if (experience < 1) {
      return;
    }

    this.experience += experience;
    addMessageToCombatLog("You've gained " + experience + " XP.");
    while (this.experience >= experienceToLevel) {
      levelUp();
    }
  }

  public void addMessageToCombatLog(String message) {
    combatLog.add(message);
    while (combatLog.size() > 5) {
      combatLog.remove(0);
    }
  }

  public boolean isInSubScreen() {
    return inSubScreen;
  }

  public boolean hasAmulet() {
    return hasAmulet;
  }

  public boolean hasWon() {
    return hasWon;
  }

  public void setWin() {
    hasWon = true;
  }

  /**
   * Attempts to add all items contained in the given inventory slot to the
   * player's inventory, removes the number of successfully added items from
   * the target slot, and returns that number.
   * @param targetSlot
   * @return
   */
  public void loot(InventorySlot targetSlot) {
    int added = Math.min(targetSlot.getAmount(),
        inventory.getAddableAmount(targetSlot.getItem()));

    if (added > 0) {
      inventory.addItem(targetSlot.getItem(), added);
      if (targetSlot.getItem().getName().equals("Amulet of Power")) {
        addMessageToCombatLog("You have picked up the Amulet of Power! Go back  to the first level and find the exit portal!");
        addMessageToCombatLog("Your enemies grow stronger...");
        hasAmulet = true;
      }
      targetSlot.remove(added);
    }
  }

  public void equip(InventorySlot slot) {
    // Check for illegal arguments.
    if (slot == null) {
      throw new IllegalArgumentException("Inventory slot cannot be null.");
    }
    if (!(slot.getItem() instanceof Equipment)) {
      throw new IllegalArgumentException(
          "Inventory slot must contain an equippable item.");
    }

    // Swap the equipped item with the one in the slot.
    int slotIndex = ((Equipment)slot.getItem()).getSlot();
    Item oldItem = equipment.getEquipment(slotIndex);
    Item newItem = slot.getItem();

    slot.clear();
    if (!oldItem.isOfType(new NullEquipment())) {
      unequip(slotIndex);
    }
    equipment.setEquipment(newItem, slotIndex);

    // Update attributes.
    int[] itemAttributes = ((Equipment)newItem).getAttributes();
    for (int i = Creature.STRENGTH; i <= Creature.INTELLECT; i++) {
      attributes[i] += itemAttributes[i];
    }
    if (newItem instanceof Armour) {
      armour += ((Armour)newItem).getDefence();
    }
    computeSecondaryAttributes();
  }

  public void unequip(int slot) {
    if (inventory.getAddableAmount(equipment.getEquipment(slot)) > 0) {
      // Update attributes.
      int[] itemAttributes = equipment.getEquipment(slot).getAttributes();
      for (int i = 0; i < Creature.NUM_OF_ATTRIBUTES; i++) {
        attributes[i] -= itemAttributes[i];
      }
      if (equipment.getEquipment(slot) instanceof Armour) {
        armour -= ((Armour)equipment.getEquipment(slot)).getDefence();
      }
      computeSecondaryAttributes();
      loot(new InventorySlot(equipment.getEquipment(slot)));
      equipment.setEquipment(new NullEquipment(), slot);
    }
  }

  public void use(InventorySlot slot) {
    if (slot.getItem().getRequiredLevel() > attributes[Creature.POWER_LEVEL]) {
      return;
    }
    if (slot.getItem() instanceof Equipment) {
      equip(slot);
    } else if (slot.getItem() instanceof ConsumableItem) {
      ((ConsumableItem)slot.getItem()).use(this);
      slot.remove(1);
    }
  }

  @Override
  public void attackTarget() {
    // Prevent players from attacking themselves.
    if (target == this || target.isInvulnerable()) {
      return;
    }

    int initialHealth = target.getCurrentHealth();

    super.attackTarget();

    boolean dodged = target.getCurrentHealth() == initialHealth &&
        target.getCurrentHealth() != 0 ? true : false;
    String message = dodged ?
        "You tried to attack " + target.getName() + ". You missed." :
        "You've hit " + target.getName() + " for " +
        (initialHealth - target.getCurrentHealth()) + " damage. " + target.getName();
    if (dodged) {
      addMessageToCombatLog(message);
      return;
    }
    if (damageDealt + initialHealth - target.getCurrentHealth() <= Integer.MAX_VALUE) {
      damageDealt += initialHealth - target.getCurrentHealth();
    }

    if (godMode) {
      target.setCurrentHealth(0);
    }

    if (target.getCurrentHealth() == 0) {
      int targetLevel = target.getAttributes()[Creature.POWER_LEVEL];
      int levelDiff = attributes[POWER_LEVEL] - targetLevel;
      int xp = levelDiff > 5 ? 0 : levelDiff < -5 ?
          targetLevel * 10 + levelDiff * 5 :
          targetLevel * 5 + (5 - Math.abs(levelDiff)) * 3;
      message += " died.";
      addMessageToCombatLog(message);
      addExperience(xp);
      if (xp == 0) {
        addMessageToCombatLog(target.getName() +
            " was too weak for you. You gain no XP.");
      }
      if (enemiesKilled < Integer.MAX_VALUE) {
        enemiesKilled++;
      }
    } else {
      message += " has " + target.getCurrentHealth() + " health left.";
      addMessageToCombatLog(message);
    }
  }

  @Override
  public void castSpell(int spellIndex) {
    if (target.isInvulnerable()) {
      return;
    }

    boolean targetStatus = target.isAlive();
    super.castSpell(spellIndex);

    String message = "You've cast " + spells.get(spellIndex).getName() +
        " on " + target.getName() + ". " +
        spells.get(spellIndex).getEffectMessage(target);
    addMessageToCombatLog(message);
    if (targetStatus && !target.isAlive()) {
      int targetLevel = target.getAttributes()[Creature.POWER_LEVEL];
      int levelDiff = attributes[POWER_LEVEL] - targetLevel;
      int xp = levelDiff > 5 ? 0 : levelDiff < -5 ?
          targetLevel * 10 + levelDiff * 5 :
          targetLevel * 5 + (5 - Math.abs(levelDiff)) * 3;
      addExperience(xp);
      if (xp == 0) {
        addMessageToCombatLog(target.getName() +
            " was too weak for you. You gain no XP.");
      }
    }
  }

  /**
   * Generate an inventory of 16 slots. Not used, just implementing the
   * superclass method.
   */
  protected void generateInventory() {
    generateInventory(16);
  }

  /**
   * The player should start with an empty inventory of the given size.
   * @param size
   */
  private void generateInventory(int size) {
    inventory = new Inventory(size, this);
    // for (int i = 0; i < size; i++) {
    //   inventory.addItem(new Sword());
    // }
  }

  @Override
  public void powerUp() {}
}

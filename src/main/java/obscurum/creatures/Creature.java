package obscurum.creatures;

import java.awt.Color;
import java.awt.Point;
import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import obscurum.creatures.abilities.Spell;
import obscurum.creatures.ai.CorpseAI;
import obscurum.creatures.ai.CreatureAI;
import obscurum.creatures.util.EquipmentList;
import obscurum.util.Line;
import obscurum.environment.Level;
import obscurum.environment.background.BackgroundTile;
import obscurum.environment.foreground.EmptyTile;
import obscurum.environment.foreground.ForegroundTile;
import obscurum.items.Equipment;
import obscurum.items.Inventory;
import obscurum.items.weapons.Weapon;
import obscurum.placeholders.NullCreature;
import obscurum.placeholders.NullEquipment;
import obscurum.placeholders.NullLevel;

/**
 * This models a creature, i.e. a foreground tile that can interact with other
 * tiles.
 * @author Alex Ghita
 */
public abstract class Creature extends ForegroundTile {
  // Creature attribute indices in the attributes arrays.
  public static final int NUM_OF_ATTRIBUTES = 11;
  public static final int POWER_LEVEL = 0;
  public static final int LINE_OF_SIGHT = 1;
  public static final int INVENTORY_SIZE = 2;
  public static final int STRENGTH = 3;
  public static final int AGILITY = 4;
  public static final int STAMINA = 5;
  public static final int SPIRIT = 6;
  public static final int INTELLECT = 7;
  public static final int ATTACK_POWER = 8;
  public static final int SPELL_POWER = 9;
  public static final int DODGE_CHANCE = 10;
  // Maximum values for attributes.
  public static final int MAX_POWER_LEVEL = 100;
  public static final int MAX_BASIC_ATTRIBUTE = 300;
  public static final int MAX_LINE_OF_SIGHT = 15;
  public static final int MAX_INVENTORY_SIZE = 54;
  public static final int MAX_DODGE_CHANCE = 30;
  public static final int COMBAT_COOLDOWN = 20;
  protected boolean alive;
  protected Level level;
  protected Point location;
  protected Inventory inventory;
  protected CreatureAI ai;
  protected HashMap<Level, Level> exploration;
  protected int[] baseAttributes;
  protected int[] attributes;
  protected int mana;
  protected int baseMana;
  protected int maxMana;
  protected int combatCooldown;
  protected int attackRange;
  protected Creature target;
  protected ArrayList<Spell> spells;
  protected EquipmentList equipment;
  protected Line attackTrajectory;
  protected int turnsAlive;
  protected int turnsAliveNearPlayer;
  protected int damageDealt;
  protected int timesAttacked;
  protected String previousAIName;

  /**
   * Class constructor specifying the creature's characteristics and location.
   */
  public Creature(String name, char glyph, Color foregroundColour, Level level,
      Point location, int powerLevel, int lineOfSight, int inventorySize,
      int attackRange, int baseHealth, int baseMana, int armour, int strength,
      int agility, int stamina, int spirit, int intellect) {
    super(name, glyph, foregroundColour, baseHealth, armour, false, false);
    // Check for illegal arguments.
    if (baseMana < 0) {
      throw new IllegalArgumentException("Base mana " + mana +
          " cannot be negative.");
    }
    if (level == null) {
      throw new IllegalArgumentException("Level cannot be null.");
    }
    if (location == null) {
      throw new IllegalArgumentException("Location cannot be null.");
    }
    if (powerLevel < 1) {
      throw new IllegalArgumentException("Power level " + powerLevel +
          " must be at least 1.");
    }
    if (lineOfSight < 0) {
      throw new IllegalArgumentException("Line of sight " + lineOfSight +
          " cannot be negative.");
    }
    if (strength < 0) {
      throw new IllegalArgumentException("Strength " + strength +
          " cannot be negative.");
    }
    if (agility < 0) {
      throw new IllegalArgumentException("Agility " + agility +
          " cannot be negative.");
    }
    if (stamina < 0) {
      throw new IllegalArgumentException("Stamina " + stamina +
          " cannot be negative.");
    }
    if (spirit < 0) {
      throw new IllegalArgumentException("Spirit " + spirit +
          " cannot be negative.");
    }
    if (intellect < 0) {
      throw new IllegalArgumentException("Intellect " + intellect +
          " cannot be negative.");
    }

    alive = true;
    this.baseMana = baseMana;
    this.maxMana = baseMana;
    this.level = level;
    this.location = location;
    // Place the creature in its designated location.
    this.level.setForegroundTile(this.location, this);

    // Add the exploration level corresponding to the creature's level.
    exploration = new HashMap<Level, Level>();
    exploration.put(this.level, new Level(
        new ForegroundTile[this.level.getWidth()][this.level.getHeight()],
        new BackgroundTile[this.level.getWidth()][this.level.getHeight()]));

    baseAttributes = new int[NUM_OF_ATTRIBUTES];
    attributes = new int[NUM_OF_ATTRIBUTES];

    baseAttributes[POWER_LEVEL] = attributes[POWER_LEVEL] = powerLevel;
    baseAttributes[LINE_OF_SIGHT] = attributes[LINE_OF_SIGHT] = lineOfSight;
    baseAttributes[INVENTORY_SIZE] = attributes[INVENTORY_SIZE] = inventorySize;
    baseAttributes[STRENGTH] = attributes[STRENGTH] = strength;
    baseAttributes[AGILITY] = attributes[AGILITY] = agility;
    baseAttributes[STAMINA] = attributes[STAMINA] = stamina;
    baseAttributes[SPIRIT] = attributes[SPIRIT] = spirit;
    baseAttributes[INTELLECT] = attributes[INTELLECT] = intellect;
    baseAttributes[ATTACK_POWER] = baseAttributes[STRENGTH];
    baseAttributes[DODGE_CHANCE] = Math.min(MAX_DODGE_CHANCE,
        baseAttributes[AGILITY] / 10);
    computeSecondaryAttributes();
    this.currentHealth = this.maxHealth;
    this.mana = this.maxMana;
    this.attackRange = attackRange;

    combatCooldown = 0;

    if (!name.equals("Null Creature")) {
      target = new NullCreature();
    }
    spells = new ArrayList<Spell>();
    equipment = new EquipmentList();
    attackTrajectory = null;

    turnsAlive = 0;
    turnsAliveNearPlayer = 0;
    damageDealt = 0;
    timesAttacked = 0;
  }

  public int getTurnsAlive() {
    return turnsAlive;
  }

  public int getTurnsAliveNearPlayer() {
    return turnsAliveNearPlayer;
  }

  public int getDamageDealt() {
    return damageDealt;
  }

  public int getTimesAttacked() {
    return timesAttacked;
  }

  public int getCombatCooldown() {
    return combatCooldown;
  }

  public int getMana() {
    return mana;
  }

  public int getBaseMana() {
    return baseMana;
  }

  public int getMaxMana() {
    return maxMana;
  }

  public int getAttackRange() {
    return attackRange;
  }

  public String getPreviousAIName() {
    return previousAIName;
  }

  public Creature getTarget() {
    return target;
  }

  /**
   * Gets the creature's status.
   * @return
   */
  public boolean isAlive() {
    return alive;
  }

  /**
   * Gets the level in which the creature currently exists.
   * @return
   */
  public Level getLevel() {
    return level;
  }

  /**
   * Gets the creature's location in its current level.
   * @return
   */
  public Point getLocation() {
    return location;
  }

  /**
   * Gets the creature's inventory.
   * @return
   */
  public Inventory getInventory() {
    return inventory;
  }

  /**
   * Gets the creature's AI.
   * @return
   */
  public CreatureAI getAI() {
    return ai;
  }

  /**
   * Gets a level that represents the level in which the creature is located,
   * as explored by it.
   * @return
   */
  public Level getExploration() {
    return exploration.get(level);
  }

  public int[] getAttributes() {
    return attributes;
  }

  public int[] getBaseAttributes() {
    return baseAttributes;
  }

  public EquipmentList getEquipment() {
    return equipment;
  }

  public ArrayList<Spell> getSpells() {
    return spells;
  }

  public Line getAttackTrajectory() {
    return attackTrajectory;
  }

  public void setAttribute(int index, int value) {
    attributes[index] = value;
  }

  public void setBaseAttribute(int index, int value) {
    int difference = value - baseAttributes[index];
    baseAttributes[index] = value;
    attributes[index] += difference;
    computeSecondaryAttributes();
  }

  /**
   * Sets the creature's AI.
   * @param ai
   */
  public void setAI(CreatureAI ai) {
    this.ai = ai;
  }

  /**
   * Sets the level in which the creature can be found.
   * @param level
   */
  public void setLevel(Level level) {
    this.level = level;
  }

  /**
   * Sets the creature's location in its current level.
   * @param location
   */
  public void setLocation(Point location) {
    this.location = location;
  }

  /**
   * Sets the exploration of the current level.
   * @param newExploration
   */
  public void setExploration(Level newExploration) {
    if (newExploration == null || newExploration instanceof NullLevel) {
      throw new IllegalArgumentException(
          "New exploration level cannot be null");
    }
    exploration.put(level, newExploration);
  }

  public void setTarget(Creature target) {
    this.target = target;
  }

  public void setAttackTrajectory(Line attackTrajectory) {
    this.attackTrajectory = attackTrajectory;
  }

  /**
   * Adds an exploration for a new level. This should be used primarily for
   * players when travelling between levels.
   * @param level
   */
  public void addExploration(Level level) {
    if (!exploration.containsKey(level)) {
      exploration.put(level, new Level(
          new ForegroundTile[level.getWidth()][level.getHeight()],
          new BackgroundTile[level.getWidth()][level.getHeight()]));
    }
  }

  public void decrementCombatCooldown() {
    combatCooldown = Math.max(0, combatCooldown - 1);
  }

  public void resetCombatCooldown() {
    combatCooldown = COMBAT_COOLDOWN;
  }

  public void regenerate() {
    currentHealth = Math.min(maxHealth, currentHealth + 1 + attributes[SPIRIT] / 2);
    mana = Math.min(maxMana, mana + 1 + attributes[SPIRIT] / 2);
  }

  /**
   * Moves the creature from its current location to a new one, clearing the
   * previously occupied tile.
   * @param x
   * @param y
   */
  public void move(int x, int y) {
    move(new Point(x, y));
  }

  /**
   * Moves the creature from its current location to a new one, clearing the
   * previously occupied tile.
   * @param location
   */
  public void move(Point location) {
    level.setForegroundTile(this.location, new EmptyTile());
    level.setForegroundTile(location, this);
    this.location = location;
  }

  /**
   * Moves the creatures by the given amounts on the x and y axes.
   * @param x
   * @param y
   */
  public void moveBy(int x, int y) {
    moveBy(new Point(x, y));
  }

  /**
   * Moves the creature by the given amounts on the x and y axes.
   * @param p
   */
  public void moveBy(Point p) {
    ai.onEnter(new Point(p.x + location.x, p.y + location.y));
  }

  public void setCurrentHealth(int currentHealth) {
    super.setCurrentHealth(currentHealth);
    if (currentHealth == 0) {
      die();
    }
  }

  /**
   * Triggers the creature's AI response to the passing of another game turn.
   */
  public void update() {
    ai.onUpdate();
    if (alive) {
      if (turnsAlive < Integer.MAX_VALUE) {
        turnsAlive++;
      }
      if (!name.equals("Player")) {
        ArrayList<Point> seenPoints = ai.getSeenPoints();
        for (Point p : seenPoints) {
          // Check if it's the player.
          if (level.getForegroundTile(p).getName().equals("Player")) {
            if (turnsAliveNearPlayer < Integer.MAX_VALUE) {
              turnsAliveNearPlayer++;
            }
            break;
          }
        }
      }
    }
  }

  /**
   * Kills the creature. This changes its glyph to the standard corpse glyph,
   * '%', changes its AI to one that does nothing and also generates its
   * lootable inventory.
   */
  public void die() {
    alive = false;
    invulnerable = true;
    glyph = '%';
    previousAIName = ai.getName();
    ai = new CorpseAI(this);
    inventory = new Inventory(attributes[INVENTORY_SIZE], this);
    generateInventory();
  }

  public boolean canCastSpell(int spellIndex) {
    if (spells.get(spellIndex).inCooldown() ||
        spells.get(spellIndex).getManaCost() > mana ||
        spells.get(spellIndex).getSpellType() == Spell.REST_SPELL &&
        getCombatCooldown() > 0) {
      return false;
    }
    return true;
  }

  public void castSpell(int spellIndex) {
    if (target.isInvulnerable()) {
      return;
    }
    spells.get(spellIndex).cast(target, attributes[SPELL_POWER]);
    mana -= spells.get(spellIndex).getManaCost();
    if (spells.get(spellIndex).getSpellType() == Spell.COMBAT_SPELL) {
      resetCombatCooldown();
      target.resetCombatCooldown();
    }
  }

  public void attackTarget() {
    if (target.isInvulnerable()) {
      return;
    }

    int damage = 0;
    boolean dodged = Math.random() <
        (double)target.getAttributes()[Creature.DODGE_CHANCE] / 100.0 ?
        true : false;
    if (!dodged) {
      if (equipment.getEquipment(Equipment.WEAPON).isOfType(
          new NullEquipment())) {
        damage = attributes[Creature.ATTACK_POWER] / 2;
      } else {
        damage = ((Weapon)equipment.getEquipment(Equipment.WEAPON)).dealDamage()
            + attributes[Creature.ATTACK_POWER];
      }
      damage = Math.min(target.getCurrentHealth(),
          Math.max(1, damage - target.getArmour() / (target.getAttributes()[Creature.POWER_LEVEL] + 2)));

      target.setCurrentHealth(target.getCurrentHealth() - damage);
    }

    target.resetCombatCooldown();
    resetCombatCooldown();

    if (target.getName().equals("Player")) {
      String message = dodged ?
          name + " tried to hit you. You dodged the attack." :
          name + " has hit you for " + damage + " damage.";
      ((Player)target).addMessageToCombatLog(message);

      if (attackRange > 1) {
        attackTrajectory = new Line(location, target.getLocation());
        attackTrajectory.plotLine();
      }
      if (damageDealt + damage <= Integer.MAX_VALUE) {
        damageDealt += damage;
      }
      if (timesAttacked < Integer.MAX_VALUE) {
        timesAttacked++;
      }
    }

    if (target.getTarget().isOfType(new NullCreature()) &&
        target.getAI().canSee(this.getLocation())) {
      target.setTarget(this);
    }
  }

  public void learnSpell(Spell spell) {
    for (Spell s : spells) {
      if (spell.isOfType(s)) {
        s.levelUp();
        return;
      }
    }
    spells.add(spell);
  }

  protected void computeSecondaryAttributes() {
    attributes[ATTACK_POWER] = attributes[STRENGTH];
    baseAttributes[ATTACK_POWER] = baseAttributes[STRENGTH];
    attributes[SPELL_POWER] = attributes[INTELLECT];
    baseAttributes[SPELL_POWER] = baseAttributes[INTELLECT];
    attributes[DODGE_CHANCE] = Math.min(MAX_DODGE_CHANCE,
        attributes[AGILITY] / 5);
    baseAttributes[DODGE_CHANCE] = Math.min(MAX_DODGE_CHANCE,
        baseAttributes[AGILITY] / 5);
    maxHealth = baseHealth + attributes[STAMINA] * 2;
    maxMana = baseMana + attributes[INTELLECT] * 2;
    currentHealth = Math.min(currentHealth, maxHealth);
    mana = Math.min(mana, maxMana);
  }

  // To be used when picking up the amulet on all other mobs.
  public abstract void powerUp();

  /**
   * Generate a creature's inventory. To be used upon death. This should
   * generate a randomised list of items specific to the creature's type.
   */
  protected abstract void generateInventory();
}

package obscurum.environment.foreground;

import java.awt.Color;

import lombok.Getter;
import lombok.Setter;
import obscurum.environment.Tile;

@Getter
public abstract class ForegroundTile extends Tile {
    protected int currentHealth;
    protected int baseHealth;
    protected int maxHealth;
    protected int armour;
    @Setter protected boolean invulnerable;
    @Setter protected boolean opaque;

    public ForegroundTile(String name, char glyph, Color foregroundColour, int baseHealth, int armour, boolean invulnerable, boolean opaque) {
        super(name, glyph, foregroundColour);

        if (baseHealth < 1) {
            throw new IllegalArgumentException(String.format("Base health %d must be at least 1.", baseHealth));
        }

        this.currentHealth = baseHealth;
        this.baseHealth = baseHealth;
        this.maxHealth = baseHealth;
        setArmour(armour);
        this.invulnerable = invulnerable;
        this.opaque = opaque;
    }

    public void setCurrentHealth(int currentHealth) {
        if (currentHealth < 0) {
            throw new IllegalArgumentException(String.format("Health %d must not be negative.", currentHealth));
        }
        this.currentHealth = currentHealth;
    }

    public void setArmour(int armour) {
        if (armour < 0) {
            throw new IllegalArgumentException(String.format("Armour %d must be non-negative.", armour));
        }
        this.armour = armour;
    }
}

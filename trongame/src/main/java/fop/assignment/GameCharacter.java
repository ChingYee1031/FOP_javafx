package fop.assignment;

public abstract class GameCharacter {
    protected String name;
    protected String color;
    protected double lives;
    protected double speed;
    protected int x, y;
    
    // --- NEW: Ammo System ---
    protected int maxDiscSlots = 1; 
    protected int currentDiscSlots = 1;

    public GameCharacter(String name, String color, double lives, double speed) {
        this.name = name;
        this.color = color;
        this.lives = lives;
        this.speed = speed;
        // Default start (Will be overridden by Player logic)
        this.maxDiscSlots = 1;
        this.currentDiscSlots = 1;
    }

    // --- MOVEMENT ---
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    // --- COMBAT & STATS ---
    public void reduceLives(double amount) {
        this.lives -= amount;
        if (this.lives < 0) this.lives = 0;
    }

    public boolean isAlive() {
        return lives > 0;
    }

    // --- NEW: AMMO METHODS ---
    public boolean hasAmmo() {
        return currentDiscSlots > 0;
    }

    public void useAmmo() {
        if (currentDiscSlots > 0) currentDiscSlots--;
    }

    public void recoverAmmo() {
        if (currentDiscSlots < maxDiscSlots) currentDiscSlots++;
    }

    public int getCurrentAmmo() { return currentDiscSlots; }
    public int getMaxAmmo() { return maxDiscSlots; }

    // --- GETTERS & SETTERS ---
    public String getName() { return name; }
    public String getColor() { return color; }
    public double getLives() { return lives; }
    public double getSpeed() { return speed; }
}
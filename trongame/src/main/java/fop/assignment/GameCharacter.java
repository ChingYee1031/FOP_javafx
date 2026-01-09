package fop.assignment;

public abstract class GameCharacter {
    protected String name;
    protected String color;
    protected double lives;
    protected double speed;
    protected int x, y;
    
    // RENAMED: Use "DiscSlots" instead of generic Ammo terms
    protected int maxDiscSlots = 1; 
    protected int currentDiscSlots = 1;
    
    // Cooldown Tracking
    protected long lastThrowTime = 0;
    protected static final long COOLDOWN_MS = 5000; // 5 Seconds

    public GameCharacter(String name, String color, double lives, double speed) {
        this.name = name;
        this.color = color;
        this.lives = lives;
        this.speed = speed;
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

    // --- RENAMED METHODS (Was "Ammo") ---

    // 1. Check if we have discs available
    public boolean hasDisc() { // Was hasAmmo()
        return currentDiscSlots > 0;
    }

    // 2. Throw a disc (Was useAmmo)
    public void useDisc() { 
        if (currentDiscSlots > 0) currentDiscSlots--;
    }

    // 3. Catch a disc (Was recoverAmmo)
    public void recoverDisc() {
        if (currentDiscSlots < maxDiscSlots) {
            currentDiscSlots++;
            // Picking up a disc instantly resets the cooldown
            this.lastThrowTime = 0; 
        }
    }

    // 4. NEW: Fix for the bug (Start level with full discs)
    public void refillDiscSlots() {
        this.currentDiscSlots = this.maxDiscSlots;
        this.lastThrowTime = 0; 
    }

    public boolean isCooldownReady() {
        return System.currentTimeMillis() - lastThrowTime >= COOLDOWN_MS;
    }
    
    public void resetCooldown() {
        lastThrowTime = System.currentTimeMillis();
    }
    
    public long getCooldownRemaining() {
        long diff = COOLDOWN_MS - (System.currentTimeMillis() - lastThrowTime);
        return (diff < 0) ? 0 : diff;
    }

    // --- RENAMED GETTERS ---
    public int getCurrentDiscSlots() { return currentDiscSlots; } // Was getCurrentAmmo
    public int getMaxDiscSlots() { return maxDiscSlots; }         // Was getMaxAmmo

    // --- STANDARD GETTERS ---
    public String getName() { return name; }
    public String getColor() { return color; }
    public double getLives() { return lives; }
    public double getSpeed() { return speed; }
}
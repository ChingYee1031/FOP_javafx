package fop.assignment;

public abstract class GameCharacter {
    protected String name;
    protected String color;
    protected double lives;
    protected double speed;
    protected int x, y; 
    protected boolean isAlive = true;
    
    // Fix for "currentDir" error in Enemy.java
    // 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT, -1=NONE
    protected int currentDir = -1; 

    public GameCharacter(String name, String color, double lives, double speed) {
        this.name = name;
        this.color = color;
        this.lives = lives;
        this.speed = speed;
    }

    // --- METHODS TO FIX ARENA CONTROLLER ERRORS ---

    public String getName() {
        return name;
    }

    public double getLives() {
        return lives;
    }

    public double getSpeed() {
        return speed;
    }

    public String getColor() {
        return color;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    
    public void setPosition(int x, int y) { 
        this.x = x; 
        this.y = y; 
    }

    public boolean isAlive() { 
        return lives > 0; 
    }

    public void reduceLives(double amount) {
        this.lives -= amount;
        if (this.lives <= 0) {
            this.lives = 0;
            this.isAlive = false;
        }
    }
}
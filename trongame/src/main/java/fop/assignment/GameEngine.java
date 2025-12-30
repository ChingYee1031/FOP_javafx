package fop.assignment;

public class GameEngine {
    public static int[] getNextPosition(int x, int y, String direction) {
        int nextX = x;
        int nextY = y;

        switch (direction) {
            case "UP":    nextY--; break;
            case "DOWN":  nextY++; break;
            case "LEFT":  nextX--; break;
            case "RIGHT": nextX++; break;
        }
        return new int[]{nextX, nextY};
    }

    public static boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < 40 && y >= 0 && y < 40;
    }
}
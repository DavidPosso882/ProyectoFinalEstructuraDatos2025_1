package org.example.model;

/**
 * Representa los diferentes rangos que puede tener un usuario basado en sus puntos acumulados.
 */
public enum UserRank {
    BRONZE(0, 499, "Bronce"),
    SILVER(500, 999, "Plata"),
    GOLD(1000, 4999, "Oro"),
    PLATINUM(5000, Integer.MAX_VALUE, "Platino");

    private final int minPoints;
    private final int maxPoints;
    private final String description;

    UserRank(int minPoints, int maxPoints, String description) {
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.description = description;
    }

    public int getMinPoints() {
        return minPoints;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Determina el rango basado en la cantidad de puntos.
     *
     * @param points Cantidad de puntos acumulados
     * @return El rango correspondiente
     */
    public static UserRank getRankByPoints(int points) {
        for (UserRank rank : UserRank.values()) {
            if (points >= rank.getMinPoints() && points <= rank.getMaxPoints()) {
                return rank;
            }
        }
        return BRONZE; // Default
    }
}

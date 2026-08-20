package com.lounge.domain.diagnosis.scoring;

public record AxisWeights(
        int storage,
        int versatility,
        int travel,
        int commute,
        int laptop,
        int cabin
) {

    public static final AxisWeights ZERO = new AxisWeights(0, 0, 0, 0, 0, 0);

    public AxisWeights plus(AxisWeights other) {
        return new AxisWeights(
                storage + other.storage,
                versatility + other.versatility,
                travel + other.travel,
                commute + other.commute,
                laptop + other.laptop,
                cabin + other.cabin
        );
    }

    public int sum() {
        return storage + versatility + travel + commute + laptop + cabin;
    }
}

package ru.practicum.ewm.stats.analyzer.dal.entity;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;

public enum ActionTypeWeight {
    VIEW(0.4),
    REGISTER(0.8),
    LIKE(1.0);

    private final double weight;

    ActionTypeWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public static double fromAvro(ActionTypeAvro avro) {
        return valueOf(avro.name()).getWeight();
    }
}

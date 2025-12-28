package ru.practicum.ewm.stats.analyzer.dal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "similarities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event1", "event2"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Similarity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long event1;
    private long event2;
    private double similarity;

    @Column(name = "ts", nullable = false)
    private Instant timestamp;
}

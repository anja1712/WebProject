package com.bank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import javax.validation.constraints.NotNull;

import com.bank.utils.Constants;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Access(AccessType.FIELD)
@Table(name = "goal", schema = Constants.DB_SCHEMA)
public class Goal extends AbstractEntity {
    @Id
    @NotNull
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name")
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "current_amount")
    private BigDecimal currentAmount;

    @NotNull
    @Column(name = "goal_amount")
    private BigDecimal goalAmount;

    @NotNull
    @Column(name = "start_date")
    private Instant startDate;

    @NotNull
    @Column(name = "target_date")
    private Instant targetDate;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;


    public BigDecimal calculatePercentReached() {
        if (goalAmount == null || goalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentAmount
                .divide(goalAmount, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}

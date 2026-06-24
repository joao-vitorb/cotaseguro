package com.cotaseguro.service;

import com.cotaseguro.domain.InsuranceType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PremiumCalculator {

    private static final Map<InsuranceType, BigDecimal> BASE_RATES = Map.of(
            InsuranceType.AUTO, new BigDecimal("0.05"),
            InsuranceType.LIFE, new BigDecimal("0.03"),
            InsuranceType.HOME, new BigDecimal("0.02"),
            InsuranceType.TRAVEL, new BigDecimal("0.04"));

    private static final int YOUNG_MAX_AGE = 25;
    private static final int SENIOR_MIN_AGE = 60;

    private static final BigDecimal YOUNG_FACTOR = new BigDecimal("1.30");
    private static final BigDecimal STANDARD_FACTOR = BigDecimal.ONE;
    private static final BigDecimal SENIOR_FACTOR = new BigDecimal("1.50");

    public BigDecimal calculate(InsuranceType insuranceType, BigDecimal coverageAmount, LocalDate birthDate) {
        BigDecimal baseRate = BASE_RATES.get(insuranceType);
        BigDecimal ageFactor = ageFactor(birthDate);

        return coverageAmount
                .multiply(baseRate)
                .multiply(ageFactor)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal ageFactor(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();

        if (age < YOUNG_MAX_AGE) {
            return YOUNG_FACTOR;
        }
        if (age > SENIOR_MIN_AGE) {
            return SENIOR_FACTOR;
        }
        return STANDARD_FACTOR;
    }

}

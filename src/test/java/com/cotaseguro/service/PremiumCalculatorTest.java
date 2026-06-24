package com.cotaseguro.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.cotaseguro.domain.InsuranceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PremiumCalculatorTest {

    private final PremiumCalculator premiumCalculator = new PremiumCalculator();

    private LocalDate ageOf(int years) {
        return LocalDate.now().minusYears(years);
    }

    @Test
    void appliesStandardFactorForAdult() {
        BigDecimal premium = premiumCalculator.calculate(
                InsuranceType.AUTO, new BigDecimal("50000.00"), ageOf(30));

        assertThat(premium).isEqualByComparingTo("2500.00");
    }

    @Test
    void appliesYoungFactorForUnderTwentyFive() {
        BigDecimal premium = premiumCalculator.calculate(
                InsuranceType.AUTO, new BigDecimal("50000.00"), ageOf(20));

        assertThat(premium).isEqualByComparingTo("3250.00");
    }

    @Test
    void appliesSeniorFactorForOverSixty() {
        BigDecimal premium = premiumCalculator.calculate(
                InsuranceType.LIFE, new BigDecimal("100000.00"), ageOf(70));

        assertThat(premium).isEqualByComparingTo("4500.00");
    }

    @Test
    void usesInsuranceTypeBaseRate() {
        BigDecimal premium = premiumCalculator.calculate(
                InsuranceType.HOME, new BigDecimal("200000.00"), ageOf(40));

        assertThat(premium).isEqualByComparingTo("4000.00");
    }

}

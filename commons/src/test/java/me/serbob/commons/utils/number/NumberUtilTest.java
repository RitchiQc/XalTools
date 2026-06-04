package me.serbob.commons.utils.number;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class NumberUtilTest {

    private static Locale originalLocale;

    @BeforeAll
    static void setUpLocale() {
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @AfterAll
    static void tearDownLocale() {
        Locale.setDefault(originalLocale);
    }


    @ParameterizedTest
    @CsvSource({
        "500, 500",
        "1500, 1.5K",
        "1000000, 1M",
        "2500000000, 2.5B",
        "1000000000000, 1T"
    })
    void formatPrice_variousInputs(long input, String expected) {
        assertEquals(expected, NumberUtil.formatPrice(input));
    }

    @Test
    void formatPrice_decimalBelowThousand() {
        assertEquals("500.5", NumberUtil.formatPrice(500.5));
    }

    @ParameterizedTest
    @CsvSource({
        "500, 500",
        "1500, 1.5K",
        "1000000, 1M",
        "2500000000, 2.5B",
        "1000000000000, 1T"
    })
    void formatNumber_variousInputs(long input, String expected) {
        assertEquals(expected, NumberUtil.formatNumber(input));
    }

    @Test
    void formatNumber_negative() {
        assertEquals("-1.5K", NumberUtil.formatNumber(-1500));
    }

    @ParameterizedTest
    @CsvSource({
        "'1k', 1000",
        "'2.5M', 2500000",
        "'1B', 1000000000",
        "'1,000', 1000",
        "'invalid', -1",
        "'', -1",
        "'1.5T', 1500000000000"
    })
    void parseAmount_variousInputs(String input, long expected) {
        assertEquals(expected, NumberUtil.parseAmount(input));
    }

    @Test
    void parseAmountInt_withinRange() {
        assertEquals(1000, NumberUtil.parseAmountInt("1k"));
    }

    @Test
    void parseAmountInt_overflowReturnsMax() {
        assertEquals(Integer.MAX_VALUE, NumberUtil.parseAmountInt("999999999999999T"));
    }
}

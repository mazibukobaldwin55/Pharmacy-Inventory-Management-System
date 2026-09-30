package com.healthfirst.pims.util;

import java.math.BigDecimal;
import java.util.Locale;

//Formats amounts the same way everywhere
public final class Money {

    private Money() { }

    public static String format(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        return String.format(Locale.US, "R %,.2f", value);
    }
}

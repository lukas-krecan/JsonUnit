package net.javacrumbs.jsonunit.core.internal;

import java.math.BigDecimal;
import net.javacrumbs.jsonunit.core.NumberComparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultNumberComparator implements NumberComparator {
    @Override
    public boolean compare(
            @NotNull BigDecimal expectedValue, @NotNull BigDecimal actualValue, @Nullable BigDecimal tolerance) {
        if (tolerance != null) {
            BigDecimal diff = expectedValue.subtract(actualValue).abs();
            return diff.compareTo(tolerance) <= 0;
        } else {
            return expectedValue.equals(actualValue);
        }
    }
}

package gregtech.api.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;

import net.minecraftforge.fluids.FluidStack;

/**
 * Полный аналог NumberFormatUtil из gtnhlib.
 * Содержит логику форматирования чисел и единицы измерения.
 */
public final class NumberFormatUtil {

    // ========================= КОНСТАНТЫ =========================
    private static final BigDecimal BD_THOUSAND = BigDecimal.valueOf(1000);
    private static final BigDecimal BD_MILLION = BigDecimal.valueOf(1_000_000);
    private static final BigDecimal BD_BILLION = BigDecimal.valueOf(1_000_000_000);
    private static final BigDecimal BD_TRILLION = BigDecimal.valueOf(1_000_000_000_000L);
    private static final BigDecimal BD_QUADRILLION = BigDecimal.valueOf(1_000_000_000_000_000L);

    // ========================= НАСТРОЙКИ =========================
    private static boolean useForgeFluidMillibuckets = false;

    // Порог, после которого числа становятся экспоненциальными (по умолчанию 1 Триллион)
    private static final BigDecimal EXPONENTIAL_THRESHOLD = BD_TRILLION;

    // ========================= КОНСТРУКТОР =========================
    private NumberFormatUtil() {}

    // ========================= ЕДИНИЦЫ ИЗМЕРЕНИЯ =========================

    public static String getFluidUnit() {
        return useForgeFluidMillibuckets ? "mB" : "L";
    }

    public static String getEnergyUnit() {
        return "EU";
    }

    // ========================= ФОРМАТИРОВАНИЕ ЖИДКОСТЕЙ =========================

    public static String formatFluid(Number value) {
        return formatNumber(value) + " " + getFluidUnit();
    }

    public static String formatFluidCompact(Number value) {
        return formatNumberCompact(value) + " " + getFluidUnit();
    }

    public static String formatFluid(FluidStack stack) {
        if (stack == null) return "0 " + getFluidUnit();
        return formatFluid(stack.amount);
    }

    // ========================= ФОРМАТИРОВАНИЕ ЭНЕРГИИ =========================

    public static String formatEnergy(Number value) {
        return formatNumber(value) + " " + getEnergyUnit();
    }

    public static String formatEnergyCompact(Number value) {
        return formatNumberCompact(value) + " " + getEnergyUnit();
    }

    // ========================= ОСНОВНОЕ ФОРМАТИРОВАНИЕ ЧИСЕЛ =========================

    /**
     * Стандартное форматирование с разделителями разрядов.
     * Числа >= 1T выводятся в экспоненциальном формате.
     */
    public static String formatNumber(Number value) {
        if (value == null) return "0";

        String special = handleSpecialCases(value);
        if (special != null) return special;

        BigDecimal val = toBigDecimal(value);
        BigDecimal abs = val.abs();

        if (abs.signum() == 0) return "0";

        // Если число больше триллиона — экспоненциальный формат
        if (abs.compareTo(EXPONENTIAL_THRESHOLD) >= 0) {
            return formatExponential(val);
        }

        // Обычное форматирование с разделителями разрядов
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
        df.setGroupingUsed(true);
        df.setMaximumFractionDigits(2);

        return centralFormatter(df.format(val));
    }

    /**
     * Компактное форматирование (K, M, B, T, Q).
     * Числа < 1000 — обычный формат, >= 1T — экспоненциальный, между — сокращённый.
     */
    public static String formatNumberCompact(Number value) {
        if (value == null) return "0";

        String special = handleSpecialCases(value);
        if (special != null) return special;

        BigDecimal val = toBigDecimal(value);
        BigDecimal abs = val.abs();

        if (abs.signum() == 0) return "0";

        // Если число меньше 1000, выводим как есть
        if (abs.compareTo(BD_THOUSAND) < 0) {
            return formatNumber(value);
        }

        // Если число больше триллиона, выводим экспоненциальный формат
        if (abs.compareTo(EXPONENTIAL_THRESHOLD) >= 0) {
            return formatExponential(val);
        }

        return abbreviate(val);
    }

    // ========================= ВНУТРЕННЯЯ ЛОГИКА =========================

    private static String abbreviate(BigDecimal value) {
        BigDecimal abs = value.abs();
        BigDecimal divisor;
        String suffix;

        if (abs.compareTo(BD_QUADRILLION) >= 0) {
            divisor = BD_QUADRILLION;
            suffix = "Q";
        } else if (abs.compareTo(BD_TRILLION) >= 0) {
            divisor = BD_TRILLION;
            suffix = "T";
        } else if (abs.compareTo(BD_BILLION) >= 0) {
            divisor = BD_BILLION;
            suffix = "B";
        } else if (abs.compareTo(BD_MILLION) >= 0) {
            divisor = BD_MILLION;
            suffix = "M";
        } else {
            divisor = BD_THOUSAND;
            suffix = "K";
        }

        BigDecimal scaled = value.divide(divisor, 2, RoundingMode.HALF_UP);

        // Предотвращаем переход через разряд из-за округления (999.5K → 1000K не должен случиться)
        BigDecimal nextThreshold = divisor.multiply(BD_THOUSAND);
        if (abs.compareTo(nextThreshold) < 0 && scaled.abs()
            .compareTo(BD_THOUSAND) >= 0) {
            BigDecimal step = BigDecimal.ONE.scaleByPowerOfTen(-2); // 0.01
            BigDecimal maxAbs = BD_THOUSAND.subtract(step)
                .setScale(2, RoundingMode.UNNECESSARY);
            scaled = (scaled.signum() < 0) ? maxAbs.negate() : maxAbs;
        }

        String result = scaled.stripTrailingZeros()
            .toPlainString();

        return result + suffix;
    }

    private static String formatExponential(BigDecimal value) {
        int precision = 3;
        BigDecimal rounded = value.round(new java.math.MathContext(precision, RoundingMode.HALF_UP));
        return rounded.toString();
    }

    private static String handleSpecialCases(Number value) {
        if (value instanceof Double || value instanceof Float) {
            double d = value.doubleValue();
            if (Double.isNaN(d)) return "NaN";
            if (Double.isInfinite(d)) return d > 0 ? "Infinity" : "-Infinity";
        }
        return null;
    }

    private static BigDecimal toBigDecimal(Number number) {
        if (number instanceof BigDecimal) return (BigDecimal) number;
        if (number instanceof BigInteger) return new BigDecimal((BigInteger) number);
        if (number instanceof Double || number instanceof Float) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.valueOf(number.longValue());
    }

    /**
     * Заменяет узкие неразрывные пробелы и обычные неразрывные пробелы
     * на обычный пробел — для единообразного отображения.
     */
    private static String centralFormatter(String s) {
        s = s.replace("\u202F", " "); // narrow no-break space → space
        s = s.replace("\u00A0", " "); // no-break space → space
        return s;
    }

    // ========================= НАСТРОЙКА =========================

    public static void setUseMillibuckets(boolean use) {
        useForgeFluidMillibuckets = use;
    }
}

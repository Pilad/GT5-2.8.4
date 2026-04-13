package gregtech.api.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;

import net.minecraftforge.fluids.FluidStack;

/**
 * Полный аналог NumberFormatUtil.
 * Содержит логику форматирования чисел и единицы измерения.
 */
public final class NumberFormatUtil {

    // ========================= КОНСТАНТЫ =========================
    // Используем BigDecimal для точных сравнений при сокращении чисел
    private static final BigDecimal BD_THOUSAND = BigDecimal.valueOf(1000);
    private static final BigDecimal BD_MILLION = BigDecimal.valueOf(1_000_000);
    private static final BigDecimal BD_BILLION = BigDecimal.valueOf(1_000_000_000);
    private static final BigDecimal BD_TRILLION = BigDecimal.valueOf(1_000_000_000_000L);
    private static final BigDecimal BD_QUADRILLION = BigDecimal.valueOf(1_000_000_000_000_000L);

    // ========================= НАСТРОЙКИ =========================

    // Если true, будет использовать "mB", если false - "L"
    // Можно вынести в конфиг, если нужно
    private static boolean useForgeFluidMillibuckets = false;

    // Порог, после которого числа становятся экспоненциальными (по умолчанию 1 Триллион)
    private static final BigDecimal EXPONENTIAL_THRESHOLD = BD_TRILLION;

    // ========================= КОНСТРУКТОР =========================
    private NumberFormatUtil() {}

    // ========================= ЕДИНИЦЫ ИЗМЕРЕНИЯ =========================

    /**
     * Возвращает единицу измерения жидкости.
     * 
     * @return "L" или "mB"
     */
    public static String getFluidUnit() {
        return useForgeFluidMillibuckets ? "mB" : "L";
    }

    /**
     * Возвращает единицу измерения энергии.
     * 
     * @return "EU"
     */
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
     * Стандартное форматирование с разделителями разрядов (запятые/пробелы).
     */
    public static String formatNumber(Number value) {
        if (value == null) return "0";

        // Обработка специальных случаев (NaN, Infinity)
        String special = handleSpecialCases(value);
        if (special != null) return special;

        BigDecimal val = toBigDecimal(value);

        // Используем стандартный форматтер Java с учетом локали
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
        df.setGroupingUsed(true); // Включаем разделители (1,000)
        df.setMaximumFractionDigits(2); // Дробная часть до 2 знаков

        return df.format(val);
    }

    /**
     * Компактное форматирование (K, M, B, T).
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

        // Определяем, какой суффикс применить
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

        // Делим и округляем
        BigDecimal scaled = value.divide(divisor, 2, RoundingMode.HALF_UP);

        // Убираем лишние нули (2.00 -> 2)
        String result = scaled.stripTrailingZeros()
            .toPlainString();

        return result + suffix;
    }

    private static String formatExponential(BigDecimal value) {
        // Простой формат: 1.23E+15
        // Используем 3 значащих цифры для аккуратности
        int scale = value.scale();
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
        // Для Integer, Long и т.д.
        return BigDecimal.valueOf(number.longValue());
    }

    // Метод для изменения настройки из конфига (опционально)
    public static void setUseMillibuckets(boolean use) {
        useForgeFluidMillibuckets = use;
    }
}

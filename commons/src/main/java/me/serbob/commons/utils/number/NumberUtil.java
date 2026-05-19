package me.serbob.commons.utils.number;

import java.text.DecimalFormat;

public class NumberUtil {
    private static final DecimalFormat df = new DecimalFormat("#.##");

    public static String formatPrice(double price) {
        if (price >= 1_000_000_000_000L) {
            return df.format(price / 1_000_000_000_000L) + "T";
        } else if (price >= 1_000_000_000) {
            return df.format(price / 1_000_000_000) + "B";
        } else if (price >= 1_000_000) {
            return df.format(price / 1_000_000) + "M";
        } else if (price >= 1000) {
            return df.format(price / 1000) + "K";
        } else if (price == (long) price) {
            return String.format("%d", (long) price);
        } else {
            return df.format(price);
        }
    }

    public static String formatNumber(long number) {
        if (number < 0) {
            return "-" + formatNumber(-number);
        }

        if (number == 0) {
            return "0";
        }

        DecimalFormat df = new DecimalFormat("#,##0.##");

        if (number < 1000) {
            return String.valueOf(number);
        }

        if (number >= 1_000_000_000_000L) {
            return df.format(number / 1_000_000_000_000.0) + "T";
        } else if (number >= 1_000_000_000) {
            return df.format(number / 1_000_000_000.0) + "B";
        } else if (number >= 1_000_000) {
            return df.format(number / 1_000_000.0) + "M";
        } else {
            return df.format(number / 1000.0) + "K";
        }
    }

    public static long parseAmount(String input) {
        if (input == null || input.trim().isEmpty()) {
            return -1;
        }

        String str = input.trim().toLowerCase();

        try {
            str = str.replaceAll("[, ]", "");

            if (str.matches("\\d+")) {
                return Long.parseLong(str);
            }

            if (str.matches("\\d*\\.?\\d+[kmbtqp]")) {
                double number = Double.parseDouble(str.substring(0, str.length() - 1));
                char suffix = str.charAt(str.length() - 1);

                switch (suffix) {
                    case 'k':
                        return (long) (number * 1_000);
                    case 'm':
                        return (long) (number * 1_000_000);
                    case 'b':
                        return (long) (number * 1_000_000_000);
                    case 't':
                        return (long) (number * 1_000_000_000_000L);
                    default:
                        return (long) number;
                }
            }

            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static int parseAmountInt(String input) {
        long amount = parseAmount(input);
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }
}

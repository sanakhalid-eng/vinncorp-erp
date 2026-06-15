package com.vinncorp.erp.modules.projects.util;

import com.vinncorp.erp.shared.exception.BadRequestException;

import java.util.regex.Pattern;

public final class ColorValidator {

    private static final Pattern HEX_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6})$");
    private static final double MIN_LUMINANCE = 0.15;

    private ColorValidator() {}

    public static void validate(String color) {
        if (color == null || color.isBlank()) {
            throw new BadRequestException("Color is required");
        }

        if (!HEX_PATTERN.matcher(color).matches()) {
            throw new BadRequestException("Color must be a valid hex code (e.g., #FF5733)");
        }

        if (isTooLight(color)) {
            throw new BadRequestException("Color is too light. Please choose a darker shade for readability");
        }

        if (isTooDark(color)) {
            throw new BadRequestException("Color is too dark. Please choose a lighter shade for readability");
        }
    }

    public static boolean isValid(String color) {
        if (color == null || color.isBlank()) return false;
        if (!HEX_PATTERN.matcher(color).matches()) return false;
        return !isTooLight(color) && !isTooDark(color);
    }

    private static boolean isTooLight(String hex) {
        double luminance = calculateRelativeLuminance(hex);
        return luminance > 0.85;
    }

    private static boolean isTooDark(String hex) {
        double luminance = calculateRelativeLuminance(hex);
        return luminance < MIN_LUMINANCE;
    }

    private static double calculateRelativeLuminance(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);

        double rs = linearize(r / 255.0);
        double gs = linearize(g / 255.0);
        double bs = linearize(b / 255.0);

        return 0.2126 * rs + 0.7152 * gs + 0.0722 * bs;
    }

    private static double linearize(double c) {
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}




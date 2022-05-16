package me.croabeast.iridiumapi.patterns;

import java.util.regex.Pattern;

/**
 * The class that handles the RGB format to parse.
 */
public abstract class BasePattern {

    /**
     * The hex basic format.
     */
    private final String HEX = "[\\da-f]{6}";

    /**
     * Compiles the gradient pattern.
     * @return gradient pattern
     */
    protected Pattern gradientPattern() {
        return Pattern.compile("(?i)<G:(" + HEX + ")>(.+?)</G:(" + HEX + ")>");
    }

    /**
     * Compiles the rainbow gradient pattern.
     * @return rainbow gradient pattern
     */
    protected Pattern rainbowPattern() {
        return Pattern.compile("(?i)<R:(\\d{1,3})>(.+?)</R>");
    }

    /**
     * Compiles the solid color pattern.
     * @return solid color pattern
     */
    protected Pattern solidPattern() {
        return Pattern.compile("(?i)\\{#(" + HEX + ")}|<#(" + HEX + ")>|&#(" + HEX + ")|#(" + HEX + ")");
    }

    /**
     * Process a string using the RGB patterns to apply colors.
     * @param string an input string
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     * @return the processed line
     */
    public abstract String process(String string, boolean useRGB);
}

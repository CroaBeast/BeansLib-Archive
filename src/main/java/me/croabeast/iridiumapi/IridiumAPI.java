package me.croabeast.iridiumapi;

import com.google.common.collect.ImmutableMap;
import me.croabeast.iridiumapi.patterns.Gradient;
import me.croabeast.iridiumapi.patterns.BasePattern;
import me.croabeast.iridiumapi.patterns.Rainbow;
import me.croabeast.iridiumapi.patterns.SolidColor;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.*;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static me.croabeast.beanslib.BeansLib.*;

/**
 * The class that handles all the RGB support.
 *
 * @author <strong>PeachesMLB</strong> (forked by CroaBeast)
 */
public final class IridiumAPI {

    /**
     * Checks if the server can handle RGB colors.
     */
    private static final boolean SUPPORTS_RGB = majorVersion() > 15;

    /**
     * Color regex strings to check.
     */
    private static final String
            BUKKIT_REGEX = "[&§][a-f\\dk-or]", GRADIENT_REGEX = "</?[gr](:\\d{1,3})?>",
            RGB_REGEX = "\\{#[\\dA-F]{6}}|<#[\\dA-F]{6}>|&#[\\dA-F]{6}|#[\\dA-F]{6}",
            COLOR_REGEX = "(?i)" + BUKKIT_REGEX + "|" + GRADIENT_REGEX + "|" + RGB_REGEX;

    /**
     * A map that handles all the Bukkit colors by its hex value.
     */
    private static final Map<Color, ChatColor> COLORS = ImmutableMap.<Color, ChatColor>builder()
            .put(new Color(0), ChatColor.getByChar('0'))
            .put(new Color(170), ChatColor.getByChar('1'))
            .put(new Color(43520), ChatColor.getByChar('2'))
            .put(new Color(43690), ChatColor.getByChar('3'))
            .put(new Color(11141120), ChatColor.getByChar('4'))
            .put(new Color(11141290), ChatColor.getByChar('5'))
            .put(new Color(16755200), ChatColor.getByChar('6'))
            .put(new Color(11184810), ChatColor.getByChar('7'))
            .put(new Color(5592405), ChatColor.getByChar('8'))
            .put(new Color(5592575), ChatColor.getByChar('9'))
            .put(new Color(5635925), ChatColor.getByChar('a'))
            .put(new Color(5636095), ChatColor.getByChar('b'))
            .put(new Color(16733525), ChatColor.getByChar('c'))
            .put(new Color(16733695), ChatColor.getByChar('d'))
            .put(new Color(16777045), ChatColor.getByChar('e'))
            .put(new Color(16777215), ChatColor.getByChar('f')).build();

    /**
     * A list with all the {@link BasePattern} classes.
     */
    private static final List<BasePattern> PATTERNS = Arrays.asList(new Gradient(), new SolidColor(), new Rainbow());

    /**
     * Process a string to apply the correct colors using the RGB format.
     * @param string an input string
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     * @return the processed string
     */
    @NotNull
    public static String process(@NotNull String string, boolean useRGB) {
        for (BasePattern pattern : PATTERNS) string = pattern.process(string, useRGB);
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Process a string to apply the correct colors using the RGB format.
     * @param string an input string
     * @return the processed string
     */
    @NotNull
    public static String process(@NotNull String string) {
        return process(string, SUPPORTS_RGB);
    }

    /**
     * Applies a single color to a string.
     * @param string an input string
     * @param color the requested color to apply
     * @return the colored string
     */
    @NotNull
    public static String color(@NotNull String string, @NotNull Color color) {
        return (SUPPORTS_RGB ? ChatColor.of(color) : getClosestColor(color)) + string;
    }

    /**
     * Applies a gradient color to an input string.
     * @param string an input string
     * @param start the start color
     * @param end the end color
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     * @return the string with the applied gradient
     */
    @NotNull
    public static String color(@NotNull String string, @NotNull Color start, @NotNull Color end, boolean useRGB) {
        int step = stripSpecial(string).length();
        return step <= 1 ? string : apply(string, createGradient(start, end, step, useRGB));
    }

    /**
     * Applies a rainbow gradient with a specific saturation to an input string.
     * @param string an input string
     * @param saturation the saturation for the rainbow gradient
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     * @return the string with the applied rainbow gradient
     */
    @NotNull
    public static String rainbow(@NotNull String string, float saturation, boolean useRGB) {
        int step = stripSpecial(string).length();
        return step <= 0 ? string : apply(string, createRainbow(step, saturation, useRGB));
    }

    /**
     * Gets the color from an input string.
     * @param string an input string
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     * @return the requested chat color
     */
    @NotNull
    public static ChatColor getColor(@NotNull String string, boolean useRGB) {
        return useRGB ? ChatColor.of(new Color(Integer.parseInt(string, 16)))
                : getClosestColor(new Color(Integer.parseInt(string, 16)));
    }

    /**
     * Removes all the bukkit color format from a string.
     * @param string an input string
     * @return the stripped string
     */
    @NotNull
    public static String stripBukkit(@NotNull String string) {
        return string.replaceAll("(?i)[&§][a-f\\d]", "");
    }

    /**
     * Removes all the bukkit special color format from a string.
     * @param string an input string
     * @return the stripped string
     */
    @NotNull
    public static String stripSpecial(@NotNull String string) {
        return string.replaceAll("(?i)[&§][k-o]", "");
    }

    /**
     * Removes all the rgb color format from a string.
     * @param string an input string
     * @return the stripped string
     */
    @NotNull
    public static String stripRGB(@NotNull String string) {
        return string.replaceAll("(?i)" + RGB_REGEX + "|" + GRADIENT_REGEX, "");
    }

    /**
     * Removes all the color format from a string.
     * @param string an input string
     * @return the stripped string
     */
    @NotNull
    public static String stripAll(@NotNull String string) {
        return string.replaceAll(COLOR_REGEX, "");
    }

    /**
     * Applies every color in an array to a source string.
     * @param source a string
     * @param colors the requested colors array
     * @return the formatted string
     */
    @NotNull
    private static String apply(@NotNull String source, @NotNull ChatColor[] colors) {
        StringBuilder specials = new StringBuilder(), builder = new StringBuilder();
        String[] characters = source.split("");

        if (StringUtils.isBlank(source)) return source;
        int outIndex = 0;

        for (int i = 0; i < characters.length; i++) {
            if (!characters[i].matches("[&§]") || i + 1 >= characters.length)
                builder.append(colors[outIndex++])
                        .append(specials).append(characters[i]);
            else {
                if (!characters[i + 1].equals("r")) {
                    specials.append(characters[i]);
                    specials.append(characters[i + 1]);
                }
                else specials.setLength(0);
                i++;
            }
        }

        return builder.toString();
    }

    /**
     * Creates an array of colors for the rainbow gradient.
     * @param step the string's length
     * @param saturation the saturation for the rainbow
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     * @return the rainbow color array
     */
    @NotNull
    private static ChatColor[] createRainbow(int step, float saturation, boolean useRGB) {
        ChatColor[] colors = new ChatColor[step];
        double colorStep = (1.00 / step);

        for (int i = 0; i < step; i++) {
            Color color = Color.getHSBColor((float) (colorStep * i), saturation, saturation);
            colors[i] = useRGB ? ChatColor.of(color) : getClosestColor(color);
        }
        return colors;
    }

    /**
     * Creates an array of colors for the gradient.
     * @param start a start color
     * @param end an end color
     * @param step the string's length
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     * @return the rainbow color array
     */
    @NotNull
    private static ChatColor[] createGradient(@NotNull Color start, @NotNull Color end, int step, boolean useRGB) {
        ChatColor[] colors = new ChatColor[step];

        int stepR = Math.abs(start.getRed() - end.getRed()) / (step - 1),
                stepG = Math.abs(start.getGreen() - end.getGreen()) / (step - 1),
                stepB = Math.abs(start.getBlue() - end.getBlue()) / (step - 1);

        int[] direction = new int[] {
                start.getRed() < end.getRed() ? +1 : -1,
                start.getGreen() < end.getGreen() ? +1 : -1,
                start.getBlue() < end.getBlue() ? +1 : -1
        };

        for (int i = 0; i < step; i++) {
            Color color = new Color(start.getRed() + ((stepR * i) * direction[0]),
                    start.getGreen() + ((stepG * i) * direction[1]),
                    start.getBlue() + ((stepB * i) * direction[2]));
            colors[i] = useRGB ? ChatColor.of(color) : getClosestColor(color);
        }

        return colors;
    }

    /**
     * Gets the closest bukkit color from a normal color.
     * @param color an input color
     * @return the closest bukkit color
     */
    @NotNull
    private static ChatColor getClosestColor(Color color) {
        Color nearestColor = null;
        double nearestDistance = Integer.MAX_VALUE;

        for (Color color1 : COLORS.keySet()) {
            double distance = Math.pow(color.getRed() - color1.getRed(), 2)
                    + Math.pow(color.getGreen() - color1.getGreen(), 2)
                    + Math.pow(color.getBlue() - color1.getBlue(), 2);
            if (nearestDistance > distance) {
                nearestColor = color1;
                nearestDistance = distance;
            }
        }
        return COLORS.get(nearestColor);
    }
}
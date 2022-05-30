package me.croabeast.beanslib.utilities.chars;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * The handler class for managing characters and determines its size in a centered chat message.
 */
public final class Handler {

    /**
     * The HashMap that stores all the characters.
     */
    static HashMap<Character, CharInfo> values = new HashMap<>();

    /**
     * The default information if a char doesn't exist in the {@link #getValues()} map.
     */
    private static final CharInfo DEFAULT = new CharInfo('a', 5);

    /*
     * Stores all the default values in the map.
     */
    static {
        for (Defaults d : Defaults.values()) {
            char c = d.character();
            values.put(c, new CharInfo(c, d.length()));
        }
    }

    /**
     * Gets the values HashMap of all the stored characters.
     * <p> <strong>DO NOT CLEAR THIS MAP:</strong> centered messages may not be displayed correctly.
     * @return the map
     */
    public static HashMap<Character, CharInfo> getValues() {
        return values;
    }

    /**
     * Gets the requested {@link CharInfo} instance of an input character.
     * <p> Returns the {@link #DEFAULT} value if the character isn't found in the {@link #getValues()} map.
     * @param c an input character
     * @return the requested info
     */
    @NotNull
    public static CharInfo getInfo(char c) {
        CharInfo info = getValues().getOrDefault(c, null);
        return info == null ? DEFAULT : info;
    }

    /**
     * Gets the requested {@link CharInfo} instance of an input string.
     * <p> Returns the {@link #DEFAULT} value if the string is null, empty or has more than 1 characters.
     * @param input an input string
     * @return the requested info
     */
    @NotNull
    public static CharInfo getInfo(String input) {
        if (input == null || input.length() <= 0) return DEFAULT;

        char[] array = input.toCharArray();
        if (array.length > 1) return DEFAULT;

        return getInfo(array[0]);
    }

    /**
     * Adds a new character in the {@link #getValues()} map.
     * @param c a character
     * @param length the char's length
     */
    public static void addChar(char c, int length) {
        getValues().put(c, new CharInfo(c, length));
    }

    /**
     * Removes a character from the {@link #getValues()} map.
     * @param c a character
     */
    public static void removeChar(char c) {
        getValues().remove(c);
    }
}

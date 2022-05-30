package me.croabeast.beanslib.utilities.chars;

/**
 * The class to store a character's information.
 */
public class CharInfo {

    private final char character;
    private final int length;

    /**
     * Creates a character info class.
     * @param character the character
     * @param length the char's length
     */
    public CharInfo(char character, int length) {
        this.character = character;
        this.length = length;
    }

    /**
     * Gets the character.
     * @return the char
     */
    private char getCharacter() {
        return character;
    }

    /**
     * Gets the char's length.
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the char's length when is bold.
     * @return the length in bold
     */
    public int getBoldLength() {
        return getLength() + (getCharacter() == ' ' ? 0 : 1);
    }
}

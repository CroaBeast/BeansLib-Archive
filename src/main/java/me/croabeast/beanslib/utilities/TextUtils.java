package me.croabeast.beanslib.utilities;

import com.google.common.collect.*;
import com.loohp.interactivechat.api.*;
import me.clip.placeholderapi.*;
import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;

/**
 * The class for static methods.
 */
public class TextUtils {

    /**
     * Parses the placeholders from {@link PlaceholderAPI} if is enabled.
     * @param player a player, can be null
     * @param message the input line
     * @return the parsed message
     */
    public static String parsePAPI(@Nullable Player player, String message) {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ?
                PlaceholderAPI.setPlaceholders(player, message) : message;
    }

    /**
     * Replace a {@link String} array of keys with another {@link String} array of values.
     * <p> It's case-insensitive and special characters are quoted to avoid errors.
     * @param line the input line
     * @param keys the array of keys
     * @param values the array of values
     * @return the parsed line with the respective values
     */
    public static String replaceInsensitiveEach(String line, String[] keys, String[] values) {
        if (keys == null || values == null) return line;
        if (keys.length > values.length) return line;

        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == null | values[i] == null) continue;
            String newKey = Pattern.quote(keys[i]);

            Matcher matcher = Pattern.compile("(?i)" + newKey).matcher(line);
            if (!matcher.find()) continue;

            line = line.replace(matcher.group(), values[i]);
        }
        return line;
    }

    /**
     * Converts a {@link String} to a {@link List <String>} from
     * a config file or section if it's not a list.
     * @param section a config file or section, can be null
     * @param path the path to locate the string or list
     * @return the converted string list or an empty list if section is null
     */
    public static List<String> toList(@Nullable ConfigurationSection section, String path) {
        if (section == null) return new ArrayList<>();
        return  section.isList(path) ? section.getStringList(path) :
                Lists.newArrayList(section.getString(path));
    }

    /**
     * Check if the line has a valid json format. Usage:
     * <pre> if (IS_JSON.apply(stringInput)) doSomethingIdk();</pre>
     */
    public static final Function<String, Boolean> IS_JSON = s -> TextKeys.JSON_PATTERN.matcher(s).find();

    /**
     * Strips the JSON format from a line.
     * @param line the line to strip.
     * @return the stripped line.
     */
    public static String stripJson(String line) {
        return line.replaceAll("(?i)</?(text|hover|run|suggest|url)" +
                "(=\\[(.+?)](\\|(hover|run|suggest|url)=\\[(.+?)])?)?>", "");
    }

    /**
     * Parse InteractiveChat placeholders for using it the Json message.
     * @param player the requested player
     * @param line the line to parse
     * @return the line with the parsed placeholders.
     */
    public static String parseInteractiveChat(Player player, String line) {
        if (Bukkit.getPluginManager().isPluginEnabled("InteractiveChat"))
            try {
                return InteractiveChatAPI.markSender(line, player.getUniqueId());
            }
            catch (Exception e) {
                e.printStackTrace();
                return line;
            }
        else return line;
    }
}

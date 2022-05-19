package me.croabeast.beanslib;

import com.google.common.collect.*;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import me.clip.placeholderapi.*;
import me.croabeast.beanslib.terminals.*;
import me.croabeast.beanslib.utilities.*;
import me.croabeast.iridiumapi.*;
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import static me.croabeast.iridiumapi.IridiumAPI.*;
import static net.md_5.bungee.api.chat.ClickEvent.Action.*;

/**
 * The main class of the Lib.
 * It has a lot of useful text-related methods.
 *
 * @author CroaBeast
 * @since 1.0
 */
public abstract class BeansLib extends TextKeys {

    /*
     * Action Bar handler.
     */
    private final ActionBar actionBar = new ActionBar();
    /*
     * Title handler.
     */
    private final TitleMngr titleMngr = new TitleMngr();

    /**
     * The {@link JavaPlugin} instance of your project.
     * @return plugin instance
     */
    @NotNull
    protected abstract JavaPlugin getPlugin();

    /**
     * It will color the console message if the server supports it.
     * @param line the input line
     * @return the formatted console message
     */
    private String colorLogger(@NotNull String line) {
        return stripJson(loggerColorSupport() ? process(line, !fixColorLogger()) : stripAll(line));
    }

    /**
     * Sends requested information for a {@link Player}.
     * @param player a valid online player
     * @param lines the information to send
     */
    public void playerLog(@NotNull Player player, String... lines) {
        for (String s : lines) if (s != null)
            player.sendMessage(process(s.replace(langPrefixKey(), langPrefix())));
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console.
     * @param lines the information to send
     */
    public void rawLog(String... lines) {
        for (String s : lines) if (s != null) Bukkit.getLogger().info(colorLogger(s));
    }

    /**
     * Sends requested information to a {@link CommandSender}.
     * @param sender a valid sender, can be the console or a player
     * @param lines the information to send
     */
    public void doLog(@Nullable CommandSender sender, String... lines) {
        if (sender instanceof Player) playerLog((Player) sender, lines);
        for (String s : lines) if (s != null)
            getPlugin().getLogger().info(colorLogger(s.replace(langPrefixKey(), "")));
    }

    /**
     * Sends requested information to the console.
     * @param lines the information to send
     */
    public void doLog(String... lines) {
        doLog(null, lines);
    }

    /**
     * Remove the text identifier prefix from the input line if {@link #isStripPrefix()} is true.
     * <p>See more in {@link TextKeys#textPattern()}
     * @param line the input line
     * @return the stripped message
     */
    public String stripPrefix(String line) {
        Matcher matcher = textPattern().matcher(line);
        line = removeSpace(line);

        return (matcher.find() && isStripPrefix()) ?
                line.replace(matcher.group(1), "") : line;
    }

    /**
     * Removes the first spaces of a line if {@link #isHardSpacing()} is true.
     * @param line the input line
     * @return the line without the first spaces
     */
    public String removeSpace(String line) {
        if (isHardSpacing()) {
            String startLine = line;
            try {
                while (line.charAt(0) == ' ') line = line.substring(1);
                return line;
            } catch (IndexOutOfBoundsException e) {
                return startLine;
            }
        }
        else return line;
    }

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
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == null | values[i] == null) continue;
            keys[i] = Pattern.quote(keys[i]);
            line = line.replaceAll("(?i)" + keys[i], values[i]);
        }
        return line;
    }

    /**
     * Use the {@link #charPattern()} to find unicode values and
     * replace them with its respective characters.
     * @param line the input line
     * @return the parsed message with the new characters
     */
    public String parseChars(String line) {
        Pattern charPattern = Pattern.compile(charPattern());
        Matcher match = charPattern.matcher(line);

        while (match.find()) {
            char s = (char) Integer.parseInt(match.group(1), 16);
            line = line.replace(match.group(), s + "");
        }
        return line;
    }

    /**
     * Parses the requested message using:
     * <blockquote><pre>
     * 1. First, parses characters using {@link #parseChars(String)}
     * 2. Then, parses {@link PlaceholderAPI} placeholders using {@link #parsePAPI(Player, String)}
     * 3. Finally, applies color format using {@link IridiumAPI#process(String)}.</pre></blockquote>
     * @param player a player, can be null
     * @param message the input message
     * @return the formatted message
     */
    public String colorize(@Nullable Player player, String message) {
        return IridiumAPI.process(parsePAPI(player, parseChars(message)));
    }

    /**
     * Converts a {@link String} to a {@link List<String>} from
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
     * Creates a centered chat message.
     * @param player a player to parse placeholders.
     * @param message the input message.
     * @return the centered chat message.
     */
    public String centerMessage(Player player, String message) {
        String initial = colorize(player, stripJson(message));

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : initial.toCharArray()) {
            if (c == '§') previousCode = true;
            else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                FontInfo dFI = FontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ?
                        dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize;
        int spaceLength = FontInfo.SPACE.getLength() + 1;
        int compensated = 0;

        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }

        return sb + colorize(player, message);
    }

    /**
     * Defines a string if is centered or not.
     * @param player a player to parse placeholders.
     * @param line the input line.
     * @return the result string.
     */
    public String centeredText(Player player, String line) {
        return line.startsWith(centerPrefix()) ?
                centerMessage(player, line.replace(centerPrefix(), "")) :
                colorize(player, line);
    }

    /**
     * Check if the line has a valid json format. Usage:
     * <pre> if (IS_JSON.apply(stringInput)) doSomethingIdk();</pre>
     */
    public static final Function<String, Boolean> IS_JSON = s -> JSON_PATTERN.matcher(s).find();

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
     * Converts a string to a TextComponent.
     * @param line the line to convert.
     * @return the requested component.
     */
    private static TextComponent toComponent(String line) {
        return new TextComponent(TextComponent.fromLegacyText(line));
    }

    /**
     * Add a click event to a component.
     * @param comp the component to add the event
     * @param type the click event type
     * @param input the input line for the click event
     */
    private void addClick(TextComponent comp, String type, String input) {
        ClickEvent.Action action = null;
        if (type.matches("(?i)run")) action = RUN_COMMAND;
        else if (type.matches("(?i)suggest")) action = SUGGEST_COMMAND;
        else if (type.matches("(?i)url")) action = OPEN_URL;
        if (action != null) comp.setClickEvent(new ClickEvent(action, input));
    }

    /**
     *
     * @param comp the component to add the event
     * @param hover the list to add as a hover
     */
    @SuppressWarnings("deprecation")
    private void addHover(Player player, TextComponent comp, List<String> hover) {
        BaseComponent[] array = new BaseComponent[hover.size()];
        for (int i = 0; i < hover.size(); i++) {
            String end = i == hover.size() - 1 ? "" : "\n";
            array[i] = toComponent(colorize(player, hover.get(i)) + end);
        }
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, array));
    }

    /**
     * Add the event found in the formatted line.
     * @param comp the component to add the event.
     * @param type the event's type.
     * @param input the input line for the event.
     */
    private void addEvent(Player player, TextComponent comp, String type, String input) {
        if (type.matches("(?i)run|suggest|url")) addClick(comp, type, input);
        else if (type.matches("(?i)hover"))
            addHover(player, comp, Arrays.asList(input.split(lineSeparator())));
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

    /**
     * Converts a line to a {@link BaseComponent} array and use it to send a json message.
     * <p>It uses the {@link TextKeys#JSON_PATTERN} to applies click and hover events.
     * @param player a player
     * @param line the input line
     * @return the converted json object
     */
    public BaseComponent[] stringToJson(Player player, String line) {
        return stringToJson(player, line, null, new ArrayList<>());
    }

    /**
     * Converts a line to a {@link BaseComponent} array and use it to send a json message.
     * <p>Uses an input string to apply a click event and a string list to apply a hover event.
     * @param player a player
     * @param line the input line
     * @param click the click line
     * @param hover a string list
     * @return the converted json object
     */
    public BaseComponent[] stringToJson(Player player, String line, @Nullable String click, List<String> hover) {
        boolean isComplex = click != null && !hover.isEmpty();
        if (isComplex && IS_JSON.apply(line)) line = stripJson(line);

        line = centeredText(player, parseInteractiveChat(player, line));
        List<BaseComponent> components = new ArrayList<>();

        if (isComplex) {
            final TextComponent comp = toComponent(line);
            addHover(player, comp, hover);

            String[] input = click.split(":", 2);
            addClick(comp, input[0], input[1]);
            components.add(comp);

            return components.toArray(new BaseComponent[0]);
        }

        final Matcher match = JSON_PATTERN.matcher(line);
        int lastEnd = 0;

        while (match.find()) {
            String extra = match.group(3), before = line.substring(lastEnd, match.start());
            final TextComponent comp = toComponent(match.group(6));

            components.addAll(Arrays.asList(TextComponent.fromLegacyText(before)));
            addEvent(player, comp, match.group(1), match.group(2));

            if (extra != null && extra.matches("(?i)\\|" + JSON_PREFIX))
                addEvent(player, comp, match.group(4), match.group(5));

            components.add(comp);
            lastEnd = match.end();
        }

        if (lastEnd < (line.length() - 1)) {
            final String after = line.substring(lastEnd);
            components.addAll(Arrays.asList(TextComponent.fromLegacyText(after)));
        }

        return components.toArray(new BaseComponent[0]);
    }

    /**
     * Sends an action bar message to a player.
     * @param player a player
     * @param message the message
     */
    public void sendActionBar(Player player, String message) {
        actionBar.getMethod().send(player, message);
    }

    /**
     * Sends a title message to a player
     * @param player a player
     * @param message an array of title and subtitle
     * @param in the fadeIn number in ticks
     * @param stay the stay number in ticks
     * @param out the fadeOut number in ticks
     */
    public void sendTitle(Player player, @NotNull String[] message, int in, int stay, int out) {
        if (message.length == 0 || message.length > 2) return;
        String subtitle = message.length == 1 ? "" : message[1];
        titleMngr.getMethod().send(player, message[0], subtitle, in, stay, out);
    }

    /**
     * Sends a message depending on its prefix. See {@link TextKeys#textPattern()} for more info
     * @param target a target player to send, can be null
     * @param sender a player to format the message
     * @param input the input line
     */
    public void sendMessage(@Nullable Player target, @NotNull Player sender, String input) {
        if (target == null) target = sender;
        Matcher matcher = textPattern().matcher(input);

        if (matcher.find()) {
            String prefix = removeSpace(matcher.group(1).substring(1, matcher.group(1).length() - 1));
            String line = colorize(sender, removeSpace(input.substring(prefix.length() + 2)));

            if (prefix.matches("(?i)" + titleKey())) {
                Matcher timeMatch = Pattern.compile("(?i)" + titleKey()).matcher(prefix);

                String timeString;
                try {
                    timeString = timeMatch.find() ? timeMatch.group(1).substring(1) : null;
                } catch (Exception e) {
                    timeString = null;
                }

                int time;
                try {
                    time = timeString == null ? defaultTitleTicks()[1] :
                            Integer.parseInt(timeString) * 20;
                } catch (Exception e) {
                    time = defaultTitleTicks()[1];
                }

                sendTitle(target, line.split(lineSeparator()),
                        defaultTitleTicks()[0], time, defaultTitleTicks()[2]);
            }
            else if (prefix.matches("(?i)" + jsonKey())) {
                String cmd = "minecraft:tellraw " + target.getName() + " " + line;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
            else if (prefix.matches("(?i)" + actionBarKey())) sendActionBar(target, line);
            else if (prefix.matches("(?i)" + bossbarKey()))
                new Bossbar(getPlugin(), target, line).display();
            else target.spigot().sendMessage(stringToJson(sender, line));
        }
        else target.spigot().sendMessage(stringToJson(sender, input));
    }

    /**
     * Sends a message list using {@link #sendMessage(Player, Player, String)}.
     * Parse keys and values using {@link #replaceInsensitiveEach(String, String[], String[])}
     * @param sender a player to format and send the message
     * @param list the message list
     * @param keys a keys array
     * @param values a values array
     */
    public void sendMessageList(CommandSender sender, List<String> list, @Nullable String[] keys, @Nullable String[] values) {
        for (String line : list) {
            if (line == null || line.equals("")) continue;

            line = line.startsWith(langPrefixKey()) ?
                    line.replace(langPrefixKey(), langPrefix()) : line;

            line = replaceInsensitiveEach(line, keys, values);

            if (sender != null && !(sender instanceof ConsoleCommandSender)) {
                Player player = (Player) sender;

                line = replaceInsensitiveEach(line, new String[] {playerKey(), playerWorldKey()},
                        new String[] {player.getName(), player.getWorld().getName()});

                sendMessage(null, player, line);
            }
            else rawLog(centeredText(null, line));
        }
    }

    /**
     * See {@link #sendMessageList(CommandSender, List, String[], String[])} to more info.
     * @param sender a player to format and send the message
     * @param section the config file or section
     * @param path the path of the string or string list
     * @param keys a keys array
     * @param values a values array
     */
    public void sendMessageList(CommandSender sender, ConfigurationSection section, String path, @Nullable String[] keys, @Nullable String[] values) {
        sendMessageList(sender, toList(section, path), keys, values);
    }

    /**
     * See {@link #sendMessageList(CommandSender, List, String[], String[])} to more info.
     * @param sender a player to format and send the message
     * @param list the message list
     */
    public void sendMessageList(CommandSender sender, List<String> list) {
        sendMessageList(sender, list, null, null);
    }

    /**
     * See {@link #sendMessageList(CommandSender, List, String[], String[])} to more info.
     * @param sender a player to format and send the message
     * @param section the config file or section
     * @param path the path of the string or string list
     */
    public void sendMessageList(CommandSender sender, ConfigurationSection section, String path) {
        sendMessageList(sender, toList(section, path));
    }

    /**
     * The enum class to manage the length of every char.
     */
    public enum FontInfo {
        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),

        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),

        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),

        LEFT_PARENTHESIS('(', 4),
        RIGHT_PARENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),

        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        private final char character;
        private final int length;

        FontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        private char getCharacter() {
            return character;
        }

        public int getLength() {
            return length;
        }

        public int getBoldLength() {
            if (this == SPACE) return getLength();
            return this.length + 1;
        }

        public static FontInfo getDefaultFontInfo(char c) {
            for (FontInfo dFI : values())
                if (dFI.getCharacter() == c) return dFI;
            return DEFAULT;
        }
    }
}

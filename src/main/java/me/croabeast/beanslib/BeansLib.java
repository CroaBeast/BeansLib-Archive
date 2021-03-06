package me.croabeast.beanslib;

import com.google.common.collect.*;
import me.clip.placeholderapi.*;
import me.croabeast.beanslib.terminals.*;
import me.croabeast.beanslib.utilities.*;
import me.croabeast.beanslib.utilities.chars.*;
import me.croabeast.iridiumapi.*;
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.regex.*;

import static me.croabeast.beanslib.utilities.TextUtils.*;
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
     * Use the {@link #charPattern()} to find unicode values and
     * replace them with its respective characters.
     * @param line the input line
     * @return the parsed message with the new characters
     */
    public String parseChars(String line) {
        if (line == null || line.length() == 0) return line;

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
     * 2. Then, parses {@link PlaceholderAPI} placeholders using {@link TextUtils#parsePAPI(Player, String)}
     * 3. Finally, applies color format using {@link IridiumAPI#process(String)}.</pre></blockquote>
     * @param player a player, can be null
     * @param message the input message
     * @return the formatted message
     */
    public String colorize(@Nullable Player player, String message) {
        return IridiumAPI.process(parsePAPI(player, parseChars(message)));
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
            if (c == '??') previousCode = true;
            else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                CharInfo dFI = Handler.getInfo(c);
                messagePxSize += isBold ?
                        dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = chatBoxSize() - halvedMessageSize;
        int compensated = 0;

        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            // 4 is the SPACE char length + 1
            compensated += 4;
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
     * Converts a string to a TextComponent.
     * @param line the line to convert.
     * @return the requested component.
     */
    private TextComponent toComponent(String line) {
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
        line = centeredText(player, parseInteractiveChat(player, line));

        if (!hover.isEmpty() || click != null) {
            final TextComponent comp = toComponent(stripJson(line));
            if (!hover.isEmpty()) addHover(player, comp, hover);

            if (click != null) {
                String[] input = click.split(":", 2);
                addClick(comp, input[0], input[1]);
            }

            return Lists.newArrayList(comp).toArray(new BaseComponent[0]);
        }

        final Matcher match = JSON_PATTERN.matcher(line);
        int lastEnd = 0;
        List<BaseComponent> components = new ArrayList<>();

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
        if (message.length <= 0 || message.length > 2) return;
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
            String pr = matcher.group(1), prefix = removeSpace(pr.substring(1, pr.length() - 1));
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
            else if (prefix.matches("(?i)" + bossbarKey())) new Bossbar(getPlugin(), target, input).display();
            else target.spigot().sendMessage(stringToJson(sender, line));
        }
        else target.spigot().sendMessage(stringToJson(sender, input));
    }

    /**
     * Sends a message list using {@link #sendMessage(Player, Player, String)}.
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     * @param sender a player to format and send the message
     * @param list the message list
     * @param keys a keys array
     * @param values a values array
     */
    public void sendMessageList(CommandSender sender, List<String> list, @Nullable String[] keys, @Nullable String[] values) {
        if (list.isEmpty()) return;

        for (String line : list) {
            if (line == null || line.equals("")) continue;

            line = line.startsWith(langPrefixKey()) ? line.replace(langPrefixKey(), langPrefix()) : line;
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
}

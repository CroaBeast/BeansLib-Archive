package me.croabeast.beanslib.terminals;

import me.croabeast.beanslib.utilities.TextKeys;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public class ActionBar implements Reflection {

    private final GetActionBar actionBar;

    public ActionBar() {
        actionBar = TextKeys.majorVersion() < 11 ? oldActionBar() : newActionBar();
    }

    public interface GetActionBar {
        void send(Player player, String message);
    }

    public GetActionBar getMethod() {
        return actionBar;
    }

    private GetActionBar oldActionBar() {
        return (player, message) -> {
            try {
                Class<?> chat = getNMSClass("IChatBaseComponent");
                Constructor<?> constructor = getNMSClass("PacketPlayOutChat").getConstructor(chat, byte.class);
                message = "{\"text\":\"" + message + "\"}";

                Object icbc = chat.getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, message),
                        packet = constructor.newInstance(icbc, (byte) 2);
                sendPacket(player, packet);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    private GetActionBar newActionBar() {
        return (player, message) ->
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
}

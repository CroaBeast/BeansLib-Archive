package me.croabeast.beanslib;

import me.croabeast.beanslib.utilities.TextUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class BeansLib extends TextUtils {

    /**
     * Only constructor to initialize the integration of BeansLib
     * @param plugin plugin's instance
     */
    public BeansLib(@NotNull JavaPlugin plugin) {
        super(plugin);
    }
}

package me.Silverwolfg11.NPCShop.listener;

import me.Silverwolfg11.NPCShop.NPCShopPlugin;
import me.Silverwolfg11.TimingLib.events.HalfHourEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TimerLibListener implements Listener {

    private NPCShopPlugin plugin;

    public TimerLibListener(NPCShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHalfHour(HalfHourEvent event) {
        plugin.getTransactionManager().getTask().runTaskAsynchronously(plugin);
    }

}

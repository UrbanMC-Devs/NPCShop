package me.Silverwolfg11.NPCShop.listener;

import me.Silverwolfg11.NPCShop.NPCShopPlugin;
import me.Silverwolfg11.NPCShop.objects.NPCShop;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCClickListener implements Listener {

    private NPCShopPlugin plugin;

    public NPCClickListener(NPCShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClickEvent(NPCRightClickEvent event) {
        if (plugin.getNPCShopFromID(event.getNPC().getId()) != null) {

            NPCShop shop = plugin.getNPCShopFromID(event.getNPC().getId());

            event.getClicker().openInventory(shop.generateMainInventory(plugin.getMainInventory()));
        }
    }
}

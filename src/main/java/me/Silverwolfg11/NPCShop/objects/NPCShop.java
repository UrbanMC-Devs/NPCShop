package me.Silverwolfg11.NPCShop.objects;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class NPCShop {

    String title;

    public void setTitle(String title) {
        this.title = title;
    }

    private NPCInventoryHolder buyInventory = new NPCInventoryHolder(this, NPCInventoryHolder.NPCInventoryType.BUY),
            sellInventory = new NPCInventoryHolder(this, NPCInventoryHolder.NPCInventoryType.SELL);

    public void createBuyInventory(List<ItemStack> items) {
        buyInventory.createInventory(ChatColor.translateAlternateColorCodes('&', title + " [Buy]"), items);
    }

    public void createSellInventory(List<ItemStack> items) {
        sellInventory.createInventory(ChatColor.translateAlternateColorCodes('&', title + " [Sell]"), items);
    }

    // The reason we generate each main inventory is because all the main inventories are the same except the title.
    // So to avoid wasting space we just generate the main inventory every time.
    public Inventory generateMainInventory(Inventory reference) {
        NPCInventoryHolder newHolder = new NPCInventoryHolder(this, NPCInventoryHolder.NPCInventoryType.MAIN);

        return newHolder.createInventory(ChatColor.translateAlternateColorCodes('&', title), reference);
    }

    public Inventory getBuyInventory() { return buyInventory.getInventory(); }

    public Inventory getSellInventory() { return sellInventory.getInventory(); }


}

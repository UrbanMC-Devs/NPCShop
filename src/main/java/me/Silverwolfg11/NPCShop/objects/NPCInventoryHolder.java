package me.Silverwolfg11.NPCShop.objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class NPCInventoryHolder implements InventoryHolder {

    private Inventory inventory;
    private NPCShop shopReference;
    private NPCInventoryType type;

    NPCInventoryHolder(NPCShop npcshop, NPCInventoryType type) {
        this.shopReference = npcshop;
        this.type = type;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }


    public NPCShop getNPCShop() {
        return shopReference;
    }

    public NPCInventoryType getInventoryType() {
        return type;
    }

    public enum NPCInventoryType {
        MAIN, BUY, SELL
    }


    public Inventory createInventory(String title, Inventory reference) {
        inventory = Bukkit.createInventory(this, reference.getSize(), title);

        inventory.setContents(reference.getContents());

        return inventory;
    }


    //Useful for creating merchant buy and sell inventories. Do not use for the main inventory!
    public void createInventory(String inventoryTitle, List<ItemStack> items) {

        inventory = Bukkit.createInventory(this, getInventorySizeFromItemAmount(items.size()), inventoryTitle);

        //Set back item
        ItemStack chest = new ItemStack(Material.CHEST, 1);

        ItemMeta chestMeta = chest.getItemMeta();

        chestMeta.setDisplayName(ChatColor.ITALIC + "Back");

        chest.setItemMeta(chestMeta);

        inventory.setItem(0, chest);

        //Add items to inventory

        int place = 2;

        for (ItemStack item : items) {
            inventory.setItem(place, item);

            place += ((place + 1) % 9.0 == 0) ? 3 : 1;
        }
    }


    private int getInventorySizeFromItemAmount(int amountOfItems) {

        if (amountOfItems < 7) return 9;

        if (amountOfItems > 42) {
            Bukkit.getLogger().warning("[NPCShop] Amount of items per shop cannot exceed 42!");
            throw new IndexOutOfBoundsException();
        }

        return (int) Math.ceil(amountOfItems / 7.0) * 9;

    }
}

package me.Silverwolfg11.NPCShop.listener;

import me.Silverwolfg11.NPCShop.NPCShopPlugin;
import me.Silverwolfg11.NPCShop.objects.NPCInventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class InventoryClickListener implements Listener {

    private NPCShopPlugin plugin;

    public InventoryClickListener(NPCShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        //Only handle NPC Shop Inventories
        if (!(event.getInventory().getHolder() instanceof NPCInventoryHolder)) return;

        ItemStack item = event.getCurrentItem();

        event.setCancelled(true);

        //Prevent the use of clicking on items in the player inventory while the shop is open
        if (event.getClickedInventory() != null && !(event.getClickedInventory().getHolder() instanceof NPCInventoryHolder)) return;

        //Make sure the item is a valid item. All shop items will either have a lore or name, so some type of item meta.
        if (item == null || item.getType().equals(Material.AIR) || !item.hasItemMeta())
            return;

        //Set a holder variable for easier access
        NPCInventoryHolder holder = (NPCInventoryHolder) event.getInventory().getHolder();

        Player player = (Player) event.getWhoClicked();

        ItemMeta meta = item.getItemMeta();

        //Handle Main Inventory
        if (holder.getInventoryType().equals(NPCInventoryHolder.NPCInventoryType.MAIN)) {

            if (isBuyItem(item)) {
                player.openInventory(holder.getNPCShop().getBuyInventory());
            } else if (isSellItem(item)) {
                player.openInventory(holder.getNPCShop().getSellInventory());
            }

            return;
        }

        //Handle player clicking on the back item
        if (item.getType().equals(Material.CHEST) && meta.hasDisplayName() &&
                ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Back")) {

            player.openInventory(holder.getNPCShop().generateMainInventory(plugin.getMainInventory()));
            return;
        }

        //Safe-check an item before handling buy/sell
        if (!meta.hasLore() || meta.getLore().size() < 1) return;

        int productAmount = item.getAmount();
        int priceMultiplier = 1;

        //Right clicking the item will give/sell 2x the amount.
        if (event.getAction() == InventoryAction.PICKUP_HALF) {
            priceMultiplier = 2;
        }
        // Shift clicking the item will give/sell the most amount less than the item's max stack size.
        else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            priceMultiplier = (item.getMaxStackSize() / item.getAmount());
        }

        //Multiply the amount by the multiplier
        productAmount *= priceMultiplier;

        //Get the first line of lore. Should contain the buy price or sell price;
        String line0 = ChatColor.stripColor(meta.getLore().get(0));

        double balance = plugin.getEconomy().getBalance(player);

        //Handle buying
        if (holder.getInventoryType().equals(NPCInventoryHolder.NPCInventoryType.BUY)) {
            //Buy Price: 50.0
            double price = Double.valueOf(line0.substring(11));

            price *= priceMultiplier;

            if (balance < price) {
                player.sendMessage(ChatColor.DARK_RED + "You do not have enough money to buy that!");
                return;
            }

            ItemStack clonedItem;

            //Check if it's a cloned item
            if (isCustomShopItem(meta)) {
                clonedItem = plugin.getCustomItemManager().getItemFromIDMap(getCustomShopItem(meta)).clone();
                removeCustomShopId(clonedItem);
            } else {
                clonedItem = item.clone();

                ItemMeta newItemMeta = clonedItem.getItemMeta();

                newItemMeta.setLore(null);

                clonedItem.setItemMeta(newItemMeta);
            }

            clonedItem.setAmount(productAmount);

            if (!checkSpace(player, clonedItem)) {
                player.sendMessage(ChatColor.DARK_RED + "You do not have enough space for this!");
                return;
            }

            player.getInventory().addItem(clonedItem);

            plugin.getTransactionManager().buyItem(clonedItem.getType(), productAmount);

            plugin.getEconomy().withdrawPlayer(player, price);

            player.sendMessage(ChatColor.GREEN + "Bought " + productAmount + " for $" + shortenedForm(price) + "!");
        }
        //Handle selling
        else if(holder.getInventoryType().equals(NPCInventoryHolder.NPCInventoryType.SELL)) {
            double price = Double.valueOf(line0.substring(12));

            price *= priceMultiplier;

            ItemStack clonedItem;

            if (isCustomShopItem(meta)) {
                clonedItem = plugin.getCustomItemManager().getItemFromIDMap(getCustomShopItem(meta)).clone();
                removeCustomShopId(clonedItem);
            } else {
                clonedItem = new ItemStack(item.getType(), productAmount);
            }

            // This is a special sell feature which allows players to sell all of that item in their inventory
            if (event.getAction() == InventoryAction.UNKNOWN) {
                int maxProductAmount = getMaxProductAmount(player, clonedItem);

                if (maxProductAmount > 0) {
                    int initialProduct = productAmount / priceMultiplier; // Calculate the initial product
                    maxProductAmount = (maxProductAmount / initialProduct) * initialProduct; // Use integer division to get most product.

                    if (maxProductAmount > 0) {
                        productAmount = maxProductAmount;
                        clonedItem.setAmount(productAmount);
                        price = (price / priceMultiplier) * ((double) (maxProductAmount / initialProduct));
                    }
                }
            }

            if (!containsSellItem(player, clonedItem, productAmount)) {
                player.sendMessage(ChatColor.DARK_RED + "You do not have enough of this item to sell!");
                return;
            }

            removeItem(player, clonedItem, productAmount);

            plugin.getTransactionManager().sellItem(clonedItem.getType(), productAmount);

            plugin.getEconomy().depositPlayer(player, price);

            player.sendMessage(ChatColor.GREEN + "Sold " + productAmount + " for $" + shortenedForm(price) + "!");
        }

    }

    private boolean isBuyItem(ItemStack stack) {
        return plugin.getBuyItem().isSimilar(stack);
    }

    private boolean isSellItem(ItemStack stack) {
        return plugin.getSellItem().isSimilar(stack);
    }

    private boolean checkSpace(Player p, ItemStack is) {

        int amount = is.getAmount();

        int increment = is.getMaxStackSize();

        for (ItemStack stack : p.getInventory().getStorageContents()) {
            if (stack == null || stack.getType().equals(Material.AIR)) {
                amount -= increment;
            } else if (is.isSimilar(stack)) {
                amount -= (increment - stack.getAmount());
            }

            if (amount <= 0) return true;
        }

        return false;
    }

    private String shortenedForm(double price) {
        //Checks if the priced floored is equal to the price, if it is return a cast-downed integer otherwise return the double.
        return "" + ((price == Math.floor(price)) ? (int) price : price);
    }

    private int getMaxProductAmount(Player player, ItemStack stack) {
        if (stack.getType().equals(Material.PLAYER_HEAD)) {
            stack = new ItemStack(Material.PLAYER_HEAD, stack.getAmount());
        }

        int size = 0;
        for (ItemStack content : player.getInventory().getStorageContents()) {
            if (content == null || content.getType().equals(Material.AIR)) continue;

            if (content.isSimilar(stack))
                size += content.getAmount();
        }

        return size;
    }

    private boolean containsSellItem(Player player, ItemStack stack, int amount) {
        if (stack.getType().equals(Material.PLAYER_HEAD)) {
            return player.getInventory().contains(Material.PLAYER_HEAD, amount);
        } else
            return player.getInventory().containsAtLeast(stack, amount);
    }

    private void removeItem(Player player, ItemStack stack, int amount) {
        if (stack.getType().equals(Material.PLAYER_HEAD)) {
            removeMaterial(player, amount, Material.PLAYER_HEAD);
        } else
            player.getInventory().removeItem(stack);
    }

    private void removeMaterial(Player player, int amount, Material mat) {

        ItemStack[] contents = player.getInventory().getContents();
        int contentsLength = contents.length;

        ItemStack stack;
        for (int i = 0; i < contentsLength; i++) {
            stack = contents[i];

            if (stack == null || !stack.getType().equals(mat)) continue;

            //If amount is greater than the stack amount, set the stack to null.
            if(amount >= stack.getAmount()) {
                amount -= stack.getAmount();
                player.getInventory().setItem(i, null);
            }
            //If amount is less than stack amount, set stack amount to stack amount - amount.
            else {
                stack.setAmount(stack.getAmount() - amount);
                player.getInventory().setItem(i, stack);
                amount = 0;
            }

            if (amount <= 0) break;
        }

        player.updateInventory();
    }

    private boolean isCustomShopItem(ItemMeta meta) {
        return meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "npcshopid"), PersistentDataType.INTEGER);
    }

    private int getCustomShopItem(ItemMeta meta) {
        return meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "npcshopid"), PersistentDataType.INTEGER);
    }

    private void removeCustomShopId(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();

        meta.getPersistentDataContainer().remove(new NamespacedKey(plugin, "npcshopid"));

        stack.setItemMeta(meta);
    }
}

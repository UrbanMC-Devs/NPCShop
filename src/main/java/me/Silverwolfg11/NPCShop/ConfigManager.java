package me.Silverwolfg11.NPCShop;

import me.Silverwolfg11.NPCShop.objects.NPCShop;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ConfigManager {

    private NPCShopPlugin plugin;

    //Prevent initialization from outside package classes
    protected ConfigManager(NPCShopPlugin plugin) {
        this.plugin = plugin;
        setupFiles();
    }

    private void setupFiles() {
        File configFile = new File("plugins/NPCShop", "config.yml");

        if (!configFile.getParentFile().isDirectory()) {
            configFile.getParentFile().mkdir();
        }

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();

                InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream("config.yml");

                Files.copy(input, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        load(config);
    }

    private void load(YamlConfiguration config) {

        //Clear all current npc shops
        plugin.clearLinks();

        //Load the main inventory
        plugin.setMainInventory(loadMainInventory(config));

        //Load Custom Items
        HashMap<String,ItemStack> customItemMap = plugin.getCustomItemManager().loadCustomItems(config);

        //Get all paths (strings) from path npcshops
        for (String path : config.getConfigurationSection("npcshops").getKeys(false)) {
            int npcID;

            //Make sure the NPC ID is a valid integer
            try {
                npcID = Integer.parseInt(path);
            } catch (NumberFormatException ex) {
                throwError("Cannot load NPC Shop because " + path + " is an invalid NPC ID");
                continue;
            }

            //Get the shop title for the shop
            String shopTitle = config.getString("npcshops." + path + ".title", "Merchant Shop");

            //Create a new NPC shop
            NPCShop newShop = new NPCShop();

            newShop.setTitle(shopTitle);

            // Create two arrays for later use in order to create the buy and sell inventories for the shop.
            List<ItemStack> buyItems = new ArrayList<>(),
                            sellItems = new ArrayList<>();

            for (String itemPath : config.getConfigurationSection("npcshops." + path + ".items").getKeys(false)) {
                String backPath = "npcshops." + path + ".items";

                //Check for space indicators
                if(itemPath.equalsIgnoreCase("empty")) {
                    String emptyInfo = config.getString(backPath + "."  + itemPath);

                    String[] infoSplit = emptyInfo.split(" ");

                    if (infoSplit.length != 2) {
                        throwError("Invalid information to load spaces " + itemPath + " for npc shop " + path);
                        continue;
                    }

                    int buySpaces, sellSpaces;

                    try {
                        buySpaces = Integer.parseInt(infoSplit[0]);
                        sellSpaces = Integer.parseInt(infoSplit[1]);
                    } catch (NumberFormatException ex) {
                        throwError("Buy/sell spaces are not valid integers for " + itemPath + " for npc shop " + path);
                        buySpaces = 0;
                        sellSpaces = 0;
                    }

                    for (int i = 0; i < buySpaces; i++)
                        buyItems.add(new ItemStack(Material.AIR));

                    for (int i = 0; i < sellSpaces; i++)
                        sellItems.add(new ItemStack(Material.AIR));

                    continue;
                }

                //Handle normal shop items

                Material material = null;
                ItemStack stack = null;

                // Make sure the material is valid
                try {
                    material = Material.valueOf(itemPath.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    //Check if itempath is a custom item
                    if (customItemMap.containsKey(itemPath)) {
                        stack = customItemMap.get(itemPath).clone();
                    }
                    //Otherwise throw an error
                    else {
                        throwError("Could not load material for shop item " + itemPath + " for npc shop " + path);
                        continue;
                    }
                }

                if (stack == null)
                 stack = new ItemStack(material);

                String productInfo = config.getString(backPath + "."  + itemPath);

                String[] infoSplit = productInfo.split(" ");

                if (infoSplit.length != 3) {
                    throwError("Invalid information to load shop item " + itemPath + " for npc shop " + path);
                    continue;
                }

                int amount;

                //Make sure the amount is valid
                try {
                    amount = Integer.parseInt(infoSplit[0]);
                } catch (NumberFormatException ex) {
                    throwError("Cannot load amount for shop item " + itemPath + " for npc shop " + path);
                    amount = 1;
                }

                stack.setAmount(amount);

                double buyPrice, sellPrice;

                // Make sure the buy price and the sell price are valid
                try {
                    buyPrice = Double.parseDouble(infoSplit[1]);
                } catch (NumberFormatException ex) {
                    throwError("Cannot load buy price for shop item " + itemPath + " for npc shop " + path);
                    buyPrice = 200;
                }

                try {
                    sellPrice = Double.parseDouble(infoSplit[2]);
                } catch (NumberFormatException ex) {
                    throwError("Cannot load sell price for shop item " + itemPath + " for npc shop " + path);
                    sellPrice = 100;
                }

                ItemMeta meta = stack.getItemMeta();

                //Set the lore to buy price, then clone the item and add it to the buy items array.
                if (buyPrice > 0) {
                    meta.setLore(Collections.singletonList("Buy Price: " + shortenedForm(buyPrice)));
                    stack.setItemMeta(meta);
                    buyItems.add(stack.clone());
                }

                // Do the same as above except for the sell price
                if (sellPrice > 0) {
                    meta.setLore(Collections.singletonList("Sell Price: " + shortenedForm(sellPrice)));
                    stack.setItemMeta(meta);
                    sellItems.add(stack.clone());
                }
            }

            //Create both the buy and sell inventories
            try {
                // Don't create the inventories if the items are empty
                if (!buyItems.isEmpty())
                    newShop.createBuyInventory(buyItems);

                if (!sellItems.isEmpty())
                    newShop.createSellInventory(sellItems);
            } catch (Exception ex) {
                throwError("Error creating inventories for npc shop " + path);
                ex.printStackTrace();
                continue;
            }

            // Check if the NPCShop should use vault banks
            boolean useBank = config.getBoolean("npcshops." + path + ".bank", false);
            newShop.setBankUse(useBank);

            // Add the newly loaded NPC Shop to the link hashmap
            plugin.addNPCShop(npcID, newShop);
        }
    }

    private Inventory loadMainInventory(YamlConfiguration config) {
        ItemStack buyItem = loadItem(config, "maininventory.buy-item");
        ItemStack sellItem = loadItem(config, "maininventory.sell-item");

        // Set the main inventory buy and sell item.
        plugin.setBuyItem(buyItem);
        plugin.setSellItem(sellItem);

        // Get where each item will be placed in the main inventory.
        int buyPlacement = config.getInt("maininventory.buy-item.placement", 0),
                sellPlacement = config.getInt("maininventory.sell-item.placement", 1);

        //Create the inventory with no title, because it's just a reference inventory.
        Inventory mainInventory = Bukkit.createInventory(null, 9, "");

        //Use the placements, to set the buy and sell item in the reference inventory.
        mainInventory.setItem(buyPlacement, buyItem);
        mainInventory.setItem(sellPlacement, sellItem);

        return mainInventory;
    }

    private ItemStack loadItem(YamlConfiguration config, String path) {
        Material material;

        try {
            material = Material.valueOf(config.getString(path + ".material", "diamond").toUpperCase());
        } catch (IllegalArgumentException ex) {
            throwError("Error loading config. Invalid Item material for " + path);
            material = Material.DIAMOND;
        }

        ItemStack stack = new ItemStack(material, 1);

        ItemMeta meta = stack.getItemMeta();

        if (config.contains(path + ".name")) {
            String name = config.getString(path + ".name");
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }

        if (config.contains(path + ".lore")) {
            List<String> configlore = config.getStringList(path + ".lore"), loreList = new ArrayList<>();

            for (String string : configlore) {
                loreList.add(ChatColor.translateAlternateColorCodes('&', string));
            }

            meta.setLore(loreList);
        }

        stack.setItemMeta(meta);

        return stack;
    }


    private String shortenedForm(double price) {
        //Checks if the priced floored is equal to the price, if it is return a cast-downed integer otherwise return the double.
        if (price == Math.floor(price))
            return "" + ((int) price);

        return "" + price;
    }

    private void throwError(String error) {
        plugin.getLogger().warning(error);
    }

    public void reload() {
        setupFiles();
    }


}

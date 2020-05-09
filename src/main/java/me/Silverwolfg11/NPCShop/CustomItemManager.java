package me.Silverwolfg11.NPCShop;

import me.Silverwolfg11.NPCShop.objects.CustomShopItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomItemManager {

    private Map<Integer, CustomShopItem> customItems = new HashMap<>();
    private NPCShopPlugin plugin;

    public CustomItemManager(NPCShopPlugin plugin) {
        this.plugin = plugin;
    }

    HashMap<String, ItemStack> loadCustomItems(YamlConfiguration config) {
        HashMap<String, ItemStack> customItemMap = new HashMap<>();

        if (!customItems.isEmpty()) customItems.clear();

        // If there are no custom items, return the empty map
        if (!config.contains("customitems")) return customItemMap;

        ConfigurationSection section = config.getConfigurationSection("customitems");

        NamespacedKey namespacedKey = new NamespacedKey(plugin, "npcshopid");

        int id = 1;

        for (String itemPath : section.getKeys(false)) {
            String quickpath = "customitems." + itemPath + ".";

            Material mat;

            try {
                mat = Material.valueOf(config.getString(quickpath + "material", "null").toUpperCase());
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().warning("[NPCShop] Error loading material for custom item " + itemPath);
                continue;
            }

            ItemStack stack = new ItemStack(mat);

            ItemMeta meta = stack.getItemMeta();

            if (config.contains(quickpath + "name")) {
                String name = ChatColor.translateAlternateColorCodes('&', config.getString(quickpath + "name", ""));

                meta.setDisplayName(name);
            }

            if (config.contains(quickpath + "lore")) {
                List<String> lore = config.getStringList(quickpath + "lore");

                lore = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());

                if (!lore.isEmpty()) meta.setLore(lore);
            }

            if (config.contains(quickpath + "enchants")) {
                HashMap<Enchantment, Integer> enchantMap = getEnchantmentFromStringList(config.getStringList(quickpath + "lore"));

                if (mat.equals(Material.ENCHANTED_BOOK)) {
                    EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta) meta;

                    enchantMap.forEach((key, value) -> enchantMeta.addStoredEnchant(key, value, true));
                } else
                    enchantMap.forEach((key, value) -> meta.addEnchant(key, value, true));
            }

            meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, id);

            stack.setItemMeta(meta);

            CustomShopItem customItem = new CustomShopItem(stack);

            customItem.setGiveOnBuy(config.getBoolean(quickpath + "giveonbuy", true));
            customItem.setTakeOnSell(config.getBoolean(quickpath + "takeonsell", true));

            List<String> buyCmds = config.getStringList(quickpath + ".buycommands");

            if (!buyCmds.isEmpty())
                customItem.setBuyCommands(buyCmds);

            List<String> sellCmds = config.getStringList(quickpath + ".sellcommands");

            if (!sellCmds.isEmpty())
                customItem.setSellCommands(buyCmds);

            customItems.put(id, customItem);

            id++;

            customItemMap.put(itemPath, stack);
        }

        return customItemMap;
    }

    private HashMap<Enchantment, Integer> getEnchantmentFromStringList(List<String> stringList) {
        HashMap<Enchantment, Integer> enchantMap = new HashMap<>();

        for (String enchantLine : stringList) {
            if (!enchantLine.contains(" ")) {
                Bukkit.getLogger().warning("[NPCShop] Error loading enchantment: " + enchantLine + " because no spaces exist to parse!");
                continue;
            }

            String[] split = enchantLine.split(" ");

            if (split.length > 2) {
                Bukkit.getLogger().warning("[NPCShop] Error loading enchantment: " + enchantLine + " because there are too many spaces to parse!");
                continue;
            }

            Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(split[0].toLowerCase()));

            if (ench == null) {
                Bukkit.getLogger().warning("[NPCShop] Error loading enchantment: " + enchantLine + " due to invalid enchantment name!");
                continue;
            }

            int level;

            try {
                level = Integer.parseInt(split[1]);
            } catch (NumberFormatException ex) {
                Bukkit.getLogger().warning("[NPCShop] Error loading enchantment: " + enchantLine + " due to invalid enchantment level!");
                continue;
            }

            enchantMap.put(ench, level);
        }

        return enchantMap;
    }

    public CustomShopItem getItemFromIDMap(int id) {
        return customItems.get(id);
    }

}

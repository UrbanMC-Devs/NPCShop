package me.Silverwolfg11.NPCShop;

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

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CustomItemManager {

    private HashMap<Integer, ItemStack> customItem = new HashMap<>();
    private boolean protLib;
    private ProtocolNBT protNBTManager;

    public CustomItemManager() {
        protLib = Bukkit.getPluginManager().getPlugin("ProtocolLib") != null;

        if (protLib) protNBTManager = new ProtocolNBT();
    }


    HashMap<String, ItemStack> loadCustomItems(YamlConfiguration config) {
        HashMap<String, ItemStack> customItemMap = new HashMap<>();

        if (!protLib) {
            Bukkit.getLogger().warning("[NPCShop] Cannot load custom items because ProtocolLib is missing!");
            return customItemMap;
        }

        if (!config.contains("customitems")) return customItemMap;

        ConfigurationSection section = config.getConfigurationSection("customitems");

        for (String itemPath : section.getKeys(false)) {
            String quickpath = "customitems." + itemPath + ".";

            Material mat;

            try {
                mat = Material.valueOf(config.getString(quickpath + "material", "null").toUpperCase());
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().warning("[NPCShop] Error loading material for custom item " + itemPath);
                continue;
            }

            int uuid = config.getInt(quickpath + "uuid", -1);

            if (uuid == -1) {
                Bukkit.getLogger().warning("[NPCShop] Error loading UUID for custom item " + itemPath);
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

            stack.setItemMeta(meta);

            customItem.put(uuid, stack);

            customItemMap.put(itemPath, protNBTManager.setCustomShopItemID(stack, uuid));
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
                level = Integer.valueOf(split[1]);
            } catch (NumberFormatException ex) {
                Bukkit.getLogger().warning("[NPCShop] Error loading enchantment: " + enchantLine + " due to invalid enchantment level!");
                continue;
            }

            enchantMap.put(ench, level);
        }

        return enchantMap;
    }

    public ItemStack getItemFromItemUUID(ItemStack stack) {
        if (!protLib) return null;
        return customItem.get(protNBTManager.getShopItemID(stack));
    }

    public boolean isCustomShopItem(ItemStack stack) {
        if (!protLib) return false;
        return protNBTManager.checkIfCustomShopItem(stack);
    }

}

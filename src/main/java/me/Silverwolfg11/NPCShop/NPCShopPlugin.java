package me.Silverwolfg11.NPCShop;

import me.Silverwolfg11.NPCShop.command.NPCShopCommand;
import me.Silverwolfg11.NPCShop.listener.InventoryClickListener;
import me.Silverwolfg11.NPCShop.listener.NPCClickListener;
import me.Silverwolfg11.NPCShop.listener.TimerLibListener;
import me.Silverwolfg11.NPCShop.objects.NPCShop;
import me.Silverwolfg11.NPCShop.transactiondata.TransactionManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class NPCShopPlugin extends JavaPlugin {

    private Economy econ;

    private ConfigManager configManager;
    private CustomItemManager customItemManager;

    private TransactionManager dataManager;

    private HashMap<Integer, NPCShop> shopLink = new HashMap<>();

    //Store the buy item, the sell item, and the maininventory here.
    private ItemStack buyItem, sellItem;
    private Inventory mainInventory;

    public void onEnable() {

        if (!setupEconomy() ) {
            Bukkit.getLogger().severe( "[NPCShop] Disabling due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerListeners();

        customItemManager = new CustomItemManager(this);
        configManager = new ConfigManager(this);

        dataManager = new TransactionManager();

        getCommand("npcshop").setExecutor(new NPCShopCommand(this));
    }

    public void onDisable() {
        if(dataManager != null)
            dataManager.onDisable();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        econ = rsp.getProvider();
        return econ != null;
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NPCClickListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("TimerLib") != null)
            Bukkit.getPluginManager().registerEvents(new TimerLibListener(this), this);
    }

    public ItemStack getBuyItem() { return buyItem; }

    void setBuyItem(ItemStack item) { buyItem = item; }

    public ItemStack getSellItem() { return sellItem; }

    void setSellItem(ItemStack item) { sellItem = item; }

    public Inventory getMainInventory() { return mainInventory; }

    void setMainInventory(Inventory inventory) {
        this.mainInventory = inventory;
    }

    public Economy getEconomy() { return econ; }

    public NPCShop getNPCShopFromID(int id) {
        return shopLink.get(id);
    }

    void addNPCShop(int id, NPCShop shop) {
        shopLink.put(id, shop);
    }

    void clearLinks() {
        if (!shopLink.isEmpty())
            shopLink.clear();
    }

    public void reload() { configManager.reload(); }

    public CustomItemManager getCustomItemManager() { return customItemManager; }

    public TransactionManager getTransactionManager() { return dataManager; }

}

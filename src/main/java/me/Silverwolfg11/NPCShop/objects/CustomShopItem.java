package me.Silverwolfg11.NPCShop.objects;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CustomShopItem {

    private ItemStack displayStack;

    private List<String> buyCommands;
    private List<String> sellCommands;

    private boolean giveOnBuy;
    private boolean takeOnSell;
    private boolean displayItem;


    public CustomShopItem(ItemStack displayStack) {
        this.displayStack = displayStack;
    }

    public void setBuyCommands(List<String> cmds) {
        this.buyCommands = cmds;
    }

    public void setSellCommands(List<String> cmds) {
        this.sellCommands = cmds;
    }

    public void setGiveOnBuy(boolean b) {
        this.giveOnBuy = b;
    }

    public boolean giveOnBuy() {
        return giveOnBuy;
    }

    public void setTakeOnSell(boolean b) {
        this.takeOnSell = b;
    }

    public boolean takeOnSell() {
        return takeOnSell;
    }

    public void setDisplayItemOnly(boolean b) {
        this.displayItem = b;
        this.giveOnBuy = !b;
        this.takeOnSell = !b;
    }

    public boolean isDisplayItemOnly() {
        return displayItem;
    }

    public ItemStack cloneDisplay() {
        return displayStack.clone();
    }

    public void executeBuyCommands(String playerName) {
        executeCommands(playerName, buyCommands);
    }

    public void executeSellCommands(String playerName) {
        executeCommands(playerName, sellCommands);
    }

    private void executeCommands(String playerName, List<String> cmds) {
        if (cmds != null) {
            for (String cmd : cmds) {
                cmd = cmd.replace("<player>", playerName);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }
    }



}

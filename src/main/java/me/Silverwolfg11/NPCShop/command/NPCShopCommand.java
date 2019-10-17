package me.Silverwolfg11.NPCShop.command;

import me.Silverwolfg11.NPCShop.NPCShopPlugin;
import me.Silverwolfg11.NPCShop.objects.NPCShop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NPCShopCommand implements CommandExecutor {

    private NPCShopPlugin plugin;

    public NPCShopCommand(NPCShopPlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("npcshop.command")) {
            sendColor(sender, "&4You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sendColor(sender, "&6 === &aNPCShop &6=== \n&bDeveloped by Silverwolfg11\n&bVersion " + plugin.getDescription().getVersion());

            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reload();

            sendColor(sender, "&aNPCShop has been reloaded!");
            return true;
        }

        if (args[0].equalsIgnoreCase("save")) {
            plugin.getTransactionManager().getTask().runTaskAsynchronously(plugin);

            sendColor(sender, "&aNPCShop transaction data has been saved!");
            return true;
        }

        return true;
    }


    private void sendColor(CommandSender sender, String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (sender instanceof Player)
            sender.sendMessage(message);

        else sender.sendMessage(ChatColor.stripColor(message));
    }
}

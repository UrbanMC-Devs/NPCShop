package me.Silverwolfg11.NPCShop.transactiondata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class TransactionRunnable extends BukkitRunnable {

    private HashMap<Material, ItemTransaction> transactionmap;

    public TransactionRunnable(Map<Material, ItemTransaction> map) {
        transactionmap = new HashMap<>(map);
    }

    @Override
    public void run() {
        Bukkit.getLogger().info("[NPCShop] Saving transaction data!");

        Map<Material, ItemTransaction> map = null;

        final String date = new SimpleDateFormat("MMddyyyy").format(new Date());

        final File FILE = new File("plugins/NPCShop/transactions/", "data_" + date + ".json");

        if (!FILE.getParentFile().isDirectory()) {
            FILE.getParentFile().mkdir();
        }

        if (FILE.exists()) {
            try (Scanner scanner = new Scanner(FILE)) {

                map = MapSerializer.deserialize(scanner.nextLine());
            } catch (Exception e) {
                if (!(e instanceof NoSuchElementException)) {
                    e.printStackTrace();
                }
            }
        }

        if (map == null) {
            map = new HashMap<>();
        }

        for (Map.Entry<Material, ItemTransaction> entry : transactionmap.entrySet()) {
            map.merge(entry.getKey(), entry.getValue(), (original, transaction) -> {
                original.bought(transaction.getBuys());
                original.sold(transaction.getSells());
                return original;
            });
        }

        try(PrintWriter writer = new PrintWriter(FILE)) {
            writer.write(MapSerializer.serialize(map));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Release
        transactionmap.clear(); // Release map
        transactionmap = null;
        map.clear();
    }
}

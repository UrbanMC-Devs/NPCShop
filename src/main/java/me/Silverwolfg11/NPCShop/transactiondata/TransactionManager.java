package me.Silverwolfg11.NPCShop.transactiondata;

import org.bukkit.Material;

import java.util.concurrent.ConcurrentHashMap;

public class TransactionManager {

    private ConcurrentHashMap<Material, ItemTransaction> queue = new ConcurrentHashMap<>();

    public void sellItem(Material material, int amount) {
        ItemTransaction transaction = queue.get(material);

        if (transaction != null) {
            transaction.sold(amount);
        } else {
            transaction = new ItemTransaction();
            transaction.sold(amount);
            queue.put(material, transaction);
        }
    }

    public void buyItem(Material material, int amount) {
        ItemTransaction transaction = queue.get(material);

        if (transaction != null) {
            transaction.bought(amount);
        } else {
            transaction = new ItemTransaction();
            transaction.bought(amount);
            queue.put(material, transaction);
        }
    }

    public TransactionRunnable getTask() {
        final TransactionRunnable runnable =  new TransactionRunnable(queue);
        queue.clear();
        return runnable;
    }

    public void onDisable() {
        new TransactionRunnable(queue).run();
    }

}

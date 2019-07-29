package me.Silverwolfg11.NPCShop;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class ProtocolNBT {

    public boolean checkIfCustomShopItem(ItemStack stack) {
        stack = MinecraftReflection.getBukkitItemStack(stack);

        NbtWrapper wrappedTag = NbtFactory.fromItemTag(stack);

        NbtCompound tag = NbtFactory.fromNMSCompound(wrappedTag.getHandle());

        return  (tag.containsKey("customshopitem"));
    }

    public int getShopItemID(ItemStack stack) {
        stack = MinecraftReflection.getBukkitItemStack(stack);

        NbtWrapper wrappedTag = NbtFactory.fromItemTag(stack);

        NbtCompound tag = NbtFactory.fromNMSCompound(wrappedTag.getHandle());

        return tag.getInteger("customshopitem");
    }

    public ItemStack setCustomShopItemID(ItemStack stack, int UUID) {
        stack = MinecraftReflection.getBukkitItemStack(stack);

        NbtWrapper wrappedTag = NbtFactory.fromItemTag(stack);

        NbtCompound tag = NbtFactory.fromNMSCompound(wrappedTag.getHandle());

        tag.put("customshopitem", UUID);

        NbtFactory.setItemTag(stack, tag);

        return stack;
    }

}

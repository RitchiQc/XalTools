package me.serbob.commons.utils.nbt;

import de.tr7zw.changeme.nbtapi.NBT;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.inventory.ItemStack;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NBTUtils {
    @Getter(lazy = true)
    private static final NBTUtils instance = new NBTUtils();

    private static final String SECONDS_KEY = "secondsRemaining";

    public boolean hasNbt(ItemStack item, String nbt) {
        return NBT.readNbt(item).getBoolean(nbt);
    }

    public boolean hasTag(ItemStack item, String tag) {
        return NBT.readNbt(item).hasTag(tag);
    }

    public void modifyNbt(ItemStack item, String nbt) {
        NBT.modify(item, readWriteItemNBT -> {
            readWriteItemNBT.setBoolean(nbt, true);
        });
    }

    public boolean hasSecondsRemaining(ItemStack item) {
        return NBT.readNbt(item).hasTag(SECONDS_KEY);
    }

    public long getSecondsRemaining(ItemStack item) {
        return NBT.readNbt(item).getLong(SECONDS_KEY);
    }

    public void setSecondsRemaining(ItemStack item, long seconds) {
        NBT.modify(item, nbt -> {
            nbt.setLong(SECONDS_KEY, seconds);
        });
    }
}

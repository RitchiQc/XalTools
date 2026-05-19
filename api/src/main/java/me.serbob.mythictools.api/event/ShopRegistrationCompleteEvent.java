package me.serbob.mythictools.api.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.serbob.mythictools.api.shop.ShopHook;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ShopRegistrationCompleteEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Map<String, ShopHook> registeredShops;
    private final ShopHook activeShop;

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

package me.serbob.xaltools.api.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.serbob.xaltools.api.shop.ShopHook;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
@AllArgsConstructor
public class XalToolsLoadedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

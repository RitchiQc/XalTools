package me.serbob.mythictools.commands.args.impl;

import me.serbob.commons.utils.message.ChatUtil;
import me.serbob.mythictools.api.currency.CurrencyHook;
import me.serbob.mythictools.api.currency.CurrencyManager;
import me.serbob.mythictools.api.permission.PermissionHook;
import me.serbob.mythictools.api.permission.PermissionManager;
import me.serbob.mythictools.api.shop.ShopHook;
import me.serbob.mythictools.api.shop.ShopManager;
import me.serbob.mythictools.commands.args.CommandArgs;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Hooks implements CommandArgs {
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Set<PermissionHook> permissionHooks = PermissionManager.getInstance().getActivePermissionHooks();
        ShopHook activeShop = ShopManager.getInstance().getActiveShop();
        CurrencyHook activeCurrency = CurrencyManager.getInstance().getActiveCurrency();

        sender.sendMessage(ChatUtil.c("&5&m                                                    "));
        sender.sendMessage(ChatUtil.c("&dMythicTools Hooks"));
        sender.sendMessage(ChatUtil.c("&5&m                                                    "));
        sender.sendMessage("");

        sender.sendMessage(ChatUtil.c("&dEconomy"));
        sender.sendMessage(ChatUtil.c("  &7Shop: " + (activeShop != null ? "&d" + activeShop.getName() : "&8None")));
        sender.sendMessage(ChatUtil.c("  &7Currency: " + (activeCurrency != null ? "&d" + activeCurrency.getName() : "&8None")));
        sender.sendMessage("");

        sender.sendMessage(ChatUtil.c("&dPermissions"));

        if (permissionHooks.isEmpty()) {
            sender.sendMessage(ChatUtil.c("  &8No active permission hooks"));
        } else {
            for (PermissionHook hook : permissionHooks) {
                sender.sendMessage(ChatUtil.c("  &f" + hook.pluginName() + " &a✓"));
            }
        }

        sender.sendMessage("");
        sender.sendMessage(ChatUtil.c("&7Total Integrations: &d" +
                (permissionHooks.size() + (activeShop != null ? 1 : 0) + (activeCurrency != null ? 1 : 0))));
        sender.sendMessage(ChatUtil.c("&5&m                                                    "));

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

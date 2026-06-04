package me.serbob.xaltools;

import de.tr7zw.changeme.nbtapi.NBT;
import lombok.Getter;
import me.serbob.commons.Commons;
import me.serbob.xaltools.abilities.Abilities;
import me.serbob.xaltools.api.currency.CurrencyManager;
import me.serbob.xaltools.api.event.CurrencyRegistrationCompleteEvent;
import me.serbob.xaltools.api.event.XalToolsLoadedEvent;
import me.serbob.xaltools.api.event.ShopRegistrationCompleteEvent;
import me.serbob.xaltools.api.permission.PermissionHook;
import me.serbob.xaltools.api.permission.PermissionManager;
import me.serbob.xaltools.api.shop.ShopManager;
import me.serbob.xaltools.commands.XalToolsCommand;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.commons.enums.Messages;
import me.serbob.xaltools.hooks.HooksLoader;
import me.serbob.xaltools.hooks.currency.VaultCurrency;
import me.serbob.xaltools.hooks.shop.DonutWorthBridge;
import me.serbob.xaltools.hooks.shop.EconomyShopGuiBridge;
import me.serbob.xaltools.hooks.shop.PrimeSellerBridge;
import me.serbob.xaltools.hooks.shop.ShopGuiPlusBridge;
import me.serbob.xaltools.manager.BlacklistManager;
import me.serbob.xaltools.manager.HooksManager;
import me.serbob.xaltools.manager.ItemTrackerManager;
import me.serbob.xaltools.manager.ProtocolLibManager;
import me.serbob.xaltools.manager.SelfDestructManager;
import me.serbob.xaltools.manager.ToolManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public final class XalTools extends JavaPlugin implements Listener {
    private static @Getter XalTools instance;

    final String RESET = "\u001B[0m";
    final String BOLD_WHITE = "\u001B[1;97m";
    final String LIGHT_PURPLE = "\u001B[38;5;141m"; {}
    final String GRAY = "\u001B[90m";

    @Override
    public void onEnable() {
        log("\n\n" +
        LIGHT_PURPLE + " __   __     _ _______                _      \n" +
        LIGHT_PURPLE + " \\ \\ / /    | |__   __|              | |     \n" +
        LIGHT_PURPLE + "  \\ V / __ _| |  | | ___   ___   ___ | |___  " + 
        BOLD_WHITE + "    XalTools " + this.getDescription().getVersion() + "\n" +
        LIGHT_PURPLE + "   > < / _` | |  | |/ _ \\ / _ \\ / _ \\| / __| " + 
        GRAY + "    Supports Paper & Folia\n" +
        LIGHT_PURPLE + "  / . \\ (_| | |  | | (_) | (_) | (_) | \\__ \\\n" +
        LIGHT_PURPLE + " /_/ \\_\\__,_|_|  |_|\\___/ \\___/ \\___/|_|___/\n" + RESET + "\n"
        );

        instance = this;
        instance.saveDefaultConfig();
        Commons.getInstance().load(this);

        Arrays.stream(ConfigSelector.values()).collect(Collectors.toList())
                        .forEach(ConfigSelector::initialize);


        for (Abilities ability : Abilities.values()) {
            Bukkit.getPluginManager().registerEvents(ability.getAbility(), this);
        }

        Bukkit.getPluginManager().registerEvents(this, this);

        NBT.preloadApi();

        Messages.load(ConfigSelector.MESSAGES);

        getCommand("xaltools").setExecutor(new XalToolsCommand());
        getCommand("xaltools").setTabCompleter(new  XalToolsCommand());

        SelfDestructManager.getInstance().load();
        ItemTrackerManager.getInstance().initialize();

        HooksManager.getInstance().load();
        loadHooks();

        SelfDestructManager.getInstance().initialize();
        ProtocolLibManager.getInstance().initialize(this);

        startExpiredItemsCleanup();

        Bukkit.getServer().getPluginManager().callEvent(new XalToolsLoadedEvent());

        load();
    }

    @Override
    public void onDisable() {}

    public void load() {
        ToolManager.getInstance().register();
        BlacklistManager.getInstance().load();
    }

    private void loadHooks() {
        switch (HooksManager.getInstance().getCurrentShop().toLowerCase()) {
            case "economyshopgui-premium":
            case "economyshopguipremium":
            case "economyshopgui":
                ShopManager.getInstance().registerShop(new EconomyShopGuiBridge());
                break;

            case "shopguiplus":
            case "shopgui+":
                ShopManager.getInstance().registerShop(new ShopGuiPlusBridge());
                break;

            case "primeseller":
                ShopManager.getInstance().registerShop(new PrimeSellerBridge());
                break;

            case "donutworth":
                ShopManager.getInstance().registerShop(new DonutWorthBridge());
                break;
        }

        Bukkit.getPluginManager().callEvent(new ShopRegistrationCompleteEvent(
                ShopManager.getInstance().getRegisteredShops(),
                ShopManager.getInstance().getActiveShop()
        ));

        ShopManager.getInstance().setActiveShop(HooksManager.getInstance().getCurrentShop());
        ShopManager.getInstance().init();

        switch (HooksManager.getInstance().getCurrentCurrency().toLowerCase()) {
            case "vault":
                CurrencyManager.getInstance().registerCurrency(new VaultCurrency());
                break;
        }

        Bukkit.getPluginManager().callEvent(new CurrencyRegistrationCompleteEvent(
                CurrencyManager.getInstance().getRegisteredCurrencies(),
                CurrencyManager.getInstance().getActiveCurrency()
        ));

        CurrencyManager.getInstance().setActiveCurrency(HooksManager.getInstance().getCurrentCurrency());

        if (CurrencyManager.getInstance().getActiveCurrency() == null) {
            log("------------");
            log("------------");
            log("CURRENCY NOT FOUND");
            log("------------");
            log("------------");
        }

        loadPermissionHooks();
    }

    private void loadPermissionHooks() {
        Map<String, Class<? extends PermissionHook>> permissionHookList = new HashMap() {{
//            put("Towny", TownyPermission.class);
        }};

        List<Map.Entry<String, Class<? extends PermissionHook>>> allHooks = HooksLoader.loadHooks();
        allHooks.addAll(permissionHookList.entrySet());

        allHooks.forEach(entry -> {
            String pluginName = entry.getKey();
            Class<? extends PermissionHook> permissionHookClass = entry.getValue();

            if (!Bukkit.getPluginManager().isPluginEnabled(pluginName))
                return;

            PermissionHook permissionHook = null;

            try {
                permissionHook = permissionHookClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            if (!permissionHook.shouldLoad())
                return;

            PermissionManager.getInstance().addPermissionHook(permissionHook);
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        SelfDestructManager.getInstance().onPlayerJoin(event.getPlayer());
    }

    private void startExpiredItemsCleanup() {
        Commons.getFoliaLib().getScheduler().runTimerAsync(asyncTask -> {
            ItemTrackerManager.getInstance().removeExpiredItems();
        }, 6000L, 6000L); // Every 5 minutes (6000 ticks)
    }

    private void log(String msg) {
        Bukkit.getConsoleSender().sendMessage(msg);
    }
}

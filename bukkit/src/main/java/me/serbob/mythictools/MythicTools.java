package me.serbob.mythictools;

import de.tr7zw.changeme.nbtapi.NBT;
import lombok.Getter;
import me.serbob.commons.Commons;
import me.serbob.mythictools.abilities.Abilities;
import me.serbob.mythictools.abilities.custom.CustomAbilityManager;
import me.serbob.mythictools.api.currency.CurrencyManager;
import me.serbob.mythictools.api.event.CurrencyRegistrationCompleteEvent;
import me.serbob.mythictools.api.event.MythicToolsLoadedEvent;
import me.serbob.mythictools.api.event.ShopRegistrationCompleteEvent;
import me.serbob.mythictools.api.permission.PermissionHook;
import me.serbob.mythictools.api.permission.PermissionManager;
import me.serbob.mythictools.api.shop.ShopManager;
import me.serbob.mythictools.commands.MythicToolsCommand;
import me.serbob.commons.enums.ConfigSelector;
import me.serbob.commons.enums.DirectorySelector;
import me.serbob.commons.enums.Messages;
import me.serbob.mythictools.hooks.HooksLoader;
import me.serbob.mythictools.hooks.currency.VaultCurrency;
import me.serbob.mythictools.hooks.shop.DonutWorthBridge;
import me.serbob.mythictools.hooks.shop.EconomyShopGuiBridge;
import me.serbob.mythictools.hooks.shop.PrimeSellerBridge;
import me.serbob.mythictools.hooks.shop.ShopGuiPlusBridge;
import me.serbob.mythictools.manager.BlacklistManager;
import me.serbob.mythictools.manager.HooksManager;
import me.serbob.mythictools.manager.SelfDestructManager;
import me.serbob.mythictools.manager.ToolManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public final class MythicTools extends JavaPlugin implements Listener {
    private static @Getter MythicTools instance;

    final String RESET = "\u001B[0m";
    final String BOLD_WHITE = "\u001B[1;97m";
    final String LIGHT_PURPLE = "\u001B[38;5;141m"; {}
    final String GRAY = "\u001B[90m";

    @Override
    public void onEnable() {
        log("\n\n" +
                LIGHT_PURPLE + "  __  __       _   _     _      _____            _     \n" +
                LIGHT_PURPLE + " |  \\/  |     | | | |   (_)    |_   _|          | |    \n" +
                LIGHT_PURPLE + " | \\  / |_   _| |_| |__  _  ___  | |  ___   ___ | |___ " +
                BOLD_WHITE + "    MythicTools " + LIGHT_PURPLE + this.getDescription().getVersion() + "\n" +
                LIGHT_PURPLE + " | |\\/| | | | | __| '_ \\| |/ __| | | / _ \\ / _ \\| / __|" +
                GRAY + "    Supports Paper & Folia\n" +
                LIGHT_PURPLE + " | |  | | |_| | |_| | | | | (__  | || (_) | (_) | \\__ \\\n" +
                LIGHT_PURPLE + " |_|  |_|\\__, |\\__|_| |_|_|\\___| |_| \\___/ \\___/|_|___/\n" +
                LIGHT_PURPLE + "          __/ |                                         \n" +
                LIGHT_PURPLE + "         |___/                                          " + RESET + "\n"
        );

        instance = this;
        instance.saveDefaultConfig();
        Commons.getInstance().load(this);

        Arrays.stream(ConfigSelector.values()).collect(Collectors.toList())
                        .forEach(ConfigSelector::initialize);

        Arrays.stream(DirectorySelector.values()).collect(Collectors.toList())
                .forEach(DirectorySelector::initialize);

        CustomAbilityManager.getInstance().load();

        for (Abilities ability : Abilities.values()) {
            Bukkit.getPluginManager().registerEvents(ability.getAbility(), this);
        }

        Bukkit.getPluginManager().registerEvents(this, this);

        NBT.preloadApi();

        Messages.load(ConfigSelector.MESSAGES);

        getCommand("mythictools").setExecutor(new MythicToolsCommand());
        getCommand("mythictools").setTabCompleter(new  MythicToolsCommand());

        SelfDestructManager.getInstance().load();

        HooksManager.getInstance().load();
        loadHooks();

        SelfDestructManager.getInstance().initialize();

        Bukkit.getServer().getPluginManager().callEvent(new MythicToolsLoadedEvent());

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

    private void log(String msg) {
        Bukkit.getConsoleSender().sendMessage(msg);
    }
}

package me.serbob.xaltools.api.currency;

import org.bukkit.OfflinePlayer;

public interface CurrencyHook {
    /**
     * Get the name of the currency plugin
     */
    String getName();

    /**
     * Initialize the currency hook
     */
    void init();

    /**
     * Get player's balance
     */
    double getBalance(OfflinePlayer player);

    /**
     * Remove amount from player's balance
     */
    void withdraw(OfflinePlayer player, double amount);

    /**
     * Add amount to player's balance
     */
    void deposit(OfflinePlayer player, double amount);
}

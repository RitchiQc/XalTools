package me.serbob.xaltools.api.currency;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyManager {
    @Getter(lazy = true)
    private static final CurrencyManager instance = new CurrencyManager();

    @Getter
    private final Map<String, CurrencyHook> registeredCurrencies = new HashMap<>();
    private CurrencyHook activeCurrency = null;

    /**
     * Register a new currency integration
     * @param currencyHook The currency hook to register
     */
    public void registerCurrency(CurrencyHook currencyHook) {
        registeredCurrencies.put(currencyHook.getName().toLowerCase(), currencyHook);
    }

    /**
     * Set active currency from config
     * @param currencyName Currency name from config
     * @return true if currency was set successfully
     */
    public boolean setActiveCurrency(String currencyName) {
        if (currencyName.equalsIgnoreCase("none")) {
            activeCurrency = null;
            return true;
        }

        CurrencyHook currency = registeredCurrencies.get(currencyName.toLowerCase());
        if (currency != null) {
            activeCurrency = currency;
            currency.init();
            return true;
        }
        return false;
    }

    /**
     * Get player's balance
     * @param player Player to check
     * @return Balance or 0 if no currency active
     */
    public double getBalance(Player player) {
        if (activeCurrency == null) return 0;
        return activeCurrency.getBalance(player);
    }

    /**
     * Remove amount from player's balance
     * @param player Player to withdraw from
     * @param amount Amount to withdraw
     */
    public void withdraw(Player player, double amount) {
        if (activeCurrency != null) {
            activeCurrency.withdraw(player, amount);
        }
    }

    /**
     * Add amount to player's balance
     * @param player Player to deposit to
     * @param amount Amount to deposit
     */
    public void deposit(Player player, double amount) {
        if (activeCurrency != null) {
            activeCurrency.deposit(player, amount);
        }
    }

    /**
     * Get currently active currency
     * @return Active currency or null if none
     */
    public CurrencyHook getActiveCurrency() {
        return activeCurrency;
    }
}

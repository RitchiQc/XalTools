package me.serbob.xaltools.manager;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.serbob.commons.Commons;
import me.serbob.xaltools.XalTools;
import me.serbob.xaltools.abilities.Abilities;
import me.serbob.xaltools.abilities.AbstractAbility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ProtocolLibManager {
    private static ProtocolLibManager instance;
    private ProtocolManager protocolManager;

    public static ProtocolLibManager getInstance() {
        if (instance == null) {
            instance = new ProtocolLibManager();
        }
        return instance;
    }

    public void initialize(XalTools plugin) {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            plugin.getLogger().info("ProtocolLib not found. Right-click in air with blocks will not work.");
            return;
        }

        protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.USE_ITEM) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if (player == null) return;

                EnumWrappers.Hand handWrapper = event.getPacket().getHands().read(0);
                EquipmentSlot hand = handWrapper == EnumWrappers.Hand.OFF_HAND
                        ? EquipmentSlot.OFF_HAND
                        : EquipmentSlot.HAND;

                ItemStack tool = hand == EquipmentSlot.OFF_HAND
                        ? player.getInventory().getItemInOffHand()
                        : player.getInventory().getItemInMainHand();

                if (tool == null || tool.getType().isAir()) return;

                // Only handle blocks here - items are already handled by Bukkit's PlayerInteractEvent
                if (!tool.getType().isBlock()) return;

                // Schedule on the player's regional thread for Folia compatibility
                Commons.getFoliaLib().getScheduler().runAtEntity(player, entityTask -> {
                    ItemStack currentTool = hand == EquipmentSlot.OFF_HAND
                            ? player.getInventory().getItemInOffHand()
                            : player.getInventory().getItemInMainHand();

                    if (currentTool == null || currentTool.getType().isAir()) return;
                    if (!currentTool.getType().isBlock()) return;

                    for (Abilities ability : Abilities.values()) {
                        AbstractAbility abilityInstance = ability.getAbility();
                        if (abilityInstance.hasAbility(currentTool)) {
                            abilityInstance.triggerAirInteract(player, hand, currentTool);
                            break;
                        }
                    }
                });
            }
        });

        plugin.getLogger().info("ProtocolLib integration enabled. Right-click in air with blocks is now supported.");
    }
}

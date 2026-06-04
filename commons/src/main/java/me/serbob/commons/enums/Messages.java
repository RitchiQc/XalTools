package me.serbob.commons.enums;

import lombok.Getter;
import me.serbob.commons.utils.message.ActionBarUtils;
import me.serbob.commons.utils.message.ChatUtil;
import me.serbob.commons.utils.message.Replaceable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static org.bukkit.Bukkit.getLogger;

public enum Messages {
    SELLAXE_SOLD("sellaxe.sold"),
    MAGNET_ENABLED("magnet.enabled"),
    MAGNET_DISABLED("magnet.disabled"),
    ;

    private final String path;
    private @Getter String message;
    private List<String> messageList;

    Messages(String path, String defaultMessage) {
        this.path = path;
        this.message = defaultMessage;
    }

    Messages(String path) {
        this(path, "");
    }

    public List<String> getMessageList() {
        return messageList != null ? messageList : Collections.emptyList();
    }

    public String[] getMessageArray() {
        if (messageList != null) {
            return messageList.toArray(new String[0]);
        }
        return new String[]{message};
    }

    public String getLine(int index) {
        if (messageList != null && index < messageList.size()) {
            return messageList.get(index);
        }
        if (index == 0 && message != null) {
            return message;
        }
        return "";
    }

    public void sendBoth(Player player) {
        if (message != null && !message.isEmpty()) {
            player.sendMessage(ChatUtil.c(message));
            ActionBarUtils.sendActionBarMessage(player, message);
        }
    }

    public void sendBoth(Player player, Replaceable replaceable) {
        if (message != null && !message.isEmpty()) {
            player.sendMessage(replaceable.replace(message));
            ActionBarUtils.sendActionBarMessage(player, replaceable.replace(message));
        }
    }

    public void sendBoth(Player player, String... placeholders) {
        if (message != null && !message.isEmpty()) {
            Replaceable replaceable = Replaceable.inst(placeholders);
            player.sendMessage(replaceable.replace(message));
            ActionBarUtils.sendActionBarMessage(player, replaceable.replace(message));
        }
    }

    public void send(CommandSender player) {
        if (message != null && !message.isEmpty()) {
            player.sendMessage(ChatUtil.c(message));
        }
    }

    public void send(CommandSender player, Replaceable replaceable) {
        if (message != null && !message.isEmpty()) {
            player.sendMessage(replaceable.replace(message));
        }
    }

    public void send(CommandSender player, String... placeholders) {
        if (message != null && !message.isEmpty()) {
            Replaceable replaceable = Replaceable.inst(placeholders);
            player.sendMessage(replaceable.replace(message));
        }
    }

    public void sendActionBar(Player player) {
        if (message != null && !message.isEmpty()) {
            ActionBarUtils.sendActionBarMessage(player, ChatUtil.c(message));
        }
    }

    public void sendActionBar(Player player, Replaceable replaceable) {
        if (message != null && !message.isEmpty()) {
            ActionBarUtils.sendActionBarMessage(player, replaceable.replace(message));
        }
    }

    public void sendActionBar(Player player, String... placeholders) {
        if (message != null && !message.isEmpty()) {
            Replaceable replaceable = Replaceable.inst(placeholders);
            ActionBarUtils.sendActionBarMessage(player, replaceable.replace(message));
        }
    }

    public static void load(ConfigSelector config) {
        config.reload();
        int missingMessages = 0;
        boolean configChanged = false;

        for (Messages value : values()) {
            if (config.getConfig().get(value.path) == null) {
                missingMessages++;
                getLogger().info("[XalTools] Adding missing message: " + value.path);

                if (value.messageList != null && value.messageList.size() > 1) {
                    config.getConfig().set(value.path, value.messageList);
                } else {
                    config.getConfig().set(value.path, value.message != null ? value.message : "");
                }
                configChanged = true;
            } else {
                List<String> configMessageList = config.getConfig().getStringList(value.path);
                if (!configMessageList.isEmpty() && configMessageList.size() > 1) {
                    value.messageList = configMessageList;
                    value.message = null;
                } else {
                    String configMessage = config.getConfig().getString(value.path);
                    if (configMessage != null) {
                        value.message = configMessage;
                        value.messageList = null;
                    }
                }
            }
        }

        if (configChanged) {
            try {
                config.saveConfig();
                getLogger().info("[XalTools] Added " + missingMessages + " missing messages to config.");
            } catch (Exception e) {
                getLogger().severe("[XalTools] Failed to save config after adding missing messages: " + e.getMessage());
            }
        }
    }
}

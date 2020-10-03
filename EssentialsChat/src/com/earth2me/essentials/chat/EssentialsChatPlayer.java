package com.earth2me.essentials.chat;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import net.ess3.api.IEssentials;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.logging.Logger;

public abstract class EssentialsChatPlayer implements Listener {
    static final Logger logger = Logger.getLogger("EssentialsChat");
    final transient IEssentials ess;
    final transient Server server;
    private final transient Map<AsyncPlayerChatEvent, ChatStore> chatStorage;

    EssentialsChatPlayer(final Server server, final IEssentials ess, final Map<AsyncPlayerChatEvent, ChatStore> chatStorage) {
        this.ess = ess;
        this.server = server;
        this.chatStorage = chatStorage;
    }

    public abstract void onPlayerChat(final AsyncPlayerChatEvent event);

    boolean isAborted(final AsyncPlayerChatEvent event) {
        return event.isCancelled();
    }

    String getChatType(final String message) {
        if (message.length() == 0) {
            //Ignore empty chat events generated by plugins
            return "";
        }

        final char prefix = message.charAt(0);
        if (prefix == ess.getSettings().getChatShout()) {
            return message.length() > 1 ? "shout" : "";
        } else if (prefix == ess.getSettings().getChatQuestion()) {
            return message.length() > 1 ? "question" : "";
        } else {
            return "";
        }
    }

    ChatStore getChatStore(final AsyncPlayerChatEvent event) {
        return chatStorage.get(event);
    }

    void setChatStore(final AsyncPlayerChatEvent event, final ChatStore chatStore) {
        chatStorage.put(event, chatStore);
    }

    ChatStore delChatStore(final AsyncPlayerChatEvent event) {
        return chatStorage.remove(event);
    }

    private void charge(final User user, final Trade charge) throws ChargeException {
        charge.charge(user);
    }

    boolean charge(final AsyncPlayerChatEvent event, final ChatStore chatStore) {
        try {
            charge(chatStore.getUser(), chatStore.getCharge());
        } catch (final ChargeException e) {
            ess.showError(chatStore.getUser().getSource(), e, "\\ chat " + chatStore.getLongType());
            event.setCancelled(true);
            return false;
        }
        return true;
    }
}

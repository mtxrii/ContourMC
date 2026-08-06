package com.mtxrii.contourmc.service;

import com.google.inject.Inject;
import com.mtxrii.contourmc.config.SanctionsConfiguration;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessageConst;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.Plugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

@Component
public class SanctionService {
    private final JacksonConfigurationLoader configLoader;
    private final SanctionsConfiguration sanctionsConfig;

    @Inject
    public SanctionService(Plugin plugin) {
        this.configLoader = JacksonConfigurationLoader.builder()
                                                      .path(plugin.getDataPath().resolve("sanctions.json"))
                                                      .build();
        try {
            this.sanctionsConfig = this.configLoader.load().get(SanctionsConfiguration.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    public void kick(Player target, String reason) {
        Message kickMessage = MessageConst.SANCTION_MSG_BORDER.append(new Message(
                MessagePrefix.BLANK,
                """
                
                &b&lYou've been kicked!
                
                {}
                
                
                """,
                reason
        )).append(MessageConst.SANCTION_MSG_BORDER);

        target.kick(kickMessage.getMessageComponent(), PlayerKickEvent.Cause.KICK_COMMAND);
    }

    public void kick(Player target) {
        Message kickMessage = MessageConst.SANCTION_MSG_BORDER.append(new Message(
                MessagePrefix.BLANK,
                """
                
                &b&lYou've been kicked!
                
                
                """
        )).append(MessageConst.SANCTION_MSG_BORDER);

        target.kick(kickMessage.getMessageComponent(), PlayerKickEvent.Cause.KICK_COMMAND);
    }

    private void saveConfig() {
        try {
            this.configLoader.save(this.configLoader.createNode().set(this.sanctionsConfig));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}

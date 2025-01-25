package com.mc1510ty.hubCommandPlugin;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Optional;

@Plugin(id = "hubcommandplugin", name = "HubCommandPlugin", version = BuildConstants.VERSION, authors = {"1510ty"})
public class HubCommandPlugin {

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public HubCommandPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @com.velocitypowered.api.event.Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // CommandMeta を作成して登録
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("hub").build(),
                new HubCommandExecutor(server)
        );
        logger.info("HubCommand plugin has been initialized!");
    }


    public static class HubCommandExecutor implements SimpleCommand {

        private final ProxyServer server;

        public HubCommandExecutor(ProxyServer server) {
            this.server = server;
        }

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();

            if (!(source instanceof Player)) {
                source.sendMessage(Component.text("This command can only be executed by players."));
                return;
            }

            Player player = (Player) source;
            Optional<RegisteredServer> targetServer = server.getServer("hub1");

            if (targetServer.isPresent()) {
                player.createConnectionRequest(targetServer.get()).connect().thenAccept(result -> {
                    if (result.isSuccessful()) {
                        player.sendMessage(Component.text("You have been teleported to the hub!"));
                    } else {
                        player.sendMessage(Component.text("Failed to connect to the hub server."));
                    }
                });
            } else {
                player.sendMessage(Component.text("The hub server is not available."));
            }
        }
    }
}

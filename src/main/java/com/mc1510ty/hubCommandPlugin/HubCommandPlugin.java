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
import com.moandjiezana.toml.Toml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Plugin(id = "hubcommandplugin", name = "HubCommandPlugin", version = BuildConstants.VERSION, authors = {"1510ty"})
public class HubCommandPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private String hubServerName = "hub"; // デフォルトのサーバー名

    @Inject
    public HubCommandPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @com.velocitypowered.api.event.Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadConfig();

        // CommandMeta を作成して登録
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("hub").build(),
                new HubCommandExecutor(server, hubServerName)
        );
        logger.info("HubCommandPluginが正常に初期化されました!");
    }

    private void loadConfig() {
        Path configPath = Path.of("plugins/HubCommandPlugin/config.toml");
        File configFile = configPath.toFile();

        if (!configFile.exists()) {
            try {
                Files.createDirectories(configPath.getParent());
                Files.writeString(configPath, "hub-server = \"hub\"");
                logger.info("config.tomlが作成されました。デフォルト設定を使用します。");
            } catch (IOException e) {
                logger.error("config.tomlの作成中にエラーが発生しました: ", e);
                return;
            }
        }

        try {
            Toml toml = new Toml().read(configFile);
            hubServerName = toml.getString("hub-server", "hub");
            logger.info("hub-server: {} が設定されました。", hubServerName);
        } catch (Exception e) {
            logger.error("config.tomlの読み込み中にエラーが発生しました: ", e);
        }
    }

    public static class HubCommandExecutor implements SimpleCommand {

        private final ProxyServer server;
        private final String hubServerName;

        public HubCommandExecutor(ProxyServer server, String hubServerName) {
            this.server = server;
            this.hubServerName = hubServerName;
        }

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();

            if (!(source instanceof Player)) {
                source.sendMessage(Component.text("このコマンドはプレイヤーのみ実行できます"));
                return;
            }

            Player player = (Player) source;
            Optional<RegisteredServer> targetServer = server.getServer(hubServerName);

            if (targetServer.isPresent()) {
                player.createConnectionRequest(targetServer.get()).connect().thenAccept(result -> {
                    if (result.isSuccessful()) {
                        player.sendMessage(Component.text(hubServerName + " に移動します"));
                    } else {
                        player.sendMessage(Component.text("すでにこのサーバーに接続しています"));
                    }
                });
            } else {
                player.sendMessage(Component.text(hubServerName + " に接続できません。"));
            }
        }
    }
}

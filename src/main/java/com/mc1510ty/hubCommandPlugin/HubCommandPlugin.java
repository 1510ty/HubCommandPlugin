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
    private String language = "en"; // デフォルトの言語

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
                new HubCommandExecutor(server, hubServerName, language)
        );
        logger.info(getMessage("plugin_initialized"));
    }

    private void loadConfig() {
        Path configPath = Path.of("plugins/HubCommandPlugin/config.toml");
        File configFile = configPath.toFile();

        if (!configFile.exists()) {
            try {
                Files.createDirectories(configPath.getParent());
                Files.writeString(configPath, "hub-server = \"hub\"\nlanguage = \"en\"");
                logger.info(getMessage("config_created"));
            } catch (IOException e) {
                logger.error(getMessage("config_creation_error"), e);
                return;
            }
        }

        try {
            Toml toml = new Toml().read(configFile);
            hubServerName = toml.getString("hub-server", "hub");
            language = toml.getString("language", "en");
            logger.info(getMessage("server_set") + hubServerName);
            logger.info(getMessage("language_set") + language);
        } catch (Exception e) {
            logger.error(getMessage("config_load_error"), e);
        }
    }

    private String getMessage(String key) {
        switch (language) {
            case "ja":
                return switch (key) {
                    case "plugin_initialized" -> "HubCommandPluginが正常に初期化されました!";
                    case "config_created" -> "config.tomlが作成されました。デフォルト設定を使用します。";
                    case "config_creation_error" -> "config.tomlの作成中にエラーが発生しました: ";
                    case "server_set" -> "hub-server: ";
                    case "language_set" -> "language: ";
                    case "config_load_error" -> "config.tomlの読み込み中にエラーが発生しました: ";
                    default -> "";
                };
            case "en":
            default:
                return switch (key) {
                    case "plugin_initialized" -> "HubCommandPlugin has been successfully initialized!";
                    case "config_created" -> "config.toml has been created. Using default settings.";
                    case "config_creation_error" -> "Error occurred while creating config.toml: ";
                    case "server_set" -> "hub-server: ";
                    case "language_set" -> "language: ";
                    case "config_load_error" -> "Error occurred while loading config.toml: ";
                    default -> "";
                };
        }
    }

    public static class HubCommandExecutor implements SimpleCommand {

        private final ProxyServer server;
        private final String hubServerName;
        private final String language;

        public HubCommandExecutor(ProxyServer server, String hubServerName, String language) {
            this.server = server;
            this.hubServerName = hubServerName;
            this.language = language;
        }

        private String getMessage(String key) {
            switch (language) {
                case "ja":
                    return switch (key) {
                        case "not_player" -> "このコマンドはプレイヤーのみ実行できます";
                        case "connecting" -> hubServerName + " に移動します";
                        case "already_connected" -> "すでにこのサーバーに接続しています";
                        case "server_unavailable" -> "Hubサーバーは利用できません。";
                        default -> "";
                    };
                case "en":
                default:
                    return switch (key) {
                        case "not_player" -> "This command can only be executed by players.";
                        case "connecting" -> "Connecting to " + hubServerName + "...";
                        case "already_connected" -> "You are already connected to this server.";
                        case "server_unavailable" -> "The hub server is not available.";
                        default -> "";
                    };
            }
        }

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();

            if (!(source instanceof Player)) {
                source.sendMessage(Component.text(getMessage("not_player")));
                return;
            }

            Player player = (Player) source;
            Optional<RegisteredServer> targetServer = server.getServer(hubServerName);

            if (targetServer.isPresent()) {
                player.createConnectionRequest(targetServer.get()).connect().thenAccept(result -> {
                    if (result.isSuccessful()) {
                        player.sendMessage(Component.text(getMessage("connecting")));
                    } else {
                        player.sendMessage(Component.text(getMessage("already_connected")));
                    }
                });
            } else {
                player.sendMessage(Component.text(getMessage("server_unavailable")));
            }
        }
    }
}

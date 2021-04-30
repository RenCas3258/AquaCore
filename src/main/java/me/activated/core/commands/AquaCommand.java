package me.activated.core.commands;

import me.activated.core.api.player.PlayerData;
import me.activated.core.utilities.Utilities;
import me.activated.core.utilities.chat.Color;
import me.activated.core.utilities.command.BaseCommand;
import me.activated.core.utilities.command.Command;
import me.activated.core.utilities.command.CommandArgs;
import me.activated.core.utilities.general.StringUtils;
import me.activated.core.utilities.general.Tasks;
import org.bson.Document;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AquaCommand extends BaseCommand {

    @Command(name = "aquacore", inGameOnly = false, aliases = {"core", "aqua"})
    public void onCommand(CommandArgs command) {
        Tasks.runAsync(plugin, () ->{
            CommandSender sender = command.getSender();
            String[] args = command.getArgs();

            if (args.length == 0) {
                sender.sendMessage(Color.translate("&7&m------------------------------------"));
                sender.sendMessage(Color.translate("&bAqua Core made by &3FaceSlap_&7/&3Activated_&b!"));
                sender.sendMessage(Color.translate("&bMC-Market username&7: &3FaceSlap_"));
                sender.sendMessage(Color.translate("&bVersion&7: &3v" + plugin.getDescription().getVersion()));
                sender.sendMessage(Color.translate("&7&m------------------------------------"));
                return;
            }
            if (!sender.isOp()) {
                sender.sendMessage(Color.translate("&7&m------------------------------------"));
                sender.sendMessage(Color.translate("&bAqua Core made by &3FaceSlap_&7/&3Activated_&b!"));
                sender.sendMessage(Color.translate("&bMC-Market username&7: &3FaceSlap_"));
                sender.sendMessage(Color.translate("&bVersion&7: &3v" + plugin.getDescription().getVersion()));
                sender.sendMessage(Color.translate("&7&m------------------------------------"));
                return;
            }
            if (args[0].equalsIgnoreCase("debug")) {
                sender.sendMessage(Color.translate("&aPlease wait..."));

                List<Document> bans = plugin.getMongoManager().getBans().find().into(new ArrayList<>());
                List<Document> mutes = plugin.getMongoManager().getMutes().find().into(new ArrayList<>());
                List<Document> warns = plugin.getMongoManager().getWarns().find().into(new ArrayList<>());
                List<Document> blacklists = plugin.getMongoManager().getBlacklists().find().into(new ArrayList<>());
                List<Document> kicks = plugin.getMongoManager().getKicks().find().into(new ArrayList<>());

                sender.sendMessage(Color.translate("&7&m-----------------------------------------"));
                sender.sendMessage(Color.translate("&3&lAqua Core debug"));
                sender.sendMessage(" ");
                sender.sendMessage(Color.translate("&bTotal bans on record&7: &3" + bans.size()));
                sender.sendMessage(Color.translate("&bTotal mutes on record&7: &3" + mutes.size()));
                sender.sendMessage(Color.translate("&bTotal warns on record&7: &3" + warns.size()));
                sender.sendMessage(Color.translate("&bTotal blacklists on record&7: &3" + blacklists.size()));
                sender.sendMessage(Color.translate("&bTotal kicks on record&7: &3" + kicks.size()));
                sender.sendMessage(Color.translate("&bTotal&7: &3" +
                        (bans.size() + mutes.size() + warns.size() + blacklists.size() + kicks.size()) + " punishmnets."));
                sender.sendMessage(" ");
                sender.sendMessage(Color.translate("&bPlugin name&7: &3" + plugin.getDescription().getName()));
                sender.sendMessage(Color.translate("&bPlugin version&7: &3" + plugin.getDescription().getVersion()));
                sender.sendMessage(Color.translate("&bPlugin author&7: &3" + StringUtils.getStringFromList(plugin.getDescription().getAuthors())));
                sender.sendMessage(Color.translate("&7&m-----------------------------------------"));
                return;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadFiles();
                plugin.getPunishmentPlugin().getConfigFile().load();
                plugin.getPunishmentPlugin().getLanguageFile().load();
                plugin.getPunishmentPlugin().getMessagesManager().setup();

                for (Player online : Utilities.getOnlinePlayers()) {
                    PlayerData playerData = plugin.getPlayerManagement().getPlayerData(online.getUniqueId());
                    if (playerData != null) {
                        playerData.loadAttachments(online);
                    }
                }

                sender.sendMessage(Color.translate("&aFiles have been reloaded&7: &7(&3ranks.yml&7, &3tags.yml&7, &3settings.yml&7, &3messages.yml&7, &3config.yml&7, &3lang.yml&7)"));
                return;
            }
            if (args[0].equalsIgnoreCase("redisreload")) {
                sender.sendMessage(Color.translate("&aPlease wait.."));
                plugin.getRedisData().reload();

                sender.sendMessage(Color.translate("&aYou have reloaded redis!"));
                sender.sendMessage(Color.translate("&aCurrent status&7: " + (plugin.getRedisData().isConnected() ? "&2Connected" : "&cDisconnected")));
                return;
            }
        });
    }
}

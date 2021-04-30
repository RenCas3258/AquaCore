package me.activated.core.punishments.menus.punishments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.activated.core.menu.menu.SwitchableMenu;
import me.activated.core.menu.slots.Slot;
import me.activated.core.menu.slots.pages.PageSlot;
import me.activated.core.punishments.PunishmentPlugin;
import me.activated.core.punishments.menus.slots.DataInfoSlot;
import me.activated.core.punishments.menus.slots.PreviousMenuSlot;
import me.activated.core.punishments.player.PunishData;
import me.activated.core.punishments.utilities.punishments.Punishment;
import me.activated.core.punishments.utilities.punishments.PunishmentType;
import me.activated.core.utilities.chat.Color;
import me.activated.core.utilities.general.DateUtils;
import me.activated.core.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public class BansMenu extends SwitchableMenu {
    private final PunishData punishData;

    @Override
    public String getPagesTitle(Player player) {
        return Color.translate("&7" + punishData.getPlayerData().getPlayerName() + "'s bans");
    }

    @Override
    public List<Slot> getEveryMenuSlots(Player player) {
        List<Slot> slots = new ArrayList<>();

        slots.add(new DataInfoSlot(punishData.getPlayerData(),4));
        slots.add(new PageSlot(this, 40));
        slots.add(new PreviousMenuSlot());
        
        return slots;
    }

    @Override
    public List<Slot> getSwitchableSlots(Player player) {
        List<Slot> slots = new ArrayList<>();

        AtomicInteger order = new AtomicInteger(1);
        List<Punishment> punishments = punishData.getPunishments().stream().sorted(Comparator.comparingLong(Punishment::getAddedAt).reversed()).filter(punishment -> punishment.getPunishmentType() == PunishmentType.BAN).collect(Collectors.toList());

        punishments.forEach(punishment -> slots.add(new PunishmentSlot(punishment, order.getAndIncrement())));

        return slots;
    }

    @AllArgsConstructor
    private class PunishmentSlot extends Slot {
        private Punishment punishment;
        private int order;

        @Override
        public ItemStack getItem(Player player) {
            ItemBuilder item = new ItemBuilder(!punishment.hasExpired() ? Material.ENCHANTED_BOOK : Material.BOOK);
            item.setName(PunishmentPlugin.MAIN_COLOR + "#" + order + " &7(" + PunishmentPlugin.SECONDARY_COLOR + DateUtils.getDate(punishment.getAddedAt()) + "&7)");
            item.addLoreLine("&7&m---&8*&7&m---------------------&8*&7&m---");
            item.addLoreLine(PunishmentPlugin.MAIN_COLOR + "Added by&7: " + PunishmentPlugin.SECONDARY_COLOR + punishment.getAddedBy());
            item.addLoreLine(PunishmentPlugin.MAIN_COLOR + "Duration&7: " + PunishmentPlugin.SECONDARY_COLOR + punishment.getNiceDuration());
            item.addLoreLine(PunishmentPlugin.MAIN_COLOR + "Expire&7: " + PunishmentPlugin.SECONDARY_COLOR + punishment.getNiceExpire());
            item.addLoreLine(PunishmentPlugin.MAIN_COLOR + "Reason&7: " + PunishmentPlugin.SECONDARY_COLOR + punishment.getReason());
            item.addLoreLine("&7&m---&8*&7&m---------------------&8*&7&m---");
            item.addLoreLine(PunishmentPlugin.MAIN_COLOR + "Permanent&7: " + (punishment.isPermanent() ? "&aYes" : "&cNo"));
            item.addLoreLine(PunishmentPlugin.MAIN_COLOR + "Active&7: " + (!punishment.hasExpired() ? "&aYes" : "&cNo"));
            item.addLoreLine(PunishmentPlugin.MAIN_COLOR + "Silent&7: " + (punishment.isSilent() ? "&aYes" : "&cNo"));
            item.addLoreLine("&7&m---&8*&7&m---------------------&8*&7&m---");
            if (!punishment.getRemovedBy().equalsIgnoreCase("")) {
                item.addLoreLine(PunishmentPlugin.MAIN_COLOR + "Removed by&7: " + PunishmentPlugin.SECONDARY_COLOR + punishment.getRemovedBy());
                item.addLoreLine(PunishmentPlugin.MAIN_COLOR + "Reason&7: " + PunishmentPlugin.SECONDARY_COLOR + punishment.getRemovedFor());
                item.addLoreLine(PunishmentPlugin.MAIN_COLOR + "Date&7: " + PunishmentPlugin.SECONDARY_COLOR + DateUtils.getDate(punishment.getWhenRemoved()));
                item.addLoreLine("&7&m---&8*&7&m---------------------&8*&7&m---");
            }
            return item.toItemStack();
        }

        @Override
        public int getSlot() {
            return 0;
        }
    }
}

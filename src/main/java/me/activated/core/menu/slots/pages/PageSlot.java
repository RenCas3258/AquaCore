package me.activated.core.menu.slots.pages;

import lombok.RequiredArgsConstructor;
import me.activated.core.menu.menu.SwitchableMenu;
import me.activated.core.menu.slots.Slot;
import me.activated.core.utilities.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class PageSlot extends Slot {
    private final SwitchableMenu switchableMenu;
    private final int slot;

    @Override
    public ItemStack getItem(Player player) {
        ItemBuilder item = new ItemBuilder(Material.PAPER);
        item.setName(switchableMenu.getPagesTitle(player));

        item.addLoreLine("");
        item.addLoreLine("&bCurrent page&7: &3" + switchableMenu.getPage());
        item.addLoreLine("&bMax pages&7: &3" + switchableMenu.getPages(player));
        item.addLoreLine("");

        return item.toItemStack();
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public int[] getSlots() {
        return new int[]{40};
    }
}

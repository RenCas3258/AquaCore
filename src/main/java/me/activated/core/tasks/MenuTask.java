package me.activated.core.tasks;

import me.activated.core.plugin.AquaCore;
import me.activated.core.menu.menu.AquaMenu;
import me.activated.core.utilities.Utilities;

public class MenuTask implements Runnable {
    private AquaCore plugin = AquaCore.INSTANCE;

    @Override
    public void run() {
        Utilities.getOnlinePlayers().forEach(player -> {
            AquaMenu aquaMenu = plugin.getMenuManager().getOpenedMenus().get(player.getUniqueId());
            if (aquaMenu != null && aquaMenu.isUpdateInTask()) {
                aquaMenu.update(player);
            }
        });
    }
}

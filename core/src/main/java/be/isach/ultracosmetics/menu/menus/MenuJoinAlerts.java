package be.isach.ultracosmetics.menu.menus;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.JoinAlertType;
import be.isach.ultracosmetics.menu.CosmeticMenu;

/**
 * Join alert cosmetic menu.
 */
public class MenuJoinAlerts extends CosmeticMenu<JoinAlertType> {

    public MenuJoinAlerts(UltraCosmetics ultraCosmetics) {
        super(ultraCosmetics, Category.JOIN_ALERTS);
    }
}

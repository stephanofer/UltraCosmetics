package be.isach.ultracosmetics.cosmetics.joinalerts;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.Cosmetic;
import be.isach.ultracosmetics.cosmetics.type.JoinAlertType;
import be.isach.ultracosmetics.events.UCCosmeticUnequipEvent;
import be.isach.ultracosmetics.player.UltraPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

/**
 * Passive cosmetic storing the selected join alert template.
 */
public class JoinAlert extends Cosmetic<JoinAlertType> {

    public JoinAlert(UltraPlayer owner, JoinAlertType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected void onEquip() {
        // Passive cosmetic: actual execution happens on player join once the profile has loaded.
    }

    @Override
    protected Component appendActivateMessage(Component base) {
        return Component.empty();
    }

    @Override
    public void clear() {
        Bukkit.getPluginManager().callEvent(new UCCosmeticUnequipEvent(getOwner(), this));
        HandlerList.unregisterAll(this);
        onClear();
        unsetCosmetic();
    }
}

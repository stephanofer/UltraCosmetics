package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.CustomConfiguration;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.joinalerts.JoinAlert;
import be.isach.ultracosmetics.hook.PlaceholderHook;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.SmartLogger.LogLevel;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Config-driven join alert templates.
 */
public class JoinAlertType extends CosmeticType<JoinAlert> {
    private static final XMaterial DEFAULT_ICON = XMaterial.BELL;
    private static final Set<String> BUILTIN_TYPES = new HashSet<>(Arrays.asList("RoyalEntry", "Spotlight"));

    public static void register() {
        registerMessages();
        registerBuiltin("RoyalEntry", DEFAULT_ICON);
        registerBuiltin("Spotlight", XMaterial.NOTE_BLOCK);

        ConfigurationSection section = SettingsManager.getConfig().getConfigurationSection(Category.JOIN_ALERTS.getConfigPath());
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            if (!section.isConfigurationSection(key)) {
                continue;
            }
            if (BUILTIN_TYPES.contains(key)) {
                continue;
            }
            registerConfigured(key, parseMaterial(key, section.getString(key + ".Icon", DEFAULT_ICON.name())));
        }
    }

    private static void registerBuiltin(String key, XMaterial defaultIcon) {
        registerConfigured(key, parseMaterial(key, SettingsManager.getConfig().getString(Category.JOIN_ALERTS.getConfigPath() + "." + key + ".Icon", defaultIcon.name())));
    }

    private static void registerConfigured(String key, XMaterial material) {
        addConfiguredStrings(key);
        new JoinAlertType(key, material);
    }

    private static void addConfiguredStrings(String key) {
        String basePath = Category.JOIN_ALERTS.getConfigPath() + "." + key;
        String displayName = SettingsManager.getConfig().getString(basePath + ".Display-Name", "<gold><bold>" + key);
        List<String> description = SettingsManager.getConfig().getStringList(basePath + ".Description");
        if (description.isEmpty()) {
            description.add("<gray>Broadcast a custom join alert");
            description.add("<gray>when you enter the server.");
        }
        MessageManager.addMessage(basePath + ".name", displayName);
        MessageManager.addMessage(basePath + ".Description", String.join("\n", description));
    }

    private static void registerMessages() {
        MessageManager.addMessage("Menu.Join-Alerts.Title", "<gold><bold>Join Alerts");
        MessageManager.addMessage("Menu.Join-Alerts.Button.Name", "<gold><bold>Join Alerts");
        MessageManager.addMessage("Menu.Join-Alerts.Button.Lore", "<gray>Unlocked: </gray><white><unlocked>");
        MessageManager.addMessage("Menu.Join-Alerts.Button.Tooltip-Equip", "<green>Click to equip");
        MessageManager.addMessage("Menu.Join-Alerts.Button.Tooltip-Unequip", "<red>Click to unequip");
        MessageManager.addMessage("Join-Alerts.Equip", "<prefix> <green>You selected <joinalertname><green> as your join alert.");
        MessageManager.addMessage("Join-Alerts.Unequip", "<prefix> <red>You no longer have a join alert equipped.");
        MessageManager.addMessage("Clear.Join-Alerts", "<red>Clear join alert");
    }

    private static XMaterial parseMaterial(String key, String materialName) {
        if (materialName == null) {
            return DEFAULT_ICON;
        }
        try {
            return XMaterial.valueOf(materialName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            UltraCosmetics ultraCosmetics = UltraCosmeticsData.get().getPlugin();
            ultraCosmetics.getSmartLogger().write(LogLevel.WARNING, "Invalid Join Alert icon for '" + key + "': " + materialName + ". Falling back to " + DEFAULT_ICON.name());
            return DEFAULT_ICON;
        }
    }

    private JoinAlertType(String configName, XMaterial material) {
        super(Category.JOIN_ALERTS, configName, material, JoinAlert.class);
    }

    @Override
    public Component getName() {
        return deserializeWithPlayer(getRawDisplayName(), null, Component.empty());
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        Style style = MessageManager.getMiniMessage().deserialize(SettingsManager.getConfig().getString("Description-Style", "")).style();
        for (String raw : SettingsManager.getConfig().getStringList(getConfigPath() + ".Description")) {
            description.add(MessageManager.toLegacy(MessageManager.getMiniMessage().deserialize(raw).applyFallbackStyle(style)));
        }
        return description;
    }

    @Override
    public ItemStack getItemStack() {
        String customHead = SettingsManager.getConfig().getString(getConfigPath() + ".Custom-Head");
        if (customHead != null) {
            return super.getItemStack();
        }
        return ItemFactory.create(getMaterial(), "");
    }

    @Override
    protected void setupConfig(CustomConfiguration config, String path) {
        super.setupConfig(config, path);
        config.addDefault(path + ".Icon", getMaterial().name(), "Item used in the menu for this join alert template.");
        config.addDefault(path + ".Display-Name", "<gold><bold>" + getConfigName(), "Display name shown in menus.");
        List<String> defaultDescription = new ArrayList<>();
        defaultDescription.add("<gray>Broadcast a custom join alert");
        defaultDescription.add("<gray>when you enter the server.");
        config.addDefault(path + ".Description", defaultDescription, "Lore shown in the menu.");
        config.addDefault(path + ".Message", "<gold><player></gold> <yellow>just joined the server!", "Base message reusable through <message>.");
        config.addDefault(path + ".Chat.Enabled", true, "Whether to broadcast this alert in chat.");
        config.addDefault(path + ".Chat.Message", "<message>", "Chat format. Use <message> to inject the base message.");
        config.addDefault(path + ".ActionBar.Enabled", false, "Whether to broadcast this alert in the action bar.");
        config.addDefault(path + ".ActionBar.Message", "<message>", "Action bar format. Use <message> to inject the base message.");
        config.addDefault(path + ".Sound.Enabled", false, "Whether to play a sound to online players.");
        config.addDefault(path + ".Sound.Type", "ENTITY_PLAYER_LEVELUP", "Any Bukkit sound enum value.");
        config.addDefault(path + ".Sound.Volume", 1.0, "Sound volume.");
        config.addDefault(path + ".Sound.Pitch", 1.0, "Sound pitch.");
    }

    public void play(UltraPlayer owner, UltraCosmetics ultraCosmetics) {
        Player player = owner.getBukkitPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }
        if (!isEnabled() || !owner.canEquip(this)) {
            return;
        }
        if (!SettingsManager.isAllowedWorld(player.getWorld())) {
            return;
        }

        Component baseMessage = deserializeWithPlayer(getRawMessage(), player, Component.empty());
        if (SettingsManager.getConfig().getBoolean(getConfigPath() + ".Chat.Enabled")) {
            Component chatMessage = deserializeWithPlayer(getRawChannelMessage("Chat.Message"), player, baseMessage);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                MessageManager.getAudiences().player(onlinePlayer).sendMessage(chatMessage);
            }
        }
        if (SettingsManager.getConfig().getBoolean(getConfigPath() + ".ActionBar.Enabled")) {
            Component actionBar = deserializeWithPlayer(getRawChannelMessage("ActionBar.Message"), player, baseMessage);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                MessageManager.getAudiences().player(onlinePlayer).sendActionBar(actionBar);
            }
        }
        if (SettingsManager.getConfig().getBoolean(getConfigPath() + ".Sound.Enabled")) {
            Sound sound = getSound();
            if (sound == null) {
                ultraCosmetics.getSmartLogger().write(LogLevel.WARNING, "Invalid sound configured for Join Alert '" + getConfigName() + "'.");
                return;
            }
            float volume = (float) SettingsManager.getConfig().getDouble(getConfigPath() + ".Sound.Volume", 1.0);
            float pitch = (float) SettingsManager.getConfig().getDouble(getConfigPath() + ".Sound.Pitch", 1.0);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
            }
        }
    }

    private String getRawDisplayName() {
        return SettingsManager.getConfig().getString(getConfigPath() + ".Display-Name", "<gold><bold>" + getConfigName());
    }

    private String getRawMessage() {
        return SettingsManager.getConfig().getString(getConfigPath() + ".Message", "<gold><player></gold> <yellow>just joined the server!");
    }

    private String getRawChannelMessage(String path) {
        return SettingsManager.getConfig().getString(getConfigPath() + "." + path, "<message>");
    }

    private Sound getSound() {
        String soundName = SettingsManager.getConfig().getString(getConfigPath() + ".Sound.Type", "ENTITY_PLAYER_LEVELUP");
        try {
            return Sound.valueOf(soundName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Component deserializeWithPlayer(String raw, Player player, Component baseMessage) {
        String resolved = raw == null ? "" : raw;
        if (player != null && resolved.contains("%") && UltraCosmeticsData.get().getPlugin().getPlaceholderHook() != null) {
            resolved = PlaceholderHook.parsePlaceholders(player, resolved);
        }
        TagResolver resolver = TagResolver.builder()
                .resolver(Placeholder.component("message", baseMessage))
                .resolver(Placeholder.unparsed("player", player == null ? "" : player.getName()))
                .resolver(Placeholder.unparsed("player_name", player == null ? "" : player.getName()))
                .resolver(Placeholder.component("player_displayname", player == null ? Component.empty() : Component.text(player.getDisplayName())))
                .build();
        return MessageManager.getMiniMessage().deserialize(resolved, resolver);
    }
}

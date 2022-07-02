package com.enhanceddiscordnotifications;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("enhanceddiscordnotifications")
public interface EnhancedDiscordNotificationsConfig extends Config {

	// Webhook config section
	@ConfigSection(
			name = "Webhook Settings",
			description = "The config for webhook content notifications",
			position = 0,
			closedByDefault = true
	)
	String webhookConfig = "webhookConfig";

	@ConfigItem(
			keyName = "webhook",
			name = "Webhook URL",
			description = "The Discord Webhook URL to send messages to.",
			section = webhookConfig,
			position = 0
	)
	String webhook();

	// Levelling config section
	@ConfigSection(
			name = "Levelling",
			description = "The config for levelling notifications",
			position = 1,
			closedByDefault = true
	)
	String levellingConfig = "levellingConfig";

	@ConfigItem(
			keyName = "includeLevelling",
			name = "Send Levelling Notifications",
			description = "Send messages when you level up a skill.",
			section = levellingConfig,
			position = 1
	)
	default boolean sendLevelling() {
		return false;
	}

	@ConfigItem(
			keyName = "minimumLevel",
			name = "Minimum level",
			description = "Levels greater than or equal to this value will send a message.",
			section = levellingConfig,
			position = 2
	)
	default int minLevel() {
		return 0;
	}

	@ConfigItem(
			keyName = "levelInterval",
			name = "Send every X levels",
			description = "Only levels that are a multiple of this value are sent. Level 99 will always be sent regardless of this value.",
			section = levellingConfig,
			position = 3
	)
	default int levelInterval() {
		return 1;
	}

	@ConfigItem(
			keyName = "linearLevelModifier",
			name = "Linear Level Modifier",
			description = "Send every `max(-.1x + linearLevelMax, 1)` levels. Will override `Send every X levels` if set to above zero.",
			section = levellingConfig,
			position = 4
	)
	default double linearLevelMax() {
		return 0;
	}

	@ConfigItem(
			keyName = "levelMessage",
			name = "Level Message",
			description = "Message to send to Discord on Level",
			section = levellingConfig,
			position = 5
	)
	default String levelMessage() { return "$name leveled $skill to $level"; }

	@ConfigItem(
			keyName = "andLevelMessage",
			name = "And Level Message",
			description = "Message to send to Discord when Multi Skill Level",
			section = levellingConfig,
			position = 6
	)
	default String andLevelMessage() { return ", and $skill to $level"; }

	@ConfigItem(
			keyName = "sendLevellingScreenshot",
			name = "Include levelling screenshots",
			description = "Include a screenshot when leveling up.",
			section = levellingConfig,
			position = 100
	)
	default boolean sendLevellingScreenshot() {
		return false;
	}
	// End levelling config section

	// Questing config section
	@ConfigSection(
			name = "Questing",
			description = "The config for questing notifications",
			position = 2,
			closedByDefault = true
	)
	String questingConfig = "questingConfig";

	@ConfigItem(
			keyName = "includeQuests",
			name = "Send Quest Notifications",
			description = "Send messages when you complete a quest.",
			section = questingConfig
	)
	default boolean sendQuestComplete() {
		return false;
	}

	@ConfigItem(
			keyName = "questMessage",
			name = "Quest Message",
			description = "Message to send to Discord on Quest",
			section = questingConfig,
			position = 1
	)
	default String questMessage() { return "$name has just completed: $quest"; }

	@ConfigItem(
			keyName = "sendQuestingScreenshot",
			name = "Include quest screenshots",
			description = "Include a screenshot with the discord notification when leveling up.",
			section = questingConfig,
			position = 100
	)
	default boolean sendQuestingScreenshot() {
		return false;
	}
	// End questing config section

	// Death config section
	@ConfigSection(
			name = "Deaths",
			description = "The config for death notifications",
			position = 3,
			closedByDefault = true
	)
	String deathConfig = "deathConfig";

	@ConfigItem(
			keyName = "includeDeaths",
			name = "Send Death Notifications",
			description = "Send messages when you die to discord.",
			section = deathConfig
	)
	default boolean sendDeath() { return false; }

	@ConfigItem(
			keyName = "deathMessage",
			name = "Death Message",
			description = "Message to send to Discord on Death",
			section = deathConfig,
			position = 1
	)
	default String deathMessage() { return "$name has just died!"; }

	@ConfigItem(
			keyName = "sendDeathScreenshot",
			name = "Include death screenshots",
			description = "Include a screenshot with the discord notification when you die.",
			section = deathConfig,
			position = 100
	)
	default boolean sendDeathScreenshot() {
		return false;
	}
	// End death config section

	// Clue config section
	@ConfigSection(
			name = "Clue Scrolls",
			description = "The config for clue scroll notifications",
			position = 4,
			closedByDefault = true
	)
	String clueConfig = "clueConfig";

	@ConfigItem(
			keyName = "includeClues",
			name = "Send Clue Notifications",
			description = "Send messages when you complete a clue scroll.",
			section = clueConfig
	)
	default boolean sendClue() { return false; }

	@ConfigItem(
			keyName = "clueMessage",
			name = "Clue Message",
			description = "Message to send to Discord on Clue",
			section = clueConfig,
			position = 1
	)
	default String clueMessage() { return "$name has just completed a clue scroll!"; }

	@ConfigItem(
			keyName = "sendClueScreenshot",
			name = "Include Clue screenshots",
			description = "Include a screenshot with the discord notification when you complete a clue.",
			section = clueConfig,
			position = 100
	)
	default boolean sendClueScreenshot() {
		return false;
	}
	// End clue config section

	// Pet config section
	@ConfigSection(
			name = "Pets",
			description = "The config for pet notifications",
			position = 5,
			closedByDefault = true
	)
	String petConfig = "petConfig";

	@ConfigItem(
			keyName = "includePets",
			name = "Send Pet Notifications",
			description = "Send messages when you receive a pet.",
			section = petConfig
	)
	default boolean setPets() { return false; }

	@ConfigItem(
			keyName = "petMessage",
			name = "Pet Message",
			description = "Message to send to Discord on Pet",
			section = petConfig,
			position = 1
	)
	default String petMessage() { return "$name has just received a pet!"; }

	@ConfigItem(
			keyName = "sendPetScreenshot",
			name = "Include Pet screenshots",
			description = "Include a screenshot with the discord notification when you receive a pet.",
			section = petConfig,
			position = 100
	)
	default boolean sendPetScreenshot() {
		return false;
	}

	// Collection Log config section
	@ConfigSection(
			name = "Collection Log",
			description = "The config for collection log notifications",
			position = 6,
			closedByDefault = true
	)
	String collectionLogConfig = "collectionLogConfig";

	@ConfigItem(
			keyName = "includeCollectionLogs",
			name = "Send Collection Log Notifications",
			description = "Send messages when you receive a collection log entry.",
			section = collectionLogConfig
	)
	default boolean setCollectionLogs() { return false; }

	@ConfigItem(
			keyName = "collectionLogMessage",
			name = "Collection Log Message",
			description = "Message to send to Discord on Collection Log",
			section = collectionLogConfig,
			position = 1
	)
	default String collectionLogMessage() { return "$name just received a new collection log item: **$itemName!**"; }

	@ConfigItem(
			keyName = "sendCollectionLogScreenshot",
			name = "Include Collection Log screenshots",
			description = "Include a screenshot with the discord notification when you receive a collection log item.",
			section = collectionLogConfig,
			position = 100
	)
	default boolean sendCollectionLogScreenshot() {
		return false;
	}

	// Valuable Drop config section
	@ConfigSection(
			name = "Valuable Drop",
			description = "The config for valuable drop notifications",
			position = 7,
			closedByDefault = true
	)
	String valuableDropConfig = "valuableDropConfig";

	@ConfigItem(
			keyName = "setValuableDrop",
			name = "Send Valuable Drops Notifications",
			description = "Send messages when you receive a valuable drops entry.",
			section = valuableDropConfig
	)
	default boolean setValuableDrop() { return false; }

	@ConfigItem(
			keyName = "valuableDropThreshold",
			name = "Valuable Drop Threshold",
			description = "The minimum value of drop for it to send a discord message.",
			position = 1,
			section = valuableDropConfig
	)
	default int valuableDropThreshold() { return 0; }

	@ConfigItem(
			keyName = "valuableDropMessage",
			name = "Valuable Drop Message",
			description = "Message to send to Discord for Valuable Drops",
			section = valuableDropConfig,
			position = 2
	)
	default String valuableDropMessage() { return "$name just received a valuable drop: $itemName! \nApprox Value: **$itemValue coins**"; }

	@ConfigItem(
			keyName = "sendValuableDropScreenshot",
			name = "Include Valuable Drop screenshots",
			description = "Include a screenshot with the discord notification when you receive a valuable drop",
			section = valuableDropConfig,
			position = 100
	)
	default boolean sendValuableDropScreenshot() {
		return false;
	}
}

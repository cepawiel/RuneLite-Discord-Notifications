package com.discordnotifications;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("discordnotifications")
public interface DiscordNotificationsConfig extends Config {

	// Levelling config section
	@ConfigSection(
			name = "Levelling",
			description = "The config for levelling notifications",
			position = 0,
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
			position = 1,
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
			position = 2,
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
			position = 3,
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

	@ConfigItem(
			keyName = "webhook",
			name = "Webhook URL",
			description = "The Discord Webhook URL to send messages to."
	)
	String webhook();

	// Pet config section
	@ConfigSection(
			name = "Pets",
			description = "The config for pet notifications",
			position = 4,
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
			keyName = "sendPetScreenshot",
			name = "Include Pet screenshots",
			description = "Include a screenshot with the discord notification when you receive a pet.",
			section = petConfig,
			position = 100
	)
	default boolean sendPetScreenshot() {
		return false;
	}
}

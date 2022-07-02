package com.enhanceddiscordnotifications;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.DrawManager;
import static net.runelite.api.widgets.WidgetID.QUEST_COMPLETED_GROUP_ID;

import net.runelite.client.util.Text;
import okhttp3.*;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.runelite.http.api.RuneLiteAPI.GSON;

@Slf4j
@PluginDescriptor(name = "Enhanced Discord Notifications")
public class EnhancedDiscordNotificationsPlugin extends Plugin
{
	private Hashtable<String, Integer> currentLevels;
	private ArrayList<String> leveledSkills;
	private boolean shouldSendLevelMessage = false;
	private boolean shouldSendQuestMessage = false;
	private boolean shouldSendClueMessage = false;
	private int ticksWaited = 0;

	private static final String COLLECTION_LOG_TEXT = "New item added to your collection log: ";
	private static final Pattern VALUABLE_DROP_PATTERN = Pattern.compile(".*Valuable drop: ([^<>]+?\\(((?:\\d+,?)+) coins\\))(?:</col>)?");
	private static final Pattern QUEST_PATTERN_1 = Pattern.compile(".+?ve\\.*? (?<verb>been|rebuilt|.+?ed)? ?(?:the )?'?(?<quest>.+?)'?(?: [Qq]uest)?[!.]?$");
	private static final Pattern QUEST_PATTERN_2 = Pattern.compile("'?(?<quest>.+?)'?(?: [Qq]uest)? (?<verb>[a-z]\\w+?ed)?(?: f.*?)?[!.]?$");
	private static final ImmutableList<String> RFD_TAGS = ImmutableList.of("Another Cook", "freed", "defeated", "saved");
	private static final ImmutableList<String> WORD_QUEST_IN_NAME_TAGS = ImmutableList.of("Another Cook", "Doric", "Heroes", "Legends", "Observatory", "Olaf", "Waterfall");
	private static final ImmutableList<String> PET_MESSAGES = ImmutableList.of("You have a funny feeling like you're being followed",
			"You feel something weird sneaking into your backpack",
			"You have a funny feeling like you would have been followed");

	private boolean shouldSendMessage;
	private boolean notificationStarted;

	@Inject
	private Client client;

	@Inject
	private EnhancedDiscordNotificationsConfig config;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private DrawManager drawManager;

	@Provides
	EnhancedDiscordNotificationsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EnhancedDiscordNotificationsConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		currentLevels = new Hashtable<String, Integer>();
		leveledSkills = new ArrayList<String>();
	}

	@Override
	protected void shutDown() throws Exception
	{
		notificationStarted = false;
	}

	@Subscribe
	public void onUsernameChanged(UsernameChanged usernameChanged)
	{
		resetState();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState().equals(GameState.LOGIN_SCREEN))
		{
			resetState();
		} else {
			shouldSendMessage = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{

		boolean didCompleteClue = client.getWidget(WidgetInfo.CLUE_SCROLL_REWARD_ITEM_CONTAINER) != null;


		if (shouldSendClueMessage && didCompleteClue && config.sendClue()) {
			shouldSendClueMessage = false;
			sendClueMessage();
		}

		if (
				shouldSendQuestMessage
						&& config.sendQuestComplete()
						&& client.getWidget(WidgetInfo.QUEST_COMPLETED_NAME_TEXT) != null
		) {
			shouldSendQuestMessage = false;
			String text = client.getWidget(WidgetInfo.QUEST_COMPLETED_NAME_TEXT).getText();
			String questName = parseQuestCompletedWidget(text);
			sendQuestMessage(questName);
		}

		if (!shouldSendLevelMessage)
		{
			return;
		}

		if (ticksWaited < 2)
		{
			ticksWaited++;
			return;
		}

		shouldSendLevelMessage = false;
		ticksWaited = 0;
		sendLevelMessage();
	}

	@Subscribe
	public void onStatChanged(net.runelite.api.events.StatChanged statChanged)
	{
		if (!config.sendLevelling())
		{
			return;
		}

		String skillName = statChanged.getSkill().getName();
		int newLevel = statChanged.getLevel();

		// .contains wasn't behaving so I went with == null
		Integer previousLevel = currentLevels.get(skillName);
		if (previousLevel == null || previousLevel == 0)
		{
			currentLevels.put(skillName, newLevel);
			return;
		}

		if (previousLevel != newLevel)
		{
			currentLevels.put(skillName, newLevel);

			// Certain activities can multilevel, check if any of the levels are valid for the message.
			for (int level = previousLevel + 1; level <= newLevel; level++)
			{
				if (shouldSendForThisLevel(level))
				{
					leveledSkills.add(skillName);
					shouldSendLevelMessage = true;
					break;
				}
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String chatMessage = event.getMessage();
		if (config.setPets() && PET_MESSAGES.stream().anyMatch(chatMessage::contains))
		{
			sendPetMessage();
		}
		if (config.setCollectionLogs() && chatMessage.startsWith(COLLECTION_LOG_TEXT) && client.getVarbitValue(Varbits.COLLECTION_LOG_NOTIFICATION) == 1)
		{
			String itemName = Text.removeTags(chatMessage).substring(COLLECTION_LOG_TEXT.length());
			sendCollectionLogMessage(itemName);
		}
		if (config.setValuableDrop())
		{
			Matcher matcher = VALUABLE_DROP_PATTERN.matcher(chatMessage);
			if (matcher.matches())
			{
				int valuableDropValue = Integer.parseInt(matcher.group(2).replaceAll(",", ""));
				if (valuableDropValue >= config.valuableDropThreshold())
				{
					String[] valuableDrop = matcher.group(1).split(" \\(");
					String valuableDropName = (String) Array.get(valuableDrop, 0);
					String valuableDropValueString = matcher.group(2);
					sendValuableDropMessage(valuableDropName, valuableDropValueString);
				}
			}
		}
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired scriptPreFired)
	{
		switch (scriptPreFired.getScriptId())
		{
			case ScriptID.NOTIFICATION_START:
				notificationStarted = true;
				break;
			case ScriptID.NOTIFICATION_DELAY:
				if (!notificationStarted)
				{
					return;
				}
				String notificationTopText = client.getVarcStrValue(VarClientStr.NOTIFICATION_TOP_TEXT);
				String notificationBottomText = client.getVarcStrValue(VarClientStr.NOTIFICATION_BOTTOM_TEXT);
				if (notificationTopText.equalsIgnoreCase("Collection log") && config.setCollectionLogs())
				{
					String itemName = "**" + Text.removeTags(notificationBottomText).substring("New item:".length()) + "**";
					sendCollectionLogMessage(itemName);
				}
				notificationStarted = false;
				break;
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath)
	{
		if (!config.sendDeath()) {
			return;
		}

		Actor actor = actorDeath.getActor();
		if (actor instanceof Player)
		{
			Player player = (Player) actor;
			if (player == client.getLocalPlayer())
			{
				sendDeathMessage();
			}
		}
	}

	private boolean shouldSendForThisLevel(int level)
	{
		return level >= config.minLevel()
				&& levelMeetsIntervalRequirement(level);
	}

	private boolean levelMeetsIntervalRequirement(int level) {
		int levelInterval = config.levelInterval();

		if (config.linearLevelMax() > 0) {
			levelInterval = (int) Math.max(Math.ceil(-.1*level + config.linearLevelMax()), 1);
		}

		return levelInterval <= 1
				|| level == 99
				|| level % levelInterval == 0;
	}

	private void sendQuestMessage(String questName)
	{
		String localName = "**" + client.getLocalPlayer().getName() + "**";

		String questMessageString = config.questMessage().replaceAll("\\$name", localName)
														 .replaceAll("\\$quest", questName);

		DiscordWebhookBody discordWebhookBody = new DiscordWebhookBody();
		discordWebhookBody.setContent(questMessageString);
		sendWebhook(discordWebhookBody, config.sendQuestingScreenshot());
	}

	private void sendDeathMessage()
	{
		String localName = "**" + client.getLocalPlayer().getName() + "**";

		String deathMessageString = config.deathMessage().replaceAll("\\$name", localName);

		DiscordWebhookBody discordWebhookBody = new DiscordWebhookBody();
		discordWebhookBody.setContent(deathMessageString);
		sendWebhook(discordWebhookBody, config.sendDeathScreenshot());
	}

	private void sendClueMessage()
	{
		String localName = "**" + client.getLocalPlayer().getName() + "**";

		String clueMessage = config.clueMessage().replaceAll("\\$name", localName);

		DiscordWebhookBody discordWebhookBody = new DiscordWebhookBody();
		discordWebhookBody.setContent(clueMessage);
		sendWebhook(discordWebhookBody, config.sendClueScreenshot());
	}

	private void sendLevelMessage()
	{
		String localName = "**" + client.getLocalPlayer().getName() + "**";

		String levelUpString = config.levelMessage().replaceAll("\\$name", localName);

		String[] skills = new String[leveledSkills.size()];
		skills = leveledSkills.toArray(skills);
		leveledSkills.clear();

		for (int i = 0; i < skills.length; i++)
		{
			if(i != 0) {
				levelUpString += config.andLevelMessage();
			}

			String fixed = levelUpString
					.replaceAll("\\$skill", skills[i])
					.replaceAll("\\$level", currentLevels.get(skills[i]).toString());

			levelUpString = fixed;
		}

		DiscordWebhookBody discordWebhookBody = new DiscordWebhookBody();
		discordWebhookBody.setContent(levelUpString);
		sendWebhook(discordWebhookBody, config.sendLevellingScreenshot());
	}

	private void sendPetMessage()
	{
		String localName = "**" + client.getLocalPlayer().getName() + "**";

		String petMessageString = config.petMessage().replaceAll("\\$name", localName);

		DiscordWebhookBody discordWebhookBody = new DiscordWebhookBody();
		discordWebhookBody.setContent(petMessageString);
		sendWebhook(discordWebhookBody, config.sendPetScreenshot());
	}

	private void sendCollectionLogMessage(String itemName)
	{
		String localName = "**" + client.getLocalPlayer().getName() + "**";

		String collectionLogMessageString = config.collectionLogMessage()
				.replaceAll("\\$name", localName)
				.replaceAll("\\$itemName", itemName);

		DiscordWebhookBody discordWebhookBody = new DiscordWebhookBody();
		discordWebhookBody.setContent(collectionLogMessageString);
		sendWebhook(discordWebhookBody, config.sendCollectionLogScreenshot());
	}

	private void sendValuableDropMessage(String itemName, String itemValue)
	{
		String localName = "**" + client.getLocalPlayer().getName() + "**";

		String valuableDropMessageString = config.valuableDropMessage()
				.replaceAll("\\$name", localName)
				.replaceAll("\\$itemName", itemName)
				.replaceAll("\\$itemValue", itemValue);

		DiscordWebhookBody discordWebhookBody = new DiscordWebhookBody();
		discordWebhookBody.setContent(valuableDropMessageString);
		sendWebhook(discordWebhookBody, config.sendValuableDropScreenshot());
	}

	private void sendWebhook(DiscordWebhookBody discordWebhookBody, boolean sendScreenshot)
	{
		String configUrl = config.webhook();
		if (Strings.isNullOrEmpty(configUrl)) { return; }

		HttpUrl url = HttpUrl.parse(configUrl);
		MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("payload_json", GSON.toJson(discordWebhookBody));

		if (sendScreenshot)
		{
			sendWebhookWithScreenshot(url, requestBodyBuilder);
		}
		else
		{
			buildRequestAndSend(url, requestBodyBuilder);
		}
	}

	private void sendWebhookWithScreenshot(HttpUrl url, MultipartBody.Builder requestBodyBuilder)
	{
		drawManager.requestNextFrameListener(image ->
		{
			BufferedImage bufferedImage = (BufferedImage) image;
			byte[] imageBytes;
			try
			{
				imageBytes = convertImageToByteArray(bufferedImage);
			}
			catch (IOException e)
			{
				log.warn("Error converting image to byte array", e);
				return;
			}

			requestBodyBuilder.addFormDataPart("file", "image.png",
					RequestBody.create(MediaType.parse("image/png"), imageBytes));
			buildRequestAndSend(url, requestBodyBuilder);
		});
	}

	private void buildRequestAndSend(HttpUrl url, MultipartBody.Builder requestBodyBuilder)
	{
		RequestBody requestBody = requestBodyBuilder.build();
		Request request = new Request.Builder()
				.url(url)
				.post(requestBody)
				.build();
		sendRequest(request);
	}

	private void sendRequest(Request request)
	{
		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.debug("Error submitting webhook", e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				response.close();
			}
		});
	}

	private static byte[] convertImageToByteArray(BufferedImage bufferedImage) throws IOException
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}

	private void resetState()
	{
		currentLevels.clear();
		leveledSkills.clear();
		shouldSendLevelMessage = false;
		shouldSendQuestMessage = false;
		shouldSendClueMessage = false;
		ticksWaited = 0;
	}

	static String parseQuestCompletedWidget(final String text)
	{
		// "You have completed The Corsair Curse!"
		final Matcher questMatch1 = QUEST_PATTERN_1.matcher(text);
		// "'One Small Favour' completed!"
		final Matcher questMatch2 = QUEST_PATTERN_2.matcher(text);
		final Matcher questMatchFinal = questMatch1.matches() ? questMatch1 : questMatch2;
		if (!questMatchFinal.matches())
		{
			return "Unable to find quest name!";
		}

		String quest = questMatchFinal.group("quest");
		String verb = questMatchFinal.group("verb") != null ? questMatchFinal.group("verb") : "";

		if (verb.contains("kind of"))
		{
			quest += " partial completion";
		}
		else if (verb.contains("completely"))
		{
			quest += " II";
		}

		if (RFD_TAGS.stream().anyMatch((quest + verb)::contains))
		{
			quest = "Recipe for Disaster - " + quest;
		}

		if (WORD_QUEST_IN_NAME_TAGS.stream().anyMatch(quest::contains))
		{
			quest += " Quest";
		}

		return quest;
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		int groupId = event.getGroupId();

		if (groupId == QUEST_COMPLETED_GROUP_ID) {
			shouldSendQuestMessage = true;
		}

		if (groupId == WidgetID.CLUE_SCROLL_REWARD_GROUP_ID) {
			shouldSendClueMessage = true;
		}
	}
}

package com.enhanceddiscordnotifications;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class EnhancedDiscordNotificationsTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(EnhancedDiscordNotificationsPlugin.class);
		RuneLite.main(args);
	}
}
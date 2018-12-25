package me.just.moderation.database;

import me.just.moderation.Bot;

public class MySqlManagementTemplate {

	public static String testGetter(String id) {
		final String out = Bot.getMySql().getString("", "", "", id);
		if (out == null)
			return "Not Set";
		return out;
	}

	public static void testSetter(String entry, String id) {
		Bot.getMySql().setString("", "", entry, "", id);
	}
}

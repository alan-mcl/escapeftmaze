package mclachlan.maze.data.v2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.UserConfig;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static mclachlan.maze.data.v2.serialisers.V2SerialiserFactory.getUserConfigSerialiser;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tier-2 round-trip for {@link UserConfig} V2 JSON serialisation.
 */
public class UserConfigSerialisationTest extends MazeTestSupport
{
	private static Database db;

	@BeforeAll
	void setUp() throws Exception
	{
		db = TestData.buildEmptyDatabase();
	}

	@AfterAll
	void tearDown()
	{
		db = null;
	}

	@Test
	void userConfigRoundTrip_matchesSerialisedJsonMap() throws Exception
	{
		UserConfig original = UserConfig.fromProperties(legacyProperties());

		Map<String, Object> expected = getUserConfigSerialiser().toObject(original, db);

		File file = File.createTempFile("user-config-", ".json");
		file.deleteOnExit();
		try (BufferedWriter writer = new BufferedWriter(
			new FileWriter(file, StandardCharsets.UTF_8)))
		{
			new SingletonSilo<>(getUserConfigSerialiser()).save(writer, original, db);
		}

		UserConfig loaded;
		try (BufferedReader reader = new BufferedReader(
			new FileReader(file, StandardCharsets.UTF_8)))
		{
			loaded = (UserConfig)new SingletonSilo<>(getUserConfigSerialiser()).load(reader, db);
		}

		Map<String, Object> actual = getUserConfigSerialiser().toObject(loaded, db);
		assertEquals(expected, actual);
		assertEquals(400, loaded.getCombatDelay());
		assertEquals("true", loaded.getExtras().get("unlock.race.gnome"));
	}

	private static Properties legacyProperties()
	{
		Properties p = new Properties();
		p.setProperty(UserConfig.Key.COMBAT_DELAY.getValue(), "400");
		p.setProperty(UserConfig.Key.PERSONALITY_CHATTINESS.getValue(), "1");
		p.setProperty(UserConfig.Key.MUSIC_VOLUME.getValue(), "33");
		p.setProperty(UserConfig.Key.CURRENT_TIP_INDEX.getValue(), "10");
		p.setProperty(UserConfig.Key.AUTO_ADD_CONSUMABLES.getValue(), "true");
		p.setProperty("unlock.race.gnome", "true");
		return p;
	}
}

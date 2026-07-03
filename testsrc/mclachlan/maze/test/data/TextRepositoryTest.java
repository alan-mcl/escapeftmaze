package mclachlan.maze.test.data;

import static org.junit.jupiter.api.Assertions.*;

import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.test.support.InMemoryLoader;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TextRepositoryTest extends MazeTestSupport
{
	private InMemoryLoader loader;

	@BeforeEach
	void setUpDatabase() throws Exception
	{
		loader = new InMemoryLoader();
		TestData.buildDatabase(loader);
	}

	@Test
	void hotStringLookupUsesExplicitValue()
	{
		loader.textRepository.putHotString("ui", "common.ok", "OK");

		assertEquals("OK", StringUtil.getUiLabel("common.ok"));
	}

	@Test
	void hotStringFormattingStillWorks()
	{
		loader.textRepository.putHotString("event", "grant.gold", "Party receives %sgp");

		assertEquals("Party receives 10gp", StringUtil.getEventText("grant.gold", 10));
	}

	@Test
	void coldStringLookupReturnsBody()
	{
		loader.textRepository.putColdString("test.book", "A long book body.");

		assertEquals("A long book body.", StringUtil.getColdString("test.book"));
	}

	@Test
	void reservedKeysMapToThemselves()
	{
		assertEquals("foo.reserved.bar", StringUtil.getColdString("foo.reserved.bar"));
	}
}

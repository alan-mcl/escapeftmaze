package mclachlan.maze.stat;

import java.awt.Color;
import java.util.*;
import mclachlan.maze.data.v1.DataObject;

/**
 *
 */
public class Personality extends DataObject
{
	private String name;
	private String description;
	private Color colour;
	private Map<String, String> speech;

	/*-------------------------------------------------------------------------*/
	public Personality(String name, String description,
		Map<String, String> speech, Color colour)
	{
		this.name = name;
		this.description = description;
		this.colour = colour;
		this.speech = new TreeMap<String, String>(speech);
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	/*-------------------------------------------------------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	public String getDescription()
	{
		return description;
	}

	/*-------------------------------------------------------------------------*/
	public void setDescription(String description)
	{
		this.description = description;
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, String> getSpeech()
	{
		return speech;
	}

	/*-------------------------------------------------------------------------*/
	public void setSpeech(Map<String, String> speech)
	{
		this.speech = speech;
	}

	/*-------------------------------------------------------------------------*/
	public Color getColour()
	{
		return colour;
	}

	/*-------------------------------------------------------------------------*/
	public void setColour(Color colour)
	{
		this.colour = colour;
	}

	/*-------------------------------------------------------------------------*/
	public String getWords(String key)
	{
		return speech.get(key);
	}

	/*-------------------------------------------------------------------------*/
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("Personality");
		sb.append("{name='").append(name).append('\'');
		sb.append(", colour=").append(colour);
		sb.append('}');
		return sb.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static enum BasicSpeech
	{
		PERSONALITY_SELECTED("personality.selected"),
		CHARACTER_RECRUITED("character.recruited"),
		ENCOUNTER_START_EASY("encounter.start.easy"),
		ENCOUNTER_START_NORMAL("encounter.start.normal"),
		ENCOUNTER_START_HARD("encounter.start.hard"),
		ENCOUNTER_WIN_EASY("encounter.win.easy"),
		ENCOUNTER_WIN_NORMAL("encounter.win.normal"),
		ENCOUNTER_WIN_HARD("encounter.win.hard"),
		MELEE_ATTACK_1("melee.attack.1"),
		MELEE_ATTACK_2("melee.attack.2"),
		MELEE_ATTACK_3("melee.attack.3"),
		SLAY_FOE("slay.foe.1"),
		ALLY_DIES_MALE("ally.dies.male"),
		ALLY_DIES_FEMALE("ally.dies.female"),
		LAST_MAN_STANDING("last.man.standing"),
		BADLY_WOUNDED("badly.wounded"),
		RESURRECTED("resurrected"),
		CONDITION_POISON("condition.poison"),
		CONDITION_FEAR("condition.fear"),
		CONDITION_BLIND("condition.blind"),
		CONDITION_PLYZE("condition.plyze"),
		CONDITION_DISEASE("condition.disease"),
		CONDITION_HEX("condition.hex"),
		CONDITION_INSANE_1("condition.insane.1"),
		CONDITION_INSANE_2("condition.insane.2"),
		CONDITION_INSANE_3("condition.insane.3"),
		CONDITION_IRRITATE("condition.irritate"),
		CONDITION_NAUSEA("condition.nausea"),
		CONDITION_WEB("condition.web"),
		INVENTORY_HEAVY_LOAD("inventory.heavy.load"),
		INVENTORY_PREMIUM_ITEM("inventory.premium.item"),
		SCOUTING_SPOT_STASH("scouting.spot.stash")
		;


		/*----------------------------------------------------------------------*/
		private final String key;

		public String getKey() {return key;}

		BasicSpeech(String key)
		{
			this.key = key;
		}
	}

	public static enum DefaultCampaignSpeech
	{
		GATEHOUSE_1("default.gatehouse.1"),
		GATEHOUSE_WELCOME_SIGN("default.gatehouse.welcome.sign"),
		GATEHOUSE_BLOCK_GOLEM("default.gatehouse.block.golem"),
		GATEHOUSE_HIDDEN_STUFF_1("default.gatehouse.hidden.stuff.1"),
		GATEHOUSE_MALIGNANT_WIDOWS("default.gatehouse.malignant.widows"),
		ICHIBA_CROSSROAD_INTRO("default.ichiba.crossroad.intro"),
		ICHIBA_CROSSROAD_HIDDEN_STUFF_1("default.ichiba.crossroad.hidden.stuff.1"),
		ICHIBA_CROSSROAD_MANTIS_GLEN("default.ichiba.crossroad.mantis.glen"),
		ICHIBA_DOMAIN_SOUTH_SHRINE("default.ichiba.domain.south.shrine"),
		ICHIBA_CITY_INTRO("default.ichiba.city.intro"),
		ICHIBA_CITY_VILLA_DOOR("default.ichiba.villa.door"),
		ICHIBA_CITY_TEMPLE_OF_DANA_SIGN("default.ichiba.city.temple.of.dana.sign"),
		ICHIBA_CITY_GNOME_GARDEN("default.ichiba.city.gnome.garden"),
		ICHIBA_CITY_THIEVES_GUILD_DOOR("default.ichiba.thieves.guild.door"),
		ICHIBA_CITY_IMOGEN_DOOR("default.ichiba.city.imogen.door"),
		ICHIBA_CITY_IMOGEN_TOWER_1("default.ichiba.city.imogen.tower.1"),
		GNOLL_VILLAGE_INTRO("default.gnoll.village.intro"),
		AENEN_CITY_INTRO("default.aenen.city.intro"),
		AENEN_OUTSKIRTS_SOLAR_PANEL_DISCOVERY("default.aenen.outskirts.solar.panel.discovery"),
		AENEN_OUTSKIRTS_CHARRED_STUMP("default.aenen.outskirts.charred.stump"),
		DANAOS_VILLAGE_INTRO("default.danaos.village.intro"),
		DANAOS_CASTLE_INTRO("default.danaos.castle.intro"),
		DANAOS_DUNGEON_INTRO("default.danaos.dungeon.intro"),
		STYGION_FOREST_INTRO("default.stygios.forest.intro"),
		STYGION_FOREST_FAERIE_CIRCLE("default.stygios.forest.faerie.circle"),
		STYGION_FOREST_SHRINE("default.stygios.forest.shrine"),
		PLAIN_OF_PILLARS_INTRO("default.plain.of.pillars.intro"),
		RUINS_OF_HAIL_INTRO("default.ruins.of.hail.intro"),
		TORNADO_MOUNTAIN_DRAGON_CAVE("default.tornado.mountain.dragon.cave"),
		WRITHING_MIRE_INTRO("default.writhing.mire.intro"),
		HIANBIAN_INTRO("default.hianbian.intro"),
		HIANBIAN_YENLUO_ENCOUNTER("default.hianbian.yenluo.encounter"),
		TEMPLE_OF_THE_GATE_END("default.temple.of.the.gate.end"),
		DALEN_INTRO("default.dalen.intro"),
		;

		/*----------------------------------------------------------------------*/
		private final String key;

		public String getKey() {return key;}

		DefaultCampaignSpeech(String key)
		{
			this.key = key;
		}
	}
}

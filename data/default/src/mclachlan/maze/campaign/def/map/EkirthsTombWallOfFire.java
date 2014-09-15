package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.CombatStatistics;
import mclachlan.maze.stat.combat.event.AttackEvent;
import mclachlan.maze.stat.combat.event.DamageEvent;
import mclachlan.maze.stat.combat.event.SoundEffectEvent;
import mclachlan.maze.stat.magic.AbstractActor;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.SpellEffect;

/**
 *
 */
public class EkirthsTombWallOfFire extends TileScript
{
	public static final String WALL_OF_FIRE_DEACTIVATED =
		"ekirths.tomb.wall.of.fire.deactivated";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(
		Maze maze,
		Point tile,
		Point previousTile,
		int facing)
	{
		if (!MazeVariables.getBoolean(WALL_OF_FIRE_DEACTIVATED))
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(new SoundEffectEvent("25712_Erdie_fire_1"));

			PlayerParty party = Maze.getInstance().getParty();

			for (UnifiedActor actor : party.getActors())
			{
				int damage = new Dice(1, 1000, 9000).roll();
				result.add(
					new DamageEvent(
						actor,
						new AbstractActor() {},
						new DamagePacket(damage, 1), 
						MagicSys.SpellEffectType.FIRE,
						MagicSys.SpellEffectSubType.HEAT,
						new DummyAttackWith(),
						new CombatStatistics("stub")));
			}

			return result;
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	private static class DummyAttackWith implements AttackWith
	{
		public String getDisplayName()
		{
			return "DummyAttackWith.getDisplayName";
		}

		public int getToHit()
		{
			return 0;
		}

		public int getToPenetrate()
		{
			return 0;
		}

		public int getToCritical()
		{
			return 0;
		}

		public Dice getDamage()
		{
			return Dice.d1;
		}

		public int getDefaultDamageType()
		{
			return MagicSys.SpellEffectType.NONE;
		}

		public String describe(AttackEvent e)
		{
			return "DummyAttackWith.describe";
		}

		public String[] getAttackTypes()
		{
			return null;
		}

		public int getMaxRange()
		{
			return 0;
		}

		public int getMinRange()
		{
			return 0;
		}

		public boolean isRanged()
		{
			return false;
		}

		public boolean isBackstabCapable()
		{
			return false;
		}

		public boolean isSnipeCapable()
		{
			return false;
		}

		public GroupOfPossibilities<SpellEffect> getSpellEffects()
		{
			return null;
		}

		public int getSpellEffectLevel()
		{
			return 0;
		}

		public String slaysFoeType()
		{
			return null;
		}

		public MazeScript getAttackScript()
		{
			return null;
		}

		public ItemTemplate.AmmoType isAmmoType()
		{
			return ItemTemplate.AmmoType.SELF;
		}

		public List<ItemTemplate.AmmoType> getAmmoRequired()
		{
			return null;
		}
	}
}

package mclachlan.maze.balance;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.stat.Stats.Modifiers.*;
import static mclachlan.maze.stat.ItemTemplate.*;

/**
 *
 */
public class ItemScorer
{
	static Map<String, Double> weaponModifierScores = new HashMap<String, Double>();

	/*-------------------------------------------------------------------------*/
	public double scoreItem(ItemTemplate t)
	{
		switch (t.getType())
		{
			case Type.SHORT_WEAPON:
			case Type.EXTENDED_WEAPON:
			case Type.RANGED_WEAPON:
			case Type.THROWN_WEAPON:
				return scoreWeapon(t);
			// todo: other types
			default:
				throw new MazeException("not implemented: " + ItemTemplate.Type.describe(t.getType()));
		}
	}

	/*-------------------------------------------------------------------------*/
	public double scoreWeapon(ItemTemplate t)
	{
		double result = 0;

		result += t.getDamage().getAverage();

		// weapon specifics
		result += t.getToHit();
		result += t.getToInitiative();
		result += t.getToCritical();
		result += (t.getToPenetrate() * 0.1);
		result += (t.getBonusAttacks() * 3);
		result += (t.getBonusStrikes() * 3);

		// modifiers
		for (String m : t.getModifiers().getModifiers().keySet())
		{
			int modifier = t.getModifiers().getModifier(m);
			result += (modifier * weaponModifierScores.get(m));
		}

		// ammo required
		if (t.getAmmo() != null)
		{
			result -= 1;
			result += (t.getAmmo().size() * .1);
		}

		// nr modifiers required to use
		int nrModifiersRequired = t.getAttackTypes().length;

		if (nrModifiersRequired == 2)
		{
			result *= 0.75;
		}
		else if (nrModifiersRequired == 3)
		{
			result *= 0.66;
		}
		else if (nrModifiersRequired >= 4)
		{
			result *= 0.5;
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V1Loader();
		Saver saver = new V1Saver();
		Database db = new Database(loader, saver);
		Campaign campaign = Maze.getStubCampaign();
		loader.init(campaign);

		Map<String, ItemTemplate> map = db.getItemTemplates();

		ItemScorer s = new ItemScorer();

		for (ItemTemplate t : map.values())
		{
			if (t.isWeapon())
			{
				try
				{
					System.out.println(t.getName() +
						"," + s.scoreItem(t) +
						"," + toAttackTypeString(t.getAttackTypes()));
				}
				catch (Exception e)
				{
					System.out.println("!!! " + t.getName());
					throw e;
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toAttackTypeString(String[] attackTypes)
	{
		StringBuilder sb = new StringBuilder();
		for (String s : attackTypes)
		{
			sb.append(s);
			sb.append("-");
		}
		return sb.substring(0, sb.length()-1);
	}

	/*-------------------------------------------------------------------------*/
	static
	{
		weaponModifierScores.put(HIT_POINTS, 1.0);
		weaponModifierScores.put(ACTION_POINTS, 1.0);
		weaponModifierScores.put(MAGIC_POINTS, 1.0);
		weaponModifierScores.put(BRAWN, 3.0);
		weaponModifierScores.put(SKILL, 3.0);
		weaponModifierScores.put(THIEVING, 1.0);
		weaponModifierScores.put(SNEAKING, 1.0);
		weaponModifierScores.put(BRAINS, 1.0);
		weaponModifierScores.put(POWER, 1.0);
		weaponModifierScores.put(SWING, 2.0);
		weaponModifierScores.put(THRUST, 2.0);
		weaponModifierScores.put(CUT, 2.0);
		weaponModifierScores.put(LUNGE, 2.0);
		weaponModifierScores.put(BASH, 2.0);
		weaponModifierScores.put(PUNCH, 2.0);
		weaponModifierScores.put(KICK, 2.0);
		weaponModifierScores.put(THROW, 2.0);
		weaponModifierScores.put(SHOOT, 2.0);
		weaponModifierScores.put(FIRE, 2.0);
		weaponModifierScores.put(DUAL_WEAPONS, 2.0);
		weaponModifierScores.put(CHIVALRY, 2.0);
		weaponModifierScores.put(KENDO, 2.0);
		weaponModifierScores.put(STREETWISE, 1.0);
		weaponModifierScores.put(DUNGEONEER, 1.0);
		weaponModifierScores.put(WILDERNESS_LORE, 1.0);
		weaponModifierScores.put(SURVIVAL, 1.0);
		weaponModifierScores.put(BACKSTAB, 2.0);
		weaponModifierScores.put(SNIPE, 2.0);
		weaponModifierScores.put(LOCK_AND_TRAP, 1.0);
		weaponModifierScores.put(STEAL, 1.0);
		weaponModifierScores.put(MARTIAL_ARTS, 2.0);
		weaponModifierScores.put(MELEE_CRITICALS, 2.0);
		weaponModifierScores.put(THROWN_CRITICALS, 2.0);
		weaponModifierScores.put(RANGED_CRITICALS, 2.0);
		weaponModifierScores.put(CHANT, 1.0);
		weaponModifierScores.put(RHYME, 1.0);
		weaponModifierScores.put(GESTURE, 1.0);
		weaponModifierScores.put(POSTURE, 1.0);
		weaponModifierScores.put(THOUGHT, 1.0);
		weaponModifierScores.put(HERBAL, 1.0);
		weaponModifierScores.put(ALCHEMIC, 1.0);
		weaponModifierScores.put(ARTIFACTS, 1.0);
		weaponModifierScores.put(MYTHOLOGY, 1.0);
		weaponModifierScores.put(CRAFT, .5);
		weaponModifierScores.put(POWER_CAST, 1D);
		weaponModifierScores.put(ENGINEERING, .5);
		weaponModifierScores.put(MUSIC, .5);
		weaponModifierScores.put(INITIATIVE, 2D);
		weaponModifierScores.put(ATTACK, 2D);
		weaponModifierScores.put(DEFENCE, 2D);
		weaponModifierScores.put(DAMAGE, 1D);
		weaponModifierScores.put(TO_PENETRATE, 1D);
		weaponModifierScores.put(VS_PENETRATE, 1D);
		weaponModifierScores.put(VS_AMBUSH, .5);
		weaponModifierScores.put(VS_DODGE, .5);
		weaponModifierScores.put(VS_HIDE, .5);
		weaponModifierScores.put(TO_THREATEN, .5);
		weaponModifierScores.put(TO_BRIBE, .5);
		weaponModifierScores.put(TO_RUN_AWAY, .5);
		weaponModifierScores.put(RESIST_BLUDGEONING, .5);
		weaponModifierScores.put(RESIST_PIERCING, .5);
		weaponModifierScores.put(RESIST_SLASHING, .5);
		weaponModifierScores.put(RESIST_FIRE, .25);
		weaponModifierScores.put(RESIST_WATER, .25);
		weaponModifierScores.put(RESIST_EARTH, .25);
		weaponModifierScores.put(RESIST_AIR, .25);
		weaponModifierScores.put(RESIST_MENTAL, .25);
		weaponModifierScores.put(RESIST_ENERGY, .25);
		weaponModifierScores.put(RED_MAGIC_GEN, 3D);
		weaponModifierScores.put(BLACK_MAGIC_GEN, 3D);
		weaponModifierScores.put(PURPLE_MAGIC_GEN, 3D);
		weaponModifierScores.put(GOLD_MAGIC_GEN, 3D);
		weaponModifierScores.put(WHITE_MAGIC_GEN, 3D);
		weaponModifierScores.put(GREEN_MAGIC_GEN, 3D);
		weaponModifierScores.put(BLUE_MAGIC_GEN, 3D);
		weaponModifierScores.put(HIT_POINT_REGEN, 1.5D);
		weaponModifierScores.put(ACTION_POINT_REGEN, 1D);
		weaponModifierScores.put(MAGIC_POINT_REGEN, 1D);
		weaponModifierScores.put(STAMINA_REGEN, 1D);
		weaponModifierScores.put(IMMUNE_TO_DAMAGE, 50D);
		weaponModifierScores.put(IMMUNE_TO_HEAT, 5D);
		weaponModifierScores.put(IMMUNE_TO_COLD, 5D);
		weaponModifierScores.put(IMMUNE_TO_POISON, 4D);
		weaponModifierScores.put(IMMUNE_TO_LIGHTNING, 4D);
		weaponModifierScores.put(IMMUNE_TO_PSYCHIC, 5D);
		weaponModifierScores.put(IMMUNE_TO_ACID, 4D);
		weaponModifierScores.put(IMMUNE_TO_BLIND, 3D);
		weaponModifierScores.put(IMMUNE_TO_DISEASE, 2D);
		weaponModifierScores.put(IMMUNE_TO_FEAR, 2D);
		weaponModifierScores.put(IMMUNE_TO_HEX, 3D);
		weaponModifierScores.put(IMMUNE_TO_INSANE, 4D);
		weaponModifierScores.put(IMMUNE_TO_INVISIBLE, 1D);
		weaponModifierScores.put(IMMUNE_TO_IRRITATE, 1.5D);
		weaponModifierScores.put(IMMUNE_TO_KO, 4D);
		weaponModifierScores.put(IMMUNE_TO_NAUSEA, 3D);
		weaponModifierScores.put(IMMUNE_TO_PARALYSE, 4D);
		weaponModifierScores.put(IMMUNE_TO_POSSESSION, 4D);
		weaponModifierScores.put(IMMUNE_TO_SILENCE, 3D);
		weaponModifierScores.put(IMMUNE_TO_SLEEP, 4D);
		weaponModifierScores.put(IMMUNE_TO_STONE, 3D);
		weaponModifierScores.put(IMMUNE_TO_SWALLOW, 3D);
		weaponModifierScores.put(IMMUNE_TO_WEB, 4D);
		weaponModifierScores.put(LIGHT_SLEEPER, 2D);
		weaponModifierScores.put(BLIND_FIGHTING, 2D);
		weaponModifierScores.put(EXTRA_GOLD, .1D);
		weaponModifierScores.put(CHEAT_DEATH, 5D);
		weaponModifierScores.put(MAGIC_ABSORPTION, .1D);
		weaponModifierScores.put(ARROW_CUTTING, .2D);
		weaponModifierScores.put(AMBUSHER, .75D);
		weaponModifierScores.put(ENTERTAINER, .1D);
		weaponModifierScores.put(DIPLOMAT, .1D);
		weaponModifierScores.put(BLINK, 1D);
		weaponModifierScores.put(TIRELESS_SWORD, 2D);
		weaponModifierScores.put(TIRELESS_AXE, 2D);
		weaponModifierScores.put(TIRELESS_SPEAR, 2D);
		weaponModifierScores.put(TIRELESS_MACE, 2D);
		weaponModifierScores.put(TIRELESS_DAGGER, 2D);
		weaponModifierScores.put(TIRELESS_STAFF, 2D);
		weaponModifierScores.put(TIRELESS_BOW, 2D);
		weaponModifierScores.put(TIRELESS_THROWN, 2D);
		weaponModifierScores.put(TIRELESS_UNARMED, 1D);
		weaponModifierScores.put(TOUCH_BLIND, 2D);
		weaponModifierScores.put(TOUCH_FEAR, 2D);
		weaponModifierScores.put(TOUCH_HEX, 2D);
		weaponModifierScores.put(TOUCH_INSANE, 2D);
		weaponModifierScores.put(TOUCH_IRRITATE, 1D);
		weaponModifierScores.put(TOUCH_NAUSEA, 2D);
		weaponModifierScores.put(TOUCH_SILENCE, 2D);
		weaponModifierScores.put(TOUCH_SLEEP, 2D);
		weaponModifierScores.put(TOUCH_STONE, 2D);
		weaponModifierScores.put(TOUCH_PARALYSE, 3D);
		weaponModifierScores.put(TOUCH_WEB, 3D);
		weaponModifierScores.put(TOUCH_DISEASE, 2D);
		weaponModifierScores.put(TOUCH_POISON, 2D);
		weaponModifierScores.put(RAZOR_CLOAK, 1D);
		weaponModifierScores.put(DAMAGE_MULTIPLIER, 7.5D);
		weaponModifierScores.put(LIGHTNING_STRIKE_SWORD, 2D);
		weaponModifierScores.put(LIGHTNING_STRIKE_AXE, 2D);
		weaponModifierScores.put(LIGHTNING_STRIKE_SPEAR, 2D);
		weaponModifierScores.put(LIGHTNING_STRIKE_MACE, 2D);
		weaponModifierScores.put(LIGHTNING_STRIKE_DAGGER, 2D);
		weaponModifierScores.put(LIGHTNING_STRIKE_STAFF, 2D);
		weaponModifierScores.put(LIGHTNING_STRIKE_UNARMED, 2D);
		weaponModifierScores.put(BERSERKER, 4D);
		weaponModifierScores.put(DEADLY_STRIKE, .1D);
		weaponModifierScores.put(DODGE, .1D);
		weaponModifierScores.put(MASTER_ARCHER, 2D);
		weaponModifierScores.put(DIVINE_PROTECTION, 3D);
		weaponModifierScores.put(KI_FURY, 2D);
		weaponModifierScores.put(FEY_AFFINITY, 2D);
		weaponModifierScores.put(ARCANE_BLOOD, 2D);
		weaponModifierScores.put(DISPLACER, 2D);
		weaponModifierScores.put(PARRY, .1D);
		weaponModifierScores.put(MELEE_MASTER, 1D);
		weaponModifierScores.put(DEADLY_AIM, 1D);
		weaponModifierScores.put(MASTER_THIEF, 1D);
		weaponModifierScores.put(OBFUSCATION, 2D);
		weaponModifierScores.put(SHADOW_MASTER, 2D);
		weaponModifierScores.put(CHARMED_DESTINY, 2D);
		weaponModifierScores.put(CHANNELLING, 2D);
		weaponModifierScores.put(SIGNATURE_WEAPON_ENGINEERING, 2D);
		weaponModifierScores.put(AMPHIBIOUS, 1D);
		weaponModifierScores.put(BONUS_ATTACKS, 2D);
		weaponModifierScores.put(BONUS_STRIKES, 2D);
	}
}

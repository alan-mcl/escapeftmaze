package mclachlan.maze.stat.combat;

import java.util.*;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.event.AttackEvent;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class BackstabSnipeAttack implements AttackWith
{
	private AttackWith weapon;
	private UnifiedActor attacker;
	private boolean isBackstab;
	private Stats.Modifier modifier;

	public BackstabSnipeAttack(AttackWith weapon, UnifiedActor attacker)
	{
		this.weapon = weapon;
		this.attacker = attacker;

		if (weapon.isBackstabCapable())
		{
			isBackstab = true;
			modifier = Stats.Modifier.BACKSTAB;
		}
		else if (weapon.isSnipeCapable())
		{
			isBackstab = false;
			modifier = Stats.Modifier.SNIPE;
		}
		else
		{
			throw new MazeException("invalid: "+weapon);
		}
	}

	public boolean isBackstab()
	{
		return isBackstab;
	}

	public AttackWith getWeapon()
	{
		return weapon;
	}

	@Override
	public String getName()
	{
		return "BackstabSnipeAttack-"+weapon.getName();
	}

	@Override
	public String getDisplayName()
	{
		return weapon.getDisplayName();
	}

	@Override
	public int getToHit()
	{
		// modifier already included by the game system
		return weapon.getToHit();
	}

	@Override
	public int getToPenetrate()
	{
		return weapon.getToPenetrate() + attacker.getModifier(modifier);
	}

	@Override
	public int getToCritical()
	{
		return weapon.getToCritical() + attacker.getModifier(modifier);
	}

	@Override
	public int getToInitiative()
	{
		return weapon.getToInitiative() + attacker.getModifier(Stats.Modifier.SNEAKING);
	}

	@Override
	public Dice getDamage()
	{
		// the attack types contain x2 damage multipliers
		return weapon.getDamage();
	}

	@Override
	public MagicSys.SpellEffectType getDefaultDamageType()
	{
		return weapon.getDefaultDamageType();
	}

	@Override
	public String describe(AttackEvent e)
	{
		return weapon.describe(e);
	}

	@Override
	public String[] getAttackTypes()
	{
		if (isBackstab)
		{
			return new String[]{"backstab"};
		}
		else
		{
			return new String[]{"snipe"};
		}
	}

	@Override
	public int getMaxRange()
	{
		return weapon.getMaxRange();
	}

	@Override
	public int getMinRange()
	{
		return weapon.getMinRange();
	}

	@Override
	public boolean isRanged()
	{
		return weapon.isRanged();
	}

	@Override
	public boolean isBackstabCapable()
	{
		return weapon.isBackstabCapable();
	}

	@Override
	public boolean isSnipeCapable()
	{
		return weapon.isSnipeCapable();
	}

	@Override
	public GroupOfPossibilities<SpellEffect> getSpellEffects()
	{
		return weapon.getSpellEffects();
	}

	@Override
	public int getSpellEffectLevel()
	{
		return weapon.getSpellEffectLevel();
	}

	@Override
	public TypeDescriptor slaysFoeType()
	{
		return weapon.slaysFoeType();
	}

	@Override
	public MazeScript getAttackScript()
	{
		return weapon.getAttackScript();
	}

	@Override
	public ItemTemplate.AmmoType isAmmoType()
	{
		return weapon.isAmmoType();
	}

	@Override
	public List<ItemTemplate.AmmoType> getAmmoRequired()
	{
		return weapon.getAmmoRequired();
	}

	@Override
	public int getWeaponType()
	{
		return weapon.getWeaponType();
	}

	@Override
	public int getActionPointCost(UnifiedActor defender)
	{
		return GameSys.getInstance().getBackstabSnipeCost(attacker, defender);
	}
}

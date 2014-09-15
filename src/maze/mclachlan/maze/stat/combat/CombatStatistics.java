package mclachlan.maze.stat.combat;

import java.lang.reflect.Field;
import java.util.*;
import mclachlan.maze.stat.*;

/**
 *
 */
public class CombatStatistics
{
	// just a name
	private String name;

	// actor details
	private int nrPlayerCharacters;
	private int nrFoeGroups;
	private int nrFoes;
	private int nrPcVictories;
	private int nrCombats;

	private List<Integer> pcLevels = new ArrayList<Integer>();
	private List<Integer> pcAttackRate = new ArrayList<Integer>();
	private List<Integer> pcHp = new ArrayList<Integer>();
	private List<Integer> foeLevels = new ArrayList<Integer>();
	private List<Integer> foeHp = new ArrayList<Integer>();
	private List<Integer> foeSp = new ArrayList<Integer>();
	private List<Integer> foeMp = new ArrayList<Integer>();
	private List<Integer> foeAttackDamage = new ArrayList<Integer>();
	private List<Integer> pcInitiative = new ArrayList<Integer>();
	private List<Integer> pcAttackDamage = new ArrayList<Integer>();

	private List<Integer> foeInitiative = new ArrayList<Integer>();
	private int nrPcAttacks, nrFoeAttacks;
	private int nrPcAttackHits, nrFoeAttackHits;

	private int nrPcAttackMiss, nrFoeAttackMiss;
	// combat instance variables
	private Combat.AmbushStatus ambushStatus;

	private int combatRounds;
	// lists of values
	private List<Integer> playerInitiatives = new ArrayList<Integer>();

	/*-------------------------------------------------------------------------*/
	public CombatStatistics(String name)
	{
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	public void captureCombatStart(PlayerParty party, List<FoeGroup> foes)
	{
		nrCombats++;

		nrPlayerCharacters = party.size();
		nrFoeGroups = foes.size();
		nrFoes = countFoes(foes);

		for (UnifiedActor actor : party.getActors())
		{
			pcLevels.add(actor.getLevel());
			pcHp.add(actor.getHitPoints().getMaximum());
			pcAttackRate.add(GameSys.getInstance().getNrAttacks(
				(PlayerCharacter)actor, true));
		}

		for (FoeGroup fg : foes)
		{
			for (UnifiedActor f : fg.getActors())
			{
				foeLevels.add(f.getLevel());
				foeHp.add(f.getHitPoints().getMaximum());
				foeSp.add(f.getActionPoints().getMaximum());
				foeMp.add(f.getMagicPoints().getMaximum());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void captureAmbushStatus(Combat.AmbushStatus ambushStatus)
	{
		this.ambushStatus = ambushStatus;
	}

	/*-------------------------------------------------------------------------*/
	public void incCombatRounds()
	{
		combatRounds++;
	}

	/*-------------------------------------------------------------------------*/
	public void captureInitiative(int initiative, UnifiedActor actor, Combat combat)
	{
		if (actor instanceof PlayerCharacter)
		{
			pcInitiative.add(initiative);
		}
		else if (!combat.isPlayerAlly(actor))
		{
			foeInitiative.add(initiative);
		}
		else
		{
			// todo: party allies
		}
	}

	/*-------------------------------------------------------------------------*/
	public void captureAttack(AttackAction attackAction, Combat combat)
	{
		UnifiedActor actor = attackAction.getActor();

		if (actor instanceof PlayerCharacter)
		{
			nrPcAttacks++;
		}
		else if (!combat.isPlayerAlly(actor))
		{
			nrFoeAttacks++;
		}
		else
		{
			// todo: party allies
		}
	}

	/*-------------------------------------------------------------------------*/
	public void captureAttackHit(AttackAction attackAction, Combat combat)
	{
		UnifiedActor actor = attackAction.getActor();

		if (actor instanceof PlayerCharacter)
		{
			nrPcAttackHits++;
		}
		else if (!combat.isPlayerAlly(actor))
		{
			nrFoeAttackHits++;
		}
		else
		{
			// todo: party allies
		}
	}

	/*-------------------------------------------------------------------------*/
	public void captureAttackMiss(AttackAction attackAction, Combat combat)
	{
		UnifiedActor actor = attackAction.getActor();

		if (actor instanceof PlayerCharacter)
		{
			nrPcAttackMiss++;
		}
		else if (!combat.isPlayerAlly(actor))
		{
			nrFoeAttackMiss++;
		}
		else
		{
			// todo: party allies
		}
	}

	/*-------------------------------------------------------------------------*/
	public void captureCombatEnd(boolean pcVictory)
	{
		if (pcVictory)
		{
			this.nrPcVictories++;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void captureAttackDamage(UnifiedActor attacker, int damage)
	{
		if (attacker instanceof PlayerCharacter)
		{
			pcAttackDamage.add(damage);
		}
		else //if (!combat.isPlayerAlly(attacker))
		{
			foeAttackDamage.add(damage);
		}
//		else
		{
			// todo: party allies
		}
	}

	/*-------------------------------------------------------------------------*/
	private int countFoes(List<FoeGroup> foes)
	{
		int result = 0;

		for (FoeGroup fg : foes)
		{
			result += fg.getActors().size();
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public double getAveragePlayerCharacterLevel()
	{
		return getAverage(pcLevels);
	}

	public double getAverageFoeLevel()
	{
		return getAverage(foeLevels);
	}

	public double getAverageFoeHp()
	{
		return getAverage(foeHp);
	}

	public double getAverageFoeSp()
	{
		return getAverage(foeSp);
	}

	public double getAverageFoeMp()
	{
		return getAverage(foeMp);
	}

	public double getAveragePlayerCharacterInitiative()
	{
		return getAverage(pcInitiative);
	}

	public double getAverageFoeInitiative()
	{
		return getAverage(foeInitiative);
	}

	public double getAveragePcHp()
	{
		return getAverage(pcHp);
	}

	public double getAveragePcAttackRate()
	{
		return getAverage(pcAttackRate);
	}

	public double getAveragePcAttackDamage()
	{
		return getAverage(pcAttackDamage);
	}

	public double getAverageFoeAttackDamage()
	{
		return getAverage(foeAttackDamage);
	}

	/*-------------------------------------------------------------------------*/
	private double getAverage(List<Integer> list)
	{
		double sum = 0D;

		for (int i : list)
		{
			sum += i;
		}

		return sum / list.size();
	}

	/*-------------------------------------------------------------------------*/

	public void aggregate(CombatStatistics s) throws Exception
	{
		for (Field f : this.getClass().getDeclaredFields())
		{
			if (f.getType() == Integer.TYPE)
			{
				f.setInt(this, f.getInt(this)+f.getInt(s));
			}
			else if (f.getType() == List.class)
			{
				((List<Integer>)f.get(this)).addAll((List<Integer>)f.get(s));
			}
			else
			{
				// todo other types
			}
		}
	}

	/*-------------------------------------------------------------------------*/

	public String getName()
	{
		return name;
	}

	public int getNrFoeGroups()
	{
		return nrFoeGroups;
	}

	public int getNrFoes()
	{
		return nrFoes;
	}

	public int getNrPlayerCharacters()
	{
		return nrPlayerCharacters;
	}

	public Combat.AmbushStatus getAmbushStatus()
	{
		return ambushStatus;
	}

	public int getCombatRounds()
	{
		return combatRounds;
	}

	public int getNrPcAttacks()
	{
		return nrPcAttacks;
	}

	public int getNrFoeAttacks()
	{
		return nrFoeAttacks;
	}

	public int getNrFoeAttackHits()
	{
		return nrFoeAttackHits;
	}

	public int getNrFoeAttackMiss()
	{
		return nrFoeAttackMiss;
	}

	public int getNrPcAttackHits()
	{
		return nrPcAttackHits;
	}

	public int getNrPcAttackMiss()
	{
		return nrPcAttackMiss;
	}

	public int getNrCombats()
	{
		return nrCombats;
	}

	public int getNrPcVictories()
	{
		return nrPcVictories;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("CombatStatistics");
		sb.append("[").append(name).append("]");
		sb.append("{nrFoes=").append(nrFoes);
		sb.append(", nrPlayerCharacters=").append(nrPlayerCharacters);
		sb.append('}');
		return sb.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		CombatStatistics s = new CombatStatistics("test agg");

		s.aggregate(new CombatStatistics("test"));
	}
}

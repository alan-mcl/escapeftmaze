package mclachlan.jgpgoap.goap;

import java.util.*;

/**
 *
 */
public class GOAPTest
{
	public static void main(String[] args) throws Exception
	{
		Set<Action> actions = new HashSet<Action>();

		String armedwithgun = "armedwithgun";
		String enemyvisible = "enemyvisible";
		String nearenemy = "nearenemy";
		String enemylinedup = "enemylinedup";
		String weaponloaded = "weaponloaded";
		String enemyalive = "enemyalive";
		String detonatebomb = "detonatebomb";
		String armedwithbomb = "armedwithbomb";
		String alive = "alive";

		actions.add(new Action("scout",
			new WorldState(new Atom(armedwithgun, true)),
			new WorldState(new Atom(enemyvisible, true)),
			1));

		actions.add(new Action("approach",
			new WorldState(new Atom(enemyvisible, true)),
			new WorldState(new Atom(nearenemy, true)),
			1));

		actions.add(new Action("aim",
			new WorldState(new Atom(enemyvisible, true), new Atom(weaponloaded, true)),
			new WorldState(new Atom(enemylinedup, true)),
			1));

		actions.add(new Action("shoot",
			new WorldState(new Atom(enemylinedup, true), new Atom(weaponloaded, true)),
			new WorldState(new Atom(enemyalive, false)),
			1));

		actions.add(new Action("load",
			new WorldState(new Atom(armedwithgun, true)),
			new WorldState(new Atom(weaponloaded, true)),
			1));

		actions.add(new Action(detonatebomb,
			new WorldState(new Atom(armedwithbomb, true), new Atom(nearenemy, true)),
			new WorldState(new Atom(alive, false), new Atom(enemyalive, false)),
			1));

		actions.add(new Action("flee",
			new WorldState(new Atom(nearenemy, true), new Atom(enemyvisible, true)),
			new WorldState(new Atom(nearenemy, false)),
			1));

		WorldState start = new WorldState(
			new Atom(enemyvisible, false),
			new Atom(armedwithgun, true),
			new Atom(weaponloaded, false),
			new Atom(enemylinedup, false),
			new Atom(enemyalive, true),
			new Atom(armedwithbomb, true),
			new Atom(nearenemy, false),
			new Atom(alive, true));

		WorldState goal = new WorldState(
			new Atom(enemyalive, false),
			new Atom(alive, true));

		GOAP goap = new GOAP(start, goal, actions);

		List<Action> plan = goap.plan();

		System.out.println("plan = [" + plan + "]");
	}
}

package mclachlan.maze.campaign.def.npc;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.npc.ChangeNpcLocationEvent;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.stat.Dice.d2;

/**
 *
 */
public class EvaWingfield extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("Striding rapidly through the wilderness comes a tall warrior woman, red cape streaming behind her."),
			new FlavourTextEvent("She stops before you, her jaw set in a look of fierce determination.")
		);
	}

	@Override
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("My greetings to you, wayfarers.", npc),
			new NpcSpeechEvent("Long are the years that I have walked the winding paths of this Second Realm, but few are the likes of you that I have encountered.", npc),
			new NpcSpeechEvent("For unless I miss my mark you are refugees from the previous Realm.", npc),
			new NpcSpeechEvent("Yes, it is clear. Your feats in the face of adversity to reach this point are admirable.", npc),
			new NpcSpeechEvent("Yet be warned that admiration is worth little here.", npc),
			new FlavourTextEvent("She pauses, and her face sets angrily."),
			new NpcSpeechEvent("I should know, for bitter have the years been since I too walked the path that have started.", npc),
			new NpcSpeechEvent("Yea, we were a merry band who came here, sure of our destiny. Sure that soon we would pass through this Realm and continue our escape!", npc),
			new NpcSpeechEvent("Yet now... well, here I am. I walk the ways and do my part to keep them open.", npc),
			new NpcSpeechEvent("Of the others I know not, and care less.", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		// by default, Sir Kay wanders in Ichiba City and Ichiba Crossroads
		// if the party steals anything from a vendor in Ichiba, he tracks them
		// and appears shortly

		boolean moveTowardsParty = !npc.isFound();

		if (moveTowardsParty)
		{
			List<MazeEvent> mazeEvents = moveNpcTowardsParty();
			if (mazeEvents != null)
			{
				return mazeEvents;
			}
		}

		// random movement otherwise
		if (turnNr % 10 == 0)
		{
			if (Dice.d20.roll("Eva zone") == 1)
			{
				// change zone
				return changeEvaZone();
			}
			else
			{
				return moveEvaWithinZone();
			}
		}

		return new ArrayList<>();
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> moveEvaWithinZone()
	{
		String zone = ((Npc)npc).getZone();

		Point newTile = switch (zone)
			{
				case ICHIBA_CROSSROAD -> getRandomIchibaCrossroadTile();
				case ICHIBA_DOMAIN_NORTH -> getRandomIchibaDomainNorthTile();
				case ICHIBA_DOMAIN_SOUTH -> getRandomIchibaDomainSouthTile();
				default ->
					throw new MazeException("Invalid zone for Eva Wingfield [" + zone + "]");
			};

		return getList(new ChangeNpcLocationEvent(((Npc)npc), newTile, zone));
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> changeEvaZone()
	{
		String zone = ((Npc)npc).getZone();

		String newZone;
		Point newTile;

		switch (zone)
		{
			case ICHIBA_CROSSROAD ->
			{
				switch (d2.roll("eva zone change"))
				{
					case 1 ->
					{
						newZone = ICHIBA_DOMAIN_NORTH;
						newTile = getRandomIchibaDomainNorthTile();
					}
					case 2 ->
					{
						newZone = ICHIBA_DOMAIN_SOUTH;
						newTile = getRandomIchibaDomainSouthTile();
					}
					default -> throw new MazeException("invalid");
				}
			}
			case ICHIBA_DOMAIN_SOUTH, ICHIBA_DOMAIN_NORTH ->
			{
				newZone = ICHIBA_CROSSROAD;
				newTile = getRandomIchibaCrossroadTile();
			}
			default ->
				throw new MazeException("Invalid zone for Eva Wingfield [" + zone + "]");
		}

		return getList(new ChangeNpcLocationEvent(((Npc)npc), newTile, newZone));
	}

	/*-------------------------------------------------------------------------*/
	private Point getRandomIchibaCrossroadTile()
	{
		Point newTile;
		newTile = new Point(Dice.d20.roll("location")+5, Dice.d20.roll("location")+5);
		return newTile;
	}

	/*-------------------------------------------------------------------------*/
	private Point getRandomIchibaDomainNorthTile()
	{
		Point newTile;
		newTile = new Point(Dice.d20.roll("location")+5, Dice.d20.roll("location")+5);
		return newTile;
	}

	/*-------------------------------------------------------------------------*/
	private Point getRandomIchibaDomainSouthTile()
	{
		Point newTile;
		newTile = new Point(Dice.d10.roll("location")+5, Dice.d20.roll("location")+15);
		return newTile;
	}

}

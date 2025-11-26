

/*
 * Copyright (c) 2011 Alan McLachlan
 *
 * This file is part of Escape From The Maze.
 *
 * Escape From The Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mclachlan.maze.stat.npc;

import java.util.*;

/**
 * Container class for defining the keyword lookups that
 * govern NPC speech.
 */
public class NpcSpeech
{
	/**
	 * All possible NPC speech rows, keyed by phrase.
	 */
	private Map<String, NpcSpeechRow> lookup = new HashMap<>();

	private List<NpcSpeechRow> dialogue = new ArrayList<>();

	// stock phrases
	private String friendlyGreeting;
	private String neutralGreeting;
	private String friendlyFarewell;
	private String neutralFarewell;
	private String partyCantAffordItem;
	private String characterInventoryFull;
	private String notInterestedInBuyingItem;
	private String cantAffordToBuyItem;
	private String npcInventoryFull;
	private String doesntWantItem;
	private String doesntKnowAbout;

	/*-------------------------------------------------------------------------*/

	public NpcSpeech()
	{
	}

	public NpcSpeech(NpcSpeech other)
	{
		this.dialogue = new ArrayList<>();
		this.lookup = new HashMap<>();

		other.getDialogue().forEach(this::addNpcSpeechRow);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Performs a keyword lookup on the given player sentence, and returns what
	 * the NPC has to say in response, via a rather brute force search.
	 * <p>
	 * The sentence is tokenised into every possible phrase of one or more words,
	 * then a lookup is done on all of them, and the highest priority response
	 * is returned.
	 *
	 * @param playerSentence
	 * 	The entire player sentence
	 *
	 * @return
	 * 	What the NPC says in response. May return null if the NPC does not
	 * 	respond.
	 */
	public String lookupPlayerSentence(String playerSentence)
	{
		// get all possible phrases from the sentence
		List<String> phrases = getPhrases(playerSentence.toLowerCase());

		NpcSpeechRow result = null;

		// find the highest priority result keyed on one of the phrases
		for (String phrase : phrases)
		{
			NpcSpeechRow found = lookupPhrase(phrase);

			if (found != null)
			{
				if (result == null || found.getPriority() < result.getPriority())
				{
					result = found;
				}
			}
		}

		if (result != null)
		{
			return result.getSpeech();
		}
		else
		{
			return null;
		}		
	}

	/*-------------------------------------------------------------------------*/
	private NpcSpeechRow lookupPhrase(String phrase)
	{
		return lookup.get(phrase);
	}

	/*-------------------------------------------------------------------------*/
	public static boolean sentenceContainsKeywords(String sentence, String... keywords)
	{
		List<String> phrases = getPhrases(sentence.toLowerCase());

		for (String k : keywords)
		{
			if (phrases.contains(k))
			{
				return true;
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private static List<String> getPhrases(String playerSentence)
	{
		List<String> result = new ArrayList<String>();

		// split on spaces or non-word characters, so that we ignore punctuation
		String[] words = playerSentence.toLowerCase().split("[\\s\\W]+");

		// first, add all individual words
		result.addAll(Arrays.asList(words));

		// then, add all possible combinations of more than one word
		for (int i=0; i<words.length-1; i++)
		{
			StringBuilder temp = new StringBuilder(words[i]);
			for (int j=i+1; j<words.length; j++)
			{
				temp.append(" ");
				temp.append(words[j]);

				result.add(temp.toString());
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void addNpcSpeechRow(NpcSpeechRow row)
	{
		for (String phrase : row.getKeywords())
		{
			lookup.put(phrase, row);
		}
		dialogue.add(row);
	}

	/*-------------------------------------------------------------------------*/
	public int getNumRows()
	{
		return dialogue.size();
	}

	/*-------------------------------------------------------------------------*/
	public Collection<NpcSpeechRow> getDialogue()
	{
		return dialogue;
	}

	public void setDialogue(List<NpcSpeechRow> dialogue)
	{
		this.dialogue = new ArrayList<>();
		this.lookup = new HashMap<>();

		dialogue.forEach(this::addNpcSpeechRow);
	}

	/*-------------------------------------------------------------------------*/

	public String getFriendlyGreeting()
	{
		return friendlyGreeting;
	}

	public void setFriendlyGreeting(String friendlyGreeting)
	{
		this.friendlyGreeting = friendlyGreeting;
	}

	public String getNeutralGreeting()
	{
		return neutralGreeting;
	}

	public void setNeutralGreeting(String neutralGreeting)
	{
		this.neutralGreeting = neutralGreeting;
	}

	public String getFriendlyFarewell()
	{
		return friendlyFarewell;
	}

	public void setFriendlyFarewell(String friendlyFarewell)
	{
		this.friendlyFarewell = friendlyFarewell;
	}

	public String getNeutralFarewell()
	{
		return neutralFarewell;
	}

	public void setNeutralFarewell(String neutralFarewell)
	{
		this.neutralFarewell = neutralFarewell;
	}

	public String getPartyCantAffordItem()
	{
		return partyCantAffordItem;
	}

	public void setPartyCantAffordItem(String partyCantAffordItem)
	{
		this.partyCantAffordItem = partyCantAffordItem;
	}

	public String getCharacterInventoryFull()
	{
		return characterInventoryFull;
	}

	public void setCharacterInventoryFull(String characterInventoryFull)
	{
		this.characterInventoryFull = characterInventoryFull;
	}

	public String getNotInterestedInBuyingItem()
	{
		return notInterestedInBuyingItem;
	}

	public void setNotInterestedInBuyingItem(String notInterestedInBuyingItem)
	{
		this.notInterestedInBuyingItem = notInterestedInBuyingItem;
	}

	public String getCantAffordToBuyItem()
	{
		return cantAffordToBuyItem;
	}

	public void setCantAffordToBuyItem(String cantAffordToBuyItem)
	{
		this.cantAffordToBuyItem = cantAffordToBuyItem;
	}

	public String getNpcInventoryFull()
	{
		return npcInventoryFull;
	}

	public void setNpcInventoryFull(String npcInventoryFull)
	{
		this.npcInventoryFull = npcInventoryFull;
	}

	public String getDoesntWantItem()
	{
		return doesntWantItem;
	}

	public void setDoesntWantItem(String doesntWantItem)
	{
		this.doesntWantItem = doesntWantItem;
	}

	public String getDoesntKnowAbout()
	{
		return doesntKnowAbout;
	}

	public void setDoesntKnowAbout(String doesntKnowAbout)
	{
		this.doesntKnowAbout = doesntKnowAbout;
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		List<String> list = getPhrases("Able was I, ere I saw Elba.");

		for (String s : list)
		{
			System.out.println(s);
		}
	}
}

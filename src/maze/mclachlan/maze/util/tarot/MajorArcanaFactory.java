package mclachlan.maze.util.tarot;

import java.util.*;

public class MajorArcanaFactory
{

	public static List<Card> createMajorArcana()
	{
		List<Card> cards = new ArrayList<>();

		// I) The Wanderer (Garret)
		cards.add(new Card(
			"The Grey Wanderer",
			1,
			"A cloaked traveler at a crossroads beneath a hanging moon.",
			Arrays.asList(
				"A new path is calling you.",
				"You are ready to begin.",
				"Take the leap that fate invites."
			),
			Arrays.asList(
				"You hesitate to act.",
				"Fear keeps you rooted in place.",
				"A chance was missed and lingers."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"You took a leap in the past.\nThat choice set your current journey in motion.",
					"A missed chance or fear held you back, and its echo still shapes you."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Now is a moment to trust your instincts and step forward despite uncertainty.",
					"You are stalled by worry.\nExamine what is truly blocking your path."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A new path will open if you embrace risk.\nMovement brings reward.",
					"If you cling to safety, stagnation will continue.\nA smaller, safer step may break the spell."
				)
			)
		));

		// II) The Shield (The White Lady)
		cards.add(new Card(
			"The Shield",
			2,
			"A warrior woman in white, haloed by the light of justice.",
			Arrays.asList(
				"You are protected by principle.",
				"Truth stands beside you.",
				"You are guided toward moral clarity."
			),
			Arrays.asList(
				"You hold too tight to a rigid view.",
				"Pride clouds your judgment.",
				"You risk becoming self-righteous."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"You were guarded by principle or a protector.\nThat strength shaped your present.",
					"A rigid stance closed doors.\nCompassion might have softened the past."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"You stand by your truth.\nHonest action will defend what matters.",
					"Watch for dogma and pride.\nFlexibility will improve your standing."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A just resolution or a protector arrives to restore balance.",
					"Overzealous enforcement or moral blindness could cause harm ahead."
				)
			)
		));

		// III) The Reaper (Nergal)
		cards.add(new Card(
			"The Reaper",
			3,
			"A blind man sowing seeds of ash across a burnt field.",
			Arrays.asList(
				"An ending makes room for the new.",
				"Transformation is underway in your life.",
				"Inevitability clears what no longer serves."
			),
			Arrays.asList(
				"You resist the necessary change.",
				"Purpose feels dead and needs revival.",
				"You cling to what should fall away."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"A conclusion in the past cleared space for what comes after.",
					"You tried to hold on. That resistance left unfinished business."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Now a necessary ending asks for acceptance so transformation can begin.",
					"You deny a change that would free you.\nAcceptance is the work at hand."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"An ending will make room for renewal.\nLet elements fall away.",
					"Clinging to what should end will trap you in a stalled cycle."
				)
			)
		));

		// IV) The Blade (Wasud)
		cards.add(new Card(
			"The Blade",
			4,
			"A warrior astride a bull, blade raised over a burning wall.",
			Arrays.asList(
				"You have courage to act.",
				"Conflict clarifies what must change.",
				"Assert your rightful boundary."
			),
			Arrays.asList(
				"You risk acting with recklessness.",
				"Fighting now may cause needless harm.",
				"Your force lacks necessary restraint."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"A bold act or confrontation in the past reshaped the terrain of your life.",
					"Rash choices caused collateral wounds that still matter."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Stand firm and speak plainly where it counts. Courage is required.",
					"Temper your force.\nAggression now will create more problems than it solves."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A decisive victory or boundary will secure a new position.",
					"Unchecked aggression risks alienation and loss. Plan your actions carefully."
				)
			)
		));

		// V) The Messenger (Aello)
		cards.add(new Card(
			"The Messenger",
			5,
			"Winged courier on a wind current high above the earth.",
			Arrays.asList(
				"A quick message will change your course.",
				"Progress arrives on swift wings.",
				"Movement and news speed your plans."
			),
			Arrays.asList(
				"Communication may become confused.",
				"Messages risk being misunderstood.",
				"Do not assume clarity without checking."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"Recent news or a swift change pushed events along for you.",
					"A misunderstanding earlier caused detours. Re-check the facts known to you."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Expect movement: messages, travel, or quick progress arrive now.",
					"Be clear in speech and writing.\nSmall errors now have outsized cost."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"Good tidings or a helpful messenger will speed your plans.",
					"Muddled signals may delay you.\nSlow down to verify sources."
				)
			)
		));

		// VI) The Rain (Beiweh)
		cards.add(new Card(
			"The Rain",
			6,
			"A radiant figure scattering blossoms and raindrops over a valley.",
			Arrays.asList(
				"Nurture will bring growth.",
				"Renewal comes with gentle care.",
				"Affection fosters new life."
			),
			Arrays.asList(
				"Emotional drought has set in.",
				"A season feels barren and dry.",
				"You have withdrawn from caring."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"Care or nurture in your past helped something grow into being.",
					"A neglect in the past left a need for tending now."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"This is a season for growth.\nRelationships, projects, or healing.",
					"If you feel dry now, seek small acts of care to restore life."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A fruitful period lies ahead if you continue to tend what matters.",
					"Without attention, promising things may fail to blossom."
				)
			)
		));

		// VII) The Weaver (Thyra)
		cards.add(new Card(
			"The Weaver",
			7,
			"A veiled crone spinning golden thread upon a loom of stars.",
			Arrays.asList(
				"You are part of a larger pattern.",
				"Destiny weaves your choices into meaning.",
				"Connections reveal the way forward."
			),
			Arrays.asList(
				"Chaos threatens when you deny fate.",
				"You refuse to see the thread that binds events.",
				"Patterns unravel when you turn away."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"A pattern in your past reveals how threads led to your present.",
					"You resisted the pattern then, creating unexpected disorder."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Recognize how your choices link with others.\nAn unseen pattern guides you.",
					"Refusing to see connections will keep you isolated from solutions."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"Events will align in a meaningful weave.\nAccept the pattern and act within it.",
					"If you fight inevitability, you may cause rupture rather than harmony."
				)
			)
		));

		// VIII) The Flame (Zerach)
		cards.add(new Card(
			"The Flame",
			8,
			"A luminous figure bearing a torch between two pillars.",
			Arrays.asList(
				"A spark of insight will enlighten you.",
				"Creativity awakens your purpose.",
				"A new idea lights your path."
			),
			Arrays.asList(
				"Pride may lead you to overreach.",
				"You risk burning out from excess.",
				"Ambition blinds you to limits."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"An insight or creative spark in the past changed how you see things.",
					"Pride in an idea led to overreach and exhaustion."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Now is a time for inspiration and clear seeing. Act on creative urges.",
					"Guard against greed and arrogance.\nTemper passion with care."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A period of bright growth and renewed vision will come if nurtured.",
					"If you ignore limits, the flame will consume what it should warm."
				)
			)
		));

		// IX) The Judge (Kalrath)
		cards.add(new Card(
			"The Judge",
			9,
			"A stone-handed justice weighing a soul against an iron chain.",
			Arrays.asList(
				"Justice will bring balance.",
				"Consequences follow your choices.",
				"Moral truth guides the outcome."
			),
			Arrays.asList(
				"Cruelty may be inflicted in error.",
				"You face the danger of unjust punishment.",
				"Severity clouds necessary mercy."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"Past actions met due reckoning\nDebts were balanced and lessons learned.",
					"An unfair judgment then has left scars that still influence you."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Now face truth plainly.\nFair decisions restore order.",
					"Be wary of harshness or blind judgment toward others or yourself."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A fair outcome or rightful recompense will arrive if you remain honorable.",
					"Injustice looms if you tolerate cruelty - act to prevent it."
				)
			)
		));

		// X) The Eye
		cards.add(new Card(
			"The Eye",
			10,
			"An all-seeing eye woven into threads of fate.",
			Arrays.asList(
				"A revelation will bring clarity.",
				"You will see what was hidden.",
				"Insight pierces the confusion."
			),
			Arrays.asList(
				"Blindness to truth blinds your choices.",
				"You may deceive yourself about motives.",
				"Hidden manipulation may be at work."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"A truth revealed earlier changed your bearings and freed you to act.",
					"You missed an obvious fact.\nDeception colored past choices."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Clarity pierces the present moment.\nSee motives and act with sight.",
					"Blind spots persist.\nSeek corroboration before deciding."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A revelation will illuminate a difficult question, guiding you forward.",
					"If you avoid truth, manipulation may steer events against you."
				)
			)
		));

		// XI) The Veil (Vireen)
		cards.add(new Card(
			"The Veil",
			11,
			"A masked woman holding a key behind her back.",
			Arrays.asList(
				"A hidden path will be revealed to you.",
				"Cunning helps you find a way forward.",
				"A secret will become your advantage."
			),
			Arrays.asList(
				"Exposure may bring betrayal.",
				"A hidden truth could harm trust.",
				"Someone may reveal what you wished private."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"A secret or clever plan in the past opened a subtle advantage.",
					"A betrayal then left trust damaged. The wound remains."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Use discretion now.\nHidden knowledge can be an asset if used wisely.",
					"Beware those who would reveal or exploit private things for gain."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A concealed route or quiet pact will reveal an unexpected opportunity.",
					"Exposure of secrets could bring loss; protect what must stay private."
				)
			)
		));

		// XII) The Storm (Morrath)
		cards.add(new Card(
			"The Storm",
			12,
			"A thunder bolt shattering chains beneath a tempest.",
			Arrays.asList(
				"Upheaval will clear stagnant ground.",
				"Chaos leads to renewal for you.",
				"A tempest will wash away the old."
			),
			Arrays.asList(
				"Destruction may follow without rebirth.",
				"Storms risk leaving only ruin behind.",
				"You face turmoil with no clear renewal."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"A rupture in the past destroyed the old so the new could be born.",
					"Chaos then left only wreckage because renewal was not possible."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Expect upheaval that clears stagnation.\nHold only to what is essential.",
					"If you provoke needless storms now, you risk losing foundations."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A cathartic change will reset your course and open fresh ground.",
					"Unchecked destruction may follow.\nGuard against reckless impulses."
				)
			)
		));

		// XIII) The Maze
		cards.add(new Card(
			"The Maze",
			13,
			"A vast labyrinth of shifting paths, an eye glimmering at its heart.",
			Arrays.asList(
				"You are tested by a complex trial.",
				"Careful mapping will reveal your way.",
				"Endurance will see you through."
			),
			Arrays.asList(
				"Hopelessness threatens to overtake you.",
				"You may become lost within your choices.",
				"Despair will blind your route out."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"You were tested by traps and choices that taught you endurance.",
					"You became lost in complexity and wasted effort in the past."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"You face a trial that asks patience and careful mapping to escape.",
					"Panic or despair will make the maze close in.\nSteady steps are needed."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A path through difficulty appears if you study the pattern of the walls.",
					"If you give up hope, you risk remaining trapped. Seek a guide, but only the right one."
				)
			)
		));

		// XIV) The Road
		cards.add(new Card(
			"The Road",
			14,
			"A lone road cutting across moor and broken stone toward a distant horizon under a hexagonal sun.",
			Arrays.asList(
				"You travel with steady purpose.",
				"Freedom comes through chosen steps.",
				"Progress accumulates with each day."
			),
			Arrays.asList(
				"You wander without clear purpose.",
				"Avoidance prolongs your journey.",
				"Detours lead you nowhere steadier."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"A previous journey taught you resilience and direction.",
					"You wandered then and lost time and resources as a result."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Keep moving with intent.\nSteady steps reach the horizon.",
					"Check your direction.\nDrifting aimlessly will not bring fulfillment."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A long, purposeful road lies ahead that leads to freedom.",
					"Without a goal, the coming travels will feel empty and long."
				)
			)
		));

		// XV) The Gate
		cards.add(new Card(
			"The Gate",
			15,
			"Twin stone doors inscribed with sigils; one ajar, one sealed.",
			Arrays.asList(
				"A threshold awaits your passage.",
				"Choosing will initiate change for you.",
				"Step through to receive initiation."
			),
			Arrays.asList(
				"You refuse to pass the threshold.",
				"Stagnation keeps you on the old side.",
				"Fear prevents your initiation."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"You crossed a threshold before that changed your standing.",
					"You refused an initiation and remained on the old side of the door."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"A choice awaits.\nStep through deliberately to gain initiation.",
					"Fear of change keeps you before the closed door. Courage is needed."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"An initiation or rite will open access to new roles and responsibilities.",
					"If you decline, you will miss growth that requires passage."
				)
			)
		));

		// XVI) The Sundering
		cards.add(new Card(
			"The Sundering",
			16,
			"A city cracking open like an egg, with light pouring from the fissure.",
			Arrays.asList(
				"Separation brings a necessary transformation.",
				"Awakening demands a cost you can meet.",
				"Splitting reveals a truer form."
			),
			Arrays.asList(
				"Fragmentation leaves you despairing.",
				"Fear keeps you from becoming whole.",
				"Despair may take root if you resist."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"A split in the past separated what once was whole and led to new clarity.",
					"A rift broke things without purpose, leaving bitter fragments."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"A painful but necessary splitting now clears the way for a truer form.",
					"If you resist integration, the rupture will become permanent and bitter."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A transformation will cost what it must but will reveal a deeper truth.",
					"Fear of change may keep you small and locked into despair."
				)
			)
		));

		// XVII) The Dragon (Primordial force)
		cards.add(new Card(
			"The Dragon",
			17,
			"A coiling drake wreathed in flame and cloud, guardian of sun and storm.",
			Arrays.asList(
				"Primal power awakens in you.",
				"Transformation by fire reshapes your role.",
				"You wield dominion with care."
			),
			Arrays.asList(
				"Pride risks destructive ends.",
				"You hoard power to your detriment.",
				"Unchecked dominion leads to ruin."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"You once harnessed a fierce power that reshaped your course.",
					"Pride or greed then caused harm and severed alliances."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Tap latent strength with respect.\nYour authority can protect and transform.",
					"Unchecked dominance will alienate allies and invite downfall."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A mighty force will aid a necessary change, if wielded wisely.",
					"If you cling to control, power will corrode into ruin."
				)
			)
		));

		// XVIII) The Mirror
		cards.add(new Card(
			"The Mirror",
			18,
			"A cloaked figure mirrored in a pool of black water.",
			Arrays.asList(
				"Confront your darkness and learn.",
				"Fear becomes a teacher when faced.",
				"Honest reflection brings healing."
			),
			Arrays.asList(
				"You deceive yourself about who you are.",
				"Shame hides truths you must meet.",
				"Repression will deepen the wound."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"A past confrontation with inner fear taught you an important lesson.",
					"You hid a truth about yourself and the cover-up created pain."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Face the reflection now; what you fear holds keys to healing.",
					"Avoiding the truth will let shame fester. Seek honest counsel."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A frank reckoning brings growth and integration in days to come.",
					"If you persist in self-deception, repeated harm will follow."
				)
			)
		));

		// XIX) The Wayfarers
		cards.add(new Card(
			"The Wayfarers",
			19,
			"Six figures walking divergent paths through the same storm.",
			Arrays.asList(
				"Companions walk with you through hardship.",
				"Shared purpose strengthens your steps.",
				"Allies support your journey."
			),
			Arrays.asList(
				"A fracture threatens your fellowship.",
				"Misunderstanding pulls the group apart.",
				"Relations need honest tending to survive."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"A shared journey or friendship in the past shaped your loyalties.",
					"A falling out then left relationships fragile and unresolved."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Lean on companions now.\nCooperation will bring greater success than lone action.",
					"Differences threaten to split a group.\nHonest conversation can mend it."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"Allies will gather to support a mutual goal.\nSolidarity brings strength.",
					"If ties fray, expect misunderstandings to widen unless someone mediates."
				)
			)
		));

		// XX) The Machine
		cards.add(new Card(
			"The Machine",
			20,
			"A brass figure with an open chest revealing a clockwork heart.",
			Arrays.asList(
				"Perseverance carries you through trials.",
				"Sacrifice yields durable reward.",
				"Endurance secures long term gains."
			),
			Arrays.asList(
				"Coldness threatens your connections.",
				"You risk losing your soul to duty.",
				"Mechanical habit drains your warmth."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"Hard work and steady effort in the past carried you through difficulty.",
					"Endurance then cost you warmth and connection."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Keep disciplined - consistent effort yields lasting results.",
					"Do not become numb to the world.\nRemember your mortality in the grind."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A long project will reward persistence and careful tending.",
					"If you sacrifice too much, you may feel hollow despite success."
				)
			)
		));

		// XXI) The Dreaming Realm (Lunara's influence)
		cards.add(new Card(
			"The Dreaming Realm",
			21,
			"A twilight field where spirits rise toward the stars while owls and wolves watch on.",
			Arrays.asList(
				"Transcendence draws you beyond the ordinary.",
				"Freedom of spirit brings new perspective.",
				"Unity with the divine offers gentle counsel."
			),
			Arrays.asList(
				"An falsehood tempts you away from truth.",
				"False ascension may hollow your gains.",
				"You risk mistaking fantasy for guidance."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"A previous encounter with wonder opened your sense of what lies beyond.",
					"An illusion in the past misled you with empty promises."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"Open to dreams and subtle guidance now.\nThe spirit world offers counsel.",
					"Beware of seductive visions that ask you to abandon ground realities."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A period of spiritual growth and unity approaches if you remain receptive.",
					"If you chase false transcendence, you may lose foothold in real life."
				)
			)
		));

		// XXII) The Return
		cards.add(new Card(
			"The Return",
			22,
			"The Wanderer on a twilight road, stepping beyond the horizon.",
			Arrays.asList(
				"Completion brings liberation.",
				"A new beginning rises from closure.",
				"You find freedom in the cycle's end."
			),
			Arrays.asList(
				"You fear the change completion requires.",
				"An endless cycle traps you in repetition.",
				"You resist the final step to move on."
			),
			Map.of(
				Card.SpreadPosition.PAST, new Card.Reading(
					"A cycle completed in your past brought closure and a sense of wholeness.",
					"You were trapped in repetition and could not find release."
				),
				Card.SpreadPosition.PRESENT, new Card.Reading(
					"You stand on the brink of completion.\nCelebrate endings that lead to freedom.",
					"Fear keeps you clinging to cycles.\nAllow a final step to move on."
				),
				Card.SpreadPosition.FUTURE, new Card.Reading(
					"A liberating new chapter begins once you accept the end of what was.",
					"If you resist change, you may be caught in an endless loop of return."
				)
			)
		));

		return cards;
	}
}

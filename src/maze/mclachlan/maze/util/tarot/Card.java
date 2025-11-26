package mclachlan.maze.util.tarot;

import java.util.*;

/**
 *
 */
public class Card
{

	public String getRandomAttributeText(Orientation pastOrientation)
	{
		List<String> attrs = (pastOrientation == Orientation.UPRIGHT)
			? getAttributes()
			: getReversedAttributes();

		if (attrs.isEmpty())
		{
			return "";
		}
		else
		{
			Random rng = new Random();
			int index = rng.nextInt(attrs.size());
			return attrs.get(index);
		}
	}

	public enum SpreadPosition
	{PAST, PRESENT, FUTURE}

	public enum Orientation
	{UPRIGHT, REVERSED}

	/**
	 * Holds upright and reversed text for a single spread position.
	 */
	public static class Reading
	{
		private final String upright;
		private final String reversed;

		public Reading(String upright, String reversed)
		{
			this.upright = upright;
			this.reversed = reversed;
		}

		public String get(Orientation orientation)
		{
			return (orientation == Orientation.UPRIGHT) ? upright : reversed;
		}
	}

	private final String name;
	private final int ordinal;
	private final String description;
	private final List<String> attributes;
	private final List<String> reversedAttributes;

	// past/present/future -> Reading (upright/reversed)
	private final EnumMap<SpreadPosition, Reading> readings;

	public Card(
		String name,
		int ordinal,
		String description,
		List<String> attributes,
		List<String> reversedAttributes,
		Map<SpreadPosition, Reading> readings
	)
	{
		this.name = Objects.requireNonNull(name);
		this.ordinal = ordinal;
		this.description = description == null ? "" : description;

		this.attributes = (attributes == null)
			? Collections.emptyList()
			: Collections.unmodifiableList(new ArrayList<>(attributes));

		this.reversedAttributes = (reversedAttributes == null)
			? Collections.emptyList()
			: Collections.unmodifiableList(new ArrayList<>(reversedAttributes));

		// Ensure all positions exist, fall back to blank readings if missing
		EnumMap<SpreadPosition, Reading> map = new EnumMap<>(SpreadPosition.class);
		for (SpreadPosition pos : SpreadPosition.values())
		{
			Reading r = (readings != null) ? readings.get(pos) : null;
			if (r == null)
			{
				r = new Reading(
					"No upright reading provided.",
					"No reversed reading provided."
				);
			}
			map.put(pos, r);
		}
		this.readings = map;
	}

	public String getName()
	{
		return name;
	}

	public int getOrdinal()
	{
		return ordinal;
	}

	public String getDescription()
	{
		return description;
	}

	public List<String> getAttributes()
	{
		return attributes;
	}

	public List<String> getReversedAttributes()
	{
		return reversedAttributes;
	}

	public String getReading(SpreadPosition position, Orientation orientation)
	{
		return readings.get(position).get(orientation);
	}

	@Override
	public String toString()
	{
		return name + " (" + ordinal + "): " + description;
	}
}

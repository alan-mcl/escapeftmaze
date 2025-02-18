package mclachlan.maze.data.v2.serialisers;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.ReflectiveSerialiser;
import mclachlan.maze.data.v2.V2SerialiserMap;
import mclachlan.maze.data.v2.V2SerialiserObject;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.magic.*;

/**
 *
 */
public class ValueListSerialiser implements V2SerialiserObject<ValueList>
{
	V2SerialiserObject<Value> valueSerialiser;

	/*-------------------------------------------------------------------------*/
	private static ReflectiveSerialiser getReflectiveSerialiser(
		Class<?> clazz, String... fields)
	{
		// some default custom serialised for the Maze db

		ReflectiveSerialiser result = new ReflectiveSerialiser(clazz, fields);
		result.addCustomSerialiser(StatModifier.class, new StatModifierSerialiser());
		result.addCustomSerialiser(Dice.class, new DiceSerialiser());
		return result;
	}


	public ValueListSerialiser()
	{
		HashMap<Class, V2SerialiserMap<Value>> map = new HashMap<>();

		map.put(Value.class, getReflectiveSerialiser(Value.class,
			"value", "scaling", "reference", "shouldNegate"));
		map.put(DiceValue.class, getReflectiveSerialiser(DiceValue.class,
			"value", "scaling", "reference", "shouldNegate", "dice"));
		map.put(ModifierValue.class, getReflectiveSerialiser(ModifierValue.class,
			"value", "scaling", "reference", "shouldNegate", "modifier"));
		map.put(MagicPresentValue.class, getReflectiveSerialiser(MagicPresentValue.class,
			"value", "scaling", "reference", "shouldNegate", "colour"));

		valueSerialiser = new MazeObjectImplSerialiser<>(map, "value", "scaling", "reference", "shouldNegate");
	}

	@Override
	public Object toObject(ValueList valueList, Database db)
	{
		ArrayList result = new ArrayList();
		valueList.getValues().forEach(v -> result.add(valueSerialiser.toObject(v, db)));
		return result;
	}

	@Override
	public ValueList fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}

		ValueList result = new ValueList();

		ArrayList list = (ArrayList)obj;
		list.forEach(v -> result.add(valueSerialiser.fromObject(v, db)));

		return result;
	}
}

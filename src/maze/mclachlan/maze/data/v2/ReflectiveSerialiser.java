/*
 * This file is part of Brewday.
 *
 * Brewday is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Brewday is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Brewday.  If not, see https://www.gnu.org/licenses.
 */

package mclachlan.maze.data.v2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import mclachlan.maze.data.Database;

/**
 *
 */
public class ReflectiveSerialiser<E> implements V2SerialiserMap<E>
{
	private final Class<E> clazz;
	private final List<String> fields;
	private final Map<Class, V2SerialiserObject> customSerialisersByType;
	private final Map<String, V2SerialiserObject> customSerialisersByField;

	/*-------------------------------------------------------------------------*/
	public ReflectiveSerialiser(Class clazz, String... fields)
	{
		this.clazz = clazz;
		this.fields = Arrays.asList(fields);
		customSerialisersByType = new HashMap<>();
		customSerialisersByField = new HashMap<>();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Map<String, Object> toObject(E e, Database db)
	{
		if (e == null)
		{
			return null;
		}

		try
		{
			// Provide for sorted keys in the JSON to help readbility
			Map<String, Object> result = new TreeMap<>((o1, o2) -> fields.indexOf(o1) - fields.indexOf(o2));

			for (String field : fields)
			{
				Method method = null;
				try
				{
					method = clazz.getMethod("get" + getMethodSuffix(field));
				}
				catch (NoSuchMethodException ex)
				{
					method = clazz.getMethod("is" + getMethodSuffix(field));
				}
				catch (SecurityException ex)
				{
					throw new RuntimeException(ex);
				}

				Object value = method.invoke(e);

				if (value == null)
				{
					result.put(field, null);
				}
				else
				{
					Class<?> valueClazz = value.getClass();
					V2SerialiserObject customSerialiser = getCustomSerialiser(field, valueClazz);

					if (customSerialiser != null)
					{
						result.put(field, customSerialiser.toObject(value, db));
					}
					else if (valueClazz.isPrimitive() ||
						Byte.class.isAssignableFrom(valueClazz) ||
						Short.class.isAssignableFrom(valueClazz) ||
						Integer.class.isAssignableFrom(valueClazz) ||
						Long.class.isAssignableFrom(valueClazz) ||
						Float.class.isAssignableFrom(valueClazz) ||
						Double.class.isAssignableFrom(valueClazz) ||
						Boolean.class.isAssignableFrom(valueClazz) ||
						Character.class.isAssignableFrom(valueClazz))
					{
						result.put(field, String.valueOf(value));
					}
					else if (Enum.class.isAssignableFrom(valueClazz))
					{
						// use name() here so that toString() can be used for the UI
						result.put(field, ((Enum<?>)value).name());
					}
					else if (Class.class.isAssignableFrom(valueClazz))
					{
						result.put(field, ((Class<?>)value).getName());
					}
					else
					{
						// yolo in the object
						result.put(field, value);
					}
				}
			}

			return result;
		}
		catch (NoSuchMethodException | IllegalAccessException |
				 InvocationTargetException ex)
		{
			throw new V2Exception(ex);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public E fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}

		try
		{
			Map<String, ?> map = (Map<String, ?>)obj;

			E result = clazz.newInstance();

			for (String field : fields)
			{
				String setMethodName = "set" + getMethodSuffix(field);
				Method[] methods = clazz.getMethods();
				Method setMethod = null;
				for (Method m : methods)
				{
					if (m.getName().equals(setMethodName) &&
						m.getParameterTypes().length == 1)
					{
						setMethod = m;
						break;
					}
				}

				if (setMethod == null)
				{
					throw new V2Exception("No set method [" + setMethodName + "] for " + clazz);
				}

				Object value = map.get(field);
				Class parameterType = setMethod.getParameterTypes()[0];

				try
				{
					if (parameterType == String.class)
					{
						setMethod.invoke(result, (String)value);
					}
					else if (parameterType == Integer.class || parameterType == int.class)
					{
						setMethod.invoke(result, Integer.valueOf((String)value));
					}
					else if (parameterType == Short.class || parameterType == short.class)
					{
						setMethod.invoke(result, Short.valueOf((String)value));
					}
					else if (parameterType == Byte.class || parameterType == byte.class)
					{
						setMethod.invoke(result, Byte.valueOf((String)value));
					}
					else if (parameterType == Long.class || parameterType == long.class)
					{
						setMethod.invoke(result, Long.valueOf((String)value));
					}
					else if (parameterType == Double.class || parameterType == double.class)
					{
						setMethod.invoke(result, Double.valueOf((String)value));
					}
					else if (parameterType == Float.class || parameterType == float.class)
					{
						setMethod.invoke(result, Float.valueOf((String)value));
					}
					else if (parameterType == Boolean.class || parameterType == boolean.class)
					{
						setMethod.invoke(result, Boolean.valueOf((String)value));
					}
					else if (parameterType == Character.class || parameterType == char.class)
					{
						setMethod.invoke(result, Character.valueOf(value.toString().charAt(0)));
					}
					else if (Enum.class.isAssignableFrom(parameterType))
					{
						if (value != null)
						{
							setMethod.invoke(result, Enum.valueOf(parameterType, (String)value));
						}
					}
					else if (Class.class.isAssignableFrom(parameterType))
					{
						if (value != null)
						{
							setMethod.invoke(result, Class.forName((String)value));
						}
					}
					else
					{
						V2SerialiserObject customSerialiser = getCustomSerialiser(field, parameterType);

						if (customSerialiser != null)
						{
							Object val = customSerialiser.fromObject(value, db);
							setMethod.invoke(result, parameterType.cast(val));
						}
						else
						{
							// honestly this probably won't work
							setMethod.invoke(result, value);
						}
					}
				}
				catch (Exception e)
				{
					throw new V2Exception("Error setting field [" + field +
						"] paramType [" + parameterType +
						"] setMethod [" + setMethod +
						"] value [" + value + "]", e);
				}
			}

			return result;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new V2Exception(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	private String getMethodSuffix(String field)
	{
		return field.substring(0, 1).toUpperCase() + field.substring(1);
	}

	/*-------------------------------------------------------------------------*/
	public void addCustomSerialiser(
		Class<?> clazz,
		V2SerialiserObject<?> serialiser)
	{
		this.customSerialisersByType.put(clazz, serialiser);
	}

	/*-------------------------------------------------------------------------*/
	public void addCustomSerialiser(
		String param,
		V2SerialiserObject<?> serialiser)
	{
		this.customSerialisersByField.put(param, serialiser);
	}

	/*-------------------------------------------------------------------------*/
	private V2SerialiserObject<?> getCustomSerialiser(String field,
		Class fieldType)
	{
		// prefer specific field custom serialisers, fall back to custom type
		V2SerialiserObject<?> customSerialiser = customSerialisersByField.get(field);
		if (customSerialiser == null)
		{
			if (customSerialisersByType.containsKey(fieldType))
			{
				customSerialiser = customSerialisersByType.get(fieldType);
			}
			else
			{
				// check for superclasses
				for (Class clazz : customSerialisersByType.keySet())
				{
					if (clazz.isAssignableFrom(fieldType))
					{
						customSerialiser = customSerialisersByType.get(clazz);
						break;
					}
				}
			}
		}
		return customSerialiser;
	}
}

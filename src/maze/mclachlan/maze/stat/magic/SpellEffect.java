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

package mclachlan.maze.stat.magic;

import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class SpellEffect extends DataObject
{
	private String name;

	/**
	 * What is displayed in the UI.
	 */
	private String displayName;

	/**
	 * The type of this spell effect.  A constant from 
	 * {@link MagicSys.SpellEffectType}. This determines which resistance
	 * modifier on the target resists this spell.
	 */ 
	private MagicSys.SpellEffectType type;

	/**
	 * The subtype of this spell effect. This may allow some actors immunity to
	 * the effects.
	 */
	private MagicSys.SpellEffectSubType subType;
	
	/**
	 * Any bonus or penalty applied to the victim's saves.
	 */ 
	private ValueList saveAdjustment;
	
	/**
	 * The result of this spell on a failed save.
	 */ 
	private SpellResult unsavedResult;
	
	/**
	 * The result of this spell on a successful save.
	 */ 
	private SpellResult savedResult;

	/**
	 * The target type for this spell effect, a constant from
	 * {@link MagicSys.SpellTargetType}.  SpellEffects attached to Spells inherit
	 * the target type of the spell, other spell effects use this field.
	 */
	private int targetType;

	/**
	 * How to apply this spell effect.
	 */
	private Application application;


	/*-------------------------------------------------------------------------*/
	/**
	 * @param name
	 * 	The name of this spell effect
	 * @param displayName
	 * 	The display name of this spell effect
	 * @param type
	 * 	A constant from {@link mclachlan.maze.stat.magic.MagicSys.SpellEffectType}
	 * @param subType
	 * 	Subtype of the spell effect.
	 * @param application
	 * 	How to apply the spell effect.
	 * @param saveModifier
	 * 	Any bonus or penalty applied to the victim's saves.
	 * @param unsavedResult
	 * 	The result of this spell on a failed save.
	 * @param savedResult
	 * 	The result of this spell on a successful save.
	 * @param targetType
	 * 	The target type for this spell effect, a constant from
	 * 	{@link mclachlan.maze.stat.magic.MagicSys.SpellTargetType}.
	 */
	public SpellEffect(
		String name,
		String displayName,
		MagicSys.SpellEffectType type,
		MagicSys.SpellEffectSubType subType,
		Application application,
		ValueList saveModifier,
		SpellResult unsavedResult,
		SpellResult savedResult,
		int targetType)
	{
		this.name = name;
		this.displayName = displayName;
		this.application = application;
		this.subType = subType;
		this.savedResult = savedResult;
		this.saveAdjustment = saveModifier;
		this.type = type;
		this.unsavedResult = unsavedResult;
		this.targetType = targetType;
		
		if (saveAdjustment == null)
		{
			saveAdjustment = new ValueList();
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	public SpellResult getSavedResult()
	{
		return savedResult;
	}

	public ValueList getSaveAdjustment()
	{
		return saveAdjustment;
	}

	/**
	 * The type of this spell effect.  A constant from {@link MagicSys.SpellEffectType}
	 */
	public MagicSys.SpellEffectType getType()
	{
		return type;
	}

	public SpellResult getUnsavedResult()
	{
		return unsavedResult;
	}

	/**
	 * The target type for this spell effect, a constant from
	 * {@link MagicSys.SpellTargetType}.  SpellEffects attached to Spells inherit
	 * the target type of the spell, other spell effects use this field.
	 */ 
	public int getTargetType()
	{
		return targetType;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public MagicSys.SpellEffectSubType getSubType()
	{
		return subType;
	}

	public void setSubType(MagicSys.SpellEffectSubType subType)
	{
		this.subType = subType;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setSaveAdjustment(ValueList saveAdjustment)
	{
		this.saveAdjustment = saveAdjustment;
	}

	public void setSavedResult(SpellResult savedResult)
	{
		this.savedResult = savedResult;
	}

	public void setTargetType(int targetType)
	{
		this.targetType = targetType;
	}

	public void setType(MagicSys.SpellEffectType type)
	{
		this.type = type;
	}

	public void setUnsavedResult(SpellResult unsavedResult)
	{
		this.unsavedResult = unsavedResult;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	/*-------------------------------------------------------------------------*/
	public Application getApplication()
	{
		return application;
	}

	/*-------------------------------------------------------------------------*/
	public void setApplication(Application application)
	{
		this.application = application;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("SpellEffect");
		sb.append("{name='").append(name).append('\'');
		sb.append(", displayName='").append(displayName).append('\'');
		sb.append(", type=").append(type);
		sb.append(", subType=").append(subType);
		sb.append(", saveAdjustment=").append(saveAdjustment);
		sb.append(", unsavedResult=").append(unsavedResult);
		sb.append(", savedResult=").append(savedResult);
		sb.append(", targetType=").append(targetType);
		sb.append(", application=").append(application);
		sb.append('}');
		return sb.toString();
	}

	/*-------------------------------------------------------------------------*/
	public boolean meetsRequirements(UnifiedActor actor)
	{
		return unsavedResult.meetsRequirements(actor);
	}

	/*-------------------------------------------------------------------------*/
	public static enum Application
	{
		AS_PER_SPELL_EFFECT,
		AS_PER_SPELL,
		APPLY_ONCE_TO_CASTER;
	}
}

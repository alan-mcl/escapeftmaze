package mclachlan.maze.editor.swing.map;

import java.util.*;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.MouseClickScript;
import mclachlan.crusader.ObjectScript;
import mclachlan.crusader.Texture;

/**
 *
 */
public class MultipleObjectProxy extends ObjectProxy
{
	List<EngineObject> objects;

	public MultipleObjectProxy(List<EngineObject> objects)
	{
		this.objects = objects;
	}

	@Override
	public String getName()
	{
		String name = objects.get(0).getName();

		for (EngineObject obj : objects)
		{
			if (name != null && obj.getName() != null && !obj.getName().equals(name))
			{
				return null;
			}
		}

		return name;
	}

	@Override
	public void setName(String name)
	{
		for (EngineObject obj : objects)
		{
			obj.setName(name);
		}
	}

	@Override
	public Texture getNorthTexture()
	{
		Texture x = objects.get(0).getNorthTexture();

		for (EngineObject obj : objects)
		{
			if (obj.getNorthTexture() != x)
			{
				return null;
			}
		}

		return x;
	}

	@Override
	public void setNorthTexture(Texture northTexture)
	{
		for (EngineObject obj : objects)
		{
			obj.setNorthTexture(northTexture);
		}
	}

	@Override
	public Texture getSouthTexture()
	{
		Texture x = objects.get(0).getSouthTexture();

		for (EngineObject obj : objects)
		{
			if (obj.getSouthTexture() != x)
			{
				return null;
			}
		}

		return x;
	}

	@Override
	public void setSouthTexture(Texture southTexture)
	{
		for (EngineObject obj : objects)
		{
			obj.setSouthTexture(southTexture);
		}
	}

	@Override
	public Texture getEastTexture()
	{
		Texture x = objects.get(0).getEastTexture();

		for (EngineObject obj : objects)
		{
			if (obj.getEastTexture() != x)
			{
				return null;
			}
		}

		return x;
	}

	@Override
	public void setEastTexture(Texture eastTexture)
	{
		for (EngineObject obj : objects)
		{
			obj.setEastTexture(eastTexture);
		}
	}

	@Override
	public Texture getWestTexture()
	{
		Texture x = objects.get(0).getWestTexture();

		for (EngineObject obj : objects)
		{
			if (obj.getWestTexture() != x)
			{
				return null;
			}
		}

		return x;
	}

	@Override
	public void setWestTexture(Texture westTexture)
	{
		for (EngineObject obj : objects)
		{
			obj.setWestTexture(westTexture);
		}
	}

	@Override
	public boolean isLightSource()
	{
		boolean x = objects.get(0).isLightSource();

		for (EngineObject obj : objects)
		{
			if (obj.isLightSource() != x)
			{
				return false;
			}
		}

		return x;
	}

	@Override
	public void setLightSource(boolean lightSource)
	{
		for (EngineObject obj : objects)
		{
			obj.setLightSource(lightSource);
		}
	}

	@Override
	public MouseClickScript getMouseClickScript()
	{
		MouseClickScript x = objects.get(0).getMouseClickScript();

		for (EngineObject obj : objects)
		{
			if (obj.getMouseClickScript() != x)
			{
				return null;
			}
		}

		return x;
	}

	@Override
	public void setMouseClickScript(MouseClickScript mouseClickScript)
	{
		for (EngineObject obj : objects)
		{
			obj.setMouseClickScript(mouseClickScript);
		}
	}

	@Override
	public EngineObject.Alignment getVerticalAlignment()
	{
		EngineObject.Alignment x = objects.get(0).getVerticalAlignment();

		for (EngineObject obj : objects)
		{
			if (obj.getVerticalAlignment() != x)
			{
				return null;
			}
		}

		return x;
	}

	@Override
	public void setVerticalAlignment(EngineObject.Alignment verticalAlignment)
	{
		for (EngineObject obj : objects)
		{
			obj.setVerticalAlignment(verticalAlignment);
		}
	}

	@Override
	public ObjectScript[] getScripts()
	{
		ObjectScript[] x = objects.get(0).getScripts();

		for (EngineObject obj : objects)
		{
			if (!Arrays.equals(obj.getScripts(), x))
			{
				return null;
			}
		}

		return x;
	}

	@Override
	public void setScripts(ObjectScript[] scripts)
	{
		for (EngineObject obj : objects)
		{
			obj.setScripts(scripts);
		}
	}

	@Override
	public int getXPos()
	{
		int x = objects.get(0).getXPos();

		for (EngineObject obj : objects)
		{
			if (obj.getXPos() != x)
			{
				return -1;
			}
		}

		return x;
	}

	@Override
	public void setXPos(int x)
	{
		for (EngineObject obj : objects)
		{
			obj.setXPos(x);
		}
	}

	@Override
	public int getYPos()
	{
		int y = objects.get(0).getYPos();

		for (EngineObject obj : objects)
		{
			if (obj.getYPos() != y)
			{
				return -1;
			}
		}

		return y;
	}

	@Override
	public void setYPos(int y)
	{
		for (EngineObject obj : objects)
		{
			obj.setYPos(y);
		}
	}
}

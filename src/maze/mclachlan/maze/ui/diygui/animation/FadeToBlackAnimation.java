package mclachlan.maze.ui.diygui.animation;

import java.awt.Graphics2D;
import mclachlan.crusader.postprocessor.FadeToBlack;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.DiyGuiUserInterface;

/**
 *
 */
public class FadeToBlackAnimation extends Animation
{
	private int duration;
	private FadeToBlack postProcessor;

	// volatile
	long startTime;

	public FadeToBlackAnimation()
	{
	}

	public FadeToBlackAnimation(int duration)
	{
		this.duration = duration;
	}

	public FadeToBlackAnimation(int duration, FadeToBlack postProcessor)
	{
		this.duration = duration;
		this.postProcessor = postProcessor;

		getUi().getRaycaster().addPostProcessor(postProcessor);

		startTime = System.currentTimeMillis();
	}

	@Override
	public void update(Graphics2D g)
	{
		double elapsedTime = System.currentTimeMillis() - startTime;

		postProcessor.setFadeAmount((elapsedTime/duration));
	}

	@Override
	public Animation spawn(AnimationContext context)
	{
		return new FadeToBlackAnimation(
			duration,
			new FadeToBlack(
				DiyGuiUserInterface.MAZE_WIDTH,
				DiyGuiUserInterface.MAZE_HEIGHT));
	}

	@Override
	public void destroy()
	{
		getUi().getRaycaster().removePostProcessor(postProcessor);
	}

	@Override
	public boolean isFinished()
	{
		return System.currentTimeMillis() - startTime >= duration;
	}

	public int getDuration()
	{
		return duration;
	}

	public void setDuration(int duration)
	{
		this.duration = duration;
	}
}

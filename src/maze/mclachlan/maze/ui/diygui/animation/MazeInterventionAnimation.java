package mclachlan.maze.ui.diygui.animation;

import java.awt.Graphics2D;
import mclachlan.crusader.postprocessor.RipplePostProcessor;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.DiyGuiUserInterface;

/**
 *
 */
public class MazeInterventionAnimation extends Animation
{
	private int duration;
	private RipplePostProcessor postProcessor;

	// volatile
	long startTime;
	double frequency, amplitude;

	public MazeInterventionAnimation()
	{
	}

	public MazeInterventionAnimation(int duration, RipplePostProcessor postProcessor)
	{
		this.duration = duration;
		this.postProcessor = postProcessor;

		getUi().getRaycaster().addPostProcessor(postProcessor);

		startTime = System.currentTimeMillis();
	}

	@Override
	public void draw(Graphics2D g)
	{
		double elapsedTime = System.currentTimeMillis() - startTime;

		postProcessor.setFrequency((elapsedTime/duration)/3);
	}

	@Override
	public Animation spawn(AnimationContext context)
	{
		return new MazeInterventionAnimation(
			1500,
			new RipplePostProcessor(
				DiyGuiUserInterface.MAZE_WIDTH,
				DiyGuiUserInterface.MAZE_HEIGHT,
				DiyGuiUserInterface.MAZE_WIDTH/2,
				DiyGuiUserInterface.MAZE_HEIGHT/2,
				1D,
				2));
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

}

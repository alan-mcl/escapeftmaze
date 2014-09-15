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

package mclachlan.maze.test;

import java.io.FileInputStream;
import javax.sound.sampled.*;

/**
 *
 */
public class TestSound implements LineListener
{
	private Clip clip;

	public static void main(String[] args)
		throws Exception
	{
		new TestSound();
	}

	/*-------------------------------------------------------------------------*/
	public TestSound() throws Exception
	{
		AudioInputStream ais = AudioSystem.getAudioInputStream(
			new FileInputStream("data/sound/dink.wav"));
		System.out.println("ais = [" + ais + "]");
		
		AudioFormat af = ais.getFormat();
		System.out.println("af = [" + af + "]");
		
		Mixer mixer = AudioSystem.getMixer(null);
		System.out.println("mixer = [" + mixer + "]");
		
		DataLine.Info info = new DataLine.Info(javax.sound.sampled.Clip.class,
			ais.getFormat(), (int)ais.getFrameLength() * af.getFrameSize());

		clip = (Clip)mixer.getLine(info);
		System.out.println("clip = [" + clip + "]");
		
		clip.addLineListener(this);
		clip.open(ais);
		
		clip.start();
		
		while (true) Thread.sleep(1000);
	}
	
	/*-------------------------------------------------------------------------*/
    public void update(LineEvent event)
    {
		 System.out.println("lineevent = [" + event + "]");
		 if(event.getType() == javax.sound.sampled.LineEvent.Type.STOP)
		 {
			 clip.stop();
			 clip.setMicrosecondPosition(0L);
			 clip.start();
//			 clip.removeLineListener(this);
		 }
	 }
}

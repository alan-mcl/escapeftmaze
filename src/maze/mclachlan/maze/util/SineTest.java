package mclachlan.maze.util;

/**
 *
 */
public class SineTest
{
	public static void main(String[] args)
	{
		long started = 0;
		double speed = 1.0;
		double speedRadiansPerMs = Math.PI*2 * speed * 1000;

		System.out.println("speedRadiansPerMs = " + speedRadiansPerMs);

		while(true)
		{
			long nanoTime = System.nanoTime();
			if (started == 0)
			{
				started = nanoTime;
			}
			double ms = (nanoTime - started) / 1_000_000_000D;

			double radians = ms * speedRadiansPerMs;
//			System.out.println("radians = " + radians);
			double sine = Math.sin(radians);
			System.out.println("sine = " + sine);
		}
	}
}

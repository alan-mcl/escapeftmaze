package mclachlan.crusader.postprocessor;

public class RipplePostProcessor implements PostProcessor
{
	private final int width, height;
	private final int centerX, centerY; // Ripple center
	private final double frequency; // Controls ripple wave frequency
	private final double amplitude; // Controls ripple strength

	public RipplePostProcessor(int width, int height, int centerX, int centerY,
		double frequency, double amplitude)
	{
		this.width = width;
		this.height = height;
		this.centerX = centerX;
		this.centerY = centerY;
		this.frequency = frequency;
		this.amplitude = amplitude;
	}

	@Override
	public void process(int[] renderBuffer, int[] outputBuffer, int screenX)
	{
		for (int y = 0; y < height; y++)
		{
			int[] distortedCoords = getRippleEffect(screenX, y);
			int srcX = distortedCoords[0];
			int srcY = distortedCoords[1];

			// Clamp coordinates to valid range
			srcX = Math.max(0, Math.min(width - 1, srcX));
			srcY = Math.max(0, Math.min(height - 1, srcY));

			outputBuffer[y * width + screenX] = renderBuffer[srcY * width + srcX];
		}
	}

	private int[] getRippleEffect(int x, int y)
	{
		double dx = x - centerX;
		double dy = y - centerY;
		double distance = Math.sqrt(dx * dx + dy * dy);

		// Ensure small distances don't cause erratic displacement
		if (distance < 1e-6)
		{
			return new int[]{x, y};
		}

		// Modify the radial distance using a sine wave
		double newDistance = distance + Math.sin(distance * frequency) * amplitude;

		// Compute corrected angle to avoid distortions
		double angle = Math.atan2(dy, dx);

		// Convert back to Cartesian coordinates using integer rounding
		int distortedX = (int)Math.round(centerX + newDistance * Math.cos(angle));
		int distortedY = (int)Math.round(centerY + newDistance * Math.sin(angle));

		return new int[]{distortedX, distortedY};
	}
}

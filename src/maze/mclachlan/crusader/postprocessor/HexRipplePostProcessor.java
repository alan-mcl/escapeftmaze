package mclachlan.crusader.postprocessor;

public class HexRipplePostProcessor implements PostProcessor
{
	private final int width, height;
	private final int centerX, centerY; // Ripple center
	private double frequency; // Controls ripple wave frequency (hexagonal pattern)
	private double amplitude; // Controls ripple strength (in pixels)
	private final double hexSize; // Approximate size of hexagonal ripple cells

	public HexRipplePostProcessor(int width, int height, int centerX,
		int centerY, double frequency, double amplitude, double hexSize)
	{
		this.width = width;
		this.height = height;
		this.centerX = centerX;
		this.centerY = centerY;

		// Clamp frequency and amplitude to reasonable ranges
		this.frequency = Math.max(0.01, Math.min(0.2, frequency)); // Prevent extreme ripple density
		this.amplitude = Math.max(1, Math.min(10, amplitude)); // Prevent excessive warping
		this.hexSize = Math.max(5, Math.min(50, hexSize)); // Prevent too small or too large hexagons
	}

	@Override
	public void process(int[] renderBuffer, int[] outputBuffer, int screenX)
	{
		for (int y = 0; y < height; y++)
		{
			int[] distortedCoords = getHexRippleEffect(screenX, y);
			int srcX = distortedCoords[0];
			int srcY = distortedCoords[1];

			// Clamp coordinates to valid range
			srcX = Math.max(0, Math.min(width - 1, srcX));
			srcY = Math.max(0, Math.min(height - 1, srcY));

			outputBuffer[y * width + screenX] = renderBuffer[srcY * width + srcX];
		}
	}

	private int[] getHexRippleEffect(int x, int y)
	{
		double dx = x - centerX;
		double dy = y - centerY;

		// Convert to hexagonal grid coordinates
		double q = (2.0 / 3.0 * dx) / hexSize;
		double r = (-1.0 / 3.0 * dx + Math.sqrt(3) / 3.0 * dy) / hexSize;

		// Compute hexagonal ripple effect using axial coordinates
		double hexDistance = Math.sqrt(q * q + r * r);
		double newDistance = hexDistance + Math.sin(hexDistance * frequency * 2 * Math.PI) * amplitude;

		// Convert back to Cartesian coordinates
		double newQ = newDistance * Math.cos(Math.PI / 3.0);
		double newR = newDistance * Math.sin(Math.PI / 3.0);

		int distortedX = centerX + (int)Math.round((3.0 / 2.0) * newQ * hexSize);
		int distortedY = centerY + (int)Math.round((Math.sqrt(3) / 2.0) * newQ * hexSize + Math.sqrt(3) * newR * hexSize);

		return new int[]{distortedX, distortedY};
	}

	public void setFrequency(double frequency)
	{
		this.frequency = frequency;
	}

	public void setAmplitude(double amplitude)
	{
		this.amplitude = amplitude;
	}
}
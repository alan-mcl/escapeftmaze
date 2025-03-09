package mclachlan.crusader.postprocessor;

/**
 *
 */
public class FadeToBlack implements PostProcessor
{
	private final int width, height;
	private volatile double fadeAmount; // Controlled externally, range [0, 1]

	public FadeToBlack(int width, int height)
	{
		this.width = width;
		this.height = height;
		this.fadeAmount = 0.0; // Start fully visible
	}

	public void setFadeAmount(double fadeAmount)
	{
		this.fadeAmount = Math.max(0.0, Math.min(1.0, fadeAmount)); // Clamp between 0 and 1
	}

	@Override
	public void process(int[] renderBuffer, int[] outputBuffer, int screenX)
	{
		for (int y = 0; y < height; y++)
		{
			int index = y * width + screenX;
			int color = renderBuffer[index];

			int a = (color >> 24) & 0xFF;
			int r = (color >> 16) & 0xFF;
			int g = (color >> 8) & 0xFF;
			int b = color & 0xFF;

			// Blend with black based on fadeAmount
			r = (int)(r * (1.0 - fadeAmount));
			g = (int)(g * (1.0 - fadeAmount));
			b = (int)(b * (1.0 - fadeAmount));

			outputBuffer[index] = (a << 24) | (r << 16) | (g << 8) | b;
		}
	}
}
package mclachlan.crusader.postprocessor;

import java.awt.Color;

/**
 * Applies a blur effect, desaturates the colors, and reduces opacity to create a ghostly appearance.
 */
public class GhostlyPostProcessor implements PostProcessor
{
	private int width;
	private int height;

	public GhostlyPostProcessor(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	public void process(int[] renderBuffer, int[] outputBuffer, int screenX)
	{
		for (int y = 1; y < height - 1; y++)
		{
			int index = y * width + screenX;
			if (index < 0 || index >= renderBuffer.length)
			{
				continue;
			}

			// Get surrounding pixels for blurring effect
			int c1 = renderBuffer[Math.max(0, index - width - 1)];
			int c2 = renderBuffer[Math.max(0, index - width)];
			int c3 = renderBuffer[Math.max(0, index - width + 1)];
			int c4 = renderBuffer[Math.max(0, index - 1)];
			int c5 = renderBuffer[index];
			int c6 = renderBuffer[Math.min(renderBuffer.length - 1, index + 1)];
			int c7 = renderBuffer[Math.min(renderBuffer.length - 1, index + width - 1)];
			int c8 = renderBuffer[Math.min(renderBuffer.length - 1, index + width)];
			int c9 = renderBuffer[Math.min(renderBuffer.length - 1, index + width + 1)];

			// Extract color components
			Color[] colors = {new Color(c1, true), new Color(c2, true), new Color(c3, true), new Color(c4, true),
				new Color(c5, true), new Color(c6, true), new Color(c7, true), new Color(c8, true), new Color(c9, true)};

			int r = 0, g = 0, b = 0, a = 0;
			for (Color color : colors)
			{
				r += color.getRed();
				g += color.getGreen();
				b += color.getBlue();
				a += color.getAlpha();
			}

			r /= 9;
			g /= 9;
			b /= 9;
			a = (int)(a / 9 * 0.6); // Reduce opacity

			int gray = (r + g + b) / 3; // Desaturate
			outputBuffer[index] = (a << 24) | (gray << 16) | (gray << 8) | gray;
		}
	}
}
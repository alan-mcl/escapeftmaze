package mclachlan.crusader;

/**
 * Renders a rough "wireframe" by only rendering the high-contrast edges in
 * grayscale.
 */
public class WireframeFilter implements PostProcessor
{
	private int width;
	private int height;

	/*-------------------------------------------------------------------------*/
	public WireframeFilter(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	estimated luminance of the given pixel
	 */
	int calcLuminance(int argb)
	{
		int red = (argb>>16) & 0xFF;
		int green = (argb>>8) & 0xFF;
		int blue =  argb & 0xFF;

		// fast approx for Y = 0.375 R + 0.5 G + 0.125 B
		return (red+red+red+green+green+green+green+blue) >> 3;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void process(int[] renderBuffer, int[] outputBuffer, int screenX)
	{
		int bufferIndex;

		for (int i = 1; i < height - 1; i++)
		{
			bufferIndex = (i * width) + screenX;

			// check the contrast
			int lumaN = calcLuminance(renderBuffer[bufferIndex - width]);
			int lumaW = calcLuminance(renderBuffer[bufferIndex -1]);
			int lumaM = calcLuminance(renderBuffer[bufferIndex]);
			int lumaE = calcLuminance(renderBuffer[bufferIndex +1]);
			int lumaS = calcLuminance(renderBuffer[bufferIndex + width]);

			int rangeMin = Math.min(lumaM, Math.min(Math.min(lumaN, lumaW), Math.min(lumaS, lumaE)));
			int rangeMax = Math.max(lumaM, Math.max(Math.max(lumaN, lumaW), Math.max(lumaS, lumaE)));

			int range = rangeMax - rangeMin;

			if (range > 50)
			{
				outputBuffer[bufferIndex] = getGreyscale(lumaM);
			}
			else
			{
				outputBuffer[bufferIndex] = 0xFF000000;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private int getGreyscale(int luma)
	{
		return 0xFF000000 | ((luma << 16) & 0xFF0000) | ((luma << 8) & 0xFF00) | (luma & 0xFF);
	}
}

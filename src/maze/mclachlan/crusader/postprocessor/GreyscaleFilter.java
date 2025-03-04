package mclachlan.crusader.postprocessor;

/**
 * Renders the scene in greyscale.
 */
public class GreyscaleFilter implements PostProcessor
{
	private final int width;
	private final int height;

	/*-------------------------------------------------------------------------*/
	public GreyscaleFilter(int width, int height)
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

		for (int i = 0; i < height; i++)
		{
			bufferIndex = (i * width) + screenX;

			int lumaM = calcLuminance(renderBuffer[bufferIndex]);

			outputBuffer[bufferIndex] = getGreyscale(lumaM);
		}
	}

	/*-------------------------------------------------------------------------*/
	private int getGreyscale(int luma)
	{
		return 0xFF000000 | ((luma << 16) & 0xFF0000) | ((luma << 8) & 0xFF00) | (luma & 0xFF);
	}
}

package mclachlan.crusader;

/**
 * See http://developer.download.nvidia.com/assets/gamedev/files/sdk/11/FXAA_WhitePaper.pdf
 *
 * This filter implements FXAA "lite" - it simply does edge detection via
 * luminance and then runs the 3x3 blur shader. Does not implement subpixel
 * antialiasing.
 */
public class FXAAFilter implements PostProcessor
{
	private static double FXAA_EDGE_THRESHOLD = 1/8D;
	private static double FXAA_EDGE_THRESHOLD_MIN = 1/16D;
	private static double FXAA_SUBPIX_TRIM = 1/4D;
	private static double FXAA_SUBPIX_TRIM_SCALE = 1D;
	private static double FXAA_SUBPIX_CAP = 3/4D;
	private final BoxFilter shader;

	private int width;
	private int height;

	/*-------------------------------------------------------------------------*/
	public FXAAFilter(int width, int height)
	{
		this.width = width;
		this.height = height;

		shader = new BoxFilter(new float[]{1, 1, 1, 1, 2, 1, 1, 1, 1}, width, height);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	estimated luminance of the given pixel
	 */
	double calcLuminance(int argb)
	{
		int red = (argb>>16) & 0xFF;
		int green = (argb>>8) & 0xFF;
		int blue =  argb & 0xFF;

		// fast approx for Y = 0.375 R + 0.5 G + 0.125 B
		return (red+red+red+green+green+green+green+blue) >> 3;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	new value for the given pixel
	 */
	boolean contrastCheck(int[] buffer, int index, int width, int height)
	{
		int rgbaN = buffer[index - width];
		int rgbaW = buffer[index-1];
		int rgbaM = buffer[index];
		int rgbaE = buffer[index+1];
		int rgbaS = buffer[index + width];

		double lumaN = calcLuminance(rgbaN);
		double lumaW = calcLuminance(rgbaW);
		double lumaM = calcLuminance(rgbaM);
		double lumaE = calcLuminance(rgbaE);
		double lumaS = calcLuminance(rgbaS);

		double rangeMin = Math.min(lumaM, Math.min(Math.min(lumaN, lumaW), Math.min(lumaS, lumaE)));
		double rangeMax = Math.max(lumaM, Math.max(Math.max(lumaN, lumaW), Math.max(lumaS, lumaE)));
		double range = rangeMax - rangeMin;

		// Should be Math.max(FXAA_EDGE_THRESHOLD_MIN, rangeMax * FXAA_EDGE_THRESHOLD))
		// but my math isn't working.
		return range > 75;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int[] process(int[] renderBuffer)
	{
		int bufferIndex;
		int[] result = new int[width * height];

		for (int i = 1; i < height - 1; i++)
		{
			for (int j = 1; j < width - 1; j++)
			{
				bufferIndex = (i * width) + j;

				if (contrastCheck(renderBuffer, bufferIndex, width, height))
				{
					result[bufferIndex] = shader.processPixel(renderBuffer, bufferIndex);
				}
				else
				{
					result[bufferIndex] = renderBuffer[bufferIndex];
				}
			}
		}

		return result;
	}
}

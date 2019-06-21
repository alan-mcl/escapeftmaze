package mclachlan.crusader;

/**
 *
 */
public class FXAAFilter implements PostProcessor
{
	static double FXAA_EDGE_THRESHOLD_MIN = 1/16D;
	static double FXAA_EDGE_THRESHOLD = 1/8D;
	static double FXAA_SUBPIX_TRIM = 1/4D;
	static double FXAA_SUBPIX_TRIM_SCALE = 1D;
	static double FXAA_SUBPIX_CAP = 3/4D;

	/**
	 * @return
	 * 	estimated luminance of the given pixel
	 */
	double fxaaLuma(int rgba)
	{
		int red = (rgba>>16) & 0xFF;
		int green = (rgba>>8) & 0xFF;

		return red * (0.587/0.299) + green;
	}

	/**
	 * @return
	 * 	new value for the given pixel
	 */
	int contrastCheck(int[] buffer, int index, int width, int height)
	{
		int rgbaN = buffer[index - width];
		int rgbaW = buffer[index-1];
		int rgbaM = buffer[index];
		int rgbaE = buffer[index+1];
		int rgbaS = buffer[index + width];

		double lumaN = fxaaLuma(rgbaN);
		double lumaW = fxaaLuma(rgbaW);
		double lumaM = fxaaLuma(rgbaM);
		double lumaE = fxaaLuma(rgbaE);
		double lumaS = fxaaLuma(rgbaS);

		double rangeMin = Math.min(lumaM, Math.min(Math.min(lumaN, lumaW), Math.min(lumaS, lumaE)));
		double rangeMax = Math.max(lumaM, Math.max(Math.max(lumaN, lumaW), Math.max(lumaS, lumaE)));
		double range = rangeMax - rangeMin;

		if(range < Math.max(FXAA_EDGE_THRESHOLD_MIN, rangeMax * FXAA_EDGE_THRESHOLD))
		{
			return fxaaFilter(rgbaM, lumaN, lumaW, lumaM, lumaE, lumaS, range);
		}
		else
		{
			return buffer[index];
		}
	}

	private int fxaaFilter(int rgbaM, double lumaN, double lumaW, double lumaM,
		double lumaE, double lumaS, double range)
	{
		double lumaL = (lumaN + lumaW + lumaE + lumaS) * 0.25;
		double rangeL = Math.abs(lumaL -lumaM);
		double blendL = Math.max(0.0, (rangeL / range) -FXAA_SUBPIX_TRIM) * FXAA_SUBPIX_TRIM_SCALE;
		blendL = Math.min(FXAA_SUBPIX_CAP, blendL);


		// todo
		return -1;
	}

	@Override
	public int[] process(int[] renderBuffer, int width, int height)
	{
		throw new RuntimeException("Unimplemented auto generated method!");
	}
}

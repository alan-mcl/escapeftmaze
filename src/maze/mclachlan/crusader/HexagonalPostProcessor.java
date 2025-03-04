package mclachlan.crusader;

/**
 *
 */
public class HexagonalPostProcessor implements PostProcessor
{
	private final int width, height;
	private final int hexSize; // Controls hexagon size
	private final float sqrt3 = (float)Math.sqrt(3);
	private final float hexWidth;
	private final float hexHeight;

	public HexagonalPostProcessor(int width, int height, int hexSize)
	{
		this.width = width;
		this.height = height;
		this.hexSize = hexSize;
		this.hexWidth = (float)(Math.sqrt(3) * hexSize); // Horizontal spacing
		this.hexHeight = (float)(1.5 * hexSize); // Vertical spacing
	}

	@Override
	public void process(int[] renderBuffer, int[] outputBuffer, int screenX)
	{
		for (int y = 0; y < height; y++)
		{
			// Map pixel (screenX, y) to its hexagon
			int[] hexCenter = getHexCenter(screenX, y);
			int centerX = hexCenter[0];
			int centerY = hexCenter[1];

			// Sample the color from the hex center
			int color = renderBuffer[centerY * width + centerX];

			// Apply sampled color to output buffer
			outputBuffer[y * width + screenX] = color;
		}
	}

/*
	private int[] getHexCenter(int x, int y)
	{
		// Calculate the row and column of the hexagon
		int row = (int)(y / (hexSize * sqrt3));
		int col = (int)(x / (1.5 * hexSize));

		// Calculate the center of the hexagon
		int centerX = (int)(col * 1.5 * hexSize + hexSize);
		int centerY = (int)(row * hexSize * sqrt3 + hexSize * sqrt3 / 2);

		return new int[]{centerX, centerY};
	}
*/

	public int[] pixelToHex(double x, double y)
	{
		double q = (Math.sqrt(3) / 3 * x - 1.0 / 3 * y) / hexSize;
		double r = (2.0 / 3 * y) / hexSize;

		return roundHex(q, r);
	}

	private int[] roundHex(double q, double r)
	{
		double s = -q - r;
		int rq = (int)Math.round(q);
		int rr = (int)Math.round(r);
		int rs = (int)Math.round(s);

		double qDiff = Math.abs(rq - q);
		double rDiff = Math.abs(rr - r);
		double sDiff = Math.abs(rs - s);

		if (qDiff > rDiff && qDiff > sDiff)
		{
			rq = -rr - rs;
		}
		else if (rDiff > sDiff)
		{
			rr = -rq - rs;
		}
		else
		{
			rs = -rq - rr;
		}

		return new int[]{rq, rr};
	}

	public int[] hexToCenter(int q, int r)
	{
		int centerX = (int)Math.round(hexSize * (Math.sqrt(3) * q + Math.sqrt(3) / 2 * r));
		int centerY = (int)Math.round(hexSize * (3.0 / 2 * r));

		return new int[]{centerX, centerY};
	}

	public int[] getHexCenter(double x, double y)
	{
		int[] hexCoords = pixelToHex(x, y);
		return hexToCenter(hexCoords[0], hexCoords[1]);
	}
}

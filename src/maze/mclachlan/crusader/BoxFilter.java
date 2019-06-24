package mclachlan.crusader;

/**
 * A 3x3 box filter, parameterisable by supplying a different kernel
 * <p>
 * Credit to http://tech-algorithm.com/articles/boxfiltering/
 */
public class BoxFilter implements PostProcessor
{
	private float[] kernel;
	private int width;
	private int height;
	private int[] indices;
	private float denominator;

	/*-------------------------------------------------------------------------*/
	public BoxFilter(float[] kernel, int width, int height)
	{
		this.kernel = kernel;
		this.width = width;
		this.height = height;

		indices = new int[]{
			-(width + 1), -width, -(width - 1),
			-1, 0, +1,
			width - 1, width, width + 1
		};

		for (int i = 0; i < kernel.length; i++)
		{
			denominator += kernel[i];
		}

		if (denominator == 0.0f)
		{
			denominator = 1.0f;
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int[] process(int[] renderBuffer)
	{
		int[] temp = new int[width * height];
		denominator = 0.0f;
		int indexOffset;

		for (int i = 1; i < height - 1; i++)
		{
			for (int j = 1; j < width - 1; j++)
			{
				indexOffset = (i * width) + j;

				int pixel = processPixel(renderBuffer, indexOffset);
				temp[indexOffset] = pixel;
			}
		}
		return temp;
	}

	/*-------------------------------------------------------------------------*/
	int processPixel(int[] renderBuffer, int indexOffset)
	{
		float blue;
		float green;
		float red;
		int rgb;
		int ired;
		int igreen;
		int iblue;

		red = green = blue = 0.0f;
		for (int k = 0; k < kernel.length; k++)
		{
			rgb = renderBuffer[indexOffset + indices[k]];
			red += ((rgb & 0xff0000) >> 16) * kernel[k];
			green += ((rgb & 0xff00) >> 8) * kernel[k];
			blue += (rgb & 0xff) * kernel[k];
		}

		ired = (int)(red / denominator);
		igreen = (int)(green / denominator);
		iblue = (int)(blue / denominator);
		if (ired > 0xff)
		{
			ired = 0xff;
		}
		else if (ired < 0)
		{
			ired = 0;
		}
		if (igreen > 0xff)
		{
			igreen = 0xff;
		}
		else if (igreen < 0)
		{
			igreen = 0;
		}
		if (iblue > 0xff)
		{
			iblue = 0xff;
		}
		else if (iblue < 0)
		{
			iblue = 0;
		}

		return 0xff000000 | ((ired << 16) & 0xff0000) | ((igreen << 8) & 0xff00) | (iblue & 0xff);
	}
}

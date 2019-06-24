package mclachlan.crusader;

/**
 * Interface for post-processing the render buffer.
 */
public interface PostProcessor
{
	int[] process(int[] renderBuffer);
}

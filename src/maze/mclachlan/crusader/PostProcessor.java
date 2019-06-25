package mclachlan.crusader;

/**
 * Interface for post-processing the render buffer.
 */
public interface PostProcessor
{
	void process(int[] renderBuffer, int[] outputBuffer, int screenX);
}

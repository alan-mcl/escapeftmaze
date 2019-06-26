package mclachlan.crusader;

/**
 * Interface for post-processing the render buffer.
 * <P>
 * Note that implementations need to be thread-safe.
 */
public interface PostProcessor
{
	/**
	 * Apply this processing operation to one column of the image.
	 *
	 * @param renderBuffer
	 * 	The rendered image to process, input to this filter. This method is
	 * 	not expected to change this buffer. It can sample any pixels in this
	 * 	buffer.
	 * @param outputBuffer
	 * 	The back-buffer in which to place processed pixels. This method is
	 * 	expected to populate one column of this output buffer with it's
	 * 	output pixels.
	 * @param screenX
	 * 	The column to process. This method is responsible for populating this
	 * 	column in the output buffer.
	 */
	void process(int[] renderBuffer, int[] outputBuffer, int screenX);
}

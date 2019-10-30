package mclachlan.dungeongen.lbaw;

/**
 * A basic rectangular room.
 *
 * @author Dominic Charley-Roy
 */
public class Room
{
	public int x;
	public int y;
	public int width;
	public int height;

	public Room(int x, int y, int width, int height)
	{
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void place(int[][] tiles)
	{
		// Fill the room with floor tiles. Offsets are necessary
		// for walls.
		for (int xP = x + 1; xP < x + width - 1; xP++)
		{
			for (int yP = y + 1; yP < y + height - 1; yP++)
			{
				tiles[xP][yP] = 1;
			}
		}
	}
}

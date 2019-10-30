package mclachlan.dungeongen.lbaw;

import java.util.*;

public class MapBuilder
{
	private static final int MAX_ROOM_WIDTH = 7;
	private static final int MAX_ROOM_HEIGHT = 7;
	private static final int MIN_ROOM_WIDTH = 3;
	private static final int MIN_ROOM_HEIGHT = 3;

	// The following ratios determine the number of rooms to place
	// based on the total possible number of rooms.
	private static final double MIN_ROOM_RATIO = 0.3;
	private static final double MAX_ROOM_RATIO = 0.7;

	private long seed;
	private Random r;

	public MapBuilder(long seed)
	{
		this.seed = seed;
		r = new Random(seed);
	}

	public int[][] buildMap(int width, int height)
	{
		int[][] tiles = new int[width][height];
		// clear the map
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				tiles[x][y] = 0;
			}
		}
		// Build the rooms and tunnels
		List<Room> rooms = placeRooms(tiles);
		placeTunnels(tiles, rooms);
		return tiles;
	}

	private List<Room> placeRooms(int[][] tiles)
	{
		int mapWidth = tiles.length, mapHeight = tiles[0].length;
		int roomsAcross = mapWidth / MAX_ROOM_WIDTH, roomsDown = mapHeight
			/ MAX_ROOM_HEIGHT;
		int maxRooms = roomsAcross * roomsDown;
		// use an array of booleans to represent which rooms have been used
		boolean[] usedRooms = new boolean[maxRooms];
		// generate the number of total rooms based on the ratios
		int totalRooms = rand((int)(maxRooms * MIN_ROOM_RATIO), (int)(maxRooms * MAX_ROOM_RATIO));
		// generate the rooms
		List<Room> rooms = new ArrayList<Room>(totalRooms);
		Room room;
		int roomCell;
		int width, height, x, y;
		for (int i = 0; i < totalRooms; i++)
		{
			// keep generating a room cell until we find an unused one
			do
			{
				roomCell = rand(0, maxRooms - 1);
			}
			while (usedRooms[roomCell]);
			usedRooms[roomCell] = true;
			// generate room width and height
			width = rand(MIN_ROOM_WIDTH, MAX_ROOM_WIDTH);
			height = rand(MIN_ROOM_HEIGHT, MAX_ROOM_HEIGHT);
			// generate x and y position based on cell x and y. we also
			// generate a random offset with the remaining space in the cell
			// in order to create less square levels.
			x = (roomCell % roomsAcross) * MAX_ROOM_WIDTH;
			x += rand(0, MAX_ROOM_WIDTH - width);
			y = (roomCell / roomsAcross) * MAX_ROOM_HEIGHT;
			y += rand(0, MAX_ROOM_HEIGHT - height);
			room = new Room(x, y, width, height);
			room.place(tiles);
			rooms.add(room);
		}
		return rooms;
	}

	private void placeTunnels(int[][] tiles, List<Room> rooms)
	{
		int deltaX, deltaY, deltaXSign, deltaYSign;
		int currentX, currentY;
		boolean movingInX;
		int carver, carveLength;
		Room currentRoom, goalRoom;
		// iterate through each room apart the last, tunnelling
		Iterator<Room> iterator = rooms.iterator();
		currentRoom = iterator.next();
		while (iterator.hasNext())
		{
			goalRoom = iterator.next();
			// calculate the starting position and distance remaining
			// based on the center of the two rooms
			currentX = currentRoom.x + (currentRoom.width / 2);
			currentY = currentRoom.y + (currentRoom.height / 2);
			deltaX = (goalRoom.x + (goalRoom.width / 2)) - currentX;
			deltaY = (goalRoom.y + (goalRoom.height / 2)) - currentY;
			// determine sign to carve in for both directions
			if (deltaX == 0)
			{
				deltaXSign = 1;
			}
			else
			{
				deltaXSign = (int)(deltaX / Math.abs(deltaX));
			}
			if (deltaY == 0)
			{
				deltaYSign = 1;
			}
			else
			{
				deltaYSign = (int)(deltaY / Math.abs(deltaY));
			}
			// iterate until we only have 1 direction left
			while (!(deltaX == 0 && deltaY == 0))
			{
				// randomly choose a direction
				movingInX = rand(0, 1) == 1;
				// if we are at 0 of current side, switch direction
				if (movingInX && deltaX == 0)
				{
					movingInX = false;
				}
				if (!movingInX && deltaY == 0)
				{
					movingInX = true;
				}
				// carve a random length
				carveLength = rand(1, (int)(Math.abs(movingInX ? deltaX : deltaY)));
				for (carver = 0; carver < carveLength; carver++)
				{
					if (movingInX)
					{
						currentX += deltaXSign * 1;
					}
					else
					{
						currentY += deltaYSign * 1;
					}
					tiles[currentX][currentY] = 1;
				}
				// update deltas
				if (movingInX)
				{
					deltaX -= deltaXSign * carveLength;
				}
				else
				{
					deltaY -= deltaYSign * carveLength;
				}
			}
			currentRoom = goalRoom;
		}
	}

	/**
	 * This function will randomly generate an integer between <i>min</i> and
	 * <i>max</i> <b>inclusively</b>. [min,max]
	 *
	 * @param min
	 *            The included lower bound.
	 * @param max
	 *            The included maximum bound.
	 * @return An integer between <i>min</i> and <i>max</i> inclusive.
	 */
	public int rand(int min, int max) {
		return min + (int) (r.nextDouble() * ((max - min) + 1));
	}

	public static void main(String[] args) throws Exception
	{
		MapBuilder mb = new MapBuilder(System.nanoTime());

		int[][] ints = mb.buildMap(32, 32);

		for (int x = 0; x < ints.length; x++)
		{
			int[] yy = ints[x];
			for (int y = 0; y < yy.length; y++)
			{
				System.out.print(yy[y]);
			}
			System.out.println();
		}
	}
}
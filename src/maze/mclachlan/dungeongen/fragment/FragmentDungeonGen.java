/*
 * Copyright (c) 2026 Alan McLachlan
 *
 * This file is part of Escape From The Maze.
 *
 * Escape From The Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mclachlan.dungeongen.fragment;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.Map;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Wall;
import mclachlan.dungeongen.*;
import mclachlan.dungeongen.noise4j.Noise4jDungeonGen;
import mclachlan.dungeongen.noise4j.map.Grid;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.MovePartyEvent;
import mclachlan.maze.map.MapGenZoneScript;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Encounter;

/**
 * Assembles a floor from authored fragment zones (rooms + corridors) welded by
 * inferred perimeter sockets. Post-gen dressing reuses the Noise4j grid contract
 * via a synthesized {@link Grid}.
 */
public final class FragmentDungeonGen implements DungeonGen
{
	public static final int WALL = Noise4jDungeonGen.WALL_THRESHOLD;
	public static final int ROOM = Noise4jDungeonGen.ROOM_THRESHOLD;
	public static final int CORRIDOR = Noise4jDungeonGen.CORRIDOR_THRESHOLD;

	public record Options(
		String layoutUsage,
		int minRooms,
		int targetRooms,
		int maxAttempts,
		int margin)
	{
		public Options
		{
			if (layoutUsage == null || layoutUsage.isEmpty())
			{
				throw new IllegalArgumentException("layoutUsage is required");
			}
			if (minRooms < 1 || targetRooms < minRooms || maxAttempts < 1 || margin < 0)
			{
				throw new IllegalArgumentException("invalid fragment gen options");
			}
		}

		public static Options defaults()
		{
			return new Options("barracks", 3, 3, 8, 1);
		}

		public static Options of(String layoutUsage)
		{
			return new Options(layoutUsage, 3, 3, 8, 1);
		}
	}

	/** Test seam: in-memory rotated clones keyed by expanded entry name. */
	static java.util.Map<String, Zone> rotatedFragmentCache = new HashMap<>();

	/** Test seam: when set, used instead of {@link Database} catalog peek. */
	static List<FragmentCatalog.Entry> testCatalogOverride;
	static java.util.Map<String, Zone> testZoneOverride;

	/** Test seam: last assembly failure reason. */
	static String lastFailureReason;

	/** Test seam: result of the most recent successful {@link #generate}. */
	static AssemblyResult lastAssemblyResult;

	private final Options options;

	public FragmentDungeonGen()
	{
		this(Options.defaults());
	}

	public FragmentDungeonGen(Options options)
	{
		this.options = options == null ? Options.defaults() : options;
	}

	/*-------------------------------------------------------------------------*/
	static void setTestCatalog(
		List<FragmentCatalog.Entry> entries,
		java.util.Map<String, Zone> zonesByName)
	{
		testCatalogOverride = entries == null ? null : List.copyOf(entries);
		testZoneOverride = zonesByName == null ? null : java.util.Map.copyOf(zonesByName);
	}

	/*-------------------------------------------------------------------------*/
	static void clearTestCatalog()
	{
		testCatalogOverride = null;
		testZoneOverride = null;
		rotatedFragmentCache.clear();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public DungeonGenResult generate(
		Zone zone,
		long seed,
		int dungeonLevel,
		MapGenZoneScript.DungeonDecorator decorator,
		DungeonGenContext context)
	{
		rotatedFragmentCache.clear();
		String layoutUsage = options.layoutUsage();
		List<FragmentCatalog.Entry> catalog =
			catalogFor(layoutUsage, dungeonLevel);

		if (catalog.isEmpty())
		{
			throw new IllegalStateException(
				"No assembly fragments for usage [" + layoutUsage
					+ "] depth=" + dungeonLevel);
		}

		clearTileScripts(zone);
		zone.setPortals(new Portal[0]);
		clearObjects(zone);

		Map baseMap = zone.getMap();
		int width = baseMap.getWidth();
		int length = baseMap.getLength();

		AssemblyResult assembly = null;
		Grid layoutGrid = null;
		List<Portal> weldedPortals = null;

		for (int attempt = 0; attempt < options.maxAttempts(); attempt++)
		{
			resetShellGeometry(zone, decorator, width, length);
			Random rng = new Random(seed ^ (attempt * 0x517cc1b727220a95L));
			AssemblyResult candidate = assemble(zone, catalog, rng, width, length, options);
			if (candidate == null)
			{
				continue;
			}

			Grid grid = buildLayoutGrid(width, length, candidate, zone);
			List<DungeonRoom> rooms = candidate.rooms();
			int startingRoomIndex = candidate.startingRoomIndex();
			Wall[] horizWalls = zone.getMap().getHorizontalWalls();
			Wall[] vertWalls = zone.getMap().getVerticalWalls();
			List<Portal> portals = new ArrayList<>();
			weldDoors(
				grid,
				zone,
				dungeonLevel,
				horizWalls,
				vertWalls,
				portals,
				decorator,
				rooms,
				startingRoomIndex,
				width,
				candidate);
			sealUnusedSockets(candidate, zone);
			zone.setPortals(portals.toArray(new Portal[0]));
			zone.getMap().init();

			if (validateConnectivity(zone, grid, candidate.spawn()))
			{
				assembly = candidate;
				layoutGrid = grid;
				weldedPortals = portals;
				lastAssemblyResult = candidate;
				break;
			}

			lastFailureReason = "connectivity spawn="
				+ candidate.spawn()
				+ " reachable="
				+ countGridReachable(grid, zone, candidate.spawn())
				+ " walkable="
				+ countGridWalkable(grid);
		}

		if (assembly == null)
		{
			throw new IllegalStateException(
				"FragmentDungeonGen failed after " + options.maxAttempts()
					+ " attempts for usage [" + layoutUsage
					+ "] seed=" + seed
					+ (lastFailureReason != null ? " (" + lastFailureReason + ")" : ""));
		}

		Grid grid = layoutGrid;
		List<DungeonRoom> rooms = assembly.rooms();
		int startingRoomIndex = assembly.startingRoomIndex();
		zone.setPortals(weldedPortals.toArray(new Portal[0]));

		Point layoutOrigin = assembly.spawn();
		StairwellPlan stairwells = StairwellPlan.empty();
		Point spawn = layoutOrigin;
		int spawnFacing = CrusaderEngine.Facing.NORTH;
		if (context != null && context.getStairwellPlanner() != null)
		{
			stairwells = context.getStairwellPlanner().planStairwells(
				zone, grid, dungeonLevel, layoutOrigin, context);
			spawn = stairwells.resolveSpawn(layoutOrigin);
			spawnFacing = stairwells.resolveSpawnFacing(CrusaderEngine.Facing.NORTH);
		}

		zone.setPlayerOrigin(spawn);

		List<MazeEvent> events = new ArrayList<>();
		events.add(new MovePartyEvent(spawn, spawnFacing));

		return new DungeonGenResult(
			events,
			stairwells,
			spawn,
			spawnFacing,
			rooms,
			grid,
			startingRoomIndex);
	}

	static AssemblyResult assembleForTest(
		Zone floor,
		List<FragmentCatalog.Entry> catalog,
		Random rng,
		int width,
		int length,
		Options options)
	{
		return assemble(floor, catalog, rng, width, length, options);
	}

	/*-------------------------------------------------------------------------*/
	private static List<FragmentCatalog.Entry> catalogFor(String usage, int depth)
	{
		List<FragmentCatalog.Entry> base;
		if (testCatalogOverride != null)
		{
			base = new ArrayList<>();
			for (FragmentCatalog.Entry entry : testCatalogOverride)
			{
				if (entry.isAssemblyFragment()
					&& usage.equals(entry.usage())
					&& depth >= entry.depthMin()
					&& depth <= entry.depthMax())
				{
					base.add(entry);
				}
			}
		}
		else
		{
			base = FragmentCatalog.eligibleForAssembly(depth, usage);
		}
		return FragmentCatalog.expandRotations(base);
	}

	/*-------------------------------------------------------------------------*/
	private static Zone loadFragment(FragmentCatalog.Entry entry)
	{
		if (rotatedFragmentCache.containsKey(entry.zoneName()))
		{
			return rotatedFragmentCache.get(entry.zoneName());
		}

		Zone source;
		if (testZoneOverride != null && testZoneOverride.containsKey(entry.sourceZoneName()))
		{
			source = testZoneOverride.get(entry.sourceZoneName());
		}
		else if (entry.quarterTurns() == 0 && testZoneOverride != null
			&& testZoneOverride.containsKey(entry.zoneName()))
		{
			source = testZoneOverride.get(entry.zoneName());
		}
		else
		{
			source = Database.getInstance().getZone(entry.sourceZoneName());
		}

		Zone loaded = FragmentRotate.rotate(
			source,
			entry.quarterTurns(),
			entry.zoneName());
		rotatedFragmentCache.put(entry.zoneName(), loaded);
		return loaded;
	}

	/*-------------------------------------------------------------------------*/
	private static AssemblyResult assemble(
		Zone floor,
		List<FragmentCatalog.Entry> catalog,
		Random rng,
		int width,
		int length,
		Options options)
	{
		int minRooms = options.minRooms();
		int targetRooms = options.targetRooms();
		int margin = options.margin();
		List<FragmentCatalog.Entry> rooms =
			FragmentCatalog.filterByKind(catalog, FragmentCatalog.Kind.ROOM);
		List<FragmentCatalog.Entry> corridors =
			FragmentCatalog.filterByKind(catalog, FragmentCatalog.Kind.CORRIDOR);
		if (rooms.isEmpty())
		{
			return null;
		}

		List<FragmentCatalog.Entry> starts = FragmentCatalog.filterStartRooms(rooms);
		FragmentCatalog.Entry startEntry = !starts.isEmpty()
			? weightedPick(starts, rng)
			: weightedPick(rooms, rng);
		if (startEntry == null)
		{
			return null;
		}

		Zone startZone = loadFragment(startEntry);
		int[] startPos = pickStartPosition(startZone, width, length, margin, rng);
		if (startPos == null)
		{
			return null;
		}
		int destX = startPos[0];
		int destY = startPos[1];

		List<Placement> placements = new ArrayList<>();
		java.util.Map<String, Integer> usedPerZone = new HashMap<>();
		Set<SocketRef> connectedSockets = new HashSet<>();
		List<SocketRef> frontier = new ArrayList<>();
		List<SocketWeld> welds = new ArrayList<>();

		Placement start = place(
			floor, startEntry, startZone, destX, destY, 0, placements, usedPerZone);
		addFrontier(frontier, start, connectedSockets);

		int roomCount = 1;
		int roomIndex = 1;
		int safety = 256;

		while (roomCount < targetRooms && !frontier.isEmpty() && safety-- > 0)
		{
			boolean progress = false;
			List<SocketRef> snapshot = new ArrayList<>(frontier);
			Collections.shuffle(snapshot, rng);

			for (SocketRef socket : snapshot)
			{
				if (connectedSockets.contains(socket))
				{
					frontier.remove(socket);
					continue;
				}

				AttachResult corridorAttach = tryAttach(
					floor,
					prioritizeBranchCorridors(corridors, roomCount, minRooms),
					socket,
					placements,
					usedPerZone,
					rng,
					width,
					length,
					margin,
					-1);
				if (corridorAttach != null)
				{
					connectWeld(socket, corridorAttach, connectedSockets, frontier, welds);
					progress = true;
					Placement corridor = corridorAttach.placement();

					for (SocketRef roomSocket : openSockets(corridor, connectedSockets))
					{
						if (roomCount >= targetRooms)
						{
							break;
						}
						AttachResult roomAttach = tryAttach(
							floor,
							rooms,
							roomSocket,
							placements,
							usedPerZone,
							rng,
							width,
							length,
							margin,
							roomIndex);
						if (roomAttach != null)
						{
							connectWeld(roomSocket, roomAttach, connectedSockets, frontier, welds);
							roomCount++;
							roomIndex++;
							addFrontier(frontier, roomAttach.placement(), connectedSockets);
							progress = true;
						}
					}

					addFrontier(frontier, corridor, connectedSockets);
					continue;
				}

				AttachResult roomAttach = tryAttach(
					floor,
					rooms,
					socket,
					placements,
					usedPerZone,
					rng,
					width,
					length,
					margin,
					roomIndex);
				if (roomAttach != null)
				{
					connectWeld(socket, roomAttach, connectedSockets, frontier, welds);
					roomCount++;
					roomIndex++;
					addFrontier(frontier, roomAttach.placement(), connectedSockets);
					progress = true;
				}
			}

			if (!progress)
			{
				break;
			}
		}

		capLeftoverSockets(
			floor,
			catalog,
			placements,
			connectedSockets,
			frontier,
			welds,
			usedPerZone,
			rng,
			width,
			length,
			margin);

		if (roomCount < minRooms)
		{
			StringBuilder names = new StringBuilder();
			for (Placement p : placements)
			{
				if (names.length() > 0)
				{
					names.append(',');
				}
				names.append(p.entry().zoneName());
			}
			lastFailureReason = "roomCount=" + roomCount + " min=" + minRooms
				+ " frontier=" + frontier.size() + " placed=[" + names + "]";
			return null;
		}

		List<DungeonRoom> dungeonRooms = new ArrayList<>();
		int startingRoomIndex = 0;
		for (int i = 0; i < placements.size(); i++)
		{
			Placement p = placements.get(i);
			if (p.kind() != FragmentCatalog.Kind.ROOM)
			{
				continue;
			}
			if (p.roomIndex() == 0)
			{
				startingRoomIndex = dungeonRooms.size();
			}
			Rectangle b = p.bounds();
			dungeonRooms.add(new DungeonRoom(b.x, b.y, b.width, b.height));
		}

		Point spawn = new Point(
			start.bounds().x + start.bounds().width / 2,
			start.bounds().y + start.bounds().height / 2);

		return new AssemblyResult(
			placements,
			connectedSockets,
			welds,
			dungeonRooms,
			startingRoomIndex,
			spawn);
	}

	/*-------------------------------------------------------------------------*/
	private static int[] pickStartPosition(
		Zone startZone,
		int width,
		int length,
		int margin,
		Random rng)
	{
		if (testCatalogOverride != null)
		{
			int destX = (width - startZone.getWidth()) / 2;
			int destY = margin + 1;
			if (fits(destX, destY, startZone, width, length, List.of(), margin))
			{
				return new int[]{destX, destY};
			}
			return null;
		}
		return pickRandomStart(startZone, width, length, margin, rng);
	}

	/*-------------------------------------------------------------------------*/
	private static int[] pickRandomStart(
		Zone startZone,
		int width,
		int length,
		int margin,
		Random rng)
	{
		List<int[]> candidates = new ArrayList<>();
		int fragW = startZone.getWidth();
		int fragL = startZone.getLength();
		for (int x = margin; x <= width - margin - fragW; x++)
		{
			for (int y = margin; y <= length - margin - fragL; y++)
			{
				if (fits(x, y, startZone, width, length, List.of(), margin))
				{
					candidates.add(new int[]{x, y});
				}
			}
		}
		if (candidates.isEmpty())
		{
			return null;
		}
		Collections.shuffle(candidates, rng);
		return candidates.get(0);
	}

	/*-------------------------------------------------------------------------*/
	private static void connectWeld(
		SocketRef parent,
		AttachResult attach,
		Set<SocketRef> connectedSockets,
		List<SocketRef> frontier,
		List<SocketWeld> welds)
	{
		connectedSockets.add(parent);
		connectedSockets.add(attach.childSocket());
		welds.add(new SocketWeld(parent, attach.childSocket()));
		frontier.remove(parent);
	}

	/*-------------------------------------------------------------------------*/
	private static void capLeftoverSockets(
		Zone floor,
		List<FragmentCatalog.Entry> catalog,
		List<Placement> placements,
		Set<SocketRef> connectedSockets,
		List<SocketRef> frontier,
		List<SocketWeld> welds,
		java.util.Map<String, Integer> usedPerZone,
		Random rng,
		int width,
		int length,
		int margin)
	{
		List<FragmentCatalog.Entry> stubs = filterStubCorridors(catalog);
		if (stubs.isEmpty())
		{
			return;
		}

		boolean progress = true;
		while (progress)
		{
			progress = false;
			List<SocketRef> snapshot = new ArrayList<>(frontier);
			Collections.shuffle(snapshot, rng);
			for (SocketRef socket : snapshot)
			{
				if (connectedSockets.contains(socket))
				{
					frontier.remove(socket);
					continue;
				}

				AttachResult cap = tryAttach(
					floor,
					stubs,
					socket,
					placements,
					usedPerZone,
					rng,
					width,
					length,
					margin,
					-1);
				if (cap != null)
				{
					connectWeld(socket, cap, connectedSockets, frontier, welds);
					addFrontier(frontier, cap.placement(), connectedSockets);
					progress = true;
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static List<FragmentCatalog.Entry> filterStubCorridors(
		List<FragmentCatalog.Entry> catalog)
	{
		List<FragmentCatalog.Entry> result = new ArrayList<>();
		for (FragmentCatalog.Entry entry : catalog)
		{
			if (entry.sourceZoneName().contains(".corr.stub"))
			{
				result.add(entry);
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static List<FragmentCatalog.Entry> prioritizeBranchCorridors(
		List<FragmentCatalog.Entry> corridors,
		int roomCount,
		int minRooms)
	{
		if (roomCount + 1 >= minRooms)
		{
			return corridors;
		}

		List<FragmentCatalog.Entry> branching = new ArrayList<>();
		List<FragmentCatalog.Entry> linear = new ArrayList<>();
		for (FragmentCatalog.Entry entry : corridors)
		{
			Zone fragment = loadFragment(entry);
			int sockets = FragmentSockets.detect(fragment).size();
			if (sockets >= 3)
			{
				branching.add(entry);
			}
			else
			{
				linear.add(entry);
			}
		}
		if (roomCount + 1 < minRooms && !branching.isEmpty())
		{
			return branching;
		}
		if (branching.isEmpty())
		{
			return corridors;
		}
		List<FragmentCatalog.Entry> merged = new ArrayList<>(branching);
		merged.addAll(linear);
		return merged;
	}

	/*-------------------------------------------------------------------------*/
	private static AttachResult tryAttach(
		Zone floor,
		List<FragmentCatalog.Entry> pool,
		SocketRef socket,
		List<Placement> placements,
		java.util.Map<String, Integer> usedPerZone,
		Random rng,
		int width,
		int length,
		int margin,
		int roomIndex)
	{
		List<FragmentCatalog.Entry> eligible = new ArrayList<>();
		for (FragmentCatalog.Entry entry : pool)
		{
			int used = usedPerZone.getOrDefault(entry.sourceZoneName(), 0);
			if (used < entry.maxPerFloor())
			{
				eligible.add(entry);
			}
		}
		if (eligible.isEmpty())
		{
			return null;
		}

		Collections.shuffle(eligible, rng);
		int opposite = socket.socket().oppositeFacing();

		for (FragmentCatalog.Entry entry : eligible)
		{
			Zone fragment = loadFragment(entry);
			List<FragmentSockets.Socket> matches =
				FragmentSockets.withFacing(
					FragmentSockets.detect(fragment), opposite);
			if (matches.isEmpty())
			{
				continue;
			}

			Collections.shuffle(matches, rng);
			for (FragmentSockets.Socket childSocket : matches)
			{
				int[] delta = FragmentSockets.delta(socket.socket().facing());
				int destX = socket.worldX() + delta[0] - childSocket.localX();
				int destY = socket.worldY() + delta[1] - childSocket.localY();
				if (!fits(destX, destY, fragment, width, length, placements, margin))
				{
					continue;
				}

				Placement placement = place(
					floor,
					entry,
					fragment,
					destX,
					destY,
					roomIndex,
					placements,
					usedPerZone);
				SocketRef childRef = new SocketRef(
					placement,
					childSocket,
					destX + childSocket.localX(),
					destY + childSocket.localY());
				return new AttachResult(placement, childRef);
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	private static Placement place(
		Zone floor,
		FragmentCatalog.Entry entry,
		Zone fragment,
		int destX,
		int destY,
		int roomIndex,
		List<Placement> placements,
		java.util.Map<String, Integer> usedPerZone)
	{
		FragmentStamp.stamp(floor, fragment, destX, destY);
		Rectangle bounds = FragmentStamp.bounds(destX, destY, fragment);
		Placement placement = new Placement(
			entry,
			bounds,
			entry.kind(),
			Math.max(roomIndex, 0));
		placements.add(placement);
		usedPerZone.put(
			entry.sourceZoneName(),
			usedPerZone.getOrDefault(entry.sourceZoneName(), 0) + 1);
		return placement;
	}

	/*-------------------------------------------------------------------------*/
	private static void addFrontier(
		List<SocketRef> frontier,
		Placement placement,
		Set<SocketRef> connected)
	{
		Zone zone = loadFragment(placement.entry());
		for (FragmentSockets.Socket socket : FragmentSockets.detect(zone))
		{
			SocketRef ref = new SocketRef(
				placement,
				socket,
				placement.bounds().x + socket.localX(),
				placement.bounds().y + socket.localY());
			if (!connected.contains(ref))
			{
				frontier.add(ref);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static List<SocketRef> openSockets(
		Placement placement,
		Set<SocketRef> connected)
	{
		List<SocketRef> result = new ArrayList<>();
		Zone zone = loadFragment(placement.entry());
		for (FragmentSockets.Socket socket : FragmentSockets.detect(zone))
		{
			SocketRef ref = new SocketRef(
				placement,
				socket,
				placement.bounds().x + socket.localX(),
				placement.bounds().y + socket.localY());
			if (!connected.contains(ref))
			{
				result.add(ref);
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static boolean fits(
		int destX,
		int destY,
		Zone fragment,
		int width,
		int length,
		List<Placement> placements,
		int margin)
	{
		if (destX < margin || destY < margin)
		{
			return false;
		}
		if (destX + fragment.getWidth() > width - margin
			|| destY + fragment.getLength() > length - margin)
		{
			return false;
		}

		Rectangle candidate = FragmentStamp.bounds(destX, destY, fragment);
		for (Placement existing : placements)
		{
			if (candidate.intersects(existing.bounds()))
			{
				return false;
			}
		}
		return true;
	}

	/*-------------------------------------------------------------------------*/
	private static void resetShellGeometry(
		Zone zone,
		MapGenZoneScript.DungeonDecorator decorator,
		int width,
		int length)
	{
		clearObjects(zone);
		zone.setPortals(new Portal[0]);
		clearTileScripts(zone);

		Map map = zone.getMap();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();
		Grid grid = new Grid(width, length);
		grid.fill(WALL / 10F);

		for (int i = 0; i < horiz.length; i++)
		{
			horiz[i] = solidWall(decorator, grid, 1, 1);
		}
		for (int i = 0; i < vert.length; i++)
		{
			vert[i] = solidWall(decorator, grid, 1, 1);
		}
		map.init();
	}

	/*-------------------------------------------------------------------------*/
	private static Wall solidWall(
		MapGenZoneScript.DungeonDecorator decorator,
		Grid grid,
		int x,
		int y)
	{
		return decorator.getRoomWall(grid, x, y);
	}

	/*-------------------------------------------------------------------------*/
	private static Grid buildLayoutGrid(
		int width,
		int length,
		AssemblyResult assembly,
		Zone floor)
	{
		Grid grid = new Grid(width, length);
		grid.fill(WALL / 10F);

		for (Placement placement : assembly.placements())
		{
			float value = placement.kind() == FragmentCatalog.Kind.ROOM
				? ROOM / 10F
				: CORRIDOR / 10F;
			Zone fragment = loadFragment(placement.entry());
			Rectangle b = placement.bounds();
			for (Point local : FragmentConnectivity.walkableFloorCells(fragment))
			{
				grid.set(b.x + local.x, b.y + local.y, value);
			}
		}
		return grid;
	}

	/*-------------------------------------------------------------------------*/
	private static void weldDoors(
		Grid grid,
		Zone zone,
		int dungeonLevel,
		Wall[] horizWalls,
		Wall[] vertWalls,
		List<Portal> portals,
		MapGenZoneScript.DungeonDecorator decorator,
		List<DungeonRoom> rooms,
		int startingRoomIndex,
		int width,
		AssemblyResult assembly)
	{
		for (SocketWeld weld : assembly.welds())
		{
			SocketRef parent = weld.parent();
			int[] delta = FragmentSockets.delta(parent.socket().facing());
			int toX = parent.worldX() + delta[0];
			int toY = parent.worldY() + delta[1];
			boolean isEncounter = weldPortal(
				grid,
				zone,
				dungeonLevel,
				horizWalls,
				vertWalls,
				portals,
				decorator,
				rooms,
				startingRoomIndex,
				width,
				parent.worldX(),
				parent.worldY(),
				parent.socket().facing(),
				toX,
				toY,
				weld.child().socket().facing());

			if (isEncounter)
			{
				int roomIndex = indexOfRoom(rooms, parent.worldX(), parent.worldY());
				if (roomIndex >= 0 && roomIndex != startingRoomIndex)
				{
					Encounter encounter = decorator.getEncounter(
						zone, parent.worldX(), parent.worldY(), dungeonLevel, roomIndex);
					if (encounter != null)
					{
						zone.getTile(new Point(parent.worldX(), parent.worldY()))
							.getScripts().add(encounter);
					}
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static boolean weldPortal(
		Grid grid,
		Zone zone,
		int dungeonLevel,
		Wall[] horizWalls,
		Wall[] vertWalls,
		List<Portal> portals,
		MapGenZoneScript.DungeonDecorator decorator,
		List<DungeonRoom> rooms,
		int startingRoomIndex,
		int width,
		int fromX,
		int fromY,
		int fromFacing,
		int toX,
		int toY,
		int toFacing)
	{
		if (!FragmentConnectivity.isOpenCell(zone, toX, toY))
		{
			return false;
		}

		List<Object> list = decorator.handlePortal(
			grid,
			new Point(fromX, fromY),
			fromFacing,
			new Point(toX, toY),
			toFacing);

		for (Object obj : list)
		{
			if (obj instanceof Wall wall)
			{
				if (fromFacing == CrusaderEngine.Facing.NORTH)
				{
					horizWalls[fromX + fromY * width] = wall;
				}
				else if (fromFacing == CrusaderEngine.Facing.SOUTH)
				{
					horizWalls[fromX + (fromY + 1) * width] = wall;
				}
				else if (fromFacing == CrusaderEngine.Facing.WEST)
				{
					vertWalls[fromX + fromY * (width + 1)] = wall;
				}
				else if (fromFacing == CrusaderEngine.Facing.EAST)
				{
					vertWalls[fromX + fromY * (width + 1) + 1] = wall;
				}
			}
			else if (obj instanceof Portal portal)
			{
				portals.add(portal);
			}
		}
		return true;
	}

	/*-------------------------------------------------------------------------*/
	private static void sealUnusedSockets(AssemblyResult assembly, Zone zone)
	{
		Map map = zone.getMap();
		int width = map.getWidth();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();

		for (Placement placement : assembly.placements())
		{
			Zone fragment = loadFragment(placement.entry());
			for (FragmentSockets.Socket socket : FragmentSockets.detect(fragment))
			{
				SocketRef ref = new SocketRef(
					placement,
					socket,
					placement.bounds().x + socket.localX(),
					placement.bounds().y + socket.localY());
				if (assembly.connectedSockets().contains(ref))
				{
					continue;
				}

				sealSocket(map, horiz, vert, width, ref.worldX(), ref.worldY(), ref.socket().facing());
			}
		}
		map.init();
	}

	/*-------------------------------------------------------------------------*/
	private static void sealSocket(
		Map map,
		Wall[] horiz,
		Wall[] vert,
		int width,
		int x,
		int y,
		int facing)
	{
		Wall solid = new Wall(
			new Texture[]{Map.NO_WALL},
			null,
			true,
			true,
			1,
			null,
			null,
			null);

		int tileIndex = y * width + x;
		switch (facing)
		{
			case CrusaderEngine.Facing.NORTH ->
				horiz[map.getNorthWall(tileIndex)] = solid;
			case CrusaderEngine.Facing.SOUTH ->
				horiz[map.getSouthWall(tileIndex)] = solid;
			case CrusaderEngine.Facing.WEST ->
				vert[map.getWestWall(tileIndex)] = solid;
			case CrusaderEngine.Facing.EAST ->
				vert[map.getEastWall(tileIndex)] = solid;
			default -> { }
		}
	}

	/*-------------------------------------------------------------------------*/
	private static boolean validateConnectivity(Zone zone, Grid grid, Point start)
	{
		if (!isGridWalkable(grid, start.x, start.y))
		{
			return false;
		}
		return countGridReachable(grid, zone, start) == countGridWalkable(grid);
	}

	/*-------------------------------------------------------------------------*/
	static int countWalkable(Zone zone)
	{
		if (lastAssemblyResult != null)
		{
			Grid grid = buildLayoutGrid(
				zone.getWidth(), zone.getLength(), lastAssemblyResult, zone);
			return countGridWalkable(grid);
		}

		int count = 0;
		for (int y = 0; y < zone.getLength(); y++)
		{
			for (int x = 0; x < zone.getWidth(); x++)
			{
				if (FragmentConnectivity.isOpenCell(zone, x, y))
				{
					count++;
				}
			}
		}
		return count;
	}

	/*-------------------------------------------------------------------------*/
	static int countReachable(Zone zone, Point start)
	{
		if (lastAssemblyResult != null)
		{
			Grid grid = buildLayoutGrid(
				zone.getWidth(), zone.getLength(), lastAssemblyResult, zone);
			return countGridReachable(grid, zone, start);
		}

		if (!FragmentConnectivity.isOpenCell(zone, start.x, start.y))
		{
			return 0;
		}

		Map map = zone.getMap();
		int width = map.getWidth();
		int length = map.getLength();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();
		boolean[][] seen = new boolean[length][width];
		ArrayDeque<Point> queue = new ArrayDeque<>();
		queue.add(start);
		seen[start.y][start.x] = true;
		int count = 1;

		java.util.Map<Point, List<Point>> portalLinks = new HashMap<>();
		if (zone.getPortals() != null)
		{
			for (Portal portal : zone.getPortals())
			{
				portalLinks.computeIfAbsent(portal.getFrom(), k -> new ArrayList<>())
					.add(portal.getTo());
				portalLinks.computeIfAbsent(portal.getTo(), k -> new ArrayList<>())
					.add(portal.getFrom());
			}
		}

		while (!queue.isEmpty())
		{
			Point cur = queue.removeFirst();
			count += enqueue(zone, null, cur, CrusaderEngine.Facing.NORTH, width, length, horiz, vert, seen, queue);
			count += enqueue(zone, null, cur, CrusaderEngine.Facing.SOUTH, width, length, horiz, vert, seen, queue);
			count += enqueue(zone, null, cur, CrusaderEngine.Facing.WEST, width, length, horiz, vert, seen, queue);
			count += enqueue(zone, null, cur, CrusaderEngine.Facing.EAST, width, length, horiz, vert, seen, queue);

			List<Point> links = portalLinks.get(cur);
			if (links != null)
			{
				for (Point next : links)
				{
					if (next.x >= 0 && next.y >= 0 && next.x < width && next.y < length
						&& !seen[next.y][next.x]
						&& FragmentConnectivity.isOpenCell(zone, next.x, next.y))
					{
						seen[next.y][next.x] = true;
						queue.add(next);
						count++;
					}
				}
			}
		}
		return count;
	}

	/*-------------------------------------------------------------------------*/
	private static int countGridWalkable(Grid grid)
	{
		int count = 0;
		for (int y = 0; y < grid.getHeight(); y++)
		{
			for (int x = 0; x < grid.getWidth(); x++)
			{
				if (isGridWalkable(grid, x, y))
				{
					count++;
				}
			}
		}
		return count;
	}

	/*-------------------------------------------------------------------------*/
	private static int countGridReachable(Grid grid, Zone zone, Point start)
	{
		if (!isGridWalkable(grid, start.x, start.y))
		{
			return 0;
		}

		Map map = zone.getMap();
		int width = map.getWidth();
		int length = map.getLength();
		Wall[] horiz = map.getHorizontalWalls();
		Wall[] vert = map.getVerticalWalls();
		boolean[][] seen = new boolean[length][width];
		ArrayDeque<Point> queue = new ArrayDeque<>();
		queue.add(start);
		seen[start.y][start.x] = true;
		int count = 1;

		java.util.Map<Point, List<Point>> portalLinks = new HashMap<>();
		if (zone.getPortals() != null)
		{
			for (Portal portal : zone.getPortals())
			{
				portalLinks.computeIfAbsent(portal.getFrom(), k -> new ArrayList<>())
					.add(portal.getTo());
				portalLinks.computeIfAbsent(portal.getTo(), k -> new ArrayList<>())
					.add(portal.getFrom());
			}
		}

		while (!queue.isEmpty())
		{
			Point cur = queue.removeFirst();
			count += enqueue(zone, grid, cur, CrusaderEngine.Facing.NORTH, width, length, horiz, vert, seen, queue);
			count += enqueue(zone, grid, cur, CrusaderEngine.Facing.SOUTH, width, length, horiz, vert, seen, queue);
			count += enqueue(zone, grid, cur, CrusaderEngine.Facing.WEST, width, length, horiz, vert, seen, queue);
			count += enqueue(zone, grid, cur, CrusaderEngine.Facing.EAST, width, length, horiz, vert, seen, queue);

			List<Point> links = portalLinks.get(cur);
			if (links != null)
			{
				for (Point next : links)
				{
					if (next.x >= 0 && next.y >= 0 && next.x < width && next.y < length
						&& !seen[next.y][next.x]
						&& isGridWalkable(grid, next.x, next.y))
					{
						seen[next.y][next.x] = true;
						queue.add(next);
						count++;
					}
				}
			}
		}
		return count;
	}

	/*-------------------------------------------------------------------------*/
	private static boolean isGridWalkable(Grid grid, int x, int y)
	{
		int value = gridValue(grid, x, y);
		return value == ROOM || value == CORRIDOR;
	}

	/*-------------------------------------------------------------------------*/
	private static int enqueue(
		Zone zone,
		Grid grid,
		Point cur,
		int facing,
		int width,
		int length,
		Wall[] horiz,
		Wall[] vert,
		boolean[][] seen,
		ArrayDeque<Point> queue)
	{
		int nx = cur.x;
		int ny = cur.y;
		Wall wall;
		switch (facing)
		{
			case CrusaderEngine.Facing.NORTH ->
			{
				wall = horiz[cur.x + cur.y * width];
				ny = cur.y - 1;
			}
			case CrusaderEngine.Facing.SOUTH ->
			{
				wall = horiz[cur.x + (cur.y + 1) * width];
				ny = cur.y + 1;
			}
			case CrusaderEngine.Facing.WEST ->
			{
				wall = vert[cur.x + cur.y * (width + 1)];
				nx = cur.x - 1;
			}
			case CrusaderEngine.Facing.EAST ->
			{
				wall = vert[cur.x + cur.y * (width + 1) + 1];
				nx = cur.x + 1;
			}
			default -> throw new IllegalStateException("facing " + facing);
		}

		if (nx < 0 || ny < 0 || nx >= width || ny >= length)
		{
			return 0;
		}
		if (wall != null && wall.isSolid())
		{
			return 0;
		}
		if (grid != null)
		{
			if (!isGridWalkable(grid, nx, ny))
			{
				return 0;
			}
		}
		else if (!FragmentConnectivity.isOpenCell(zone, nx, ny))
		{
			return 0;
		}
		if (seen[ny][nx])
		{
			return 0;
		}
		seen[ny][nx] = true;
		queue.add(new Point(nx, ny));
		return 1;
	}

	/*-------------------------------------------------------------------------*/
	private static int gridValue(Grid grid, int x, int y)
	{
		return (int)(grid.get(x, y) * 10);
	}

	/*-------------------------------------------------------------------------*/
	private static int indexOfRoom(List<DungeonRoom> rooms, int x, int y)
	{
		for (int i = 0; i < rooms.size(); i++)
		{
			if (rooms.get(i).contains(x, y))
			{
				return i;
			}
		}
		return -1;
	}

	/*-------------------------------------------------------------------------*/
	private static FragmentCatalog.Entry weightedPick(
		List<FragmentCatalog.Entry> entries,
		Random rng)
	{
		int total = 0;
		for (FragmentCatalog.Entry e : entries)
		{
			total += e.weight();
		}
		if (total <= 0)
		{
			return entries.isEmpty() ? null : entries.get(0);
		}
		int roll = rng.nextInt(total);
		for (FragmentCatalog.Entry e : entries)
		{
			roll -= e.weight();
			if (roll < 0)
			{
				return e;
			}
		}
		return entries.get(entries.size() - 1);
	}

	/*-------------------------------------------------------------------------*/
	private static void clearTileScripts(Zone zone)
	{
		mclachlan.maze.map.Tile[][] tiles = zone.getTiles();
		if (tiles == null)
		{
			return;
		}
		for (int x = 0; x < tiles.length; x++)
		{
			if (tiles[x] == null)
			{
				continue;
			}
			for (int y = 0; y < tiles[x].length; y++)
			{
				mclachlan.maze.map.Tile tile = tiles[x][y];
				if (tile != null && tile.getScripts() != null)
				{
					tile.getScripts().clear();
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void clearObjects(Zone zone)
	{
		Map map = zone.getMap();
		if (map.getExpandedObjects() != null)
		{
			map.getExpandedObjects().clear();
		}
		map.init();
	}

	/*-------------------------------------------------------------------------*/
	public static record Placement(
		FragmentCatalog.Entry entry,
		Rectangle bounds,
		FragmentCatalog.Kind kind,
		int roomIndex)
	{
	}

	/*-------------------------------------------------------------------------*/
	public static record SocketRef(
		Placement placement,
		FragmentSockets.Socket socket,
		int worldX,
		int worldY)
	{
	}

	/*-------------------------------------------------------------------------*/
	public static record SocketWeld(SocketRef parent, SocketRef child)
	{
	}

	/*-------------------------------------------------------------------------*/
	private static record AttachResult(Placement placement, SocketRef childSocket)
	{
	}

	/*-------------------------------------------------------------------------*/
	public static record AssemblyResult(
		List<Placement> placements,
		Set<SocketRef> connectedSockets,
		List<SocketWeld> welds,
		List<DungeonRoom> rooms,
		int startingRoomIndex,
		Point spawn)
	{
	}
}

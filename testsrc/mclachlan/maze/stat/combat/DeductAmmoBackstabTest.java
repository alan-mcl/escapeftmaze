/*
 * Copyright (c) 2011 Alan McLachlan
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

package mclachlan.maze.stat.combat;

import java.util.*;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.combat.event.StrikeEvent;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.StubActor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression: backstab/snipe wraps the weapon in {@link BackstabSnipeAttack},
 * which still reports SELF ammo. {@code UnifiedActor.deductAmmo} must unwrap
 * to the underlying {@link Item} rather than casting the wrapper.
 */
public class DeductAmmoBackstabTest extends MazeTestSupport
{
	@Test
	void deductAmmoUnwrapsBackstabSnipeAttackForSelfAmmo()
	{
		ItemTemplate template = new ItemTemplate();
		template.setName("dart");
		template.setMaxItemsPerStack(20);
		template.setAmmo(List.of(ItemTemplate.AmmoType.SELF));
		template.setBackstabCapable(true);
		Item dart = template.create(5);

		StubActor attacker = new StubActor("rogue");
		BackstabSnipeAttack attack =
			new BackstabSnipeAttack(dart, attacker);

		StrikeEvent strike = new StrikeEvent(
			null,
			attacker,
			attacker,
			attack,
			AttackType.NULL_ATTACK_TYPE,
			MagicSys.SpellEffectType.NONE,
			null,
			null,
			null);

		Item deducted = attacker.deductAmmo(strike);

		assertSame(dart, deducted);
		assertEquals(4, dart.getStack().getCurrent());
	}
}

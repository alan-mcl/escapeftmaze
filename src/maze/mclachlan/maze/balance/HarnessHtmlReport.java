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

package mclachlan.maze.balance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;

/**
 * Writes a self-contained HTML report for a {@link HarnessRunReport}.
 * No external CSS, JS, or fonts.
 */
public final class HarnessHtmlReport
{
	private HarnessHtmlReport()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static Path write(Path path, HarnessRunReport report) throws IOException
	{
		if (path.getParent() != null)
		{
			Files.createDirectories(path.getParent());
		}
		Files.writeString(path, render(report), StandardCharsets.UTF_8);
		return path;
	}

	/*-------------------------------------------------------------------------*/
	public static String render(HarnessRunReport report)
	{
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
		html.append("<meta charset=\"utf-8\">\n");
		html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
		html.append("<title>").append(esc(report.title)).append("</title>\n");
		html.append("<style>\n").append(css()).append("</style>\n");
		html.append("</head>\n<body>\n");
		html.append("<header>\n<h1>").append(esc(report.title)).append("</h1>\n");
		html.append("<p class=\"meta\">Generated ").append(esc(Instant.now().toString()));
		html.append(" · seed <code>").append(report.seed).append("</code>");
		if (report.logPath != null && !report.logPath.isEmpty())
		{
			html.append(" · log <code>").append(esc(report.logPath)).append("</code>");
		}
		html.append("</p>\n");
		html.append("</header>\n");

		html.append("<section class=\"cards\">\n");
		card(html, "Outcome", report.outcome, outcomeClass(report.outcome));
		card(html, "Max depth", Integer.toString(report.maxDepthReached), null);
		card(html, "Turns", Long.toString(report.turnsElapsed()), null);
		card(html, "Combats", Integer.toString(report.totalCombats), null);
		card(html, "Avg TTK (rounds)", fmt(report.averageRoundsPerCombat()), null);
		card(html, "HP lost", Integer.toString(report.totalPartyHpLost), null);
		card(html, "XP gained", Integer.toString(report.totalXpGained), null);
		card(html, "Loot (base gp)", Integer.toString(report.totalLootBaseCost), null);
		card(html, "Levels gained", Integer.toString(report.levelsGained), null);
		card(html, "Gold", Integer.toString(report.gold), null);
		card(html, "Party", report.partyAlive + " / " + report.partySize + " alive", null);
		html.append("</section>\n");

		html.append("<section>\n<h2>Party</h2>\n");
		html.append("<table>\n<thead><tr>");
		th(html, "Name");
		th(html, "Class");
		th(html, "Race");
		th(html, "Gender");
		th(html, "Lvl");
		th(html, "HP");
		th(html, "XP");
		th(html, "Status");
		html.append("</tr></thead>\n<tbody>\n");
		for (HarnessRunReport.PartyMember m : report.party)
		{
			html.append("<tr class=\"").append(m.alive ? "alive" : "dead").append("\">");
			td(html, m.name);
			td(html, m.characterClass);
			td(html, m.race);
			td(html, m.gender);
			td(html, Integer.toString(m.level));
			html.append("<td>");
			hpBar(html, m.hpCurrent, m.hpMax);
			html.append("</td>");
			td(html, Integer.toString(m.experience));
			td(html, m.alive ? "Alive" : "Dead");
			html.append("</tr>\n");
		}
		html.append("</tbody></table>\n</section>\n");

		html.append("<section>\n<h2>Floors</h2>\n");
		html.append("<table>\n<thead><tr>");
		th(html, "Depth");
		th(html, "Steps");
		th(html, "Combats");
		th(html, "Rounds");
		th(html, "Avg TTK");
		th(html, "HP lost");
		th(html, "XP");
		th(html, "Loot gp");
		html.append("</tr></thead>\n<tbody>\n");
		int maxHp = 1;
		int maxRounds = 1;
		for (HarnessRunReport.Floor f : report.floors)
		{
			maxHp = Math.max(maxHp, f.partyHpLost);
			maxRounds = Math.max(maxRounds, f.combatRounds);
		}
		for (HarnessRunReport.Floor f : report.floors)
		{
			double ttk = f.combats > 0 ? (double)f.combatRounds / f.combats : 0;
			html.append("<tr>");
			td(html, Integer.toString(f.depth));
			td(html, Integer.toString(f.tileSteps));
			td(html, Integer.toString(f.combats));
			html.append("<td>");
			spark(html, f.combatRounds, maxRounds, "rounds");
			html.append(" ").append(f.combatRounds).append("</td>");
			td(html, fmt(ttk));
			html.append("<td>");
			spark(html, f.partyHpLost, maxHp, "hp");
			html.append(" ").append(f.partyHpLost).append("</td>");
			td(html, Integer.toString(f.xpGained));
			td(html, Integer.toString(f.lootBaseCost));
			html.append("</tr>\n");
		}
		html.append("</tbody></table>\n</section>\n");

		html.append("<script>\n").append(js()).append("</script>\n");
		html.append("</body>\n</html>\n");
		return html.toString();
	}

	/*-------------------------------------------------------------------------*/
	private static void card(StringBuilder html, String label, String value, String extraClass)
	{
		html.append("<article class=\"card");
		if (extraClass != null)
		{
			html.append(" ").append(extraClass);
		}
		html.append("\"><h3>").append(esc(label)).append("</h3><p>");
		html.append(esc(value)).append("</p></article>\n");
	}

	private static void th(StringBuilder html, String text)
	{
		html.append("<th>").append(esc(text)).append("</th>");
	}

	private static void td(StringBuilder html, String text)
	{
		html.append("<td>").append(esc(text)).append("</td>");
	}

	private static void hpBar(StringBuilder html, int current, int max)
	{
		int pct = max <= 0 ? 0 : Math.max(0, Math.min(100, (int)Math.round(100.0 * current / max)));
		String cls = pct <= 0 ? "empty" : pct < 35 ? "low" : "ok";
		html.append("<div class=\"bar\" title=\"").append(current).append("/").append(max).append("\">");
		html.append("<span class=\"").append(cls).append("\" style=\"width:").append(pct).append("%\"></span>");
		html.append("<em>").append(current).append("/").append(max).append("</em></div>");
	}

	private static void spark(StringBuilder html, int value, int max, String kind)
	{
		int pct = max <= 0 ? 0 : Math.max(0, Math.min(100, (int)Math.round(100.0 * value / max)));
		html.append("<span class=\"spark ").append(kind).append("\" style=\"width:")
			.append(Math.max(4, pct / 2)).append("px\"></span>");
	}

	private static String outcomeClass(String outcome)
	{
		if (outcome == null)
		{
			return null;
		}
		String lower = outcome.toLowerCase(Locale.ROOT);
		if (lower.contains("wipe"))
		{
			return "wipe";
		}
		if (lower.contains("clear"))
		{
			return "clear";
		}
		return "incomplete";
	}

	private static String fmt(double n)
	{
		return String.format(Locale.ROOT, "%.1f", n);
	}

	static String esc(String s)
	{
		if (s == null)
		{
			return "";
		}
		return s.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;");
	}

	private static String css()
	{
		return """
			:root { --bg:#16130f; --panel:#221c16; --ink:#f3e6c8; --muted:#b7a88a; --line:#3b3228; --accent:#c9a227; --ok:#3d8b5a; --bad:#a33b3b; --warn:#b36b2a; }
			* { box-sizing:border-box; }
			body { margin:0; font:15px/1.45 Georgia, "Times New Roman", serif; background:var(--bg); color:var(--ink); }
			header, section { max-width:1100px; margin:0 auto; padding:1.25rem 1.5rem; }
			h1 { margin:0 0 .25rem; font-size:1.8rem; letter-spacing:.02em; }
			h2 { margin:0 0 .75rem; font-size:1.2rem; color:var(--accent); }
			.meta { margin:0; color:var(--muted); }
			code { font-family:ui-monospace, Consolas, monospace; }
			.cards { display:grid; grid-template-columns:repeat(auto-fill,minmax(140px,1fr)); gap:.75rem; }
			.card { background:var(--panel); border:1px solid var(--line); padding:.75rem; }
			.card h3 { margin:0; font-size:.75rem; text-transform:uppercase; letter-spacing:.08em; color:var(--muted); }
			.card p { margin:.35rem 0 0; font-size:1.25rem; }
			.card.clear { border-color:var(--ok); }
			.card.wipe { border-color:var(--bad); }
			.card.incomplete { border-color:var(--warn); }
			table { width:100%; border-collapse:collapse; background:var(--panel); }
			th, td { text-align:left; padding:.45rem .55rem; border-bottom:1px solid var(--line); }
			th { font-size:.75rem; text-transform:uppercase; letter-spacing:.06em; color:var(--muted); cursor:pointer; }
			tr.dead td { color:#c98989; }
			.bar { position:relative; min-width:90px; height:1.1rem; background:#2a241c; }
			.bar span { display:block; height:100%; background:var(--ok); }
			.bar span.low { background:var(--warn); }
			.bar span.empty { background:var(--bad); width:100% !important; opacity:.35; }
			.bar em { position:absolute; inset:0; font-style:normal; font-size:.75rem; text-align:center; line-height:1.1rem; }
			.spark { display:inline-block; height:.65rem; vertical-align:middle; background:var(--accent); }
			.spark.hp { background:var(--bad); }
			""";
	}

	private static String js()
	{
		return """
			document.querySelectorAll('table').forEach(function(table){
			  var headers = table.querySelectorAll('th');
			  headers.forEach(function(th, col){
			    th.addEventListener('click', function(){
			      var tbody = table.tBodies[0];
			      if (!tbody) return;
			      var rows = Array.from(tbody.rows);
			      var dir = th.dataset.dir === 'asc' ? 'desc' : 'asc';
			      headers.forEach(function(h){ h.dataset.dir = ''; });
			      th.dataset.dir = dir;
			      rows.sort(function(a,b){
			        var av = a.cells[col] ? a.cells[col].innerText.trim() : '';
			        var bv = b.cells[col] ? b.cells[col].innerText.trim() : '';
			        var an = parseFloat(av), bn = parseFloat(bv);
			        var cmp = (!isNaN(an) && !isNaN(bn)) ? an-bn : av.localeCompare(bv);
			        return dir === 'asc' ? cmp : -cmp;
			      });
			      rows.forEach(function(r){ tbody.appendChild(r); });
			    });
			  });
			});
			""";
	}
}

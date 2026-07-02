#!/usr/bin/env bash
# Compare HandleKeyEvent partial vs full resolve timings and related incTurn costs.
# Usage: scripts/analyze-perf-log.sh [path/to/perf_log.txt]

set -uo pipefail

LOG="${1:-}"
if [[ -z "$LOG" ]]; then
	LOG="$(find log -name 'perf_log.txt' 2>/dev/null | sort | tail -1)"
fi

if [[ -z "$LOG" || ! -f "$LOG" ]]; then
	echo "No perf_log.txt found. Pass a path or run the game first." >&2
	exit 1
fi

echo "Analyzing: $LOG"
echo

stats() {
	local pattern="$1"
	local label="$2"
	local values
	values=$(rg -o "$pattern" "$LOG" 2>/dev/null | sed 's/.*: //; s/ms$//' | sort -n || true)
	local count min max avg
	count=$(echo "$values" | wc -l | tr -d ' ')
	if [[ "$count" -eq 0 || -z "$values" ]]; then
		echo "$label: (no samples)"
		return
	fi
	min=$(echo "$values" | head -1)
	max=$(echo "$values" | tail -1)
	avg=$(echo "$values" | awk '{s+=$1} END {printf "%.1f", s/NR}')
	echo "$label: count=$count min=${min}ms max=${max}ms avg=${avg}ms"
}

stats 'HandleKeyEvent::movePlayer: [0-9]+ms' 'HandleKeyEvent::movePlayer (partial)'
stats 'Maze::resolveEvent \[mclachlan\.maze\.ui\.diygui\.HandleKeyEvent\]: [0-9]+ms' 'Maze::resolveEvent [HandleKeyEvent] (full input lockout)'
stats 'HandleKeyEvent::appendToResolve: [0-9]+ms' 'HandleKeyEvent::appendToResolve (queue wait)'
stats 'HandleKeyEvent::droppedKey: [0-9]+ms' 'HandleKeyEvent::droppedKey (gated keys)'
stats 'RenderThread::avgDraw: [0-9]+ms' 'RenderThread::avgDraw'
stats 'RenderThread::fps: [0-9]+ms' 'RenderThread::fps'
stats 'Maze::eventQueueDepth: [0-9]+ms' 'Maze::eventQueueDepth'
stats 'ConditionManager\.endOfTurn: [0-9]+ms' 'ConditionManager.endOfTurn'
stats 'NpcManager\.endOfTurn: [0-9]+ms' 'NpcManager.endOfTurn'
stats 'GameTime::refreshCharacterData: [0-9]+ms' 'GameTime::refreshCharacterData'

echo
echo "Interpretation:"
echo "  - If movePlayer << resolveEvent [HandleKeyEvent], child incTurn events dominate input delay."
echo "  - If avgDraw is high while resolveEvent is low, render thread adds visual latency."
echo "  - droppedKey > 0 under rapid movement confirms acceptInput serialisation."

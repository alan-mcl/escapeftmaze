#!/usr/bin/env bash
# Headless Temple of Wasud depths 1–4 run (full party of 6).
# Compiles tests, runs TempleFloorRunTest, writes the HTML report.
# Progress lines are printed to stdout during the run (depth, steps, combats).
#
# Usage (from anywhere; script cds to repo root):
#   ./temple-run.sh
#
# Report:
#   build/test-reports/temple-floor-run-42.html

set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

JUNIT=(oem/junit/junit-platform-console-standalone-*.jar)
if [[ ! -f "${JUNIT[0]}" ]]; then
	echo "missing JUnit jar under oem/junit/" >&2
	exit 1
fi

ant compile-tests
mkdir -p build/test-reports

java -Djava.awt.headless=true \
	-cp "build/classes:build/default/classes:build/temple/classes:build/test-classes:oem/jorbis/jorbis0.0.17.jar:oem/gson/gson-2.8.6.jar:${JUNIT[0]}" \
	org.junit.platform.console.ConsoleLauncher execute \
	--select-class=mclachlan.maze.campaign.temple.TempleFloorRunTest \
	--disable-banner \
	--details=tree

REPORT="build/test-reports/temple-floor-run-42.html"
echo
if [[ -f "$REPORT" ]]; then
	echo "HTML report: $ROOT/$REPORT"
	LOG_PATH="$(grep -oP '(?<= · log <code>)[^<]+' "$REPORT" || true)"
	if [[ -n "$LOG_PATH" ]]; then
		echo "Session log: $LOG_PATH"
	fi
else
	echo "expected report missing: $REPORT" >&2
	exit 1
fi

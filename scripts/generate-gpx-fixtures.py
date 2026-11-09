#!/usr/bin/env python3
"""core:domain testFixtures의 GPX fixture와 기대값(*.expected.json)을 만든다.

사용: python3 scripts/generate-gpx-fixtures.py
시드를 고정해 항상 같은 파일이 나온다. 좌표는 원점 근처의 가상 경로다 (docs/run/tracking.md 8-1).
"""
import json
import math
import random
from datetime import datetime, timedelta, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "core/domain/src/testFixtures/resources/gpx"
EARTH_RADIUS_M = 6_371_000.0
START = datetime(2026, 1, 1, tzinfo=timezone.utc)


def to_lat_lng(north_m, east_m):
    return math.degrees(north_m / EARTH_RADIUS_M), math.degrees(east_m / EARTH_RADIUS_M)


def trkpt(t_sec, north_m, east_m, accuracy):
    lat, lng = to_lat_lng(north_m, east_m)
    time = (START + timedelta(seconds=t_sec)).strftime("%Y-%m-%dT%H:%M:%SZ")
    return (
        f'<trkpt lat="{lat:.8f}" lon="{lng:.8f}"><time>{time}</time>'
        f"<extensions><accuracy>{accuracy:g}</accuracy></extensions></trkpt>"
    )


def write(name, points, expected):
    body = "\n".join(trkpt(*p) for p in points)
    (OUT / f"{name}.gpx").write_text(
        f'<?xml version="1.0" encoding="UTF-8"?>\n<gpx version="1.1" creator="generate-gpx-fixtures">\n'
        f"<trk><trkseg>\n{body}\n</trkseg></trk>\n</gpx>\n",
        encoding="utf-8",
    )
    (OUT / f"{name}.expected.json").write_text(json.dumps(expected, indent=2) + "\n", encoding="utf-8")
    print(f"{name}: {len(points)} points")


def straight_5km():
    speed = 1000 / 300
    points = [(t, t * speed, 0.0, 5) for t in range(1502)]
    write(
        "straight_5km",
        points,
        {
            "distanceM": 5000,
            "distanceTolerancePct": 1,
            "movingMs": 1_501_000,
            "movingToleranceMs": 1000,
            "splitCount": 5,
            "splitMovingMs": 300_000,
            "splitToleranceMs": 1000,
            "autoPauseCount": 0,
            "segmentCount": 1,
        },
    )


def loop_with_spikes():
    rng = random.Random(3)
    radius = 3000 / (2 * math.pi)
    points = []
    duration = 1000
    for t in range(duration + 1):
        angle = 2 * math.pi * t / duration
        north = radius * math.sin(angle) + rng.uniform(-0.5, 0.5)
        east = radius * (1 - math.cos(angle)) + rng.uniform(-0.5, 0.5)
        if t and t % 60 == 0:
            points.append((t, north + 30, east, 80))
        elif t and t % 97 == 0:
            points.append((t, north + 200, east - 150, 10))
        else:
            points.append((t, north, east, 8))
    write(
        "loop_with_spikes",
        points,
        {
            "distanceM": 3000,
            "distanceTolerancePct": 2,
            "movingMs": 1_000_000,
            "movingToleranceMs": 1000,
            "splitCount": 3,
            "autoPauseCount": 0,
            "segmentCount": 1,
        },
    )


def standing_jitter():
    rng = random.Random(5)
    north = east = 0.0
    points = []
    for t in range(301):
        north = max(-4.0, min(4.0, north + rng.uniform(-0.3, 0.3)))
        east = max(-4.0, min(4.0, east + rng.uniform(-0.3, 0.3)))
        points.append((t, north, east, rng.randint(5, 15)))
    write("standing_jitter", points, {"maxDistanceM": 10, "autoPauseCount": 1, "segmentCount": 1})


def traffic_light():
    speed = 3.0
    points = []
    t = 0
    north = 0.0
    for leg in range(4):
        for _ in range(300):
            points.append((t, north, 0.0, 5))
            t += 1
            north += speed
        if leg < 3:
            for _ in range(40):
                points.append((t, north, 0.0, 5))
                t += 1
    points.append((t, north, 0.0, 5))
    write(
        "traffic_light",
        points,
        {
            "distanceM": 3600,
            "distanceTolerancePct": 1,
            "movingMs": t * 1000 - 3 * 34_000,
            "movingToleranceMs": 12_000,
            "splitCount": 4,
            "autoPauseCount": 3,
            "segmentCount": 1,
        },
    )


def tunnel_gap():
    speed = 3.0
    points = []
    missing = set(range(300, 320)) | set(range(620, 680))
    for t in range(981):
        if t not in missing:
            points.append((t, t * speed, 0.0, 5))
    write(
        "tunnel_gap",
        points,
        {
            "distanceM": 2760,
            "distanceTolerancePct": 1,
            "movingMs": 980_000,
            "movingToleranceMs": 1000,
            "autoPauseCount": 0,
            "segmentCount": 2,
        },
    )


def long_60min():
    rng = random.Random(7)
    speed = 10_000 / 3600
    north = east = 0.0
    points = []
    for t in range(3601):
        heading = 0.8 * math.sin(t / 240)
        points.append((t, north + rng.uniform(-0.3, 0.3), east + rng.uniform(-0.3, 0.3), 6))
        north += speed * math.cos(heading)
        east += speed * math.sin(heading)
    write(
        "long_60min",
        points,
        {
            "distanceM": 10_000,
            "distanceTolerancePct": 1,
            "movingMs": 3_600_000,
            "movingToleranceMs": 1000,
            "autoPauseCount": 0,
            "segmentCount": 1,
        },
    )


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    straight_5km()
    loop_with_spikes()
    standing_jitter()
    traffic_light()
    tunnel_gap()
    long_60min()


if __name__ == "__main__":
    main()

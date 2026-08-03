"""Dependency-free PNG bar chart renderer used by the Java analytics adapter."""

from __future__ import annotations

import struct
import sys
import zlib
from pathlib import Path

WIDTH = 1200
BACKGROUND = (13, 20, 33)
CARD = (24, 35, 52)
TEXT = (235, 242, 255)
MUTED = (142, 158, 180)
GRID = (50, 66, 88)
COLORS = ((69, 196, 176), (84, 139, 255), (255, 184, 77), (238, 99, 125), (170, 112, 255))

FONT = {
    "A": ("01110", "10001", "10001", "11111", "10001", "10001", "10001"),
    "B": ("11110", "10001", "10001", "11110", "10001", "10001", "11110"),
    "C": ("01111", "10000", "10000", "10000", "10000", "10000", "01111"),
    "D": ("11110", "10001", "10001", "10001", "10001", "10001", "11110"),
    "E": ("11111", "10000", "10000", "11110", "10000", "10000", "11111"),
    "F": ("11111", "10000", "10000", "11110", "10000", "10000", "10000"),
    "G": ("01111", "10000", "10000", "10111", "10001", "10001", "01111"),
    "H": ("10001", "10001", "10001", "11111", "10001", "10001", "10001"),
    "I": ("11111", "00100", "00100", "00100", "00100", "00100", "11111"),
    "J": ("00111", "00010", "00010", "00010", "10010", "10010", "01100"),
    "K": ("10001", "10010", "10100", "11000", "10100", "10010", "10001"),
    "L": ("10000", "10000", "10000", "10000", "10000", "10000", "11111"),
    "M": ("10001", "11011", "10101", "10101", "10001", "10001", "10001"),
    "N": ("10001", "11001", "10101", "10011", "10001", "10001", "10001"),
    "O": ("01110", "10001", "10001", "10001", "10001", "10001", "01110"),
    "P": ("11110", "10001", "10001", "11110", "10000", "10000", "10000"),
    "Q": ("01110", "10001", "10001", "10001", "10101", "10010", "01101"),
    "R": ("11110", "10001", "10001", "11110", "10100", "10010", "10001"),
    "S": ("01111", "10000", "10000", "01110", "00001", "00001", "11110"),
    "T": ("11111", "00100", "00100", "00100", "00100", "00100", "00100"),
    "U": ("10001", "10001", "10001", "10001", "10001", "10001", "01110"),
    "V": ("10001", "10001", "10001", "10001", "10001", "01010", "00100"),
    "W": ("10001", "10001", "10001", "10101", "10101", "11011", "10001"),
    "X": ("10001", "10001", "01010", "00100", "01010", "10001", "10001"),
    "Y": ("10001", "10001", "01010", "00100", "00100", "00100", "00100"),
    "Z": ("11111", "00001", "00010", "00100", "01000", "10000", "11111"),
    "0": ("01110", "10001", "10011", "10101", "11001", "10001", "01110"),
    "1": ("00100", "01100", "00100", "00100", "00100", "00100", "01110"),
    "2": ("01110", "10001", "00001", "00010", "00100", "01000", "11111"),
    "3": ("11110", "00001", "00001", "01110", "00001", "00001", "11110"),
    "4": ("00010", "00110", "01010", "10010", "11111", "00010", "00010"),
    "5": ("11111", "10000", "10000", "11110", "00001", "00001", "11110"),
    "6": ("01110", "10000", "10000", "11110", "10001", "10001", "01110"),
    "7": ("11111", "00001", "00010", "00100", "01000", "01000", "01000"),
    "8": ("01110", "10001", "10001", "01110", "10001", "10001", "01110"),
    "9": ("01110", "10001", "10001", "01111", "00001", "00001", "01110"),
    "-": ("00000", "00000", "00000", "11111", "00000", "00000", "00000"),
    "_": ("00000", "00000", "00000", "00000", "00000", "00000", "11111"),
    ".": ("00000", "00000", "00000", "00000", "00000", "00110", "00110"),
    "@": ("01110", "10001", "10111", "10101", "10111", "10000", "01110"),
    "?": ("01110", "10001", "00010", "00100", "00100", "00000", "00100"),
    " ": ("00000",) * 7,
}


class Canvas:
    def __init__(self, width: int, height: int, color: tuple[int, int, int]):
        self.width, self.height = width, height
        self.pixels = bytearray(color * (width * height))

    def rect(self, x: int, y: int, width: int, height: int, color: tuple[int, int, int]):
        left, top = max(0, x), max(0, y)
        right, bottom = min(self.width, x + width), min(self.height, y + height)
        row = bytes(color) * max(0, right - left)
        for py in range(top, bottom):
            start = (py * self.width + left) * 3
            self.pixels[start:start + len(row)] = row

    def text(self, x: int, y: int, value: str, scale: int, color: tuple[int, int, int]):
        cursor = x
        for char in value.upper():
            glyph = FONT.get(char, FONT["?"])
            for gy, row in enumerate(glyph):
                for gx, bit in enumerate(row):
                    if bit == "1":
                        self.rect(cursor + gx * scale, y + gy * scale, scale, scale, color)
            cursor += 6 * scale

    def png(self, path: Path):
        raw = b"".join(b"\x00" + self.pixels[y * self.width * 3:(y + 1) * self.width * 3]
                       for y in range(self.height))
        def chunk(kind: bytes, data: bytes) -> bytes:
            return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)
        payload = b"\x89PNG\r\n\x1a\n"
        payload += chunk(b"IHDR", struct.pack(">IIBBBBB", self.width, self.height, 8, 2, 0, 0, 0))
        payload += chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
        path.write_bytes(payload)


def read_chart(path: Path) -> tuple[str, str, list[tuple[str, int]]]:
    lines = path.read_text(encoding="utf-8").splitlines()
    title = lines[0] if lines else "MICKEY ANALYTICS"
    subtitle = lines[1] if len(lines) > 1 else ""
    points = []
    for line in lines[2:]:
        if "\t" not in line:
            continue
        label, raw_value = line.rsplit("\t", 1)
        points.append((label[:22], max(0, int(raw_value))))
    return title[:34], subtitle[:48], points or [("NO DATA", 0)]


def render(input_path: Path, output_path: Path):
    title, subtitle, points = read_chart(input_path)
    height = 220 + len(points) * 105
    canvas = Canvas(WIDTH, height, BACKGROUND)
    canvas.rect(35, 30, WIDTH - 70, height - 60, CARD)
    canvas.text(75, 68, title, 5, TEXT)
    canvas.text(76, 122, subtitle, 3, MUTED)
    maximum = max(value for _, value in points) or 1
    for index, (label, value) in enumerate(points):
        y = 190 + index * 105
        canvas.text(75, y, label, 3, TEXT)
        canvas.text(930, y, str(value), 3, TEXT)
        canvas.rect(75, y + 42, 1000, 26, GRID)
        bar_width = 0 if value == 0 else max(8, int(1000 * value / maximum))
        canvas.rect(75, y + 42, bar_width, 26, COLORS[index % len(COLORS)])
    canvas.png(output_path)


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise SystemExit("usage: render_chart.py INPUT.tsv OUTPUT.png")
    render(Path(sys.argv[1]), Path(sys.argv[2]))

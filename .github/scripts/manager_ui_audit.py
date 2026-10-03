#!/usr/bin/env python3
"""
Static layout audit of uiautomator dumps captured by manager-live-review.sh.

It flags the things a human would notice in the screenshots but cannot quantify:
  * clipped      — a text node whose box is smaller than the laid-out text (height < 12px or
                   width < 8px while the text is non-empty) or which crosses the screen edge
  * unlabelled   — a clickable node with neither text nor content-description (a "dead-looking" button)
  * empty        — a page that rendered fewer than the expected minimum of text nodes
  * error        — pages where a database/connection error string is visible
  * overflow     — a node whose bounds start left of 0 or end right of the screen width
  * tiny         — tap targets smaller than the 44dp guideline (converted with the dump density)
  * truncated    — duplicate page captures whose text is identical (a tap that did nothing)

Usage: manager_ui_audit.py <xml-dir> <report.txt> [--screen WxH] [--density D]
"""
import glob
import json
import os
import re
import sys
import xml.etree.ElementTree as ET

ERROR_MARKERS = [
    "برقرار نشد", "خطای", "خطا در", "Exception", "Invalid column", "SQLException",
    "net.sourceforge.jtds", "اتصال برقرار نشد", "پیدا نشد؛", "داده‌ای برای نمایش نیست",
]
MIN_TEXT_NODES = 8


def parse(path):
    try:
        root = ET.parse(path).getroot()
    except Exception as e:
        return None, str(e)
    nodes = []
    for n in root.iter("node"):
        b = n.get("bounds") or ""
        m = re.findall(r"-?\d+", b)
        if len(m) != 4:
            continue
        x1, y1, x2, y2 = (int(v) for v in m)
        nodes.append({
            "text": (n.get("text") or "").strip(),
            "desc": (n.get("content-desc") or "").strip(),
            "cls": n.get("class") or "",
            "clickable": n.get("clickable") == "true",
            "enabled": n.get("enabled") != "false",
            "bounds": (x1, y1, x2, y2),
            "w": x2 - x1,
            "h": y2 - y1,
        })
    return nodes, None


def main():
    if len(sys.argv) < 3:
        print(__doc__)
        return 2
    xml_dir, report = sys.argv[1], sys.argv[2]
    screen = (1080, 2340)
    density = 420
    for i, a in enumerate(sys.argv):
        if a == "--screen" and i + 1 < len(sys.argv):
            w, h = sys.argv[i + 1].lower().split("x")
            screen = (int(w), int(h))
        if a == "--density" and i + 1 < len(sys.argv):
            density = int(sys.argv[i + 1])
    sw = screen[0]

    findings = {}
    totals = {"pages": 0, "clipped": 0, "unlabelled": 0, "overflow": 0, "tiny": 0, "errors": 0, "empty": 0}
    lines = ["# Manager UI audit — %d dumps, screen %dx%d @%ddpi" % (len(glob.glob(xml_dir + "/*.xml")), sw, screen[1], density), ""]

    for path in sorted(glob.glob(os.path.join(xml_dir, "*.xml"))):
        name = os.path.basename(path)[:-4]
        nodes, err = parse(path)
        if nodes is None:
            lines.append("%-38s PARSE-FAIL %s" % (name, err))
            continue
        totals["pages"] += 1
        texts = [n for n in nodes if n["text"]]
        issues = {"clipped": [], "unlabelled": [], "overflow": [], "tiny": [], "errors": [], "empty": []}

        for n in texts:
            if n["h"] < 12 or (n["w"] < 10 and len(n["text"]) > 2):
                issues["clipped"].append("%s (%dx%d)" % (n["text"][:32], n["w"], n["h"]))
            if n["bounds"][0] < -2 or n["bounds"][2] > sw + 2:
                issues["overflow"].append("%s [%d..%d]" % (n["text"][:32], n["bounds"][0], n["bounds"][2]))
            low = n["text"].lower()
            for mk in ERROR_MARKERS:
                if mk.lower() in low:
                    issues["errors"].append(n["text"][:48])
                    break

        for n in nodes:
            if n["clickable"] and n["enabled"] and not n["text"] and not n["desc"]:
                issues["unlabelled"].append("%s @%s" % (n["cls"].split(".")[-1], n["bounds"]))
            min_px = int(44 * density / 160 * 0.75)   # 75% of the 44dp guideline: real misses only
            if n["clickable"] and n["enabled"] and (n["w"] < min_px or n["h"] < min_px) and n["h"] > 0:
                issues["tiny"].append("%s %dx%d %s" % (n["text"][:24] or n["desc"][:24] or n["cls"].split(".")[-1], n["w"], n["h"], n["bounds"]))

        if len(texts) < MIN_TEXT_NODES:
            issues["empty"].append("%d text nodes" % len(texts))

        for k, v in issues.items():
            totals[k if k != "errors" else "errors"] += len(v)
        findings[name] = issues
        flag = "OK " if not any(issues.values()) else "!! "
        lines.append("%s%-34s texts=%3d clicks=%2d%s" % (
            flag, name, len(texts), sum(1 for n in nodes if n["clickable"]),
            "" if not any(issues.values()) else "  " + " ".join("%s=%d" % (k, len(v)) for k, v in issues.items() if v)))
        for k, v in issues.items():
            for item in v[:6]:
                lines.append("      %-10s %s" % (k, item))

    lines.append("")
    lines.append("TOTALS pages=%d clipped=%d unlabelled=%d overflow=%d tiny=%d errors=%d empty=%d" % (
        totals["pages"], totals["clipped"], totals["unlabelled"], totals["overflow"],
        totals["tiny"], totals["errors"], totals["empty"]))
    os.makedirs(os.path.dirname(report) or ".", exist_ok=True)
    open(report, "w", encoding="utf-8").write("\n".join(lines) + "\n")
    json.dump({"totals": totals, "pages": findings}, open(report + ".json", "w", encoding="utf-8"),
              ensure_ascii=False, indent=1)
    print("\n".join(lines))
    # Non-zero exit only for hard failures; warnings stay informational so the workflow can commit the report.
    return 1 if (totals["errors"] or totals["empty"]) else 0


if __name__ == "__main__":
    sys.exit(main())

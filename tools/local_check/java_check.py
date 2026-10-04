#!/usr/bin/env python3
"""Local gate for the finance edition sources.

The Android build is the real compiler; this script exists so that the obvious mistakes are caught
before pushing (they cost a full CI round trip):

  * unbalanced braces / parentheses / brackets (string, char and comment aware),
  * a method call on one of the finance classes that has no such method (typo detector),
  * a JDBC or org.json type used in a signature without its import — the exact error that broke the
    1.2.4 build: "cannot find symbol: class Connection",
  * a static method that would hide an inherited method with a different return type.

Usage:  python3 tools/local_check/java_check.py <source-dir> [more dirs…]
"""
import os
import re
import sys
from collections import defaultdict

JDBC_TYPES = ["Connection", "PreparedStatement", "ResultSet", "Statement", "SQLException", "ResultSetMetaData"]
JSON_TYPES = ["JSONObject", "JSONArray"]


def strip_code(text):
    """Removes comments and string/char literals so braces inside them do not count."""
    out = []
    i, n = 0, len(text)
    while i < n:
        ch = text[i]
        nxt = text[i + 1] if i + 1 < n else ""
        if ch == "/" and nxt == "/":
            while i < n and text[i] != "\n":
                i += 1
        elif ch == "/" and nxt == "*":
            i += 2
            while i + 1 < n and not (text[i] == "*" and text[i + 1] == "/"):
                i += 1
            i += 2
        elif ch in "\"'":
            quote = ch
            i += 1
            while i < n and text[i] != quote:
                i += 2 if text[i] == "\\" else 1
            i += 1
        else:
            out.append(ch)
            i += 1
    return "".join(out)


def check_balance(path, code):
    problems = []
    pairs = {")": "(", "]": "[", "}": "{"}
    stack = []
    for idx, ch in enumerate(code):
        if ch in "([{":
            stack.append((ch, idx))
        elif ch in ")]}":
            if not stack or stack[-1][0] != pairs[ch]:
                line = code[:idx].count("\n") + 1
                problems.append("%s:%d unbalanced '%s'" % (path, line, ch))
                return problems
            stack.pop()
    if stack:
        ch, idx = stack[-1]
        line = code[:idx].count("\n") + 1
        problems.append("%s:%d unbalanced '%s'" % (path, line, ch))
    return problems


def check_imports(path, code):
    problems = []
    for name in JDBC_TYPES + JSON_TYPES:
        used = re.search(r"(^|[^\w.])%s\s+[a-zA-Z_]" % name, code, re.M)
        if not used:
            continue
        if "import java.sql.%s;" % name in code or "import org.json.%s;" % name in code:
            continue
        if re.search(r"\b(class|interface|enum)\s+%s\b" % name, code):
            continue
        if re.search(r"(^|[^\w.])%s\." % name, code) and not re.search(r"\b%s\s+[a-zA-Z_]" % name, code):
            continue
        line = code[:used.start()].count("\n") + 1
        problems.append("%s:%d uses %s without importing it" % (path, line, name))
    return problems


def collect_definitions(sources):
    """class name → set of member names defined in it (methods and nested types)."""
    members = defaultdict(set)
    returns = defaultdict(set)
    for path, code in sources.items():
        cls = os.path.basename(path)[:-5]
        members[cls].add(cls)
        for m in re.finditer(r"\b(?:static\s+)?(?:final\s+)?(?:public|private|protected)?\s*"
                             r"(?:[\w<>\[\],. ]+?)\s+(\w+)\s*\(", code):
            members[cls].add(m.group(1))
        for m in re.finditer(r"\b(?:class|interface|enum)\s+(\w+)", code):
            members[cls].add(m.group(1))
    return members, returns


def check_calls(sources, members):
    problems = []
    for path, code in sources.items():
        owners = [c for c in members if c != os.path.basename(path)[:-5]]
        for owner in owners:
            for m in re.finditer(r"\b%s\.(\w+)\s*\(" % re.escape(owner), code):
                name = m.group(1)
                if name not in members[owner]:
                    line = code[:m.start()].count("\n") + 1
                    problems.append("%s:%d %s.%s( → no such member" % (path, line, owner, name))
    return problems


def check_static_hiding(sources):
    """A static method may not hide an inherited instance method with another return type."""
    problems = []
    for path, code in sources.items():
        cls = os.path.basename(path)[:-5]
        parent = None
        m = re.search(r"class\s+%s\s+extends\s+(\w+)" % re.escape(cls), code)
        if m:
            parent = m.group(1)
        if not parent:
            continue
        parent_file = None
        for p in sources:
            if os.path.basename(p)[:-5] == parent:
                parent_file = p
        if not parent_file:
            continue
        parent_code = sources[parent_file]
        parent_methods = defaultdict(set)
        for mm in re.finditer(r"(?:public|protected)\s+(?:static\s+)?([\w<>\[\],. ]+?)\s+(\w+)\s*\(", parent_code):
            parent_methods[mm.group(2)].add(mm.group(1).strip())
        for mm in re.finditer(r"public\s+static\s+([\w<>\[\],. ]+?)\s+(\w+)\s*\(", code):
            name, ret = mm.group(2), mm.group(1).strip()
            for pret in parent_methods.get(name, set()):
                if pret != ret:
                    line = code[:mm.start()].count("\n") + 1
                    problems.append("%s:%d static %s %s hides %s.%s (%s)"
                                    % (path, line, ret, name, parent, name, pret))
    return problems


def main(argv):
    roots = argv[1:] or ["."]
    sources = {}
    for root in roots:
        for dirpath, _dirs, files in os.walk(root):
            for f in files:
                if f.endswith(".java"):
                    path = os.path.join(dirpath, f)
                    sources[path] = strip_code(open(path, encoding="utf-8").read())

    problems = []
    for path, code in sources.items():
        problems += check_balance(path, code)
        problems += check_imports(path, code)

    members, _ = collect_definitions(sources)
    problems += check_calls(sources, members)
    problems += check_static_hiding(sources)

    for p in problems:
        print(p)
    print("checked %d files, %d failed" % (len(sources), len(problems)))
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))

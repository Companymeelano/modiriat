#!/usr/bin/env python3
"""
READ-ONLY financial schema discovery for «آتیران مالی» (Atiran Finance).

Connects to the real Atiran2 SQL Server with the same obfuscated credentials the Android app
uses (XOR-73 int arrays in MainActivity.java) and extracts **metadata only**:

  * tables / views / procedures / functions that exist, with row-count estimates,
  * columns (name, type, length, precision, scale, nullability, identity, computed, default),
  * primary keys, unique constraints, foreign keys, indexes,
  * for finance-relevant tables: date-storage format, status/code domains, freshness
    (min/max dates) and whether the table actually holds rows.

Only SELECT statements are executed. Amounts, customer names, phones and long free text are
never written to the output - long strings are masked exactly like .github/scripts/atiran_probe.py
does it. The output drives docs/finance/FinancialSchemaMapping.md and the app's data layer, so
that no table, column, status or calculation in «آتیران مالی» is guessed.

Usage: python3 atiran_finance_schema.py <out.json> [out.md]
"""
import datetime
import decimal
import json
import os
import re
import sys
import uuid

import pytds

SRC = "MEELANO-Android/app/src/main/java/ir/meelano/android/MainActivity.java"
OUT = sys.argv[1] if len(sys.argv) > 1 else "docs/finance/AtiranFinancialSchema.json"
MD = sys.argv[2] if len(sys.argv) > 2 else "docs/finance/FinancialSchemaMapping.md"
S_KEY = 73

# Names the product spec asked us to look for. Existence is reported as yes/no; a missing name
# stays UNKNOWN and no screen may be built on it.
SEED_NAMES = [
    "CUSTOMERS", "inventory", "Variety", "forosh_price", "kagroup", "sailfact", "subsailfact",
    "buyfact", "subbuyfact", "getchk", "putchk", "CheckTypes", "BANK", "visitors", "vis_goals",
    "Visit", "masir", "cust_act", "Sys_Mandeh_Customer", "vw_customer", "TellBook",
]

# Word stems that make a table or column financially interesting. Persian/Arabic stems are
# matched against DATABASE_DEFAULT collation names as they really are.
FIN_WORDS = [
    "chk", "chek", "check", "bank", "pos", "terminal", "card", "sandogh", "naghd", "cash",
    "daryaft", "vosol", "vosool", "pardakht", "pay", "tasvieh", "settle", "mandeh", "balance",
    "sail", "forosh", "sell", "buy", "kharid", "factor", "act", "sanad", "sarfasl", "doc",
    "mablagh", "amount", "price", "visitor", "user", "haml", "anbar", "cust", "shomare",
]

DATE_HINT = ["date", "tarikh", "rooz", "roz", "time", "zaman", "saat", "dt", "miladi", "shamsi"]
STATUS_HINT = ["status", "vaz", "vaziat", "stat", "type", "noe", "kind", "flag", "enable",
               "active", "state", "situation", "halat", "group", "onvan", "group"]
CODE_HINT = ["code", "cod", "shomare", "sh", "no", "id", "type", "noe", "key"]

MASK_LONG = 28


def hidden(name, src):
    m = re.search(r"int\[\] " + name + r" = \{([^}]*)\}", src)
    if not m:
        raise SystemExit("cannot find %s in %s" % (name, SRC))
    return "".join(chr(int(x) ^ S_KEY) for x in m.group(1).replace("\n", "").split(",") if x.strip())


def mask(v):
    """Keep numbers, dates, flags and short codes; hide long text and binary."""
    if v is None:
        return None
    if isinstance(v, (bytes, bytearray)):
        return "<bin %d>" % len(v)
    if isinstance(v, (datetime.datetime, datetime.date, datetime.time)):
        return v.isoformat()
    if isinstance(v, decimal.Decimal):
        f = float(v)
        return f
    if isinstance(v, uuid.UUID):
        return str(v)
    if isinstance(v, str):
        s = v.strip()
        return s if len(s) <= MASK_LONG else "<text %d>" % len(s)
    return v


def scrub(text, secrets):
    """Never let a credential or the server address reach the committed report."""
    out = str(text)
    for sec in secrets:
        if sec and len(sec) > 2:
            out = out.replace(sec, "<hidden>")
    out = re.sub(r"\b\d{1,3}(?:\.\d{1,3}){3}\b", "<server>", out)
    out = re.sub(r"(?i)(password|pwd)\s*=\s*[^\s;]+", r"\1=<hidden>", out)
    return out[:300]


def money_masked(v):
    """Amounts are never published: only a magnitude bucket is kept."""
    if v is None:
        return None
    try:
        f = abs(float(v))
    except Exception:
        return None
    for limit, label in ((0, "0"), (10 ** 6, "<1e6"), (10 ** 9, "<1e9"), (10 ** 12, "<1e12")):
        if f < limit:
            return label
    return ">=1e12"


def main():
    src = open(SRC, encoding="utf-8").read()
    host = hidden("S_HOST", src)
    db = hidden("S_DB", src)
    user = hidden("S_USER", src)
    pw = hidden("S_PASS", src)
    SECRETS = [host, user, pw, db]
    print("connecting to live Atiran server (host/db/user hidden, read-only)")

    def dump(partial=False):
        out["_partial"] = bool(partial)
        os.makedirs(os.path.dirname(OUT) or ".", exist_ok=True)
        with open(OUT, "w", encoding="utf-8") as fh:
            json.dump(out, fh, ensure_ascii=False, indent=1, default=str)
    conn = pytds.connect(server=host, port=1433, database=db, user=user, password=pw,
                         login_timeout=30, timeout=180, autocommit=True)
    out = {"ok": True, "generated_utc": datetime.datetime.now(datetime.timezone.utc)
           .replace(tzinfo=None).isoformat() + "Z",
           "server": {}, "objects": {}, "tables": {}, "views": {}, "procedures": {}, "functions": {},
           "column_index": {}, "date_probe": {}, "domain_probe": {}, "freshness": {},
           "seed_lookup": {}, "candidates": [], "errors": []}

    def q(sql, params=None, limit=None):
        cur = conn.cursor()
        cur.execute(sql, params)
        if not cur.description:
            return []
        cols = [d[0] for d in cur.description]
        rows = []
        for r in cur.fetchall():
            rows.append(dict(zip(cols, [mask(x) for x in r])))
            if limit and len(rows) >= limit:
                break
        return rows

    def safe(key, fn, default=None):
        try:
            return fn()
        except Exception as ex:
            out["errors"].append("%s: %s" % (key, scrub(ex, SECRETS)))
            return default

    def q_safe(key, sql, params=None, limit=None):
        return safe(key, lambda: q(sql, params, limit), [])

    q_safe("_iso", "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET LOCK_TIMEOUT 5000;")
    for stmt in ("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED", "SET LOCK_TIMEOUT 5000",
                 "SET NOCOUNT ON"):
        safe(stmt, lambda s=stmt: q(s))

    # ---------------------------------------------------------------- server / database
    def server_info():
        rows = q("""SELECT DB_NAME() AS db_name, CONVERT(nvarchar(20), SERVERPROPERTY('ProductVersion')) AS version,
                           CONVERT(nvarchar(40), SERVERPROPERTY('Edition')) AS edition,
                           CONVERT(nvarchar(40), DATABASEPROPERTYEX(DB_NAME(), 'Collation')) AS collation,
                           CONVERT(nvarchar(40), DATABASEPROPERTYEX(DB_NAME(), 'Updateability')) AS updateability,
                           CONVERT(nvarchar(40), DATABASEPROPERTYEX(DB_NAME(), 'Recovery')) AS recovery,
                           CONVERT(nvarchar(40), COMPATIBILITY_LEVEL) AS compat_level
                    FROM sys.databases WHERE name = DB_NAME()""")
        base = rows[0] if rows else {}
        extra = q("""SELECT CONVERT(nvarchar(30), SYSDATETIME(), 120) AS server_time,
                            CONVERT(nvarchar(30), GETUTCDATE(), 120) AS utc_time,
                            CONVERT(nvarchar(30), SYSDATETIMEOFFSET(), 127) AS tz_offset,
                            CONVERT(nvarchar(40), CURRENT_USER) AS login_name,
                            ISNULL(CONVERT(nvarchar(40), SUSER_SNAME()), N'') AS login_full,
                            (SELECT COUNT(*) FROM sys.tables) AS table_count,
                            (SELECT COUNT(*) FROM sys.views) AS view_count,
                            (SELECT COUNT(*) FROM sys.procedures) AS proc_count,
                            (SELECT COUNT(*) FROM sys.objects WHERE type IN ('FN','IF','TF')) AS func_count""")
        if extra:
            base.update(extra[0])
        return base

    out["server"] = safe("server_info", server_info, {})
    dump(partial=True)

    # ---------------------------------------------------------------- all objects + row counts
    def objects():
        return q("""SELECT o.name AS name, o.type AS type, s.name AS schema_name,
                           CONVERT(bigint, ISNULL(SUM(p.rows), 0)) AS rows_est,
                           CONVERT(nvarchar(23), o.create_date, 120) AS created,
                           CONVERT(nvarchar(23), o.modify_date, 120) AS modified
                    FROM sys.objects o
                    JOIN sys.schemas s ON s.schema_id = o.schema_id
                    LEFT JOIN sys.partitions p ON p.object_id = o.object_id AND p.index_id IN (0, 1)
                    WHERE o.type IN ('U', 'V', 'P', 'FN', 'IF', 'TF')
                    GROUP BY o.name, o.type, s.name, o.create_date, o.modify_date
                    ORDER BY o.name""")

    objs = safe("objects", objects, []) or []
    by_name = {}
    for o in objs:
        t = o.get("type")
        key = {"U": "tables", "V": "views", "P": "procedures", "FN": "functions",
               "IF": "functions", "TF": "functions"}.get(t)
        if key:
            out[key][o["name"]] = {"schema": o.get("schema_name"), "rows": o.get("rows_est"),
                                   "created": o.get("created"), "modified": o.get("modified")}
            by_name[o["name"].lower()] = o
    print("objects: %d tables, %d views, %d procedures, %d functions" % (
        len(out["tables"]), len(out["views"]), len(out["procedures"]), len(out["functions"])))
    dump(partial=True)

    # ---------------------------------------------------------------- seed lookup
    for n in SEED_NAMES:
        hit = by_name.get(n.lower())
        out["seed_lookup"][n] = ({"exists": True, "kind": {"U": "table", "V": "view", "P": "procedure"}
                                  .get(hit["type"], hit["type"]), "rows": hit.get("rows_est")}
                                 if hit else {"exists": False, "status": "UNKNOWN"})

    # ---------------------------------------------------------------- columns for every user table
    def columns():
        return q("""SELECT t.name AS tbl, c.column_id AS colid, c.name AS col,
                           ty.name AS typ, c.max_length AS len, c.precision AS prec, c.scale AS scale,
                           c.is_nullable AS nullable, c.is_identity AS identity_col,
                           c.is_computed AS computed, c.is_rowguidcol AS rowguid,
                           ISNULL(dc.definition, N'') AS default_def,
                           ISNULL(cc.definition, N'') AS computed_def
                    FROM sys.columns c
                    JOIN sys.tables t ON t.object_id = c.object_id
                    JOIN sys.types ty ON ty.user_type_id = c.user_type_id
                    LEFT JOIN sys.default_constraints dc ON dc.parent_object_id = c.object_id
                                                        AND dc.parent_column_id = c.column_id
                    LEFT JOIN sys.computed_columns cc ON cc.object_id = c.object_id
                                                     AND cc.column_id = c.column_id
                    ORDER BY t.name, c.column_id""")

    cols = safe("columns", columns, []) or []
    for c in cols:
        t = out["tables"].setdefault(c["tbl"], {"schema": "dbo", "rows": None, "columns": []})
        t.setdefault("columns", []).append({
            "name": c["col"], "type": c["typ"], "len": c["len"], "precision": c["prec"],
            "scale": c["scale"], "nullable": bool(c["nullable"]), "identity": bool(c["identity_col"]),
            "computed": bool(c["computed"]), "default": (c["default_def"] or "")[:160],
            "computed_expr": (c["computed_def"] or "")[:200] or None})

    dump(partial=True)

    # ---------------------------------------------------------------- keys, FKs, indexes
    def keys():
        return q("""SELECT t.name AS tbl, kc.name AS name, kc.type AS kc_type,
                           CASE WHEN ic.key_ordinal > 0 THEN c.name END AS col,
                           ic.key_ordinal AS ord
                    FROM sys.key_constraints kc
                    JOIN sys.tables t ON t.object_id = kc.parent_object_id
                    JOIN sys.index_columns ic ON ic.object_id = kc.parent_object_id
                                             AND ic.index_id = kc.unique_index_id
                    JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
                    ORDER BY t.name, kc.name, ic.key_ordinal""")

    for k in (safe("keys", keys, []) or []):
        t = out["tables"].setdefault(k["tbl"], {})
        pk = "PK" if k["kc_type"] == "PK" else "UQ"
        t.setdefault("keys", {}).setdefault(k["name"], {"kind": pk, "columns": []})
        t["keys"][k["name"]]["columns"].append(k["col"])

    def fks():
        return q("""SELECT fk.name AS name, tp.name AS parent_tbl, cp.name AS parent_col,
                           tr.name AS ref_tbl, cr.name AS ref_col, fk.delete_referential_action_desc AS on_delete
                    FROM sys.foreign_keys fk
                    JOIN sys.tables tp ON tp.object_id = fk.parent_object_id
                    JOIN sys.tables tr ON tr.object_id = fk.referenced_object_id
                    JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
                    JOIN sys.columns cp ON cp.object_id = fkc.parent_object_id
                                        AND cp.column_id = fkc.parent_column_id
                    JOIN sys.columns cr ON cr.object_id = fkc.referenced_object_id
                                        AND cr.column_id = fkc.referenced_column_id
                    ORDER BY tp.name, fk.name""")

    for f in (safe("fks", fks, []) or []):
        t = out["tables"].setdefault(f["parent_tbl"], {})
        t.setdefault("foreign_keys", []).append({"name": f["name"], "column": f["parent_col"],
                                                "references": "%s.%s" % (f["ref_tbl"], f["ref_col"]),
                                                "on_delete": f["on_delete"]})

    def indexes():
        return q("""SELECT t.name AS tbl, i.name AS name, i.type_desc AS type_desc,
                           i.is_unique AS is_unique, i.is_primary_key AS is_pk,
                           STUFF((SELECT ',' + c2.name FROM sys.index_columns ic2
                                  JOIN sys.columns c2 ON c2.object_id = ic2.object_id
                                                     AND c2.column_id = ic2.column_id
                                  WHERE ic2.object_id = i.object_id AND ic2.index_id = i.index_id
                                    AND ic2.is_included_column = 0
                                  ORDER BY ic2.key_ordinal FOR XML PATH(''), TYPE).value('.', 'nvarchar(max)'), 1, 1, '') AS cols
                    FROM sys.indexes i
                    JOIN sys.tables t ON t.object_id = i.object_id
                    WHERE i.type > 0 AND i.name IS NOT NULL AND i.is_primary_key = 0
                    ORDER BY t.name, i.name""")

    for i in (safe("indexes", indexes, []) or []):
        t = out["tables"].setdefault(i["tbl"], {})
        t.setdefault("indexes", []).append({"name": i["name"], "type": i["type_desc"],
                                            "unique": bool(i["is_unique"]), "columns": i["cols"]})

    dump(partial=True)

    # ---------------------------------------------------------------- column keyword index
    idx = {}
    for tbl, meta in out["tables"].items():
        for c in meta.get("columns", []):
            low = c["name"].lower()
            for w in FIN_WORDS:
                if w in low:
                    idx.setdefault(w, {}).setdefault(tbl, []).append(c["name"])
    out["column_index"] = idx

    # ---------------------------------------------------------------- candidate tables
    scored = []
    for tbl, meta in out["tables"].items():
        low = tbl.lower()
        score = 0
        hits = []
        for w in FIN_WORDS:
            if w in low:
                score += 3
                hits.append(w)
        for c in meta.get("columns", []):
            cl = c["name"].lower()
            for w in FIN_WORDS:
                if w in cl:
                    score += 1
                    hits.append("%s.%s" % (tbl, c["name"]))
                    break
        if score:
            scored.append({"table": tbl, "rows": meta.get("rows"), "score": score,
                           "hits": sorted(set(hits))[:12],
                           "columns": len(meta.get("columns", []))})
    out["candidates"] = sorted(scored, key=lambda x: -x["score"])[:80]
    dump(partial=True)

    # Only these tables are probed row-by-row (keeps load on the live server minimal).
    focus = [c["table"] for c in out["candidates"][:60]]
    focus += [n for n in SEED_NAMES if n in out["tables"]]
    focus = [t for t in dict.fromkeys(focus) if (out["tables"].get(t, {}).get("rows") or 0) > 0]
    heavy = [t for t in focus if (out["tables"].get(t, {}).get("rows") or 0) > 2_000_000]
    out["probe_focus"] = focus
    print("probing %d finance tables individually" % len(focus))

    # ---------------------------------------------------------------- date format probe
    def date_probe():
        res = {}
        for tbl in focus:
            meta = out["tables"].get(tbl, {})
            cols = meta.get("columns", [])
            for c in cols:
                low = c["name"].lower()
                if not any(h in low for h in DATE_HINT):
                    continue
                typ = (c["type"] or "").lower()
                key = "%s.%s" % (tbl, c["name"])
                colq = "[%s]" % c["name"].replace("]", "]]")
                tblq = "[%s]" % tbl.replace("]", "]]")
                try:
                    if typ in ("datetime", "datetime2", "smalldatetime", "date"):
                        r = q("""SELECT CONVERT(nvarchar(30), MIN({C}), 120) AS min_v,
                                        CONVERT(nvarchar(30), MAX({C}), 120) AS max_v,
                                        COUNT(*) AS rows_all, COUNT({C}) AS rows_notnull
                                 FROM dbo.{T} WITH (NOLOCK)""".format(C=colq, T=tblq))
                        res[key] = {"type": typ, "kind": "datetime", "min": r[0]["min_v"],
                                    "max": r[0]["max_v"], "notnull": r[0]["rows_notnull"]}
                    elif typ in ("char", "varchar", "nchar", "nvarchar", "text", "ntext"):
                        r = q("""SELECT COUNT(*) AS rows_all, COUNT({C}) AS notnull,
                                        SUM(CASE WHEN {C} LIKE '[1][234][0-9][0-9]/[0-9][0-9]/[0-9][0-9]%' THEN 1 ELSE 0 END) AS jalali,
                                        SUM(CASE WHEN {C} LIKE '[12][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]%' THEN 1 ELSE 0 END) AS iso,
                                        MIN({C}) AS min_v, MAX({C}) AS max_v
                                 FROM dbo.{T} WITH (NOLOCK)""".format(C=colq, T=tblq))
                        res[key] = {"type": typ, "kind": "text",
                                    "sample_count": r[0]["notnull"], "jalali_like": r[0]["jalali"],
                                    "iso_like": r[0]["iso"], "min": r[0]["min_v"], "max": r[0]["max_v"]}
                except Exception as ex:
                    res[key] = {"type": typ, "error": str(ex)[:120]}
        return res

    out["date_probe"] = safe("date_probe", date_probe, {})
    dump(partial=True)
    print("date probe columns: %d" % len(out["date_probe"]))

    # ---------------------------------------------------------------- domain probe (status / codes)
    def domain_probe():
        res = {}
        for tbl in focus:
            meta = out["tables"].get(tbl)
            if not meta:
                continue
            for c in meta.get("columns", []):
                low = c["name"].lower()
                if not any(h in low for h in STATUS_HINT):
                    continue
                typ = (c["type"] or "").lower()
                if typ not in ("char", "varchar", "nchar", "nvarchar", "int", "smallint", "tinyint", "bit"):
                    continue
                try:
                    r = q("""SELECT TOP (14) {C} AS v, COUNT(*) AS n FROM dbo.{T} WITH (NOLOCK)
                             GROUP BY {C} ORDER BY COUNT(*) DESC""".format(
                             C="[%s]" % c["name"].replace("]", "]]"),
                             T="[%s]" % tbl.replace("]", "]]")))
                    res["%s.%s" % (tbl, c["name"])] = r
                except Exception as ex:
                    res["%s.%s" % (tbl, c["name"])] = [{"error": str(ex)[:120]}]
        return res

    out["domain_probe"] = safe("domain_probe", domain_probe, {})
    dump(partial=True)
    print("status domains: %d" % len(out["domain_probe"]))

    # ---------------------------------------------------------------- freshness (is there data now?)
    def freshness():
        res = {}
        res["_server_now"] = q("SELECT CONVERT(nvarchar(19), SYSDATETIME(), 120) AS now_v")
        for tbl in [t for t in ["sailfact", "subsailfact", "getchk", "putchk", "cust_act", "customers",
                                "bank", "visitors", "salefacttasvieh", "sys_mandeh_customer"]
                    if t in out["tables"]]:
            meta = out["tables"].get(tbl)
            if not meta:
                continue
            entry = {"rows_est": meta.get("rows")}
            try:
                r = q("SELECT COUNT(*) AS n FROM dbo.[%s] WITH (NOLOCK)" % tbl.replace("]", "]]"))
                entry["rows_exact"] = r[0]["n"]
            except Exception as ex:
                entry["count_error"] = str(ex)[:120]
            res[tbl] = entry
        return res

    out["freshness"] = safe("freshness", freshness, {})
    dump(partial=True)
    print("freshness tables: %d" % len(out["freshness"]))

    # ---------------------------------------------------------------- sample values (masked, per candidate)
    def samples():
        res = {}
        for tbl in focus:
            if tbl not in out["tables"]:
                continue
            try:
                r = q("SELECT TOP (2) * FROM dbo.[%s] WITH (NOLOCK)" % tbl.replace("]", "]]"))
                res[tbl] = r
            except Exception as ex:
                res[tbl] = [{"error": str(ex)[:140]}]
        return res

    out["samples_masked"] = safe("samples_masked", samples, {})
    print("sample dumps: %d" % len(out["samples_masked"]))

    # ---------------------------------------------------------------- write
    out["ok"] = True
    dump()
    print("schema json written: %s (%d bytes)" % (OUT, os.path.getsize(OUT)))

    if MD:
        write_markdown(out, MD)
        print("schema mapping written: %s" % MD)

    summary(out)


def write_markdown(o, path):
    L = []
    add = L.append
    add("# FinancialSchemaMapping — آتیران مالی")
    add("")
    add("این سند از **Metadata واقعی** پایگاه‌داده آتیران تولید می‌شود (فقط SELECT، فقط ساختار).")
    add("هیچ نام مشتری، مبلغ، تلفن یا متن آزاد در آن ثبت نمی‌شود؛ متن‌های بلند ماسک می‌شوند.")
    add("")
    add("- زمان تولید (UTC): `%s`" % o.get("generated_utc"))
    s = o.get("server") or {}
    add("- پایگاه‌داده: `%s` — نسخه سرور `%s`" % (s.get("db_name"), s.get("version")))
    add("- Collation: `%s` — سطح سازگاری `%s`" % (s.get("collation"), s.get("compat_level")))
    add("- زمان سرور: `%s` (UTC `%s`, منطقه `%s`)" % (s.get("server_time"), s.get("utc_time"), s.get("tz_offset")))
    add("- شمارش اشیا: %s جدول، %s ویو، %s رویه" % (s.get("table_count"), s.get("view_count"), s.get("proc_count")))
    add("")
    add("## ۱) جست‌وجوی نام‌های آغازین (Seed Names)")
    add("")
    add("| نام درخواستی | وجود | نوع | تخمین ردیف |")
    add("|---|---|---|---|")
    for n, v in (o.get("seed_lookup") or {}).items():
        add("| `%s` | %s | %s | %s |" % (n, "بله" if v.get("exists") else "**خیر — UNKNOWN**",
                                        v.get("kind", "—"), v.get("rows", "—")))
    add("")
    add("## ۲) جدول‌های نامزد مالی (مرتب‌شده بر اساس ارتباط)")
    add("")
    add("| جدول | تخمین ردیف | امتیاز | ستون‌های کلیدی مالی |")
    add("|---|---|---|---|")
    for c in (o.get("candidates") or [])[:60]:
        add("| `%s` | %s | %s | %s |" % (c["table"], c.get("rows"), c.get("score"),
                                        ", ".join("`%s`" % h for h in c.get("hits", [])[:6])))
    add("")
    add("## ۳) ستون‌های جدول‌های مالی کلیدی")
    for tbl in sorted(o.get("tables") or {}):
        meta = o["tables"][tbl]
        cols = meta.get("columns") or []
        if not cols:
            continue
        if meta.get("rows") is None:
            continue
        add("")
        add("### `%s` — %s ردیف (تخمین)" % (tbl, meta.get("rows")))
        keys = meta.get("keys") or {}
        if keys:
            add("- کلیدها: " + "; ".join("%s(%s)=%s" % (k, v["kind"], ", ".join(v["columns"]))
                                         for k, v in keys.items()))
        for fk in meta.get("foreign_keys") or []:
            add("- FK `%s`: %s → %s" % (fk["name"], fk["column"], fk["references"]))
        add("")
        add("| ستون | نوع | Null | Identity | پیش‌فرض |")
        add("|---|---|---|---|---|")
        for c in cols:
            add("| `%s` | %s(%s) | %s | %s | %s |" % (
                c["name"], c["type"], c.get("len"), "بله" if c.get("nullable") else "خیر",
                "بله" if c.get("identity") else "—", (c.get("default") or "").replace("|", "/")))
    add("")
    add("## ۴) قالب ذخیره تاریخ (Probe)")
    add("")
    add("| ستون | نوع | ماهیت | بازه واقعی | نشانه‌ها |")
    add("|---|---|---|---|---|")
    for k, v in sorted((o.get("date_probe") or {}).items()):
        if "error" in v:
            add("| `%s` | %s | خطا | %s | |" % (k, v.get("type"), v["error"]))
            continue
        note = ""
        if v.get("kind") == "text":
            note = "jalali_like=%s iso_like=%s" % (v.get("jalali_like"), v.get("iso_like"))
        add("| `%s` | %s | %s | %s تا %s | %s |" % (k, v.get("type"), v.get("kind"),
                                                   v.get("min"), v.get("max"), note))
    add("")
    add("## ۵) دامنه مقادیر وضعیت/کد (Status Domains)")
    add("")
    for k, rows in sorted((o.get("domain_probe") or {}).items()):
        vals = []
        for r in rows:
            if "error" in r:
                vals.append("خطا: " + r["error"])
            else:
                vals.append("`%s`×%s" % (r.get("v"), r.get("n")))
        add("- **%s**: %s" % (k, ", ".join(vals) or "—"))
    add("")
    add("## ۶) تازگی داده (Freshness)")
    add("")
    add("| جدول | تخمین ردیف | ردیف دقیق |")
    add("|---|---|---|")
    for k, v in sorted((o.get("freshness") or {}).items()):
        add("| `%s` | %s | %s |" % (k, v.get("rows_est"), v.get("rows_exact", v.get("count_error", "—"))))
    add("")
    add("## ۷) خطاهای Probe")
    add("")
    for e in (o.get("errors") or [])[:60]:
        add("- `%s`" % e)
    add("")
    with open(path, "w", encoding="utf-8") as fh:
        fh.write("\n".join(L))


def summary(o):
    print("---- finance candidates ----")
    for c in (o.get("candidates") or [])[:25]:
        print("  %-28s rows=%-10s score=%-4s hits=%s" % (c["table"], c.get("rows"), c.get("score"),
                                                          ",".join(c.get("hits", [])[:6])))
    print("---- seed lookup ----")
    for n, v in (o.get("seed_lookup") or {}).items():
        print("  %-24s %s" % (n, "OK (%s, %s rows)" % (v.get("kind"), v.get("rows")) if v.get("exists") else "MISSING"))
    print("---- errors (%d) ----" % len(o.get("errors") or []))
    for e in (o.get("errors") or [])[:15]:
        print("  " + e)


def cli():
    try:
        main()
    except Exception as ex:  # never leave CI without a report
        import traceback
        tb = traceback.format_exc()
        print("FATAL: " + tb)
        try:
            cur = {"ok": False, "fatal": scrub(ex, []), "generated_utc":
                   datetime.datetime.now(datetime.timezone.utc).replace(tzinfo=None).isoformat() + "Z",
                   "errors": tb.splitlines()[-6:]}
            with open(OUT, "w", encoding="utf-8") as fh:
                json.dump(cur, fh, ensure_ascii=False, indent=1)
        except Exception:
            pass
        raise SystemExit(3)


if __name__ == "__main__":
    cli()

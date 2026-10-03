#!/usr/bin/env python3
"""
READ-ONLY detail probe #2 for «آتیران مالی».

Stage 1 (atiran_finance_schema.py) proved what exists. This stage reads the *business* tables the
finance product needs, with real columns only:

  * treasury chain   : dar (receipt header) / ban_act (bank movements) / COW (cash box) / cust_act (ledger)
  * invoices         : sailfact (+ subsailfact), salefacttasvieh, directtasvieh
  * cheques          : getchk / putchk / CheckTypes / getcheckhistorystatus / chkbatch / checks
  * POS              : PosDetails / TerminalPos / TerminalCompanyPos / BankPos / UserPos
  * balances         : CUSTOMERS.man, Sys_Mandeh_Customer, vw_customer (view definition digest)
  * operators        : visitors, sys_users, meelano_access_*
  * money + dates    : column scales, server Jalali date, settings that tell the currency scale

Money is never published: only magnitude buckets, scales and counts. Free text is masked unless it
is short. Object definitions are reduced to a short, filtered digest of the formula-relevant lines
(never the vendor's full source). Only SELECT statements are executed.

Usage: python3 atiran_finance_detail.py <out.json>
"""
import datetime
import decimal
import json
import os
import re
import sys

import pytds

SRC = "MEELANO-Android/app/src/main/java/ir/meelano/android/MainActivity.java"
OUT = sys.argv[1] if len(sys.argv) > 1 else "docs/finance/AtiranFinanceDetail.json"
S_KEY = 73

TABLES = ["dar", "darDescriptionType", "ban_act", "COW", "cust_act", "sailfact", "subsailfact",
          "SalefactTasvieh", "DirectTasvieh", "getchk", "putchk", "CheckTypes",
          "getcheckhistorystatus", "chkbatch", "checks", "PosDetails", "TerminalPos",
          "TerminalCompanyPos", "BankPos", "UserPos", "BANK", "CUSTOMERS", "Sys_Mandeh_Customer",
          "visitors", "sys_users", "overal_setting", "ka_act", "anbars", "inventory"]

# Definitions whose formula lines are needed for documented calculations.
DEF_OBJECTS = ["vw_customer", "Daryaft", "AddInvoice", "FixManCustomer", "FixManBank", "FixTasvie",
               "DaryaftMultiFactor", "ReturnDateServer", "TemplateDaryaftCheque", "EditInvoice",
               "PutBan_act", "PutGetcheck", "InvoiceTrigger"]

FORMULA_WORDS = ("man", "mandeh", "tasvie", "MabDaryaft", "act_id", "act_bes", "act_bed", "sum(",
                 "update", "set ", "insert", "select", "getchk", "cust_act", "ban_act", "cow",
                 "shfacfo", "ghno", "mab", "posdetails")


def hidden(name, src):
    m = re.search(r"int\[\] " + name + r" = \{([^}]*)\}", src)
    if not m:
        raise SystemExit("cannot find %s" % name)
    return "".join(chr(int(x) ^ S_KEY) for x in m.group(1).replace("\n", "").split(",") if x.strip())


def scrub(text, secrets):
    out = str(text)
    for s in secrets:
        if s and len(s) > 2:
            out = out.replace(s, "<hidden>")
    out = re.sub(r"\b\d{1,3}(?:\.\d{1,3}){3}\b", "<server>", out)
    out = re.sub(r"(?i)(password|pwd)\s*=\s*[^\s;]+", r"\1=<hidden>", out)
    return out[:300]


def bucket(v):
    try:
        f = abs(float(v))
    except Exception:
        return None
    for limit, label in ((1e3, "<1e3"), (1e6, "<1e6"), (1e9, "<1e9"), (1e12, "<1e12"), (1e15, "<1e15")):
        if f < limit:
            return label
    return ">=1e15"


def main():
    src = open(SRC, encoding="utf-8").read()
    host, db, user, pw = (hidden("S_HOST", src), hidden("S_DB", src),
                          hidden("S_USER", src), hidden("S_PASS", src))
    secrets = [host, db, user, pw]
    conn = pytds.connect(server=host, port=1433, database=db, user=user, password=pw,
                         login_timeout=30, timeout=180, autocommit=True)
    out = {"ok": True, "generated_utc": datetime.datetime.now(datetime.timezone.utc)
           .replace(tzinfo=None).isoformat() + "Z", "errors": [], "tables": {}, "domains": {},
           "pos": {}, "treasury": {}, "checks": {}, "balances": {}, "operators": {},
           "definitions_digest": {}, "money": {}, "counts": {}}

    def ident(name):
        """Metadata-only script: never interpolate anything that is not a plain identifier."""
        if not re.fullmatch(r"[A-Za-z_][A-Za-z_0-9]{0,80}", str(name)):
            raise ValueError("unsafe identifier: %r" % name)
        return str(name)

    def q(sql, params=None, limit=200):
        cur = conn.cursor()
        cur.execute(sql, params)
        if not cur.description:
            return []
        cols = [d[0] for d in cur.description]
        rows = []
        for r in cur.fetchall():
            row = {}
            for c, v in zip(cols, r):
                if isinstance(v, (bytes, bytearray)):
                    row[c] = "<bin %d>" % len(v)
                elif isinstance(v, (datetime.datetime, datetime.date, datetime.time)):
                    row[c] = v.isoformat()
                elif isinstance(v, decimal.Decimal):
                    row[c] = bucket(v)
                elif isinstance(v, str):
                    row[c] = v if len(v) <= 40 else "<text %d>" % len(v)
                else:
                    row[c] = v
            rows.append(row)
            if limit and len(rows) >= limit:
                break
        return rows

    def safe(key, fn, default=None):
        try:
            return fn()
        except Exception as ex:
            out["errors"].append("%s: %s" % (key, scrub(ex, secrets)))
            return default

    def q_safe(key, sql, params=None, limit=200):
        return safe(key, lambda: q(sql, params, limit), [])

    safe("iso", lambda: q("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED"))
    safe("lock", lambda: q("SET LOCK_TIMEOUT 5000"))

    def columns_of(name):
        return q("""SELECT c.name AS col, ty.name AS typ, c.max_length AS len, c.precision AS prec,
                           c.scale AS scale, c.is_nullable AS nullable, c.is_identity AS ident,
                           ISNULL(dc.definition,N'') AS def
                    FROM sys.columns c
                    JOIN sys.objects o ON o.object_id = c.object_id
                    JOIN sys.types ty ON ty.user_type_id = c.user_type_id
                    LEFT JOIN sys.default_constraints dc ON dc.parent_object_id=c.object_id
                                                        AND dc.parent_column_id=c.column_id
                    WHERE o.name = N'%s' ORDER BY c.column_id""" % ident(name), None, 400)

    # ---------------------------------------------------------------- 1. tables
    out["counts"]["object_types"] = q_safe("objtypes", """SELECT o.type, COUNT(*) AS n
        FROM sys.objects o WHERE o.type IN ('U','V','P','FN','IF','TF') GROUP BY o.type""")
    for t in TABLES:
        rows = safe("cols_" + t, lambda tt=t: columns_of(tt), [])
        if rows:
            out["tables"][t] = rows
    out["counts"]["resolved"] = {t: len(v) for t, v in out["tables"].items()}

    # ---------------------------------------------------------------- 2. domains on the real columns
    DOMAINS = [
        ("cust_act", "act_id"), ("cust_act", "isActive"), ("cust_act", "ShowInReport"),
        ("dar", "p"), ("dar", "Active"), ("dar", "darDescriptionTypeID"),
        ("ban_act", "act_id"), ("ban_act", "p"), ("ban_act", "Active"),
        ("COW", "p"), ("COW", "act_id"),
        ("sailfact", "Status"), ("sailfact", "active"), ("sailfact", "tasvieh"),
        ("sailfact", "nahve_namayesh_daryaft"), ("salefacttasvieh", "type"),
        ("getchk", "chk_satus"), ("getchk", "soo"), ("getchk", "back"), ("getchk", "our_bankrdf"),
        ("getchk", "CheckTypeID"), ("getchk", "ghno"), ("getchk", "kharj_mod"), ("getchk", "mod"),
        ("putchk", "putchk_status"), ("putchk", "amani"), ("putchk", "bankrdf"),
        ("putchk", "CheckTypeID"), ("putchk", "shfacbuy"),
        ("CheckTypes", "ID"), ("CheckTypes", "Desciption"),
        ("checks", "type"), ("checks", "status"),
        ("PosDetails", "IsHavaleh"), ("PosDetails", "PosBankRdf"), ("PosDetails", "TerminalID"),
        ("PosDetails", "UserID"), ("PosDetails", "Rdf_"),
        ("TerminalPos", "TerminalCompanyID"), ("TerminalPos", "Active"),
        ("BANK", "BankRdf"), ("BANK", "IsPos"), ("BANK", "IsCard"), ("BANK", "Active"),
        ("CUSTOMERS", "kind"), ("CUSTOMERS", "active"), ("CUSTOMERS", "hesab_status"),
        ("visitors", "kind"), ("visitors", "active"), ("visitors", "is_supervisor"),
        ("sys_users", "role_id"), ("sys_users", "active"), ("sys_users", "IsLocked"),
    ]
    for t, c in DOMAINS:
        rows = safe("dom_%s.%s" % (t, c), lambda tt=t, cc=c: q(
            """SELECT TOP (16) [{c}] AS v, COUNT(*) AS n FROM dbo.[{t}] WITH (NOLOCK)
               GROUP BY [{c}] ORDER BY COUNT(*) DESC""".format(c=c, t=t)), [])
        if rows:
            out["domains"]["%s.%s" % (t, c)] = rows
    # code tables that give the ledger / receipt kinds their real names
    for code_tbl, id_col, name_col in [("act_kind", "act_id", "act_name"), ("darDescriptionType", "rowId", "name"),
                                       ("daftar", None, None)]:
        rows = safe("code_" + code_tbl, lambda tt=code_tbl: q(
            "SELECT TOP (60) * FROM dbo.[%s] WITH (NOLOCK)" % tt), [])
        if rows:
            out["domains"]["_table_" + code_tbl] = rows

    # ---------------------------------------------------------------- 3. POS chain
    def pos():
        res = {}
        res["posdetails_cols"] = out["tables"].get("PosDetails")
        res["counts"] = q("""SELECT (SELECT COUNT(*) FROM dbo.PosDetails WITH (NOLOCK)) AS pos_rows,
                                    (SELECT COUNT(*) FROM dbo.PosDetails WITH (NOLOCK) WHERE UserID IS NOT NULL) AS pos_with_user,
                                    (SELECT COUNT(DISTINCT PosBankRdf) FROM dbo.PosDetails WITH (NOLOCK)) AS distinct_banks,
                                    (SELECT COUNT(DISTINCT TerminalID) FROM dbo.PosDetails WITH (NOLOCK)) AS distinct_terminals,
                                    (SELECT COUNT(DISTINCT ghno) FROM dbo.PosDetails WITH (NOLOCK)) AS distinct_receipts,
                                    (SELECT COUNT(*) FROM dbo.TerminalPos WITH (NOLOCK)) AS terminals,
                                    (SELECT COUNT(*) FROM dbo.TerminalCompanyPos WITH (NOLOCK)) AS companies,
                                    (SELECT COUNT(*) FROM dbo.dar WITH (NOLOCK)) AS receipts,
                                    (SELECT COUNT(*) FROM dbo.ban_act WITH (NOLOCK)) AS bank_rows,
                                    (SELECT COUNT(*) FROM dbo.COW WITH (NOLOCK)) AS cash_rows""")
        res["link_pos_to_dar"] = q("""SELECT COUNT(*) AS matched FROM dbo.PosDetails pd WITH (NOLOCK)
                                      INNER JOIN dbo.dar d WITH (NOLOCK) ON d.ghno = pd.ghno AND d.p = 0""")
        res["link_pos_to_bank"] = q("""SELECT COUNT(*) AS matched FROM dbo.PosDetails pd WITH (NOLOCK)
                                       INNER JOIN dbo.BANK b WITH (NOLOCK) ON b.RDF = pd.PosBankRdf""")
        res["link_pos_to_user"] = q("""SELECT COUNT(*) AS matched FROM dbo.PosDetails pd WITH (NOLOCK)
                                       INNER JOIN dbo.visitors v WITH (NOLOCK) ON v.UserID = pd.UserID""")
        res["link_pos_to_sysuser"] = q("""SELECT COUNT(*) AS matched FROM dbo.PosDetails pd WITH (NOLOCK)
                                          INNER JOIN dbo.sys_users u WITH (NOLOCK) ON u.user_id = pd.UserID""")
        res["pos_amount_scale"] = q("""SELECT TOP (5) ID, ghno, MabPos, Karmozd, PosBankRdf, TerminalID, UserID,
                                              ShPeigiri, IsHavaleh, PosDesc
                                       FROM dbo.PosDetails WITH (NOLOCK) ORDER BY ID DESC""")
        res["pos_by_bank"] = q("""SELECT PosBankRdf AS bank, COUNT(*) AS n, SUM(MabPos) AS total
                                  FROM dbo.PosDetails WITH (NOLOCK) GROUP BY PosBankRdf ORDER BY n DESC""")
        res["pos_by_user"] = q("""SELECT UserID, COUNT(*) AS n, SUM(MabPos) AS total
                                  FROM dbo.PosDetails WITH (NOLOCK) GROUP BY UserID ORDER BY n DESC""")
        res["terminals"] = q("""SELECT t.TerminalID, t.TerminalCompanyID, t.TerminalName, t.TerminalNumber,
                                       t.AcceptorId, t.SerialNo, t.Active, c.TerminalCompanyName
                                FROM dbo.TerminalPos t WITH (NOLOCK)
                                LEFT JOIN dbo.TerminalCompanyPos c WITH (NOLOCK)
                                       ON c.TerminalCompanyID = t.TerminalCompanyID""", None, 40)
        res["companies"] = q("SELECT * FROM dbo.TerminalCompanyPos WITH (NOLOCK)")
        return res

    out["pos"] = safe("pos", pos, {})

    # ---------------------------------------------------------------- 4. treasury chain
    def treasury():
        res = {}
        res["dar_cols"] = out["tables"].get("dar")
        res["cow_cols"] = out["tables"].get("COW")
        res["ban_act_cols"] = out["tables"].get("ban_act")
        res["dar_recent"] = q("""SELECT TOP (8) ghno, p, mab, shmo, date, shfac, Active,
                                        darDescriptionTypeID, UserID, UniqueID
                                 FROM dbo.dar WITH (NOLOCK) WHERE p = 0 ORDER BY ghno DESC""", None, 10)
        res["dar_by_month"] = q("""SELECT LEFT(date, 7) AS ym, COUNT(*) AS n FROM dbo.dar WITH (NOLOCK)
                                   GROUP BY LEFT(date, 7) ORDER BY ym DESC""", None, 12)
        res["cust_act_by_month"] = q("""SELECT LEFT(date, 7) AS ym, COUNT(*) AS n FROM dbo.cust_act WITH (NOLOCK)
                                        GROUP BY LEFT(date, 7) ORDER BY ym DESC""", None, 12)
        res["cust_act_act_kinds"] = q("""SELECT a.act_id, COUNT(*) AS n FROM dbo.cust_act a WITH (NOLOCK)
                                        GROUP BY a.act_id ORDER BY n DESC""", None, 40)
        res["getchk_by_month"] = q("""SELECT LEFT(getdate, 7) AS ym, COUNT(*) AS n FROM dbo.getchk WITH (NOLOCK)
                                      GROUP BY LEFT(getdate, 7) ORDER BY ym DESC""", None, 12)
        res["putchk_by_month"] = q("""SELECT LEFT(putdate, 7) AS ym, COUNT(*) AS n FROM dbo.putchk WITH (NOLOCK)
                                      GROUP BY LEFT(putdate, 7) ORDER BY ym DESC""", None, 12)
        res["sailfact_by_month"] = q("""SELECT LEFT(date, 7) AS ym, COUNT(*) AS n,
                                               SUM(CASE WHEN tasvieh = 1 THEN 1 ELSE 0 END) AS settled_rows
                                        FROM dbo.sailfact WITH (NOLOCK) GROUP BY LEFT(date, 7)
                                        ORDER BY ym DESC""", None, 14)
        res["sales_totals_by_month"] = q("""SELECT LEFT(date, 7) AS ym, COUNT(*) AS n, SUM([all]) AS sum_all,
                                                   SUM(MabDaryaftFactor) AS sum_received, SUM(bamandeh) AS sum_left
                                            FROM dbo.sailfact WITH (NOLOCK) WHERE active = 't'
                                            GROUP BY LEFT(date, 7) ORDER BY ym DESC""", None, 14)
        res["server_dates"] = q("""SELECT CONVERT(char(10), dbo.ReturnDateServer()) AS jalali_today,
                                          CONVERT(char(10), SYSDATETIME(), 23) AS gregorian_now,
                                          CONVERT(char(8), SYSDATETIME(), 108) AS time_now""")
        res["settlement_tables"] = q("""SELECT o.name, SUM(p.rows) AS rows_est FROM sys.objects o
                                        LEFT JOIN sys.partitions p ON p.object_id=o.object_id
                                                                  AND p.index_id IN (0,1)
                                        WHERE o.name LIKE '%tasvie%' GROUP BY o.name ORDER BY o.name""")
        return res

    out["treasury"] = safe("treasury", treasury, {})

    # ---------------------------------------------------------------- 5. checks
    def checks():
        res = {}
        res["checktypes"] = q("SELECT * FROM dbo.CheckTypes WITH (NOLOCK)")
        res["getchk_sample"] = q("""SELECT TOP (8) rdf, getdate, sardate, shgetchk, getchkshhes, getchbank,
                                           getchkmab, shmo, VIRTUALNAME, chk_satus, our_bankrdf, back,
                                           soo, CheckTypeID, DateOfReceipt, ShenaseSayad, ghno
                                    FROM dbo.getchk WITH (NOLOCK) ORDER BY rdf DESC""", None, 10)
        res["putchk_sample"] = q("""SELECT TOP (8) rdf, putdate, sardate, shputchk, bankrdf, putchkmab, shmo,
                                           girande, putchk_status, shfacbuy, ghno, CheckTypeID, amani
                                    FROM dbo.putchk WITH (NOLOCK) ORDER BY rdf DESC""", None, 10)
        res["getchk_counts"] = q("""SELECT COUNT(*) AS n,
                                           SUM(CASE WHEN back IN ('t','T') THEN 1 ELSE 0 END) AS returned,
                                           SUM(CASE WHEN our_bankrdf > 0 THEN 1 ELSE 0 END) AS with_our_bank,
                                           SUM(CASE WHEN kharj_date NOT LIKE '%-%' AND kharj_date <> '' THEN 1 ELSE 0 END) AS with_kharj,
                                           SUM(CASE WHEN shmo > 0 THEN 1 ELSE 0 END) AS with_customer,
                                           COUNT(DISTINCT shmo) AS customers
                                    FROM dbo.getchk WITH (NOLOCK)""")
        res["putchk_counts"] = q("""SELECT COUNT(*) AS n, SUM(CASE WHEN bankrdf > 0 THEN 1 ELSE 0 END) AS with_bank,
                                           COUNT(DISTINCT bankrdf) AS banks, COUNT(DISTINCT girande) AS payees
                                    FROM dbo.putchk WITH (NOLOCK)""")
        res["getcheckhistorystatus"] = q("SELECT TOP (30) * FROM dbo.getcheckhistorystatus WITH (NOLOCK)")
        res["chkbatch_cols"] = out["tables"].get("chkbatch")
        res["checks_table"] = q("SELECT TOP (5) * FROM dbo.checks WITH (NOLOCK)")
        res["check_dates_relative"] = q("""SELECT SUM(CASE WHEN sardate = dbo.ReturnDateServer() THEN 1 ELSE 0 END) AS due_today,
                                                  SUM(CASE WHEN sardate > dbo.ReturnDateServer()
                                                            AND sardate <= DATEPART(year, GETDATE()) THEN 1 ELSE 0 END) AS _x,
                                                  SUM(CASE WHEN sardate < dbo.ReturnDateServer() THEN 1 ELSE 0 END) AS overdue,
                                                  COUNT(*) AS all_rows
                                           FROM dbo.getchk WITH (NOLOCK)""", None, 4)
        return res

    out["checks"] = safe("checks", checks, {})

    # ---------------------------------------------------------------- 6. balances
    def balances():
        res = {}
        res["customers_balance_cols"] = q("""SELECT TOP (1) SHMO, MONAME, man, cred, check_eteb, hesab_status,
                                                    maxopen_time, just_naghdi, black_list, kind, c_pos
                                             FROM dbo.CUSTOMERS WITH (NOLOCK)""")
        res["mandeh_sample"] = q("""SELECT TOP (5) ID, Shmo, Mandeh, Etebar, CheckAddToMandeh,
                                           MaxFaktorTasvieNashode, MaxSarCheck, MaxCheckPassNashode,
                                           MaxOpenTime, BlockResult
                                    FROM dbo.Sys_Mandeh_Customer WITH (NOLOCK)""", None, 6)
        res["mandeh_stats"] = q("""SELECT COUNT(*) AS n, SUM(CASE WHEN Mandeh <> 0 THEN 1 ELSE 0 END) AS nonzero,
                                          SUM(CASE WHEN Etebar <> 0 THEN 1 ELSE 0 END) AS with_credit,
                                          MIN(LEN(CONVERT(varchar(60), Mandeh))) AS len_min,
                                          MAX(LEN(CONVERT(varchar(60), Mandeh))) AS len_max
                                   FROM dbo.Sys_Mandeh_Customer WITH (NOLOCK)""")
        res["cust_act_pair_check"] = q("""SELECT TOP (6) rdf_, shmo, date, act_bes, act_bed, act_dis, act_id, ghno
                                          FROM dbo.cust_act WITH (NOLOCK) ORDER BY rdf_ DESC""", None, 8)
        res["cust_act_sums"] = q("""SELECT COUNT(*) AS n, SUM(act_bes) AS sum_bes, SUM(act_bed) AS sum_bed,
                                           COUNT(DISTINCT shmo) AS customers FROM dbo.cust_act WITH (NOLOCK)""")
        res["view_customer_like"] = q("""SELECT v.name AS view_name FROM sys.views v
                                         WHERE v.name LIKE '%cust%' OR v.name LIKE '%mandeh%'
                                         ORDER BY v.name""", None, 40)
        res["functions_like_mandeh"] = q("""SELECT o.name, o.type FROM sys.objects o
                                            WHERE (o.name LIKE '%mandeh%' OR o.name LIKE '%Mandeh%'
                                                   OR o.name LIKE '%man%' AND o.type IN ('FN','IF','TF'))
                                              AND o.is_ms_shipped = 0 ORDER BY o.name""", None, 40)
        # does CUSTOMERS.man equal the ledger sum? (authoritative balance check, no names)
        res["balance_agreement"] = q("""SELECT COUNT(*) AS compared,
                                               SUM(CASE WHEN ABS(ISNULL(c.man,0) - ISNULL(x.ledger,0)) < 1 THEN 1 ELSE 0 END) AS agreeing
                                        FROM dbo.CUSTOMERS c WITH (NOLOCK)
                                        LEFT JOIN (SELECT shmo, SUM(ISNULL(act_bed,0) - ISNULL(act_bes,0)) AS ledger
                                                   FROM dbo.cust_act WITH (NOLOCK) GROUP BY shmo) x
                                               ON x.shmo = c.SHMO""")
        return res

    out["balances"] = safe("balances", balances, {})

    # ---------------------------------------------------------------- 7. operators
    def operators():
        res = {}
        res["visitors"] = q("""SELECT vis_rdf, vis_name, Username, UserID, kind, active, is_supervisor,
                                      supervisor_rdf, vis_man, rdf_device, NotCalculateCommision, BlackList
                               FROM dbo.visitors WITH (NOLOCK) ORDER BY vis_rdf""", None, 20)
        res["visitor_pass_state"] = q("""SELECT vis_rdf, Username, CASE WHEN Password IS NULL THEN 'null' ELSE 'set' END AS pw_state,
                                                DATALENGTH(Password) AS pw_bytes
                                         FROM dbo.visitors WITH (NOLOCK) ORDER BY vis_rdf""", None, 20)
        res["sys_users"] = q("""SELECT user_id, user_name, user_fname, user_lname, role_id, active, IsLocked,
                                       shmo, TafsilID, AccessToCRM,
                                       CASE WHEN user_password IS NULL THEN 'null' ELSE 'set' END AS pw_state,
                                       DATALENGTH(user_password) AS pw_bytes
                                FROM dbo.sys_users WITH (NOLOCK) ORDER BY user_id""", None, 20)
        res["role_tables"] = q("""SELECT o.name FROM sys.objects o
                                  WHERE o.type IN ('U','V') AND (o.name LIKE '%role%' OR o.name LIKE '%Role%'
                                     OR o.name LIKE '%access%' OR o.name LIKE '%Access%') ORDER BY o.name""", None, 40)
        res["meelano_access_roles"] = q("""SELECT role_key, role_label, LEN(ISNULL(permissions,N'')) AS perm_len,
                                                  CONVERT(nvarchar(19), updated_at, 120) AS updated
                                           FROM dbo.meelano_access_roles WITH (NOLOCK)""", None, 40)
        res["meelano_access_users"] = q("""SELECT TOP (40) username, display_name, source, role_key, enabled,
                                                  LEN(ISNULL(permissions,N'')) AS perm_len
                                           FROM dbo.meelano_access_users WITH (NOLOCK) ORDER BY updated_at DESC""", None, 40)
        return res

    out["operators"] = safe("operators", operators, {})

    # ---------------------------------------------------------------- 8. definitions digest
    def definitions():
        res = {}
        for name in DEF_OBJECTS:
            d = q("SELECT OBJECT_DEFINITION(OBJECT_ID(N'%s')) AS d" % ident(name))
            if not d or not d[0].get("d"):
                continue
            txt = d[0]["d"]
            lines = [ln.strip() for ln in txt.splitlines()]
            keep = []
            for ln in lines:
                low = ln.lower()
                if any(w.lower() in low for w in FORMULA_WORDS):
                    keep.append(re.sub(r"\s+", " ", ln)[:220])
                if len(keep) >= 26:
                    break
            res[name] = {"chars": len(txt), "lines": len(lines), "digest": keep,
                         "objects_touched": sorted(set(re.findall(
                             r"dbo\.([A-Za-z_][A-Za-z_0-9]*)", txt)))[:30]}
        return res

    out["definitions_digest"] = safe("definitions", definitions, {})

    # ---------------------------------------------------------------- 9. money scale / settings
    def money():
        res = {}
        res["money_columns"] = q("""SELECT t.name AS tbl, c.name AS col, ty.name AS typ, c.precision, c.scale
                                    FROM sys.columns c JOIN sys.tables t ON t.object_id=c.object_id
                                    JOIN sys.types ty ON ty.user_type_id=c.user_type_id
                                    WHERE ty.name IN ('money','smallmoney')
                                      AND (t.name IN ('sailfact','getchk','putchk','BANK','cust_act','dar',
                                                      'ban_act','COW','PosDetails','CUSTOMERS',
                                                      'Sys_Mandeh_Customer','salefacttasvieh'))
                                    ORDER BY t.name, c.name""", None, 120)
        res["sailfact_amounts"] = q("""SELECT TOP (6) [all] AS total, MabDaryaftFactor AS received,
                                              bamandeh AS left_amount, tax, avarez, barbari, tafif
                                       FROM dbo.sailfact WITH (NOLOCK) ORDER BY date DESC""", None, 8)
        res["setting_rows"] = q("""SELECT TOP (60) * FROM dbo.overal_setting WITH (NOLOCK)""", None, 60)
        res["setting_cols"] = columns_of("overal_setting")
        return res

    out["money"] = safe("money", money, {})

    os.makedirs(os.path.dirname(OUT) or ".", exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as fh:
        json.dump(out, fh, ensure_ascii=False, indent=1, default=str)
    print("detail json: %s (%d bytes)" % (OUT, os.path.getsize(OUT)))
    print("errors: %d" % len(out["errors"]))
    for e in out["errors"][:20]:
        print("  " + e)
    print("resolved tables: %s" % json.dumps(out["counts"].get("resolved", {}), ensure_ascii=False))
    print("pos counts: %s" % json.dumps(out["pos"].get("counts", []), ensure_ascii=False)[:600])
    print("server dates: %s" % json.dumps(out["treasury"].get("server_dates", []), ensure_ascii=False)[:300])
    print("definitions: %s" % list(out["definitions_digest"].keys()))


def cli():
    try:
        main()
    except Exception as ex:
        import traceback
        tb = traceback.format_exc()
        print("FATAL: " + tb)
        try:
            if os.path.exists(OUT) and os.path.getsize(OUT) > 3000:
                raise SystemExit(4)
            with open(OUT, "w", encoding="utf-8") as fh:
                json.dump({"ok": False, "errors": scrub(ex, []), "trace": tb.splitlines()[-8:]},
                          fh, ensure_ascii=False, indent=1)
        except SystemExit:
            raise
        except Exception:
            pass
        raise SystemExit(3)


if __name__ == "__main__":
    cli()

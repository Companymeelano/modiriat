#!/usr/bin/env python3
"""
READ-ONLY validation probe (#3) for «آتیران مالی».

Turns the discovered structure into *proven semantics*:

  1. Status name tables (getcheckhistorystatus, Roles, ...) so no status code is invented.
  2. Ledger kind names: the real `act_dis` text sampled per `act_id` in cust_act / ban_act / COW.
  3. Formula agreement tests on real rows (counts and mismatch counts only, never amounts):
       dar.mab          = dar.naghd + dar.mabcheck + SUM(PosDetails.MabPos of that receipt)
       CUSTOMERS.man    = SUM(cust_act.act_bed - cust_act.act_bes)
       sailfact.[all]   = SUM(subsailfact.LINESUM)  (per invoice, tolerance 1)
       sailfact settling columns vs receipt headers
  4. Due-date coverage: checks due today / this week / overdue (Jalali string compare against the
     server's own dbo.ReturnDateServer()).
  5. POS reality: how PosDetails joins dar / visitors / sys_users, per bank and per day.
  6. Cash box (COW) identity and balance behaviour, bank movement kinds (ban_act.act_id).
  7. Credit terms: salefacttasvieh / zamanbanditasviehfactor / CUSTOMERS.CheckDateDay.

Amounts stay hidden: only counts, magnitudes and booleans are written.

Usage: python3 atiran_finance_validate.py <out.json>
"""
import datetime
import decimal
import json
import os
import re
import sys

import pytds

SRC = "MEELANO-Android/app/src/main/java/ir/meelano/android/MainActivity.java"
OUT = sys.argv[1] if len(sys.argv) > 1 else "docs/finance/AtiranFinanceValidation.json"
S_KEY = 73


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
                         login_timeout=30, timeout=240, autocommit=True)
    out = {"ok": True, "generated_utc": datetime.datetime.now(datetime.timezone.utc)
           .replace(tzinfo=None).isoformat() + "Z", "errors": [], "sections": {}}

    def q(sql, limit=200):
        cur = conn.cursor()
        cur.execute(sql)
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
                    # keep short names (status/kind labels) but never long free text
                    row[c] = v if len(v) <= 40 else "<text %d>" % len(v)
                else:
                    row[c] = v
            rows.append(row)
            if limit and len(rows) >= limit:
                break
        return rows

    def sec(name, sql, limit=200):
        try:
            out["sections"][name] = q(sql, limit)
        except Exception as ex:
            out["sections"][name] = []
            out["errors"].append("%s: %s" % (name, scrub(ex, secrets)))

    def ident(name):
        if not re.fullmatch(r"[A-Za-z_][A-Za-z_0-9]{0,60}", str(name)):
            raise ValueError("unsafe identifier")
        return str(name)

    q("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED")
    q("SET LOCK_TIMEOUT 5000")

    # 1. server clock / jalali today
    sec("clock", """SELECT CONVERT(char(10), dbo.ReturnDateServer()) AS jalali_today,
                           CONVERT(char(10), SYSDATETIME(), 23) AS gregorian_now,
                           CONVERT(char(8), SYSDATETIME(), 108) AS time_now""")

    # 2. status name tables
    sec("check_status_names", "SELECT * FROM dbo.getcheckhistorystatus WITH (NOLOCK)")
    sec("roles_table", "SELECT TOP (30) * FROM dbo.Roles WITH (NOLOCK)")
    sec("role_table", "SELECT TOP (30) * FROM dbo.role WITH (NOLOCK)")
    sec("user_role", "SELECT TOP (30) * FROM dbo.UserRole WITH (NOLOCK)")
    sec("checktypes", "SELECT * FROM dbo.CheckTypes WITH (NOLOCK)")
    sec("tasvieh_types", "SELECT * FROM dbo.salefacttasvieh WITH (NOLOCK)")
    sec("bank_list", """SELECT RDF, BANKNAME, SHHE, ShabaNumber, MAN, IsPos, IsCard, Active, BankRdf,
                               AccountType, BranchCode, HaveEChecks, BlackList
                        FROM dbo.BANK WITH (NOLOCK) ORDER BY RDF""")
    sec("bank_flags", """SELECT COUNT(*) AS banks, SUM(CASE WHEN ISNULL(Active,1)=1 THEN 1 ELSE 0 END) AS active,
                                SUM(CASE WHEN ISNULL(IsPos,0)=1 THEN 1 ELSE 0 END) AS pos_banks,
                                SUM(CASE WHEN ISNULL(HaveEChecks,0)=1 THEN 1 ELSE 0 END) AS echeck_banks,
                                SUM(CASE WHEN ShabaNumber IS NOT NULL AND ShabaNumber <> '' THEN 1 ELSE 0 END) AS with_shaba
                         FROM dbo.BANK WITH (NOLOCK)""")

    # 3. ledger kind names: what text does Atiran itself put next to each act_id?
    for tbl, col in [("cust_act", "act_dis"), ("ban_act", "act_dis"), ("COW", "DIS")]:
        sec("kinds_%s" % tbl, """SELECT act_id, COUNT(*) AS n, MIN(LEFT(%s, 34)) AS sample_text
                                 FROM dbo.%s WITH (NOLOCK) GROUP BY act_id ORDER BY n DESC""" % (col, tbl))

    # 4. formula agreement (counts and mismatch counts only)
    sec("agree_receipt_total", """SELECT COUNT(*) AS receipts,
                SUM(CASE WHEN ABS(d.mab - (ISNULL(d.naghd,0) + ISNULL(d.mabcheck,0) + ISNULL(p.pos_sum,0))) <= 1
                         THEN 1 ELSE 0 END) AS agreeing,
                SUM(CASE WHEN ABS(d.mab - (ISNULL(d.naghd,0) + ISNULL(d.mabcheck,0) + ISNULL(p.pos_sum,0))) > 1
                         THEN 1 ELSE 0 END) AS mismatch
            FROM dbo.dar d WITH (NOLOCK)
            LEFT JOIN (SELECT ghno, SUM(MabPos) AS pos_sum FROM dbo.PosDetails WITH (NOLOCK) GROUP BY ghno) p
                   ON p.ghno = d.ghno
            WHERE d.p = 0 AND ISNULL(d.Active,1) = 1""")
    sec("agree_customer_balance", """SELECT COUNT(*) AS compared,
                SUM(CASE WHEN ABS(ISNULL(c.man,0) - ISNULL(x.ledger,0)) <= 1 THEN 1 ELSE 0 END) AS agreeing,
                SUM(CASE WHEN ABS(ISNULL(c.man,0) - ISNULL(x.ledger,0)) > 1 THEN 1 ELSE 0 END) AS mismatch,
                SUM(CASE WHEN ISNULL(c.man,0) < 0 THEN 1 ELSE 0 END) AS negative_balance
            FROM dbo.CUSTOMERS c WITH (NOLOCK)
            LEFT JOIN (SELECT shmo, SUM(ISNULL(act_bed,0) - ISNULL(act_bes,0)) AS ledger
                       FROM dbo.cust_act WITH (NOLOCK) GROUP BY shmo) x ON x.shmo = c.SHMO""")
    sec("agree_invoice_lines", """SELECT COUNT(*) AS invoices,
                SUM(CASE WHEN ABS(ISNULL(s.[all],0) - ISNULL(l.lines,0)) <= 2 THEN 1 ELSE 0 END) AS agreeing,
                SUM(CASE WHEN ABS(ISNULL(s.[all],0) - ISNULL(l.lines,0)) > 2 THEN 1 ELSE 0 END) AS mismatch
            FROM dbo.sailfact s WITH (NOLOCK)
            LEFT JOIN (SELECT shfacfo, SUM(LINESUM) AS lines FROM dbo.subsailfact WITH (NOLOCK)
                       GROUP BY shfacfo) l ON l.shfacfo = s.shfacfo AND l.shfacfo = s.shfacfo""")
    sec("receipt_breakdown", """SELECT COUNT(*) AS receipts,
                SUM(CASE WHEN ISNULL(naghd,0) <> 0 THEN 1 ELSE 0 END) AS with_cash,
                SUM(CASE WHEN ISNULL(mabcheck,0) <> 0 THEN 1 ELSE 0 END) AS with_checks,
                SUM(ISNULL(ted_chk,0)) AS check_rows,
                SUM(CASE WHEN ISNULL(shfac,0) > 0 THEN 1 ELSE 0 END) AS linked_to_invoice
            FROM dbo.dar WITH (NOLOCK) WHERE p = 0 AND ISNULL(Active,1) = 1""")

    # 5. checks: status meaning, due windows, returned
    sec("getchk_by_status", """SELECT g.chk_satus AS status_id, ISNULL(s.StatusName, N'(بي نام)') AS status_name,
                                      COUNT(*) AS n, SUM(CASE WHEN g.back IN ('t','T') THEN 1 ELSE 0 END) AS returned,
                                      SUM(CASE WHEN g.our_bankrdf > 0 THEN 1 ELSE 0 END) AS deposited,
                                      SUM(CASE WHEN g.kharj_date NOT LIKE '%-%' AND g.kharj_date <> '' THEN 1 ELSE 0 END) AS withdrawn
                               FROM dbo.getchk g WITH (NOLOCK)
                               LEFT JOIN dbo.getcheckhistorystatus s WITH (NOLOCK) ON s.GetStatusID = g.chk_satus
                               GROUP BY g.chk_satus, s.StatusName ORDER BY n DESC""")
    sec("getchk_due_windows", """SELECT COUNT(*) AS all_checks,
                SUM(CASE WHEN sardate = dbo.ReturnDateServer() THEN 1 ELSE 0 END) AS due_today,
                SUM(CASE WHEN sardate < dbo.ReturnDateServer() THEN 1 ELSE 0 END) AS overdue,
                SUM(CASE WHEN sardate > dbo.ReturnDateServer() THEN 1 ELSE 0 END) AS future,
                SUM(CASE WHEN sardate < '1400/01/01' THEN 1 ELSE 0 END) AS unreasonably_old,
                MIN(sardate) AS min_due, MAX(sardate) AS max_due
                FROM dbo.getchk WITH (NOLOCK)""")
    sec("getchk_open_amount", """SELECT chk_satus AS status_id, COUNT(*) AS n, SUM(getchkmab) AS total
                                 FROM dbo.getchk WITH (NOLOCK) GROUP BY chk_satus ORDER BY n DESC""")
    sec("putchk_by_status", """SELECT putchk_status AS status_id, COUNT(*) AS n, SUM(putchkmab) AS total,
                                      SUM(CASE WHEN sardate < dbo.ReturnDateServer() THEN 1 ELSE 0 END) AS overdue
                               FROM dbo.putchk WITH (NOLOCK) GROUP BY putchk_status ORDER BY n DESC""")
    sec("getchk_link", """SELECT COUNT(*) AS all_checks,
                SUM(CASE WHEN ghno > 0 THEN 1 ELSE 0 END) AS with_receipt,
                SUM(CASE WHEN shmo > 0 THEN 1 ELSE 0 END) AS with_customer,
                SUM(CASE WHEN vis_rdf > 0 THEN 1 ELSE 0 END) AS with_visitor,
                SUM(CASE WHEN our_bankrdf > 0 THEN 1 ELSE 0 END) AS with_bank,
                SUM(CASE WHEN ShenaseSayad IS NOT NULL AND ShenaseSayad <> '' THEN 1 ELSE 0 END) AS with_sayad
                FROM dbo.getchk WITH (NOLOCK)""")

    # 6. POS reality
    sec("pos_join", """SELECT COUNT(*) AS pos_rows,
                SUM(CASE WHEN EXISTS (SELECT 1 FROM dbo.dar d WITH (NOLOCK) WHERE d.ghno = pd.ghno AND d.p = 0)
                         THEN 1 ELSE 0 END) AS with_receipt,
                SUM(CASE WHEN EXISTS (SELECT 1 FROM dbo.visitors v WITH (NOLOCK) WHERE v.vis_rdf = pd.UserID)
                         THEN 1 ELSE 0 END) AS with_visitor_rdf,
                SUM(CASE WHEN EXISTS (SELECT 1 FROM dbo.visitors v WITH (NOLOCK) WHERE v.UserID = pd.UserID)
                         THEN 1 ELSE 0 END) AS with_visitor_userid,
                SUM(CASE WHEN EXISTS (SELECT 1 FROM dbo.sys_users u WITH (NOLOCK) WHERE u.user_id = pd.UserID)
                         THEN 1 ELSE 0 END) AS with_sys_user,
                SUM(CASE WHEN pd.TerminalID IS NOT NULL THEN 1 ELSE 0 END) AS with_terminal,
                SUM(CASE WHEN pd.IsHavaleh = 1 THEN 1 ELSE 0 END) AS havala,
                SUM(CASE WHEN pd.ShPeigiri IS NOT NULL AND pd.ShPeigiri <> '' THEN 1 ELSE 0 END) AS with_reference
                FROM dbo.PosDetails pd WITH (NOLOCK)""")
    sec("pos_by_bank_name", """SELECT pd.PosBankRdf AS bank, ISNULL(b.BANKNAME, N'(بي نام)') AS bank_name,
                                      COUNT(*) AS n, SUM(pd.MabPos) AS total, SUM(pd.Karmozd) AS fee
                               FROM dbo.PosDetails pd WITH (NOLOCK)
                               LEFT JOIN dbo.BANK b WITH (NOLOCK) ON b.RDF = pd.PosBankRdf
                               GROUP BY pd.PosBankRdf, b.BANKNAME ORDER BY n DESC""")
    sec("pos_by_user", """SELECT pd.UserID, ISNULL(v.vis_name, N'(بي نام)') AS vis_name, ISNULL(v.Username,N'') AS username,
                                 COUNT(*) AS n, SUM(pd.MabPos) AS total
                          FROM dbo.PosDetails pd WITH (NOLOCK)
                          LEFT JOIN dbo.visitors v WITH (NOLOCK) ON v.vis_rdf = pd.UserID
                          GROUP BY pd.UserID, v.vis_name, v.Username ORDER BY n DESC""")
    sec("pos_by_day", """SELECT TOP (10) d.date AS jalali_date, COUNT(*) AS n, SUM(pd.MabPos) AS total
                         FROM dbo.PosDetails pd WITH (NOLOCK)
                         JOIN dbo.dar d WITH (NOLOCK) ON d.ghno = pd.ghno AND d.p = 0
                         GROUP BY d.date ORDER BY d.date DESC""")
    sec("terminal_tables", """SELECT o.name, o.type, SUM(p.rows) AS rows_est FROM sys.objects o
                              LEFT JOIN sys.partitions p ON p.object_id=o.object_id AND p.index_id IN (0,1)
                              WHERE o.name LIKE '%Terminal%' OR o.name LIKE '%Pos%' OR o.name LIKE '%POS%'
                              GROUP BY o.name, o.type ORDER BY o.name""")

    # 7. bank movement kinds and cash box
    sec("ban_act_kinds", """SELECT act_id, COUNT(*) AS n, MIN(LEFT(act_dis, 34)) AS sample_text,
                                   SUM(act_bes) AS sum_bes, SUM(act_bed) AS sum_bed
                            FROM dbo.ban_act WITH (NOLOCK) WHERE ISNULL(isActive,1)=1
                            GROUP BY act_id ORDER BY n DESC""")
    sec("cow_summary", """SELECT COUNT(*) AS rows_all, SUM(BES) AS sum_bes, SUM(BED) AS sum_bed,
                                 COUNT(DISTINCT bank_rdf) AS boxes, MIN(DATE) AS min_date, MAX(DATE) AS max_date
                          FROM dbo.COW WITH (NOLOCK) WHERE ISNULL(isActive,1)=1""")
    sec("cow_kinds", """SELECT act_id, COUNT(*) AS n, MIN(LEFT(DIS,30)) AS sample_text
                        FROM dbo.COW WITH (NOLOCK) GROUP BY act_id ORDER BY n DESC""")
    sec("bank_act_recent", """SELECT TOP (6) bank_rdf, act_id, act_bes, act_bed, act_date, Ghno, isActive
                              FROM dbo.ban_act WITH (NOLOCK) ORDER BY rdf DESC""")
    sec("cow_recent", """SELECT TOP (6) DATE, act_id, BES, BED, bank_rdf, Ghno, isActive
                         FROM dbo.COW WITH (NOLOCK) ORDER BY rdf DESC""")

    # 8. credit terms (for documented aging)
    sec("customers_terms", """SELECT COUNT(*) AS n, SUM(CASE WHEN maxopen_time LIKE '14%' THEN 1 ELSE 0 END) AS with_date,
                                     SUM(CASE WHEN CheckDateDay <> 0 THEN 1 ELSE 0 END) AS with_checkdays,
                                     MIN(CheckDateDay) AS min_checkdays, MAX(CheckDateDay) AS max_checkdays,
                                     SUM(CASE WHEN ISNULL(cred,0) > 0 THEN 1 ELSE 0 END) AS with_credit,
                                     SUM(CASE WHEN ISNULL(check_eteb,0) = 1 THEN 1 ELSE 0 END) AS check_credit_on
                              FROM dbo.CUSTOMERS WITH (NOLOCK)""")
    sec("zamanbandi", """SELECT TOP (20) * FROM dbo.zamanbanditasviehfactor WITH (NOLOCK)""")
    sec("sailfact_tasvieh_mix", """SELECT tasvieh, COUNT(*) AS n, SUM(bamandeh) AS sum_left, SUM([all]) AS sum_all
                                   FROM dbo.sailfact WITH (NOLOCK) WHERE active = 't'
                                   GROUP BY tasvieh""")
    sec("sailfact_aging_source", """SELECT COUNT(*) AS invoices, SUM(CASE WHEN ISNULL(bamandeh,0) > 0 THEN 1 ELSE 0 END) AS open_invoices,
                                           MIN(date) AS min_date, MAX(date) AS max_date,
                                           SUM(CASE WHEN ISNULL(bamandeh,0) > 0 THEN bamandeh ELSE 0 END) AS open_total
                                    FROM dbo.sailfact WITH (NOLOCK) WHERE active = 't'""")

    # 9. accounting documents (audit references)
    sec("accounting_docs", """SELECT o.name, SUM(p.rows) AS rows_est FROM sys.objects o
                              LEFT JOIN sys.partitions p ON p.object_id=o.object_id AND p.index_id IN (0,1)
                              WHERE o.name LIKE 'Document%' GROUP BY o.name ORDER BY o.name""")

    # 10. employee/user sales attribution
    sec("sales_by_visitor", """SELECT COUNT(*) AS invoices, SUM(CASE WHEN vis_rdf > 0 THEN 1 ELSE 0 END) AS with_visitor,
                                      COUNT(DISTINCT vis_rdf) AS visitors
                               FROM dbo.sailfact WITH (NOLOCK) WHERE active = 't'""")

    with open(OUT, "w", encoding="utf-8") as fh:
        json.dump(out, fh, ensure_ascii=False, indent=1, default=str)
    print("validation json: %s (%d bytes), errors: %d" % (OUT, os.path.getsize(OUT), len(out["errors"])))
    for e in out["errors"][:20]:
        print("  " + e)
    for k in ["clock", "agree_receipt_total", "agree_customer_balance", "agree_invoice_lines",
              "getchk_by_status", "getchk_due_windows", "pos_join", "cow_summary"]:
        print("%-24s %s" % (k, json.dumps(out["sections"].get(k), ensure_ascii=False)[:400]))


def cli():
    try:
        main()
    except Exception as ex:
        import traceback
        tb = traceback.format_exc()
        print("FATAL: " + tb)
        try:
            with open(OUT, "w", encoding="utf-8") as fh:
                json.dump({"ok": False, "errors": scrub(ex, []), "trace": tb.splitlines()[-8:]},
                          fh, ensure_ascii=False, indent=1)
        except Exception:
            pass
        raise SystemExit(3)


if __name__ == "__main__":
    cli()

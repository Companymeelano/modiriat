# -*- coding: utf-8 -*-
"""Guard: the warehouse module must only use schema verified against the real Atiran DB.

Scans the *string literals* of the warehouse Java sources, builds an alias->table map
from FROM/JOIN dbo.<table> <alias>, then checks every <alias>.<col> and <alias>.[col]
reference against the verified whitelist (docs/warehouse/WAREHOUSE-SCHEMA-MAPPING-fa.md).
Also rejects dbo.<table> names outside the allowed set and any forbidden guessed
identifier (mojodi, minstock, expiry, batch, bin/location, …).

Exit 0 = clean. Non-zero = violation (printed).
"""
import re, sys, os

ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
JAVA = os.path.join(ROOT, 'MEELANO-Android', 'app', 'src', 'main', 'java', 'ir', 'meelano', 'android')
FILES = ['MeelanoWarehouse.java', 'MeelanoWarehouseActivity.java']

VERIFIED = {
 'sailfact': {'shfacfo','rdf__','active','status','uniqueid','all','panevis','rdf_tahbarg','nah_par','moname','vis_rdf','tax','vazn','tafif','sumtafifaghlam','modpar','documentsourceid','taeeduser','date','done_date','shmo'},
 'subsailfact': {'shfacfo','rdf__','rdf_anbar','shka','tedvah','tedjoz','vahprice','jozprice','bastebandi','tedbastebandi','linesum','pertafif','pervis','litakhma','active','naka','ptax','tax','avarez','pavarez','gift','date','done_date','userid','vis_rdf','sysid','tafifaghlam','tafinline','productionseriesid','tedvahmain','tedjozmain','perpromotion','promotionvalue','multipishfactor','tafifpos','tafifnaghd','varietyid','rdf'},
 'subsailtemp': {'shfacfo','rdf__','shka','rdf_anbar','tedvah','tedjoz','active','rdf'},
 'inventory': {'shka','naka','mohvah','bastebandi','mojkavah','mojkajoz'},
 'ka_act': {'shka','tedvah','tedjoz','active','act_id'},
 'anbars': {'rdf_anbar','active','base'},
 'customers': {'shmo','moname','man','rdf_masir'},
 'buyfact': {'shmo'},
 'subbuyfact': set(),
 'meelano_delivery': {'id','shfacfo','rdf__','status','assignee','assignee_name','delivered_at','receiver_name','signature','created_at','updated_at'},
 'meelano_delivery_item': {'id','delivery_id','shka','name','qty','state','reason'},
 'meelano_delivery_log': {'id','delivery_id','action','actor'},
 'meelano_wh_task': {'id','shfacfo','rdf__','shka','requested','picked','state','assignee','assignee_name','reason','started_at','done_at','created_by','created_at','updated_at'},
 'meelano_wh_receive': {'id','buy_shmo','shka','expected','received','diff','state','note','review_state'},
 'meelano_wh_count': {'id','shka','system_qty','actual_qty','diff','blind','state'},
 'meelano_wh_audit': {'id','actor','actor_name','action','ref_table','ref_id','before_val','after_val','note','created_at'},
 'meelano_wh_transfer': {'id','shka','qty','src_anbar','dst_anbar','state','requested_by','confirmed_by','confirmed_at','note','created_at'},
 'meelano_access_users': {'username','display_name','source','source_id','role_key','permissions','enabled','updated_at'},
}
ALLOWED_TABLES = set(VERIFIED) | {'meelano_access_users','meelano_access_roles','meelano_chat_settings','sys_users','visitors'}
# Verified scalar functions / stored procedures (not tables).
FUNCTIONS = {'udf_gregorian_to_persian','returndateserver','isaccountingsystemstarted','addinvoice',
             'updatemojodiinventory','updatemojodiinventoryanbars','factorconfirmation','sp_getapplock'}
FORBIDDEN = ['mojodi','minstock','minimum','hadeaghal','expiry','expire','batch','lot','location','bin','shelf','rack','aisle','zone','vw_customer','checktypes','shfacb']

def literals(src):
    return re.findall(r'"((?:[^"\\]|\\.)*?)"', src)

def main():
    errs = []
    for fn in FILES:
        path = os.path.join(JAVA, fn)
        src = open(path, encoding='utf-8', errors='replace').read()
        for lit in literals(src):
            low = lit.lower()
            for f in FORBIDDEN:
                if re.search(r'\b' + f + r'\b', low):
                    errs.append('%s: forbidden/guessed identifier "%s" in SQL literal' % (fn, f))
            # table names
            for m in re.finditer(r'dbo\.(\w+)', lit):
                t = m.group(1).lower()
                if t not in ALLOWED_TABLES and t not in FUNCTIONS:
                    errs.append('%s: table dbo.%s not in allowed set' % (fn, t))
            # alias map
            alias = {}
            for m in re.finditer(r'(?:FROM|JOIN)\s+dbo\.(\w+)(?:\s+(?:AS\s+)?([a-zA-Z]\w?))?', lit):
                t = m.group(1).lower(); a = m.group(2)
                if a: alias[a.lower()] = t
            # column refs
            for m in re.finditer(r'([a-zA-Z]\w?)\.(\[?\w+\]?)', lit):
                a = m.group(1).lower(); col = m.group(2).strip('[]').lower()
                t = alias.get(a)
                if not t: continue
                if t not in VERIFIED: continue
                if col and col not in VERIFIED[t]:
                    errs.append('%s: column %s.%s NOT verified (table %s)' % (fn, a, col, t))
    if errs:
        print("SCHEMA GUARD: %d violation(s)" % len(errs))
        for e in sorted(set(errs)): print("  -", e)
        return 1
    print("SCHEMA GUARD: OK — warehouse SQL uses only verified Atiran tables/columns.")
    return 0

if __name__ == '__main__':
    sys.exit(main())

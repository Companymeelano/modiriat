# Finance build diagnostics (51730349a6e85e51d73ab69b3440bc4c7be5fc7e)

## Manifest merger errors
```
```

## Task failure block
```
  reason: actual and formal argument lists differ in length
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
45 errors

> Task :app:compileFinanceDebugJavaWithJavac FAILED
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/__run-1791066744410.json
[Incubating] Problems report is available at: file:///home/runner/work/modiriat/modiriat/MEELANO-Android/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileFinanceDebugJavaWithJavac'.
> Compilation failed; see the compiler output below.
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinQueries.java:3: error: MeelanoJalali is not public in ir.meelano.android; cannot be accessed from outside package
  import ir.meelano.android.MeelanoJalali;
                           ^
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinQueries.java:106: error: MeelanoJalali is not public in ir.meelano.android; cannot be accessed from outside package
          String monthStart = MeelanoJalali.monthStart(today);
                              ^
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinQueries.java:186: error: MeelanoJalali is not public in ir.meelano.android; cannot be accessed from outside package
          int mismatched = mismatchCount(c, MeelanoJalali.monthStart(today), today);
                                            ^
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinQueries.java:741: error: MeelanoJalali is not public in ir.meelano.android; cannot be accessed from outside package
          o.put("inMonth", sum(c, "SELECT ISNULL(SUM(BED),0) AS v" + live + " AND DATE>=? AND DATE<=?", MeelanoJalali.monthStart(today), today));
                                                                                                        ^
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinQueries.java:742: error: MeelanoJalali is not public in ir.meelano.android; cannot be accessed from outside package
```

## Java errors
```
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinDb.java:496: error: method text(Connection,String,Object...) is already defined in class FinDb
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinQueries.java:3: error: MeelanoJalali is not public in ir.meelano.android; cannot be accessed from outside package
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinQueries.java:15: error: cannot find symbol
  symbol:   static text
  location: class FinDb
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/AtiranFinanceActivity.java:309: error: cannot find symbol
  symbol:   method fa(serverToda[...]Today)
  location: class AtiranFinanceActivity
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinQueries.java:85: error: cannot find symbol
  symbol:   method text(Connection,String)
  location: class FinQueries
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinQueries.java:106: error: MeelanoJalali is not public in ir.meelano.android; cannot be accessed from outside package
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinQueries.java:186: error: MeelanoJalali is not public in ir.meelano.android; cannot be accessed from outside package
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinQueries.java:741: error: MeelanoJalali is not public in ir.meelano.android; cannot be accessed from outside package
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinQueries.java:742: error: MeelanoJalali is not public in ir.meelano.android; cannot be accessed from outside package
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinQueries.java:745: error: cannot find symbol
  symbol:   method text(Connection,String)
  location: class FinQueries
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinQueries.java:746: error: cannot find symbol
  symbol:   method text(Connection,String)
  location: class FinQueries
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:58: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:58: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:60: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:63: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:64: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:72: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:74: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:78: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:80: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:81: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:85: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:87: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:112: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:114: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:116: error: cannot find symbol
  symbol:   variable MeelanoJalali
  location: class FinFmt
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCustomer.java:79: error: method kv in class FinScreen cannot be applied to given types;
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCustomer.java:80: error: method kv in class FinScreen cannot be applied to given types;
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCustomer.java:81: error: method kv in class FinScreen cannot be applied to given types;
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCustomer.java:82: error: method kv in class FinScreen cannot be applied to given types;
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCustomer.java:83: error: method kv in class FinScreen cannot be applied to given types;
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCustomer.java:84: error: method kv in class FinScreen cannot be applied to given types;
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCustomer.java:87: error: method kv in class FinScreen cannot be applied to given types;
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCustomer.java:88: error: method kv in class FinScreen cannot be applied to given types;
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCustomer.java:89: error: method kv in class FinScreen cannot be applied to given types;
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCustomer.java:91: error: method kv in class FinScreen cannot be applied to given types;
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenHome.java:68: error: MeelanoJalali is not public in ir.meelano.android; cannot be accessed from outside package
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenHome.java:252: error: cannot find symbol
  symbol:   class TextView
  location: class FinScreenHome
```

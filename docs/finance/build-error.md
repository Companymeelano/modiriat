# Finance build diagnostics (67fa1e0dc376034d2e89c1543ff9326dbef2af5a)

## Manifest merger errors
```
```

## Task failure block
```
  location: class FinFmt
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
19 errors

> Task :app:compileFinanceDebugJavaWithJavac FAILED
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/__run-1791066889003.json
[Incubating] Problems report is available at: file:///home/runner/work/modiriat/modiriat/MEELANO-Android/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileFinanceDebugJavaWithJavac'.
> Compilation failed; see the compiler output below.
  Note: Recompile with -Xlint:deprecation for details.
  Note: Some input files use or override a deprecated API.
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:58: error: cannot find symbol
      public static String todayLocal() { return MeelanoJalali.format(MeelanoJalali.today()); }
                                                 ^
    symbol:   variable MeelanoJalali
    location: class FinFmt
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:58: error: cannot find symbol
      public static String todayLocal() { return MeelanoJalali.format(MeelanoJalali.today()); }
                                                                      ^
    symbol:   variable MeelanoJalali
    location: class FinFmt
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinFmt.java:60: error: cannot find symbol
```

## Java errors
```
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinDb.java:108: error: unreported exception JSONException; must be caught or declared to be thrown
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinDb.java:109: error: unreported exception JSONException; must be caught or declared to be thrown
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinDb.java:168: error: unreported exception JSONException; must be caught or declared to be thrown
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinDb.java:465: error: unreported exception JSONException; must be caught or declared to be thrown
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
```

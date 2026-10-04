# Finance build diagnostics (2aa6ed373bb91573d47120cb2be4d3262fe914d7)

## Manifest merger errors
```
```

## Task failure block
```
  attempting to assign weaker access privileges; was protected
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
7 errors

> Task :app:compileFinanceDebugJavaWithJavac FAILED
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/__run-1791072603658.json
[Incubating] Problems report is available at: file:///home/runner/work/modiriat/modiriat/MEELANO-Android/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileFinanceDebugJavaWithJavac'.
> Compilation failed; see the compiler output below.
  Note: Recompile with -Xlint:deprecation for details.
  Note: Some input files use or override a deprecated API.
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBank.java:59: error: cannot find symbol
          if (daily.length >= 2) {
                   ^
    symbol:   variable length
    location: variable daily of type JSONArray
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBank.java:61: error: cannot find symbol
              int n = Math.min(30, daily.length);
                                        ^
    symbol:   variable length
    location: variable daily of type JSONArray
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBanks.java:149: error: cannot find symbol
```

## Java errors
```
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBank.java:59: error: cannot find symbol
  symbol:   variable length
  location: variable daily of type JSONArray
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBank.java:61: error: cannot find symbol
  symbol:   variable length
  location: variable daily of type JSONArray
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBanks.java:149: error: cannot find symbol
  symbol:   variable length
  location: variable daily of type JSONArray
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBanks.java:151: error: cannot find symbol
  symbol:   variable length
  location: variable daily of type JSONArray
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenPos.java:176: error: cannot find symbol
  symbol:   variable length
  location: variable byDay of type JSONArray
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenPos.java:178: error: cannot find symbol
  symbol:   variable length
  location: variable byDay of type JSONArray
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenSales.java:256: error: top(int) in FinScreenSales cannot override top(int) in FinScreen
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBank.java:59: error: cannot find symbol
    symbol:   variable length
    location: variable daily of type JSONArray
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBank.java:61: error: cannot find symbol
    symbol:   variable length
    location: variable daily of type JSONArray
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBanks.java:149: error: cannot find symbol
    symbol:   variable length
    location: variable daily of type JSONArray
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBanks.java:151: error: cannot find symbol
    symbol:   variable length
    location: variable daily of type JSONArray
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenPos.java:176: error: cannot find symbol
    symbol:   variable length
    location: variable byDay of type JSONArray
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenPos.java:178: error: cannot find symbol
    symbol:   variable length
    location: variable byDay of type JSONArray
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenSales.java:256: error: top(int) in FinScreenSales cannot override top(int) in FinScreen
```

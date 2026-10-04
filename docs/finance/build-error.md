# Finance build diagnostics (cd445ce5aa9daad1dee1b15fe0285fad091e6df9)

## Manifest merger errors
```
```

## Task failure block
```
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
Note: Some messages have been simplified; recompile with -Xdiags:verbose to get full output
4 errors

> Task :app:compileFinanceDebugJavaWithJavac FAILED
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/__run-1791076899705.json
[Incubating] Problems report is available at: file:///home/runner/work/modiriat/modiriat/MEELANO-Android/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileFinanceDebugJavaWithJavac'.
> Compilation failed; see the compiler output below.
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinDb.java:167: error: incompatible types: Throwable cannot be converted to Exception
              lastError = safeMessage(e);
                                      ^
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinDb.java:292: error: incompatible types: Throwable cannot be converted to Exception
                  lastError = safeMessage(e);
                                          ^
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinDb.java:489: error: incompatible types: Throwable cannot be converted to Exception
                  lastError = safeMessage(e);
                                          ^
  Note: Recompile with -Xlint:deprecation for details.
  Note: Some input files use or override a deprecated API.
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenHome.java:120: error: cannot find symbol
              card.addView(ui.text(FinDiag.shortError(t), 11f, ui.textDim, false), ui.lp(-1, -2));
```

## Java errors
```
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinDb.java:167: error: incompatible types: Throwable cannot be converted to Exception
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinDb.java:292: error: incompatible types: Throwable cannot be converted to Exception
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinDb.java:489: error: incompatible types: Throwable cannot be converted to Exception
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenHome.java:120: error: cannot find symbol
  symbol:   method shortError(Throwable)
  location: class FinDiag
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinDb.java:167: error: incompatible types: Throwable cannot be converted to Exception
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinDb.java:292: error: incompatible types: Throwable cannot be converted to Exception
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinDb.java:489: error: incompatible types: Throwable cannot be converted to Exception
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenHome.java:120: error: cannot find symbol
    symbol:   method shortError(Throwable)
    location: class FinDiag
```

# Finance build diagnostics (82b6f4e99fcebf84451b3bbf742150ce9662c441)

## Manifest merger errors
```
```

## Task failure block
```
                                   ^
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
1 error

> Task :app:compileFinanceDebugJavaWithJavac FAILED
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/__run-1791080081289.json
[Incubating] Problems report is available at: file:///home/runner/work/modiriat/modiriat/MEELANO-Android/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileFinanceDebugJavaWithJavac'.
> Compilation failed; see the compiler output below.
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/AtiranFinanceActivity.java:1051: error: incompatible types: LinearLayout cannot be converted to TextView
          TextView dataChip = ui.chip(dataStatusLabel(), ui.textDim);
                                     ^
  Note: Recompile with -Xlint:deprecation for details.
  Note: Some input files use or override a deprecated API.
  1 error

* Try:
> Check your code and dependencies to fix the compilation error(s)
> Run with --scan to get full insights.

BUILD FAILED in 18s
35 actionable tasks: 35 executed
```

## Java errors
```
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/AtiranFinanceActivity.java:1051: error: incompatible types: LinearLayout cannot be converted to TextView
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/AtiranFinanceActivity.java:1051: error: incompatible types: LinearLayout cannot be converted to TextView
```

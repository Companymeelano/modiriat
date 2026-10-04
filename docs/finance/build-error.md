# Finance build diagnostics (0f0b9eda7aee05e9cd7889423274d0401db2a959)

## Manifest merger errors
```
```

## Task failure block
```
                              ^
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
23 errors

> Task :app:compileFinanceDebugJavaWithJavac FAILED
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/__run-1791078958843.json
[Incubating] Problems report is available at: file:///home/runner/work/modiriat/modiriat/MEELANO-Android/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileFinanceDebugJavaWithJavac'.
> Compilation failed; see the compiler output below.
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenDaily.java:104: error: incompatible types: JSONArray cannot be converted to JSONObject
          JSONObject p = payload(env);
                                ^
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBanks.java:63: error: incompatible types: JSONArray cannot be converted to JSONObject
          JSONObject p = payload(env);
                                ^
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenHome.java:94: error: incompatible types: JSONArray cannot be converted to JSONObject
          JSONObject p = payload(env);
                                ^
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenMore.java:58: error: incompatible types: JSONArray cannot be converted to JSONObject
          JSONObject p = payload(env);
                                ^
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenReceivables.java:71: error: incompatible types: JSONArray cannot be converted to JSONObject
```

## Java errors
```
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBank.java:46: error: cannot find symbol
  symbol:   class Connection
  location: class FinScreenBank
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenRecon.java:46: error: cannot find symbol
  symbol:   class Connection
  location: class FinScreenRecon
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenDaily.java:43: error: cannot find symbol
  symbol:   class Connection
  location: class FinScreenDaily
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCheques.java:91: error: cannot find symbol
  symbol:   class Connection
  location: class FinScreenCheques
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCash.java:53: error: cannot find symbol
  symbol:   class Connection
  location: class FinScreenCash
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBanks.java:40: error: cannot find symbol
  symbol:   class Connection
  location: class FinScreenBanks
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCustomer.java:42: error: cannot find symbol
  symbol:   class Connection
  location: class FinScreenCustomer
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenHome.java:63: error: cannot find symbol
  symbol:   class Connection
  location: class FinScreenHome
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenPos.java:44: error: cannot find symbol
  symbol:   class Connection
  location: class FinScreenPos
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenProblems.java:36: error: cannot find symbol
  symbol:   class Connection
  location: class FinScreenProblems
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenMore.java:43: error: cannot find symbol
  symbol:   class Connection
  location: class FinScreenMore
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenReceivables.java:51: error: cannot find symbol
  symbol:   class Connection
  location: class FinScreenReceivables
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenSales.java:59: error: cannot find symbol
  symbol:   class Connection
  location: class FinScreenSales
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenDaily.java:43: error: payload(Connection) in FinScreenDaily cannot hide payload(JSONObject) in FinScreen
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenDaily.java:104: error: incompatible types: JSONArray cannot be converted to JSONObject
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBanks.java:40: error: payload(Connection) in FinScreenBanks cannot hide payload(JSONObject) in FinScreen
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBanks.java:63: error: incompatible types: JSONArray cannot be converted to JSONObject
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenHome.java:63: error: payload(Connection) in FinScreenHome cannot hide payload(JSONObject) in FinScreen
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenHome.java:94: error: incompatible types: JSONArray cannot be converted to JSONObject
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenMore.java:43: error: payload(Connection) in FinScreenMore cannot hide payload(JSONObject) in FinScreen
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenMore.java:58: error: incompatible types: JSONArray cannot be converted to JSONObject
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenReceivables.java:51: error: payload(Connection) in FinScreenReceivables cannot hide payload(JSONObject) in FinScreen
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenReceivables.java:71: error: incompatible types: JSONArray cannot be converted to JSONObject
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenDaily.java:104: error: incompatible types: JSONArray cannot be converted to JSONObject
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBanks.java:63: error: incompatible types: JSONArray cannot be converted to JSONObject
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenHome.java:94: error: incompatible types: JSONArray cannot be converted to JSONObject
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenMore.java:58: error: incompatible types: JSONArray cannot be converted to JSONObject
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenReceivables.java:71: error: incompatible types: JSONArray cannot be converted to JSONObject
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenDaily.java:43: error: payload(Connection) in FinScreenDaily cannot hide payload(JSONObject) in FinScreen
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBanks.java:40: error: payload(Connection) in FinScreenBanks cannot hide payload(JSONObject) in FinScreen
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenHome.java:63: error: payload(Connection) in FinScreenHome cannot hide payload(JSONObject) in FinScreen
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenMore.java:43: error: payload(Connection) in FinScreenMore cannot hide payload(JSONObject) in FinScreen
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenReceivables.java:51: error: payload(Connection) in FinScreenReceivables cannot hide payload(JSONObject) in FinScreen
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBank.java:46: error: cannot find symbol
    symbol:   class Connection
    location: class FinScreenBank
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenRecon.java:46: error: cannot find symbol
    symbol:   class Connection
    location: class FinScreenRecon
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenDaily.java:43: error: cannot find symbol
    symbol:   class Connection
    location: class FinScreenDaily
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCheques.java:91: error: cannot find symbol
    symbol:   class Connection
    location: class FinScreenCheques
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCash.java:53: error: cannot find symbol
    symbol:   class Connection
    location: class FinScreenCash
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenBanks.java:40: error: cannot find symbol
    symbol:   class Connection
    location: class FinScreenBanks
  /home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/java/ir/meelano/android/finance/FinScreenCustomer.java:42: error: cannot find symbol
    symbol:   class Connection
    location: class FinScreenCustomer
```

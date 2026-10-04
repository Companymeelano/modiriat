# Finance unit test output

```
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.11.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :app:preBuild UP-TO-DATE
> Task :app:preFinanceDebugBuild UP-TO-DATE
> Task :app:javaPreCompileFinanceDebug UP-TO-DATE
> Task :app:checkFinanceDebugAarMetadata UP-TO-DATE
> Task :app:generateFinanceDebugResValues UP-TO-DATE
> Task :app:mapFinanceDebugSourceSetPaths UP-TO-DATE
> Task :app:generateFinanceDebugResources UP-TO-DATE
> Task :app:mergeFinanceDebugResources UP-TO-DATE
> Task :app:packageFinanceDebugResources UP-TO-DATE
> Task :app:parseFinanceDebugLocalResources UP-TO-DATE
> Task :app:createFinanceDebugCompatibleScreenManifests UP-TO-DATE
> Task :app:extractDeepLinksFinanceDebug UP-TO-DATE
> Task :app:processFinanceDebugMainManifest UP-TO-DATE
> Task :app:processFinanceDebugManifest UP-TO-DATE
> Task :app:processFinanceDebugManifestForPackage UP-TO-DATE
> Task :app:processFinanceDebugResources UP-TO-DATE
> Task :app:compileFinanceDebugJavaWithJavac UP-TO-DATE
> Task :app:preFinanceDebugUnitTestBuild UP-TO-DATE
> Task :app:processFinanceDebugJavaRes NO-SOURCE
> Task :app:javaPreCompileFinanceDebugUnitTest
> Task :app:bundleFinanceDebugClassesToCompileJar
> Task :app:processFinanceDebugUnitTestJavaRes
> Task :app:bundleFinanceDebugClassesToRuntimeJar
> Task :app:compileFinanceDebugUnitTestJavaWithJavac

> Task :app:testFinanceDebugUnitTest FAILED

FinChartsTest > csvEscapesQuotes FAILED
    org.junit.ComparisonFailure at FinChartsTest.java:97

FinChartsTest > chartColoursMixPredictably FAILED
    java.lang.RuntimeException at FinChartsTest.java:82

37 tests completed, 2 failed
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/finTests-1791073524994.json

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:testFinanceDebugUnitTest'.
> There were failing tests. See the report at: file:///home/runner/work/modiriat/modiriat/MEELANO-Android/app/build/reports/tests/testFinanceDebugUnitTest/index.html

* Try:
> Run with --scan to get full insights.

BUILD FAILED in 9s
21 actionable tasks: 6 executed, 15 up-to-date
```

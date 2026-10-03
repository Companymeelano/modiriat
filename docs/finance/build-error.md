# Finance build diagnostics (3500cf601ff36beb532e807c32b30afdb0ab1972)

## Manifest merger errors
```

> Task :app:processFinanceDebugMainManifest FAILED
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/AndroidManifest.xml:19:9-43 Error:
	Attribute application@icon value=(@mipmap/ic_fin_launcher) from AndroidManifest.xml:19:9-43
	is also present at AndroidManifest.xml:19:9-43 value=(@mipmap/ic_launcher).
	Suggestion: add 'tools:replace="android:icon"' to <application> element at AndroidManifest.xml:25:5-54:19 to override.
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/AndroidManifest.xml:21:9-41 Error:
	Attribute application@label value=(@string/fin_app_name) from AndroidManifest.xml:21:9-41
	is also present at AndroidManifest.xml:21:9-41 value=(@string/app_name).
	Suggestion: add 'tools:replace="android:label"' to <application> element at AndroidManifest.xml:25:5-54:19 to override.
/home/runner/work/modiriat/modiriat/MEELANO-Android/app/src/finance/AndroidManifest.xml:20:9-54 Error:
	Attribute application@roundIcon value=(@mipmap/ic_fin_launcher_round) from AndroidManifest.xml:20:9-54
	is also present at AndroidManifest.xml:20:9-54 value=(@mipmap/ic_launcher_round).
	Suggestion: add 'tools:replace="android:roundIcon"' to <application> element at AndroidManifest.xml:25:5-54:19 to override.

See https://developer.android.com/r/studio-ui/build/manifest-merger for more information about the manifest merger.


> Task :app:parseFinanceDebugLocalResources
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/__run-1791066663502.json

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:processFinanceDebugMainManifest'.
> Manifest merger failed with multiple errors, see logs

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

BUILD FAILED in 33s
11 actionable tasks: 11 executed
```

## Task failure block
```
	is also present at AndroidManifest.xml:20:9-54 value=(@mipmap/ic_launcher_round).
	Suggestion: add 'tools:replace="android:roundIcon"' to <application> element at AndroidManifest.xml:25:5-54:19 to override.

See https://developer.android.com/r/studio-ui/build/manifest-merger for more information about the manifest merger.


> Task :app:parseFinanceDebugLocalResources
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/__run-1791066663502.json

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:processFinanceDebugMainManifest'.
> Manifest merger failed with multiple errors, see logs

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

BUILD FAILED in 33s
11 actionable tasks: 11 executed
```

## Java errors
```
```

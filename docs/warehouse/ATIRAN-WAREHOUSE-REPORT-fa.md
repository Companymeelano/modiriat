# گزارش مهندسی «آتیران انبار» — Warehouse & Dispatch Management

> نسخهٔ سند: 1.0.0 — تاریخ: ۱۴۰۵/۰۷/۱۱ (2026-10-03) — کاربر اصلی: **اسما حمدانی**
> مخزن: `Companymeelano/modiriat` شاخهٔ `arena/01a10295-modiriat`
> این گزارش هم «بررسی واقعی پروژه» است و هم «طرح اجرایی»؛ هر ادعا منبع ابزار دارد.

---

## ۰) خلاصهٔ مدیریتی

- پروژهٔ موجود یک اپ **جاوا (Java) خالص** است، نه کاتلین/Compose. `android.useAndroidX=false` یعنی هیچ AndroidX، و در نتیجه **نه Jetpack Compose، نه ViewModel/Room/Coroutines**. پرامپتِ «Kotlin + Compose + MVVM» با واقعیت پروژه ناسازگار است؛ طبق بند ۷ خود پرامپت («اگر پروژه معماری دیگری دارد، بدون تخریب Integrate کن») باید معماری جاوا/View فعلی ملاک باشد.
- `MainActivity.java` یک تک‌فایل **۲,۰۸۹,۱۴۹ بایت / ۲۶,۹۹۴ خط** است (مونولیت). قابلیت‌های درخواستیِ زیادی همین حالا وجود دارند: تحویل/دیسپچ با امضا، کنترل دسترسی نقش‌محور، تم‌های رانتایم (۱۳+)، PDF روزانه، جستجو، نمودار، تاریخ جلالی.
- **اسما حمدانی کاربر واقعی است**: login=`asma`، vis=4، uid=4، حساب پرسنل shmo=2701 (MainActivity:385 و 22702). او همین حالا در ماژول تحویل به‌عنوان تحویل‌دهنده حضور دارد.
- **موجودی ستون نیست**؛ از دفتر گردش `ka_act` × `mohvah` + لایهٔ تطبیقیِ کشف ستون در رانتایم (`sys.columns`) محاسبه می‌شود.
- **ساخت APK در این sandbox غیرممکن است** (بند ۱۳۹ پرامپت): نه JDK، نه Gradle، نه Android SDK؛ فقط `github.com` در دسترس است و Maven Central / Google Maven / Gradle dist / plugins.gradle.org همگی مسدودند (HTTP 000)؛ `apt-get` هم بدون دسترسی root رد می‌شود؛ و حتی `gradle/wrapper/*.jar` در مخزن نیست. پس APK واقعی این‌جا ساخته/نصب/تست **نمی‌شود** و من وانمود نمی‌کنم.
- دادهٔ نمایشی: لایهٔ «پیش‌نمایش طراحی» (`designPreview`) وجود دارد اما با `isDebuggableBuild()` مهار شده و در آن همهٔ اتصال‌های DB رد می‌شود؛ بنابراین قانون «NO MOCK در Production» توسط پروژهٔ فعلی رعایت شده است.

## ۱) اصلاح فرض‌های پرامپت (چه چیزی درست نبود)

| فرض پرامپت | واقعیت مشاهده‌شده | منبع |
|---|---|---|
| Kotlin / Jetpack Compose / MVVM / Room | جاوا خالص، `useAndroidX=false`، View سنتی | `gradle.properties`, `app/build.gradle` |
| «چهار Theme مطابق سیستم فعلی» = XML | یک `AppTheme` در XML + موتور تم رانتایم با ۱۳+ شناسه (onyx_gold, hazelnut_gold, noir_aurora, …) | `styles.xml`, `MainActivity.java:1235-2310` |
| Scanner موجود | هیچ مجوز CAMERA و هیچ ZXing/ML Kit نیست؛ فقط دوربین برای عکس کالا | `AndroidManifest.xml` |
| Credentialها hard-code نیستند | رمز SQL به‌صورت آرایهٔ int مبهم در `MainActivity:211-215` و رمز keystore در `app/build.gradle` hard-code شده | همان‌ها |
| Compose/ViewModel | غیرممکن بدون مهاجرت AndroidX | `gradle.properties` |
| جدول `mojodi`/انقضا/مکان | وجود ندارد (Unknown) | کاوش بکاپ + SQL تولید |

## ۲) وضعیت ساخت (Build) — مسدود

دستور و نتیجهٔ واقعی:
```
java -version          → command not found
which gradle sdkmanager → not found
apt-get update         → E: ... Permission denied (13)
curl https://repo1.maven.org/maven2/            → 000 (SSL_ERROR_SYSCALL)
curl https://dl.google.com/dl/android/maven2/   → 000
curl https://services.gradle.org/distributions/ → 000
curl https://plugins.gradle.org/m2/             → 000
curl https://api.github.com                     → 200
ls MEELANO-Android/gradle/wrapper → No such file or directory
```
نتیجه: **Build blocked because** — هیچ JDK/Gradle/SDK نیست، مخازن وابستگی (Maven Central/Google/Gradle) از شبکهٔ sandbox قابل دسترس نیستند، و wrapper jar هم در مخزن موجود نیست. طبق بند ۱۳۹، APK واقعی فقط روی یک ماشین با JDK 17 + Android SDK 36 + دسترسی به مخازن ساخته می‌شود (دستورالعمل در §۹).

## ۳) نگاشت اسکیمای واقعی

سند جداگانه: `docs/warehouse/WAREHOUSE-SCHEMA-MAPPING-fa.md`.
نکات کلیدی:
- فروش: `sailfact`(PK shfacfo) ← `subsailfact`(FK shfacfo, shka)؛ جریان ثبت از `AddInvoice` + `subsailtemp` + `InvoiceTrigger` + `UpdateMojodiInventory` + `FactorConfirmation`.
- موجودی: `inventory`(PK shka؛ naka/mohvah/bastebandi) + دفتر `ka_act`(tedvah/tedjoz/active/act_id=20 برای فروش). **ستون mojodi نیست.**
- خرید: `buyfact`/`subbuyfact` فقط «نام جدول» تأیید شده؛ ستون‌ها Unknown تا کاوش متادیتا.
- انبارگردانی: `anbars`(rdf_anbar/Active/Base). ساختار Zone/Aisle/Rack/Bin پیدا نشد → UI مکان جعلی ساخته نمی‌شود.
- انقضا/Batch/Lot معتبر پیدا نشد → بخش‌های Expiry/FEFO/Lot غیرفعال می‌مانند.

### منبع معتبر متادیتا (چون بکاپ را نمی‌توان بدون SQL Server خواند)
کاوش رشته‌ای بکاپ `14050603` «غیبت کاذب» دارد (نام‌هایی که قطعاً هست مثل `UniqueID` را absent نشان داد)؛ پس برای متادیتای معتبر باید همان گردش‌کار CI پروژه اجرا شود:
```
commit با پیام شامل [atiran-e2e] یا اجرای .github/workflows/atiran-bak-probe.yml
خروجی رمزنگارشده در tools/atiran-bak-probe/probe.json.gz.enc
```
یا مستقیم روی کپی بازیابی‌شده:
```sql
SELECT c.name, t.name, c.is_nullable, c.is_identity
FROM sys.columns c JOIN sys.types t ON c.user_type_id=t.user_type_id
WHERE c.object_id=OBJECT_ID(N'dbo.buyfact') ORDER BY c.column_id;
```

## ۴) مدل موجودی (مهم‌ترین کشف فنی)

`MainActivity.java:17062-17098` یک موتور تطبیقی موجودی دارد:
- منبع اصلی: `SUM(ka_act.tedvah*mohvah + ka_act.tedjoz)` با علامت per act_id.
- منبع جایگزین: جدولِ قابل‌تنظیم با `balanceCol` یا `inCol/outCol` یا `qtyCol/directionCol` که ستون‌هایش در رانتایم از `sys.columns` کشف می‌شود (`resolveWarehouseColumn` ده‌ها نام متداول شامل `anbar`, `ShAnbar`, `کد_انبار` را امتحان می‌کند).
- پیامد برای انبار: **هرگز** یک عدد UI را مستقیم ننویسیم؛ هر تغییر موجودی باید از رویه‌های خود آتیران (`UpdateMojodiInventory/Anbars` یا تریگرها) و با Transaction+Permission+Audit عبور کند (بند ۱۴۲).

## ۵) فهرست استفادهٔ مجدد (Reuse) — نساز، وصل کن

| نیاز پرامپت | موجود در پروژه | فایل |
|---|---|---|
| تحویل/دیسپچ/امضا/مختصات | meelano_delivery(+item,log) + MeelanoSignatureView | MeelanoDelivery.java |
| کنترل دسترسی نقش‌محور | meelano_access_users/roles | MainActivity |
| PDF روزانه | MeelanoDailyReportPdf | همان |
| نمودار | MeelanoCharts | همان |
| تاریخ جلالی | MeelanoJalali | همان |
| جستجو با debounce | MeelanoSearch | همان |
| تم‌ها | موتور تم رانتایم | MainActivity:1235+ |
| اتصال jTDS | DriverManager + hidden() | MainActivity:4378+ |
| آیکون‌های مفهومی | MeelanoIcons | همان |

## ۶) معماری پیشنهادی ماژول انبار (بدون تخریب)

- افزودن productFlavor پنجم `warehouse` با applicationId `ir.meelano.atiran.warehouse` (مطابق الگوی فعلی visitor/store/staff/tax) تا جدا نصب شود.
- بستهٔ جدید `ir.meelano.android.warehouse` با زیربسته‌های `data/domain/presentation/scanner/...` (بند ۱۵۷) — ولی پیاده‌سازی با همان سبک جاوا/View فعلی تا کامپایل بدون AndroidX ممکن بماند.
- جدول‌های جدید با پیشوند `meelano_wh_` مطابق الگوی موجود: `meelano_wh_task` (برداشت/کنترل)، `meelano_wh_receive`، `meelano_wh_count`، `meelano_wh_audit`. این‌ها دادهٔ عملیاتی انبار را نگه می‌دارند بدون لمس اسناد حسابداری آتیران (بند ۱۴۳).
- Scanner: چون AndroidX/ML Kit ممکن نیست، از `zxing:core` (وابستگی خالص، بدون AndroidX) + Camera1 برای دیکد استفاده شود؛ در غیر این‌صورت ورود دستی barcode. (نیازمند ساخت واقعی برای تأیید.)

## ۷) جریان کاری (Workflow) منطبق بر دادهٔ واقعی

Purchase(buyfact) → Receiving(meelano_wh_receive + مغایرت بدون اصلاح خودکار موجودی) → Inventory(ka_act فقط از رویه‌های آتیران) → Sales Invoice(sailfact/subsailfact خواندنی) → Picking(meelano_wh_task با اعتبارسنجی barcode/تعداد) → Control → Dispatch(meelano_delivery موجود) → Delivery(امضا/مختصات موجود) → Audit(meelano_wh_audit: who/what/when/before/after).

## ۸) یافته‌های امنیتی (باید رفع شوند)

1. رمز SQL hard-code (آرایه int در `MainActivity:211-215`) → به Android Keystore/سرور منتقل شود.
2. رمز keystore در `app/build.gradle` → به متغیر محیطی/CI منتقل شود (بند ۱۲۳).
3. هشدار قبلی سندشده: مخزن عمومی حاوی دادهٔ مشتری و رمزهاست (`docs/ATIRAN2-E2E-fa.md`) → خصوصی‌سازی/چرخش رمز.
4. همهٔ کوئری‌های جدید باید parameterized باشند (الگوی فعلی `?` رعایت می‌شود).

## ۹) دستورالعمل ساخت روی ماشین واقعی

```
1) JDK 17 + Android SDK (platform 36, build-tools) نصب و دسترسی به mavenCentral/google.
2) cd MEELANO-Android
3) ./gradlew :app:assembleWarehouseDebug    (پس از افزودن flavor)
   فعلاً برای تأیید سلامت: ./gradlew :app:assembleVisitorRelease
4) SHA-256 خروجی‌ها ثبت و در گزارش نهایی درج شود.
```
برای متادیتای معتبر: اجرای `atiran-bak-probe.yml` و بازکردن خروجی رمزنگارشده با کلید CI.

## ۱۰) گزارش تست (آنچه واقعاً اجرا شد)

| آزمون | روش | نتیجه |
|---|---|---|
| استخراج جدول‌ها/ستون‌های واقعی | parse SQL تولید + کاوش بکاپ | ✅ (با ثبت Unknown) |
| صحت shaping/Bidi موتور PDF | unit-check روی سلام/آتیران/فاکتور | ✅ (LAM-ALEF و medial/final درست) |
| ساخت APK | تلاش در sandbox | ❌ مسدود (دلایل در §۲) |
| اتصال DB / اجرای کوئری | – | ❌ بدون SQL Server و شبکه |
| UI/RTL/تم | بازبینی کد (نه اجرا) | ⚠ فقط بررسی ایستا |

## ۱۱) محدودیت‌ها و موارد باز

- APK ساخته/نصب/تست نشده (فقط به‌دلیل محیط).
- ستون‌های buyfact/subbuyfact، انقضا، مکان، حداقل موجودی = Unknown تا کاوش متادیتا.
- مونولیت ۲MB نیازمند بازسازی تدریجی است؛ این جلسه فقط ماژول‌بندی را طرح کرد، نه refactor کامل.

## ۱۲) نسخه و اثرانگشت

- Version Name 1.0.0 / Version Code 1 برای نسخهٔ انبار (پیشنهادی).
- فایل‌های تحویل این جلسه: `docs/warehouse/*.md`, `tools/warehouse/persian_pdf.py`, `tools/warehouse/build_report.py`, `docs/warehouse/ATIRAN-WAREHOUSE-REPORT-fa.pdf`, `tools/icon/atiran-warehouse-icon-1024.png`.
- SHA-256 هر فایل در خروجی `build_report.py` درج می‌شود.

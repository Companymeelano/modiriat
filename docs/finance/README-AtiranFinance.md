# «آتیران مالی» — Financial Operations for Atiran

نسخه تخصصی مالی روی همان کد و همان دیتابیس آتیران، به‌صورت یک اپ مستقل قابل نصب.

| مورد | مقدار |
|---|---|
| بسته (applicationId) | `ir.meelano.atiran.finance` |
| flavor | `finance` (نسخه‌های visitor/store/staff/tax دست‌نخورده) |
| Activity | `ir.meelano.android.finance.AtiranFinanceActivity` (تنها Launcher) |
| نسخه | `versionCode 5`, `versionName 1.2.1` (صفحهٔ بررسی سلامت + رابط بدون بسته‌شدن ناگهانی) |
| دیتابیس | همان SQL Server آتیران (jtds، بدون هیچ رمزی در سورس/APK/لاگ) |
| نوشتن | فقط `dbo.meelano_fin_*` — جدول‌های اصلی فقط خوانده می‌شوند |

## ساخت

```bash
cd MEELANO-Android
gradle --no-daemon assembleFinanceDebug assembleFinanceRelease \
  -Pandroid.injected.signing.store.file="$PWD/app/signing/meelano-install.p12" \
  -Pandroid.injected.signing.store.password=derakhshan-install \
  -Pandroid.injected.signing.key.alias=meelano \
  -Pandroid.injected.signing.key.password=derakhshan-install
```

CI: ورک‌فلوی `.github/workflows/atiran-finance-build.yml` با پیام کامیت شامل `[fin-build]` (یا
`workflow_dispatch`) هر دو APK را می‌سازد، امضا را با `apksigner verify` بررسی می‌کند، `sha256sum`
را در لاگ می‌گذارد و فایل‌ها را در `apk-finance/` روی همین برنچ کامیت می‌کند:

```
apk-finance/AtiranFinance-debug.apk
apk-finance/AtiranFinance-release.apk
```

خروجی همیشه شامل SHA-256 است که CI چاپ می‌کند (گزارش نهایی آن را بازتاب می‌دهد).

## ورود و نقش

* مسیر ورود واقعی است: `FinAuth.authenticate` روی `visitors` (و در نبود آن `sys_users`) با مقایسه
  رمز به چند شکل (متن ساده، بایت خام، MD5/SHA-1/SHA-256 در چند کدگذاری) — پشتیبانی از داده قدیمی.
* قفل پس از ۵ تلاش ناموفق (۵ دقیقه) و شمارش تلاش‌ها روی دستگاه.
* **UserName معیار نقش نیست.** نقش از جدول‌های دسترسی موجود (`meelano_access_users` /
  `meelano_access_roles`) خوانده می‌شود؛ اگر ردیفی نباشد، نقش پیش‌فرض همان کاربر (read-only/حسابدار)
  اعمال و در هدر صریحاً «نقش از جدول دسترسی خوانده نشد» نمایش داده می‌شود.
* رمز الهام فقط در تست CI قابل تنظیم است و هیچ‌جا در سورس، Git، APK یا لاگ نیست.

## مجوزها (Permission Keys)

`finance_dashboard, finance_sales, finance_banks, finance_receivables, finance_checks, finance_pos,
finance_cash, finance_settlement, finance_reconcile, finance_reports, finance_audit, finance_settings,
finance_create, finance_confirm, finance_approve, finance_adjust, finance_export, finance_print,
finance_daily_close`

هر عملیات نویسنده: Transaction + Audit + Idempotency + Permission (`FinDb.runGuarded`).

## ماژول‌ها (فازبندی تحویل)

| فاز | محتوا | وضعیت |
|---|---|---|
| ۱ | زیرساخت: محیط، اتصال، جداول اختصاصی، نشست، UI kit، تم‌ها، ورود | ✅ |
| ۲ | خانه (۸ KPI + Drill-down)، فروش/وصول، مرکز چک، مطالبات + سنی‌بندی + پیگیری، پرونده مشتری، بانک‌ها/جزئیات، POS، صندوق، تسویه کاربران، مغایرت بانکی، گزارش و بستن روز، کارهای باز، Audit، خودآزمون فنی | ✅ |
| ۳ | نمودارهای بومی و بازطراحی رابط (نسخه ۱.۱.۰) | ✅ |
| ۴ | PDF/CSV، دسته‌چک، هشدار زمان‌بندی‌شده، پیشنهاد AI (فقط پیشنهاد با Approval)، Sync کامل با حل تعارض | ⏳ |

## نمودارها

موتور نمودار بومی و بدون کتابخانه بیرونی (`FinCharts`) با شش نوع نمودار: Area، Columns، Bars،
Donut، Ring و Spark. همه راست‌چین، با یک انیمیشن ورودی، انتخاب نقطه با لمس، اعداد قابل خواندن برای
TalkBack و رنگ‌های هماهنگ با هر چهار پوسته. جزئیات در `CHANGES-v1.1.0-fa.md`.

## تم‌ها

Midnight Finance (پیش‌فرض) · Obsidian Gold · Platinum Finance (روشن) · Executive Finance.
رنگ‌های معنایی در چهار تم یکسان می‌مانند.

## تست پذیرش (Acceptance)

1. نصب APK روی دستگاه با اینترنت (VPN در صورت نیاز).
2. ورود با کاربر واقعی؛ هدر باید نقش واقعی و تاریخ سرور را نشان دهد (تاریخ از `ReturnDateServer`).
3. خانه: هشت KPI باید با_query های `docs/finance/DataSourceRegistry.md` بخوانند.
4. صفحه آزمون فنی (`FinSelfTestActivity`, debug فقط) باید همه بررسی‌ها را ✓ کند، از جمله
   تطبیق `CUSTOMERS.man` با دفتر `cust_act` و پنجره‌های سررسید چک.
5. بستن روز: پس از ثبت، ردیفی در `meelano_fin_dayclose` با snapshot همان اعداد ساخته می‌شود.
6. خروج و ورود دوباره: نشست پاک، رمز ذخیره نمی‌شود.

## محدودیت‌های شناخته‌شده

* فقط خواندن از جدول‌های اصلی؛ هیچ اصلاح/ابطال سند مالی از اپ انجام نمی‌شود (طبق تصمیم پروژه).
* شبا، چک الکترونیک و انتساب اپراتور روی چک: **UNKNOWN** در داده واقعی ⇒ در UI نمایش داده نمی‌شود.
* پیام خطای SQL هرگز در UI نیست؛ فقط «کد رویداد» ۶ رقمی برای پیگیری.
* Aging بالای ۳۰ روز در داده فعلی خالی است (پنجره فاکتورهای باز ≈ ۱ ماه).
* PDF/CSV و دسته‌چک در فاز ۳.

## صفحهٔ شروع (۱.۲.۰)

برنامه با «بررسی سلامت و بارگذاری» بالا می‌آید: اینترنت گوشی، DNS، پورت ۱۴۳۳، ورود به SQL، تاریخ سرور و
نمونه‌خوانی جدول‌های مالی — هر خط با زمان خودش و درصد پیشرفت. خطای هر گام با متن دقیق خطا روی همان خط
می‌آید، و دکمه‌های «تلاش دوباره» / «بررسی اتصال به سرور» / «ورود با فرم ساده» ظاهر می‌شوند.

## عیب‌یابی روی گوشی (۱.۱.۱+)

* صفحهٔ اول برنامه یک پنل سادهٔ شروع است که هر مرحله را با نام و زمان نشان می‌دهد؛ اگر برنامه جایی متوقف
  شد، **آخرین خط همان پنل** می‌گوید کجا. همان خط و «کد رویداد» را برای پشتیبانی بفرستید.
* دکمهٔ «بررسی اتصال به سرور» در صفحهٔ ورود (و در «بیشتر» ← «تشخیص و پشتیبانی») زنجیرهٔ
  اینترنت → DNS → پورت ۱۴۳۳ → ورود به SQL → تاریخ سرور را با زمان و خطای دقیق گزارش می‌دهد.
* اگر ساخت رابط گرافیکی ناممکن باشد، فرم ورود ساده روی همان پنل شروع فعال است؛ ورود و داده قطع نمی‌شود.
* گزارش‌های روی دستگاه: `fin-boot.txt` (مرحله‌های شروع) و `fin-crash.txt` (آخرین خطای کشنده) در
  پوشهٔ خصوصی برنامه — بدون هیچ رمز یا مقدار مالی.

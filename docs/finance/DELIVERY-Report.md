# گزارش تحویل «آتیران مالی» — فاز ۱ و ۲

تاریخ: ۱۴۰۵/۰۷/۱۲ (سرور آتیران) · تاریخ سرور در زمان ساخت: `ReturnDateServer()`
برنچ: `arena/01a10310-modiriat` · آخرین کامیت سورس اپ: `e459116`
آخرین کامیت تأییدشده ساخت: `a6aac36` (سورس اپ یکسان) — ساخت بعدی روی `e459116` هم موفق بود.

## ۱) نتیجه ساخت (واقعی، از CI)

| مورد | مقدار |
|---|---|
| Workflow | `Atiran Finance build` (`.github/workflows/atiran-finance-build.yml`) |
| Trigger | پیام کامیت شامل `[fin-build]` (یا `workflow_dispatch`) |
| Job | `assembleFinanceDebug` + `assembleFinanceRelease` |
| نتیجه | **success** — کامپایل، امضا، `apksigner verify`، انتشار آرتیفکت و کامیت APKها |
| امضا | `apksigner verify: Verified using v1/v2/v3` با کلید `app/signing/meelano-install.p12` (تأیید در لاگ CI) |

### فایل‌های خروجی و SHA-256

| فایل | حجم (بایت) | SHA-256 |
|---|---|---|
| `apk-finance/AtiranFinance-debug.apk` | 3,703,617 | `64c7db317c4e9dcd0980a88e3047c828fd2a155bbca1ff014d29a124a8fb026a` |
| `apk-finance/AtiranFinance-release.apk` | 3,262,760 | `8d19666db0a2a5a5ce4396215afc26187a5bde7e0ccc2ba622e30aab47a3868a` |

نکته: هش دیباگ در دو ساخت متوالی یکسان باقی ماند (`64c7db31…`)، هش ریلیز در هر ساخت به‌دلیل
Timestamp امضا تغییر می‌کند؛ هش بالا مربوط به آخرین ساخت موفق است. هر ساخت، هش خود را در لاگ
`sha256sum` چاپ می‌کند.

## ۲) تحویل‌شده در این نسخه

| بخش | وضعیت | منبع داده |
|---|---|---|
| ورود واقعی + نقش از جدول دسترسی | ✅ | `visitors`/`sys_users` + `meelano_access_users`/`meelano_access_roles` |
| خانه «مرکز کنترل مالی»: ۸ KPI + هشدارها + نمودار ۱۴ روز + Drill-down | ✅ | `sailfact`, `dar`, `PosDetails`, `CUSTOMERS`, `BANK`, `COW`, `getchk`, `putchk` |
| فروش در برابر وصول + ترکیب نقد/چک/POS + عملکرد اپراتور + مغایرت‌ها | ✅ | همان + `visitors` |
| مرکز چک: دریافتی/پرداختی، سررسید امروز/معوق/۷ روز/در خزانه/برگشتی، تقویم ۳۰ روز، راهنمای وضعیت‌ها | ✅ | `getchk`, `putchk`, `getcheckhistorystatus`, `CheckTypes` |
| مطالبات: مانده، سنی‌بندی ۰-۷/۸-۳۰/۳۱-۶۰/۶۱-۹۰/۹۰+، بدهکاران، صف فاکتور باز | ✅ | `CUSTOMERS.man`, `sailfact.bamandeh/MabDaryaftFactor` |
| پیگیری و وعده پرداخت (نوشتن) | ✅ | `meelano_fin_followup` (Transaction + Audit + Idempotency) |
| پرونده مشتری: صورت‌حساب از دفتر `cust_act`، فاکتورهای باز، چک‌ها، پیگیری‌ها، رویدادها | ✅ | `cust_act`, `sailfact`, `getchk`, `Sys_Mandeh_Customer` |
| بانک‌ها: موجودی، مالک/شعبه، گردش، به تفکیک روز، حساب بدون قبض | ✅ | `BANK`, `ban_act`, `PosDetails` |
| جزئیات حساب با شرح هر گردش | ✅ | `ban_act` |
| مرکز POS: مبلغ/کارمزد/حواله، به تفکیک بانک و اپراتور، بدون قبض | ✅ | `PosDetails` ⋈ `dar` ⋈ `visitors` |
| صندوق: موجودی، ورود/خروج، ترکیب نوع گردش، دفتر صندوق | ✅ | `COW` |
| تسویه کاربران: Expected (نقد/چک/POS هر اپراتور) در برابر Actual (تحویل ثبت‌شده) و تفاوت | ✅ | `dar.rdf_vis`, `PosDetails.UserID`, `meelano_fin_settlement` |
| ثبت تحویل وجه (نوشتن) با امضا (PNG) و Idempotency | ✅ | `meelano_fin_settlement` |
| مغایرت‌گیری بانکی: صف‌های واقعی + چرخه `open→review→resolved→confirmed` | ✅ | سه صف واقعی + `meelano_fin_recon` |
| گزارش روزانه + بستن روز با Snapshot اعداد | ✅ | `meelano_fin_dayclose` |
| Audit (فعالیت‌ها) و Idempotency | ✅ | `meelano_fin_audit`, `meelano_fin_ops` |
| چهار تم سازمانی | ✅ | `FinTheme` (Midnight/Obsidian/Platinum/Executive) |
| خودآزمون فنی روی دیتابیس واقعی | ✅ | `FinSelfTestActivity` (debug، سه توافق کلیدی) |

## ۳) امنیت و انطباق با قواعد پروژه

* هیچ Credential در سورس/APK/لاگ نیست (همان الگوی مبهم XOR پروژه، فقط رمزگشایی در حافظه).
* رمز الهام فقط در تست CI قابل تنظیم است و در مخزن ذخیره نمی‌شود.
* تمام کوئری‌ها پارامتری‌اند؛ تنها رشته‌های الحاقی، اعداد صفحه‌بندی محاسبه‌شده هستند.
* خطای SQL هرگز در UI دیده نمی‌شود؛ فقط کد رویداد ۶ رقمی (SHA-256 کوتاه‌شده).
* نوشتن فقط در `meelano_fin_*`؛ جدول‌های اصلی آتیران فقط خوانده می‌شوند.
* فقط خواندن مانده: `CUSTOMERS.man` نمایش داده می‌شود و هرگز بازنویسی نمی‌شود.
* عملیات حساس (پیگیری، تحویل وجه، پرونده مغایرت، بستن روز) با Transaction + Audit + Idempotency + مجوز.
* قفل ورود بعد از ۵ تلاش ناموفق (۵ دقیقه) و پاک‌کردن رمز از فرم پس از خطا.

## ۴) بررسی‌های دادگانی مبنای اطمینان

| بررسی | نتیجه |
|---|---|
| تطبیق `CUSTOMERS.man` با جمع دفتر `cust_act` | ۲۷۲۴ از ۲۷۲۴ ✔ |
| پنجره سررسید چک (کل = معوق + آینده) | ۱۱۸ = ۲۹ + ۸۹ ✔ |
| اتصال `PosDetails.ghno` به `dar` | در خودآزمون هر نصب سنجیده می‌شود |
| مغایرت اجزای قبض (`mab` در برابر `naghd+mabcheck+POS`) | ۶۹۴ از ۸۱۲ دقیق ✔ (۱۱۸ مورد واقعی) |

## ۵) نصب و پذیرش

```bash
# نصب روی دستگاه (Android 7 تا 16)
adb install -r apk-finance/AtiranFinance-release.apk
```

۱. ورود با کاربر واقعی (رمز الهام پیش‌فرض است؛ در صورت نیاز از VPN استفاده شود).
۲. هدر باید «تاریخ سرور» را از `ReturnDateServer` نشان دهد؛ حالت «آفلاین — آخرین نسخه» یعنی اتصال
   برقرار نشده و اپ همان داده ذخیره‌شده را با برچسب صریح نشان می‌دهد.
۳. آزمون فنی: `adb shell am start -n ir.meelano.atiran.finance/ir.meelano.android.finance.FinSelfTestActivity`
   (فقط نسخه debug) — همه ردیف‌ها باید ✓ شوند.
۴. بستن روز: رکوردی در `dbo.meelano_fin_dayclose` با `checklist` شامل Snapshot اعداد همان لحظه.
۵. خروج/ورود دوباره: نشست پاک می‌شود و رمز ذخیره نمی‌شود.

## ۶) فاز ۳ (باقی‌مانده، اعلام‌شده)

* PDF صورت‌حساب/گزارش و CSV (فایل، اشتراک‌گذاری، چاپ).
* دسته‌چک و پیگیری سررسید زمان‌بندی‌شده (Notification).
* پیشنهاد AI (فقط پیشنهاد، با Approval) و حذف نیاز به تأیید دستی برای موارد کم‌ریسک.
* Sync کامل آفلاین با حل تعارض و صف عملیات.

## ۷) محدودیت‌ها و UNKNOWN (عمداً نمایش داده نمی‌شوند)

شبا (`ShabaNumber` = متن «IR»)، چک الکترونیک (`HaveEChecks=false`)، انتساب اپراتور روی چک
(`getchk.vis_rdf=0`)، اسناد حسابداری (`Document`/`DocumentDetails` صفر ردیف)، شرایط پرداخت مشتری
(`customers.checkdays` خالی)، تطبیق خط فاکتور به قبض (فقط ۴۵۸/۱۱۶۸). جزئیات کامل در
`docs/finance/DataSourceRegistry.md`.

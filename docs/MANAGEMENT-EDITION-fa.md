# بررسی دقیق `Modirat.zip` و طرح ساخت «نسخه مدیریت» اندروید

**تاریخ:** ۱۴۰۵/۰۷/۰۸ (۲۰۲۶-۰۹-۳۰)
**موضوع:** آیا می‌شود نسخهٔ مدیریت (مدیر/مدیرکل) را به‌صورت یک اپ اندروید جدا ساخت؟ چه چیزی لازم است؟
**روش:** باز شدن `Modirat.zip` و خواندن مستقیم کد، Gradle، مانیفست، ورک‌فلوهای CI و همهٔ مستندات `docs/`.
همهٔ شماره‌خط‌ها مربوط به فایل‌های داخل خودِ zip است.

---

## ۰) پاسخ کوتاه

**بله — و خبر خوب این است که تقریباً همهٔ منطق نسخهٔ مدیریت از قبل نوشته شده است؛ فقط در هیچ‌کدام از ۴ APK فعلی قابل رسیدن نیست.**

پروژه امروز ۴ نسخهٔ نصب‌شدنی از یک کدبیس می‌سازد (ویزیتور، فروشگاه، پرسنل، مودیان). نسخهٔ پنجم —
**«پخش درخشان مدیریت»** — با همان الگو و با حدود ۱ تا ۱٫۵ روز کار متمرکز ساخته می‌شود، چون:

- صفحهٔ داشبورد مدیر، اتاق فرمان، گزارشات، پرسنل، مدیریت دسترسی کاربران، مدیریت حضور، تأیید مشتری جدید،
  مدیریت تحویل بار و مدیریت گفتگو **همه پیاده‌سازی شده‌اند** (جزئیات در بخش ۳).
- موتور نقش/دسترسی (`admin`, `manager`, ۴۰ کلید دسترسی، ۱۴ نقش) آماده است.
- زیرساخت CI، امضای ثابت، اسکرین‌شات خودکار و بررسی به‌روزرسانی آماده است.

کاری که باید انجام شود بیشتر **بازکردن قفل، افزودن طعم (flavor) جدید، و یک لایهٔ طراحی یکدست** است،
نه نوشتن برنامه از صفر.

---

## ۱) آنچه دقیقاً در فایل پیوست هست

| مورد | مقدار (اندازه‌گیری‌شده) |
|---|---|
| فایل | `Modirat.zip` — ۱۷٬۲۶۴٬۹۵۶ بایت، ۷۷۷ فایل، ۲۶ مگابایت پس از باز شدن |
| پروژهٔ اندروید | `MEELANO-Android/` (۶٫۹ مگابایت) |
| مستندات | `docs/` (۱۷ مگابایت؛ ۱۶ سند فارسی + ۲۰۲ تصویر اسکرین‌شات و dump UI) |
| ابزار CI | `.github/` (۵ ورک‌فلو + ۶ اسکریپت) و `tools/` (کلیدهای آزمون رمز‌شده، آیکون) |
| نسخهٔ کد | `versionName '6.0.1'` و `versionCode 108` در `MEELANO-Android/app/build.gradle` |
| ⚠️ ناسازگاری | `latest.json` ریشهٔ zip می‌گوید `versionCode 107 / 6.0.0` — یعنی آخرین APK منتشرشده یک نسخه عقب‌تر از کد است |
| طعم‌ها (flavors) | `visitor` → `ir.meelano.visitor` • `store` → `ir.meelano.store` • `staff` → `ir.meelano.staff` • `tax` → `ir.meelano.tax` |
| اندازهٔ کد | `MainActivity.java` = **۲۶٬۹۹۴ خط**؛ مجموع جاوا = **۳۷٬۷۰۷ خط** در ۲۸ فایل |
| ابزار ساخت | AGP 8.10.1، Gradle 8.11.1، `compileSdk 36`، `minSdk 24`، `targetSdk 36`، Java 17 |
| وابستگی‌ها | فقط `net.sourceforge.jtds:jtds:1.3.1` (اتصال مستقیم SQL Server) + JUnit برای تست |
| رابط کاربری | کاملاً برنامه‌نویسی‌شده در جاوا؛ در `res/layout` فقط **۱ فایل** وجود دارد (`widget_meelano.xml`) |
| آیکون | ۱۳۵ وکتور `mi_*.xml` تولیدشده از ۱۳۵ SVG متریال (`icons-svg/` + `tools/svg_to_vector.py`) |
| فونت | Vazirmatn Regular + Bold در `assets/fonts` |
| پایگاه داده | SQL Server «آتیران»؛ ۳۳ جدول `dbo.meelano_*` که خودِ برنامه با `IF OBJECT_ID ... CREATE TABLE` می‌سازد + ۳ فایل `res/raw/atiran_*.sql` |
| تست | ۲ فایل (۵۷۵ خط): `MeelanoHrTest`، `MeelanoTaxTest` + خودآزمون درون‌برنامه‌ای با `--es meelano_selftest` |
| CI | ساخت هر ۴ طعم (debug+release)، امضا، انتشار Release، job تحلیل (javac/Lint/PMD)، job اسکرین‌شات روی امولاتور API 35 |

---

## ۲) مهم‌ترین یافتهٔ فنی: چرا امروز «نسخه مدیریت» در دسترس نیست

این بخش دلیل اصلی پیشنهاد ساخت نسخهٔ مدیریت است و **کد به کد راستی‌آزمایی شده** است:

### ۲٫۱ پرچم نسخهٔ ویزیتور ثابت و همیشه `true` است

```java
// MainActivity.java:140
private static final boolean VISITOR_EDITION = true;
```

در مقابل، `STORE_EDITION` (خط ۲۲۹۹۱) و `STAFF_EDITION` (خط ۲۱۵۷۲) متغیرِ عادی‌اند و از
`res/values/edition.xml` هر طعم خوانده می‌شوند (خطوط ۳۳۱–۳۳۳). نتیجه: **در هر ۴ APK، `VISITOR_EDITION` برابر `true` است**
(چون ثابت `final` است، کامپایلر آن را جای‌گذاری می‌کند و هیچ طعمی نمی‌تواند تغییرش دهد).

### ۲٫۲ همین پرچم، همهٔ صفحه‌های مدیریتی را قفل می‌کند

```java
// MainActivity.java:6508
private boolean canUsePermission(String key) {
    ...
    if ("delivery_admin".equals(key.trim())) return !STORE_EDITION && isFullAccessUser();
    if (VISITOR_EDITION) return visitorEditionPermissionAllowed(key);   // ← همیشه اجرا می‌شود
    if (isFullAccessUser()) return true;                                 // ← هرگز اجرا نمی‌شود
    return currentPermissionSet().contains(key);                         // ← هرگز اجرا نمی‌شود
}
```

و `visitorEditionPermissionAllowed` (خط ۶۵۱۹) یک فهرست سفید **فقط ویزیتوری** است که این کلیدها را ندارد:
`dashboard`، `reports`، `command`، `personnel`، `management_access`، `taxpayers`، `cameras`، `alarm`،
`products`، `attendance_admin`، `assistant`، `ai_settings`، `tax_settings`، `hardware_control`.

**پیامد:** حتی کاربری که نقشش `admin` است (و `identityLooksAdmin` او را مدیرکل تشخیص می‌دهد و
`allPermissionString()` را می‌گیرد) در اپ ویزیتور **نمی‌تواند** داشبورد مدیر، گزارشات، اتاق فرمان،
پرسنل یا مدیریت دسترسی را باز کند — چون بررسی، قبل از رسیدن به نقش، با فهرست سفید ویزیتور قطع می‌شود.

### ۲٫۳ منوی مدیریتی ساخته شده ولی هرگز رندر نمی‌شود

```java
// MainActivity.java:3354
private void buildNav() {
    if (VISITOR_EDITION) { buildVisitorLuxuryNav(); return; }   // ← همیشه برمی‌گردد
```

نوار ۱۵‌آیتمی زیرِ همان `return` — شامل «داشبورد، گزارشات، میلو، پرسنل، مودیان، دوربین، دزدگیر، فرماندهی» —
**کد مرده** است. داک واقعی هر نسخه در `buildVisitorLuxuryNav()` (خط ۳۴۱۳) است و ۵ آیتم دارد
(پرسنل: خانه/حضور/تحویل بار/حقوق/بیشتر • فروشگاه: خانه/کالاها/فاکتور/گزارش‌ها/بیشتر • ویزیتور: خانه/ویزیت/کالاها/سبد/بیشتر).

### ۲٫۴ مسیر «داشبورد» در `showApp` به صفحهٔ دیگری تغییر مسیر می‌دهد

```java
// MainActivity.java:3301–3304
if (VISITOR_EDITION && "dashboard".equals(targetPage)) targetPage = "visitor_dashboard";
if (STORE_EDITION && (...)) targetPage = "store_home";
if (STAFF_EDITION && (... || "dashboard".equals(targetPage))) targetPage = "staff_home";
```

پس `loadDashboard()` (خط ۴۶۳۴) با کارت‌های «صبح‌بخیر مدیر»، خلاصهٔ تیم و جدول KPI،
در هیچ‌کدام از ۴ APK باز نمی‌شود.

### ۲٫۵ آدرس بررسی به‌روزرسانی به مخزن دیگری اشاره می‌کند

```java
// MainActivity.java:15010 / 21573 / 23001
"https://github.com/Companymeelano/Newhamrah/raw/arena/01a0e474-newhamrah/apk/latest.json"
```

این سه آدرس به مخزن/برنچ `Newhamrah` اشاره دارند، در حالی که این پروژه در مخزن `modiriat` است.
تا وقتی اصلاح نشود، «بروزرسانی برنامه» در همهٔ نسخه‌ها فایل مخزن دیگر را پیشنهاد می‌دهد.

### ۲٫۷ دکمه‌های «مدیریت دسترسی» و «جستجوی سراسری» در هدر اصلاً ساخته نمی‌شوند

```java
// MainActivity.java:2074–2077  (داخل buildFrame)
if (!VISITOR_EDITION) {
    addHeaderTool(tools, "⌕", "جستجوی سراسری", GOLD, v -> showGlobalSearchDialog());
    addHeaderTool(tools, "♛", "مدیریت دسترسی کاربران", GOLD, v -> { ... showApp("management"); });
}
```

چون `VISITOR_EDITION` همیشه `true` است، این دو دکمه در هیچ نسخه‌ای به هدر اضافه نمی‌شوند.
حتی اگر اضافه می‌شدند، `canOpenPage("management")` کلید `management_access` را می‌خواهد که در فهرست سفید
ویزیتور (بخش ۲٫۲) نیست و کاربر با پیام «فقط بخش‌های مجاز این حساب نمایش داده می‌شود» به خانه برمی‌گشت.
یعنی **مسیر رسیدن به مدیریت دسترسی کاربران، دو بار بسته شده است.**

### ۲٫۸ جمع‌بندی این بخش

> نسخهٔ مدیریت امروز **وجود ندارد** — نه به این دلیل که نوشته نشده، بلکه به این دلیل که
> یک ثابت `true` و یک فهرست سفید ویزیتوری، جلوی آن را گرفته‌اند. ساخت نسخهٔ مدیریت یعنی:
> **پرچم را منبع‌محور کن، یک طعم جدید اضافه کن، دسترسی را بر پایهٔ نقش باز کن، و یک خانهٔ مدیریتی بساز.**

---

## ۳) سرمایهٔ موجود: آنچه برای نسخهٔ مدیریت آماده است

| بخش مدیریتی | متد موجود | وضعیت |
|---|---|---|
| داشبورد مدیر (صبح‌بخیر + KPI + خلاصهٔ تیم) | `loadDashboard` (۴۶۳۴) • `renderDashboardJson` • `addGoodMorningManagerCard` | ✅ نوشته شده، قفل |
| اتاق فرمان (پیش‌بینی نقدینگی، رادار کالا، ریسک مشتری) | `loadCommandCenter` | ✅ نوشته شده، قفل |
| گزارشات و نمودارها | `loadReports` (۱۸۲۱۱) • `renderAnalytics` • `MeelanoCharts.java` (۳۴۰ خط) | ✅ نوشته شده، قفل |
| پرسنل | `loadPersonnel` (۷۷۵۹) • `MeelanoHr.java` (۴۴۸ خط) + تست واحد | ✅ نوشته شده، قفل |
| مدیریت دسترسی کاربران (نقش/دسترسی/غیرفعال‌سازی) | `loadAccessManagement` (۲۰۸۵۵) + جدول `meelano_access_users/roles` | ✅ نوشته شده، قفل (دکمهٔ «♛» هدر اصلاً ساخته نمی‌شود — بخش ۲٫۷) |
| جستجوی سراسری | `showGlobalSearchDialog` | ✅ نوشته شده، قفل (همان `if (!VISITOR_EDITION)`) |
| مدیریت حضور و مرخصی همه | `addAttendanceAdminBlocks` • `attendance_admin` | ✅ نوشته شده |
| تأیید مشتری جدید (workflow با قفل `approving`) | `approveCustomerRequest` (۹۶۹۵) • `meelano_customer_requests` | ✅ فعال و در دسترس |
| مدیریت تحویل بار | `renderDeliveryAdminPage` (۲۲۶۶۹) • `MeelanoDelivery.java` (۷۲۸ خط) | ✅ فعال برای مدیر در نسخهٔ ویزیتور |
| مدیریت گفتگو (بستن، سکوت، پین، اخراج، اعطای نقش) | `chatSetClosed`, `chatMemberUpdate` | ✅ نوشته شده |
| حقوق، مساعده، مأموریت، شیفت، منطقه | جداول `meelano_hr_month/advance/mission/shift/zone/staff/holiday` | ✅ نوشته شده (سمت پرسنل فقط مشاهده) |
| کانال اعلان مدیریتی | `NOTIFY_CHANNEL = "meelano_management_alerts"` (خط ۲۰۱) | ✅ آماده |
| صندوق پیام‌های مدیر | جدول `meelano_hr_inbox` | ✅ آماده |

**نتیجه:** نسخهٔ مدیریت تقریباً یک کار «اتصال و طراحی» است، نه «توسعهٔ از صفر».

---

## ۴) معماری پیشنهادی نسخهٔ مدیریت

### ۴٫۱ طعم پنجم، دقیقاً با همان الگوی موجود

```gradle
// MEELANO-Android/app/build.gradle → productFlavors
manager {
    dimension 'edition'
    applicationId 'ir.meelano.manager'
}
```

فایل‌های تازهٔ لازم (۵ فایل کوچک + آیکون):

| فایل | محتوا |
|---|---|
| `app/src/manager/res/values/edition.xml` | `<bool name="meelano_manager_edition">true</bool>` |
| `app/src/manager/res/values/strings.xml` | `app_name` = «پخش درخشان مدیریت»، `web_url_hint` = «ورود مدیر» |
| `app/src/manager/res/values/colors.xml` | رنگ شروع برنامه هم‌رنگ آیکون (سرمه‌ای/طلایی) |
| `app/src/manager/res/mipmap-*/ic_launcher*.png` | آیکون اختصاصی (۵ چگالی) |
| `app/src/manager/AndroidManifest.xml` | حذف `MeelanoDeliveryJob` (اختیاری) و مجوزهای بی‌استفاده |

### ۴٫۲ اصلاح بنیادی پرچم نسخه (مهم‌ترین تغییر)

```java
// به‌جای ثابت true:
private boolean VISITOR_EDITION = true;      // دیگر final نیست
private boolean MANAGER_EDITION = false;
...
MANAGER_EDITION = getResources().getBoolean(R.bool.meelano_manager_edition);
if (MANAGER_EDITION) VISITOR_EDITION = false;   // کلید باز شدن همهٔ صفحه‌های مدیریتی
```

این یک تغییر چند خطی است، ولی **باید با رگرسیون کامل ۳ نسخهٔ دیگر تست شود**، چون `VISITOR_EDITION`
در ۱۰۳ نقطه از همین فایل استفاده شده است (پس‌زمینهٔ چرمی، ارتفاع هدر، داک، تم‌ها، متن ورود).

> **توصیهٔ ایمن:** به‌جای لمس `VISITOR_EDITION`، ابتدا یک متغیر جدا
> `MANAGER_EDITION` بسازید و فقط در `canUsePermission` و `buildNav` و `showApp` و `homePage` از آن استفاده کنید.
> این‌طور هیچ ریسکی برای ویزیتور/فروشگاه/پرسنل ایجاد نمی‌شود.

### ۴٫۳ خانهٔ مدیر (داک ۵ تایی)

| آیتم داک | صفحه | کلید دسترسی | نوشتن در DB؟ |
|---|---|---|---|
| خانه | `dashboard` (داشبورد مدیر + هشدارها) | `dashboard` | ✗ فقط خواندن |
| تأییدها | صفحهٔ تازهٔ تجمیعی: مشتری جدید، مرخصی، مساعده، مأموریت، تردد ناقص | `management_access` | ✔ (با تأیید دومرحله‌ای) |
| گزارش‌ها | `reports` + `command` | `reports`, `command` | ✗ |
| پرسنل | `personnel` + `attendance_admin` | `personnel`, `attendance_admin` | ✔ (اصلاح تردد) |
| بیشتر | دسترسی کاربران، تحویل بار، مودیان، گفتگو، سلامت اتصال، تنظیمات | — | ✔ |

### ۴٫۴ دروازهٔ ورود مدیریتی (امن و ساده)

1. ورود با همان حساب آتیران (بدون تغییر در مکانیزم فعلی).
2. **فیلتر سخت:** اگر `isFullAccessUser()` نبود → پیام «این نسخه مخصوص مدیر است» و هدایت به نصب نسخهٔ مناسب
   (به‌جای نشان دادن یک برنامهٔ خالی).
3. ورود با اثر انگشت/قفل خودکار (زیرساختش در `MeelanoTaxVault`/Keystore موجود است).
4. ثبت «چه کسی، چه چیزی را، کِی تغییر داد» در `meelano_management_alerts`/`meelano_hr_inbox` برای هر تصمیم.

---

## ۵) طراحی بصری: «بسیار زیبا و کاربردی» چگونه

پروژهٔ شما سند بازبینی UI دارد (`docs/UI-REVIEW-fa.md`) که ۱۵ باگ قطعی و یک سیستم طراحی پیشنهادی را فهرست کرده.
نسخهٔ مدیریت باید **اولین نسخه‌ای باشد که آن سیستم طراحی را کامل اجرا می‌کند** تا الگوی بقیه شود.

**پیشنهاد پالت مدیر (هم‌خانواده با پالت‌های موجود، ولی متمایز):**

| نام تم | پس‌زمینه | رنگ اصلی | رنگ دوم | حس |
|---|---|---|---|---|
| **اونیکس طلایی** (پیش‌فرض تیره) | `#08090A` | `#CC9941` | `#5BC597` | اقتدار، اتاق فرمان شبانه |
| **پلاتین سرمه‌ای** (پیش‌فرض روشن) | `#F5F7FB` | `#1F3A68` | `#A87A2C` | اداری، خوانا زیر نور روز |

این دو دقیقاً از تم‌های موجود (`onyx_gold` خط ۲۳۲۳ و `pearl_platinum` خط ۲۳۱۵) مشتق شده‌اند،
پس با موتور تم فعلی (`addThemeOption`) بدون کد تازه کار می‌کنند.

**قواعد اجرایی (برگرفته از سند UI خودتان):**

1. **۵ نوع دکمهٔ ثابت:** اصلی/ثانویه/خطی/متنی/خطرناک — همه ارتفاع `48dp`، شعاع `14dp`، بدون سایه روی متن.
2. **یک رنگ برند ثابت** برای دکمهٔ اصلی در همهٔ صفحه‌ها؛ رنگ اختصاصی صفحه فقط برای آیکون و عنوان.
3. **آیکون وکتور یکدست:** از همان ۱۳۵ آیکون `mi_*` استفاده شود؛ هیچ ایموجی/یونیکد تازه‌ای اضافه نشود.
4. **تایپوگرافی ۵ اندازه:** ۲۲ / ۱۸ / ۱۵ / ۱۳ / ۱۱؛ مبلغ‌ها Bold با ارقام فارسی و جداکنندهٔ هزارگان.
5. **شبکهٔ ۸dp** و حذف کارت‌درکارت؛ فقط یک سطح کارت.
6. **هدر ۶۴dp** با عنوان صفحهٔ فعلی (نه نام ثابت برنامه).
7. **RTL کامل در همهٔ ورودی‌ها** (باگ B4 سند UI: فیلدها امروز چپ‌چین‌اند).
8. **حالت خطای مهربان:** هر صفحه باید کش‌شده را نشان دهد + دکمهٔ تلاش مجدد (الگوی `renderCachedDashboard` موجود است).
9. **دسترس‌پذیری:** حداقل لمس ۴۸dp، کنتراست ≥ ۴٫۵ (WCAG)، برچسب صفحه‌خوان روی همهٔ دکمه‌های آیکونی
   (`MeelanoA11y.java` آماده است).
10. **تبلت:** چیدمان دوستونی برای گزارشات (زیرساخت `compactUi()` موجود است).

> یک **پروتوتایپ زندهٔ HTML** از همین صفحهٔ اصلی مدیریت در `prototype/manager/index.html` ساخته شده
> (RTL، فونت Vazirmatn، همان پالت، داک ۵ تایی، صفحهٔ تأییدها، گزارش‌ها و پرسنل). آن را باز کنید تا ظاهر نهایی را
> پیش از نوشتن کد اندروید تأیید کنید.

---

## ۶) پیش‌نیازهای امنیتی (قبل از دادن APK مدیر به دست کسی)

سند `docs/CODE-AUDIT-fa.md` سه مورد بحرانی را ثبت کرده که **برای نسخهٔ مدیریت جدی‌تر می‌شوند**،
چون این نسخه به دادهٔ بیشتر و عملیات نوشتن دسترسی دارد:

| # | یافته (از سند خودتان) | وضعیت امروز | اقدام لازم |
|---|---|---|---|
| S1 | اطلاعات SQL Server داخل برنامه (خطوط ۲۱۰–۲۱۳، فقط XOR با کلید ۷۳) | `hidden(S_HOST/S_USER/S_PASS/S_DB)` | 🔴 **حداقل:** یک کاربر SQL **کم‌دسترسی و تفکیک‌شده برای مدیر** با IP allowlist؛ **اصولی:** API واسط با توکن |
| S2 | احراز هویت کاربر روی گوشی انجام می‌شود | `authenticate` / `passwordMatches` | 🔴 انتقال تصمیم دسترسی به سمت سرور (یا视图 سروری) |
| S3 | ارتباط بدون رمزنگاری؛ jTDS 1.3.1 گواهی را بررسی نمی‌کند | `network_security_config` = `cleartextTrafficPermitted="true"` | 🔴 VPN/شبکهٔ داخلی + `ssl=require`؛ اصولی: HTTPS API |
| D1/D2 | شمارهٔ سند با `MAX(no)+1` و بدون قفل | `nextNativeNumber` | 🟠 `UPDLOCK, HOLDLOCK` یا sequence (برای نوشتن‌های مدیر حیاتی است) |
| — | کلید امضای نصب **داخل مخزن** است (`app/signing/meelano-install.p12` + رمز در `build.gradle`) | امضای release هم با همان کلید | 🟠 کلید واقعی فقط در Secrets گیت‌هاب؛ این فایل از مخزن خارج شود |
| — | آدرس به‌روزرسانی به مخزن `Newhamrah` اشاره می‌کند | خطوط ۱۵۰۱۰/۲۱۵۷۳/۲۳۰۰۱ | 🟠 اصلاح به مخزن/برنچ درست، به‌همراه `latest-manager.json` |

**توصیهٔ صریح:** نسخهٔ مدیریت را تا رفع S1–S3 فقط روی **شبکهٔ داخلی/VPN شرکت** توزیع کنید.
در غیر این صورت، هر کسی که APK را بگیرد عملاً کلید پایگاه دادهٔ آتیران را در دست دارد.

---

## ۷) CI و انتشار

تغییرات لازم در `.github/workflows/build-apk.yml` (الگو از store/staff کپی می‌شود):

1. `testManagerDebugUnitTest` به مرحلهٔ تست اضافه شود.
2. در «Collect and verify APKs»:
   `cp .../apk/manager/release/*.apk "out/MEELANO-Manager-v$APP_VERSION-release.apk"` (و نسخهٔ debug).
3. در artifact اسکرین‌شات: `app-manager-debug.apk`.
4. نوشتن `apk/latest-manager.json` در مرحلهٔ commit.
5. در کد: `MANAGER_UPDATE_MANIFEST_URL` + انتخاب آن در خط ۱۵۰۴۲.
6. در `.github/scripts/screenshots.sh` و `ui-review.sh`: بستهٔ `ir.meelano.manager.debug`
   و صفحه‌های `dashboard / approvals / reports / personnel / management`.

---

## ۸) برنامهٔ اجرایی (فازبندی‌شده، با معیار پذیرش)

### فاز ۰ — آماده‌سازی (نیم روز)
- خارج کردن پروژه از zip به مخزن `modiriat` (ساختار همان‌طور که هست).
- همسان‌سازی نسخه: `versionCode`/`versionName` با `latest.json`.
- اصلاح آدرس‌های به‌روزرسانی.
- **معیار پذیرش:** `assembleVisitorDebug` روی CI سبز بماند.

### فاز ۱ — اسکلت نسخهٔ مدیریت (۱ روز)
- طعم `manager` + `edition.xml` + رشته‌ها + رنگ + آیکون.
- متغیر `MANAGER_EDITION` (منبع‌محور) و استفاده از آن در: `canUsePermission`، `buildNav`، `showApp`، `homePage`، `firstAllowedPage`، `editionTitle`.
- دروازهٔ «فقط مدیر».
- **معیار پذیرش:** `MEELANO-Manager-v…-debug.apk` نصب شود، کاربر `admin` داشبورد مدیر را ببیند،
  کاربر ویزیتور پیام مناسب بگیرد؛ **و هر ۳ نسخهٔ دیگر بدون تغییر رفتار بمانند** (اسکرین‌شات CI مقایسه شود).

### فاز ۲ — صفحهٔ «تأییدها» (۱ روز) ← بیشترین ارزش کاربری
- تجمیع ۵ صف: مشتری جدید (`meelano_customer_requests`)، مرخصی (`meelano_leave_requests`)،
  مساعده (`meelano_hr_advance`)، مأموریت (`meelano_hr_mission`)، تردد ناقص (`meelano_hr_incomplete`).
- هر کارت: خلاصهٔ تصمیم + دو دکمهٔ «تأیید / رد» با دیالوگ تأیید و ثبت دلیل.
- badge شمارنده روی داک + اعلان از کانال `meelano_management_alerts`.
- **معیار پذیرش:** خودآزمون (`--es meelano_selftest`) تأیید/رد/تأیید دوباره (idempotency) را پاس کند؛
  دو مدیر هم‌زمان نتوانند یک درخواست را دو بار تأیید کنند (الگوی `approving` موجود است).

### فاز ۳ — گزارشات و اتاق فرمان مدیر (۱ روز)
- اتصال `reports` + `command` با فیلتر تاریخ شمسی (`MeelanoJalali.java` آماده است).
- خروجی PDF/اشتراک (الگوی `MeelanoDailyReportPdf.java` موجود است) برای گزارش روزانهٔ مدیر.
- **معیار پذیرش:** روی دادهٔ واقعی آتیران، اعداد با خودِ آتیران یکی باشند (جدول تطبیق).

### فاز ۴ — پرسنل و حضور (۱ روز)
- `personnel` + `attendance_admin`: تأیید مرخصی، اصلاح تردد، تعریف شیفت/منطقه/تعطیلات.
- **معیار پذیرش:** `MeelanoHrTest` گسترش یابد و محاسبهٔ حقوق/اضافه‌کار طبق قانون کار ۱۴۰۵ پاس شود.

### فاز ۵ — پرداخت فنی و زیبایی (۱ روز)
- اعمال سیستم طراحی بخش ۵ روی همهٔ صفحه‌های مدیر.
- `RecyclerView` برای فهرست‌های بلند (مورد P6 سند بازبینی).
- Lint/PMD صفر مورد تازه؛ اسکرین‌شات‌های روشن/تیره/تبلت در `docs/`.
- **معیار پذیرش:** اسکرین‌شات‌های CI بدون باگ بصری؛ کنتراست همهٔ متن‌ها ≥ ۴٫۵.

### فاز ۶ — امنیت (موازی، اولویت بالا)
- کاربر SQL تفکیک‌شدهٔ مدیر + کمینه‌سازی دسترسی.
- لاگ ممیزی هر تغییر مدیر در `meelano_management_alerts`.
- خروج `meelano-install.p12` از مخزن و انتقال به Secrets.

**جمع: حدود ۵–۶ روز کاری برای یک نسخهٔ مدیریتی کامل، زیبا و قابل اتکا.**

---

## ۹) ریسک‌ها و راه‌حل‌شان

| ریسک | چرا مهم است | راه‌حل |
|---|---|---|
| `MainActivity.java` ۲۷ هزار خطی | هر تغییر می‌تواند ۳ نسخهٔ دیگر را بشکند | ادامهٔ روند تفکیک (الگو: `MeelanoDelivery`, `MeelanoHr`, `MeelanoTaxUi`)؛ کد مدیر به `MeelanoManagerUi.java` برود |
| نوشتن هم‌زمان دو مدیر | سند/شمارهٔ تکراری در آتیران | `sp_getapplock` یا `UPDLOCK, HOLDLOCK` + `client_uuid` (الگوی `meelano_customer_requests` موجود است) |
| دسترسی بیش از حد | نشت دادهٔ حقوق/مشتری | نقش‌محوری واقعی + `meelano_access_roles`؛ پیش‌فرض «کمترین دسترسی» |
| نبود تست UI | رگرسیون خاموش | گسترش `screenshots.sh` و `ui-review.sh` برای طعم manager (زیرساخت آماده است) |
| اتصال مستقیم DB از گوشی | هر APK = کلید پایگاه داده | VPN + کاربر کم‌دسترسی؛ نقشهٔ راه API واسط |
| `VISITOR_EDITION` ثابت | تغییرش همه‌جا اثر می‌گذارد | متغیر جدا `MANAGER_EDITION` + رگرسیون اسکرین‌شاتی ۳ نسخهٔ دیگر |

---

## ۱۰) تصمیم‌هایی که باید شما بگیرید

1. **نام و آیکون:** «پخش درخشان مدیریت» یا «میلانو مدیریت»؟ آیکون سرمه‌ای‌طلایی یا رنگ دیگری؟
2. **دامنهٔ دسترسی مدیر:** فقط خواندن + تأییدها؟ یا اجازهٔ اصلاح اسناد/قیمت/موجودی هم باشد؟
3. **تفکیک نقش‌ها:** فقط «مدیرکل»، یا «مدیر» و «حسابدار ارشد» و «سرپرست انبار» هم APK جدا بگیرند؟
4. **امنیت:** فعلاً VPN/شبکهٔ داخلی کافی است یا ساخت API واسط در همین مرحله انجام شود؟
5. **اولویت صفحه‌ها:** «تأییدها» اول یا «گزارشات/اتاق فرمان»؟
6. **توزیع:** فقط APK داخلی، یا انتشار در Google Play (نیاز به AAB و کلید Play)؟

---

## پیوست — راهنمای سریع فایل‌ها

| می‌خواهید… | بروید به |
|---|---|
| تعریف طعم‌ها و امضا | `MEELANO-Android/app/build.gradle` |
| پرچم نسخه‌ها | `MainActivity.java:140` و `res/values/edition.xml` |
| موتور دسترسی | `MainActivity.java:6488–6650` (`isFullAccessUser`, `canUsePermission`, `permissionCatalog`, `roleCatalog`) |
| صفحه‌ها | `MainActivity.java:3644` (`renderActivePage`) |
| داشبورد مدیر | `MainActivity.java:4634` |
| مدیریت دسترسی کاربران | `MainActivity.java:20855` |
| مدیریت تحویل بار | `MainActivity.java:22669` |
| تأیید مشتری جدید | `MainActivity.java:9687–9780` |
| حقوق و پرسنل | `MeelanoHr.java` + جداول `meelano_hr_*` |
| سامانهٔ مودیان | `MeelanoTax*.java` (۸ فایل) |
| CI | `.github/workflows/build-apk.yml` |
| اسکرین‌شات خودکار | `.github/scripts/screenshots.sh`, `ui-review.sh` |
| بازبینی‌های قبلی | `docs/CODE-AUDIT-fa.md`, `docs/UI-REVIEW-fa.md`, `docs/STORE-REVIEW-fa.md` |

---

## ۱۱) وضعیت اجرای فاز ۰ و ۱ (همین نشست) — `arena/01a0efdc-modiriat`

پس از تأیید شما (نام «مدیریت»، پالت روشن متمایز، آیکون هم‌رنگ با المان مدیریتی، دامنهٔ مشاهده+تأییدها+پرسنل،
شروع از فاز ۰+۱)، موارد زیر **اجرا و commit** شد:

### فاز ۰ — استخراج و هم‌سان‌سازی
- کل پروژه از `Modirat.zip` به ریشهٔ مخزن باز شد (`MEELANO-Android/`, `docs/`, `.github/`, `tools/`, `.gitignore`).
- پروتوتایپ رابط کاربری در `prototype/manager/index.html` (هم‌رنگ پالت «پلاتین سرمه‌ای»).

### فاز ۱ — طعم `manager` و بازکردن قفل
- `app/build.gradle`: طعم `manager` با `applicationId 'ir.meelano.manager'` (+ کامنت).
- `app/src/main/res/values/edition.xml`: `<bool name="meelano_manager_edition">false</bool>`.
- `app/src/manager/res/values/{edition,strings,colors}.xml`: نام «پخش درخشان مدیریت»، رنگ شروع پلاتین/طلایی.
- `app/src/manager/res/mipmap-*/`: ۱۰ PNG (۵ چگالی × ساده/گرد) + `drawable-nodpi/{ic_launcher_art,meelano_3d}.png`
  آیکون نهایی (انتخاب شما): حرف **D طلایی سه‌بعدی** (امضای Derakhshan) + **تاج کوچک** (نشان نسخهٔ مدیریت) + **یک برگ پسته‌ای** (اشاره به آجیل و خشکبار) روی **زمینهٔ مرواریدی نورانی** (پالت روشن پیش‌فرض) — خلوت و لوکس. منبع: `tools/icon/manager-icon-master.png` و در همهٔ بخش‌ها (هدر، ورود، اعلان، PDF، لانچر).
- `MainActivity.java` (۲۸ ویرایش، همگی با کنترل «دقیقاً یک‌بار»):
  - متغیر منبع‌محور `MANAGER_EDITION` (خط ~۱۴۱) + خواندن `R.bool.meelano_manager_edition` در `onCreate`.
  - دروازهٔ «فقط مدیر» در `showApp` → `showManagerGate()`.
  - `canUsePermission` → شاخهٔ `managerEditionPermissionAllowed` (فهرست سفید مدیر: داشبورد/گزارش/فرماندهی/پرسنل/حضور/مشتری/کالا/دسترسی/تحویل‌بار/گفتگو/دستیار/تنظیمات).
  - `showApp` دیگر `dashboard` را بازنویسی نمی‌کند؛ داک ۵تایی مدیر (خانه/گزارش‌ها/پرسنل/حضور/بیشتر) + صفحهٔ تازهٔ «بیشتر» (`renderManagerMorePage`).
  - تم پیش‌فرض `pearl_platinum` (روشن متمایز) + هدر و متن ورود مدیر + دکمه‌های «♛ مدیریت دسترسی» و «⌕ جستجوی سراسری» برای مدیر.
  - `MANAGER_UPDATE_MANIFEST_URL` به `apk/latest-manager.json` همین مخزن.
- CI: `build-apk.yml` (کپی APK مدیر + `latest-manager.json` + آرتیفکت اسکرین‌شات) و `screenshots.sh`
  (۱۰ اسکرین‌شات مدیر: ورود/داشبورد/گزارش/پرسنل/حضور/بیشتر/دسترسی/فرماندهی + تیره + تبلت).

### راستی‌آزمایی انجام‌شده در این محیط
- تجزیهٔ کامل AST `MainActivity.java` با `javalang`: **بدون خطا**؛ شمارش متدها ۱۵۲۵→۱۵۲ (فقط ۳ متد افزوده، صفر حذف).
- همهٔ ارجاع‌های جدید به منابع (`R.bool`, `R.drawable`, مipmap) موجودند؛ همهٔ XMLهای دست‌خورده well-formed؛ YAML ورک‌فلوها و `screenshots.sh` معتبر.

### محدودیت (شفاف)
در این سندباکس **کامپایل واقعی ممکن نیست**: نه JDK کامپایلر (`javac`) موجود است و نه دسترسی به
`dl.google.com`/Maven برای Android SDK و AGP. ساخت و تست واقعی روی **GitHub Actions** همین مخزن انجام می‌شود
(`gradle assembleManagerDebug` + اسکرین‌شات امولاتور). در صورت سبز نبودن CI، همین شاخه را بازبینی کنید.

# گزارش Schema آتیران مالی

**وضعیت:** `METADATA SNAPSHOT موجود — Schema زنده و معنی مالی هنوز تأیید نشده`

## منبع و زمان

- فایل GitHub: `14050603.zip` (backup کامل database با نام `Atiran`).
- زمان پایان backup در header: `2026-08-25 23:07:06`؛ برای تاریخ امروز این نسخه stale محسوب می‌شود و سرور زنده باید دوباره مقایسه شود.
- Restore و خواندن Catalog در SQL Server موقت/ایزولهٔ Workflow انجام شد؛ نام database بازیابی‌شده `Atiran2` بود.
- SQL Server محیط Restore: `16.0.4295.3`، compatibility level `120`، collation `SQL_Latin1_General_CP1256_CI_AS`.
- دامنهٔ اسکریپت snapshot: Catalog، ACL پیکربندی‌شدهٔ غیرشخصی و تعریف چند procedure/view/trigger منتخب. هیچ procedure اجرا یا ردیف database تغییر داده نشد.

## شمارش Metadata

| بخش | تعداد در backup |
|---|---:|
| شیءهای غیرسیستمی | 1,119 |
| ستون‌ها | 7,512 |
| عضوهای Index/Key | 815 |
| ستون‌های Foreign Key | 547 |
| نقش‌های reference | 7 |
| زیرسیستم‌ها | 17 |
| Permissionها | 5 |
| Formها | 498 |
| Menuها | 557 |
| Fieldها | 252 |
| `RoleFormPermission` | 0 ردیف |
| `RoleFieldPermission` | 1 ردیف |

این شمارش به معنی تعداد مجوزهای مؤثر، کاربران مجاز یا رکوردهای عملیاتی نیست. هیچ دادهٔ حساب کاربری، password/hash، permission کاربر مشخص، فروش/مشتری/مالی استخراج نشده است.

## کاندیدهای Menu/Form

Menu/Formهای واقعی موجود در backup در `ACCESS-CONTROL-fa.md` ثبت شده‌اند؛ از جمله فروش (`Invoice`)، تسویه (`TasvieFactor`)، دریافت/پرداخت حسابداری، مغایرت صندوق/بانک، گزارش POS، چک‌ها، مطالبات و گزارش login/اسناد ویرایش‌شده. این نگاشت‌ها فقط وجود Menu/Form را ثابت می‌کنند؛ نه مجوز کاربر، Query منبع، semantics ستون‌ها یا محاسبه.

یک مورد مهم: Menu دریافت وجه (`DaryaftVajh`, FormId 21) در backup دارای `SubSystemID = NULL` است و از `INNER JOIN` procedure `dbo.ProcMenuPermission` عبور نمی‌کند. این مسیر تا رفع/تأیید مالک سامانه UNKNOWN می‌ماند؛ فرم دریافت حسابداری، فرم جداگانه‌ای است.

## مسیر دریافت و نگه‌داری snapshot

- جمع‌آوری: `.github/scripts/atiran_finance_schema_snapshot.py`
- Workflow: `.github/workflows/atiran-finance-schema.yml`
- snapshotهای تولیدشده در Git فقط با AES-256 رمز شده‌اند؛ کلید خصوصی snapshot در `.arena` محلی و خارج از Git است.
- فایل‌های رمز‌شده زیر `tools/finance-schema/snapshots/` قرار دارند. JSON رمزگشایی‌شده در repository نگه‌داری نمی‌شود.
- آخرین Workflow موفق برای تکمیل Menu/Form و تعریف‌های منتخب access control: `37148901942`؛ artifact رمز‌شدهٔ همان اجرا `schema-37148901942.json.enc` است.

## Inspector داخل Android

`finance/src/main/java/ir/atiran/finance/FinanceSchemaInspector.java` در صورت اجرای دستی فقط `sys.*`، جدول/View، ستون‌ها، PK، FK، Index و نسخهٔ سرور را می‌خواند؛ ردیف عملیاتی نمی‌خواند. ماژول در GitHub Actions build شده، اما Inspector هنوز روی Android/SQL Server اجرا نشده است. نبودن FK ثبت‌شده نیز نبودن رابطهٔ منطقی را اثبات نمی‌کند.

## Unknownهای عملیاتی

| موضوع | وضعیت |
|---|---|
| Schema و Menu روی سرور زنده/امروز | `UNKNOWN — backup مورخ 2026-08-25 است` |
| مجوز مؤثر کاربر جاری و Role فعال | `UNKNOWN — ردیف هیچ کاربر/مجوز شخصی خوانده نشده` |
| منبع دقیق مبلغ/مانده و محاسبهٔ آن | `UNKNOWN — هیچ Query تجاری اجرا نشده` |
| معنی Status، تاریخ، Amount، reversals و cutoff | `UNKNOWN` |
| دادهٔ POS/صندوق/بانک/چک/مطالبات و audit واقعی | `UNKNOWN — هیچ ردیف تجاری خوانده نشده` |
| `:finance:testDebugUnitTest` و `:finance:assembleDebug` در GitHub Actions | `PASS — run 37149665009` |
| نصب/اجرای دستگاه، Release signing و end-to-end | `NOT RUN` |

# آتیران مالی — وضعیت پیاده‌سازی

## وضعیت فعلی

ماژول مستقل Android با شناسهٔ `ir.atiran.finance` در `:finance` قرار دارد و فارسی/RTL است. این ماژول Activity و منطق ورود/Role قدیمی `:app` را کپی نمی‌کند. شاخهٔ فعلی دارای **پوستهٔ غیراجرایی، کشف Metadata و آداپتور read-only برای ACL** است؛ هنوز نسخهٔ عملیاتی مالی یا `COMPLETE` نیست.

### آنچه واقعاً انجام شده

- از فایل `14050603.zip` موجود در GitHub، backup پایگاه `Atiran` در یک SQL Server موقت و ایزوله Restore شد. آخرین snapshot موفق این شاخه از backup با پایان پشتیبان‌گیری `2026-08-25` است؛ بنابراین Schema زنده/روز نیست و باید پیش از انتشار دوباره تطبیق شود.
- Catalog و تعریف ACL غیرشخصی، Menu/Form، و چند تعریف SQL منتخب خوانده و در شاخه فقط به‌صورت رمز‌شده ذخیره شدند. Workflow هیچ procedure را اجرا نکرد.
- هیچ ردیف فروش، مشتری، مانده، بانک، چک، حساب کاربر، گذرواژه/hash یا Permission یک کاربر مشخص خوانده نشد. هیچ داده‌ای در backup تغییر نکرد.
- `FinanceAuthorizationRepository` مسیر مستقیم SQL را به‌صورت fail-closed طراحی می‌کند: هویت SQL فعلی باید غیرـ`sysadmin`/غیرـ`db_owner` باشد و دقیقاً به یک `sys_users` فعال/باز و `dbo.get_role_id` منطبق شود؛ password/hash خوانده نمی‌شود. این بررسی به دکمهٔ Debug متصل است اما با سرور زنده تأیید نشده و policy نهایی permission هنوز از مالک سامانه تأیید نشده است.
- آزمون‌های فعلی فقط منطق fail-closed محلی را بررسی می‌کنند؛ اتصال، کاربر، دسترسی یا ردیف مالی واقعی را نمی‌آزمایند. این Unit Testها در GitHub Actions `37149830102` گذشتند.
- اجرای GitHub Actions `37149830102`، Unit Test و Debug APK را موفق ساخت؛ سپس APK را روی Android 35 Emulator نصب و Activity را اجرا کرد و process برنامه را بررسی کرد.

### مواردی که هنوز اجرا/تأیید نشده‌اند

- هیچ صفحهٔ مالی ردیف تجاری نمی‌خواند و هیچ عملیات مالی نوشتنی وجود ندارد.
- prototype احراز هویت از SQL principal به `sys_users` و Role واقعی به دکمهٔ Debug وصل شده، اما هیچ session عملیاتی نگه نمی‌دارد و با سرور واقعی آزموده نشده است.
- نگاشت‌های Menu/Form در backup به‌عنوان **کاندیدهای واقعی** ثبت شده‌اند، اما مجوز کاربر جاری یا سازگاری آن‌ها با سرور روز اثبات نشده است.
- Debug Build، Unit Test و نصب/اجرای shell در Android 35 Emulator در GitHub Actions موفق است؛ تست روی گوشی فیزیکی، اتصال SQL واقعی، ورود/دسترسی کاربر و آزمون مالی end-to-end انجام نشده‌اند.
- APKهای امضاشده، نسخهٔ عملیاتی، گزارش تست کامل و SHA-256 نهایی تحویل نشده‌اند.

## مستندات

- `SCHEMA-STATUS-fa.md`: نتیجهٔ snapshot، تاریخ backup و کاندیدهای واقعی Menu/Form.
- `ACCESS-CONTROL-fa.md`: قرارداد ACL، محدودیت `ProcMenuPermission` و طرح fail-closed.
- `SECURITY-STATUS-fa.md`: وضعیت امنیتی و مانع‌های پرخطر باقی‌مانده.
- `sqlserver-schema-export.sql`: پرس‌وجوی اختیاری Catalog-only برای تطبیق مستقیم روی محیط مجاز.

## اتصال مستقیم SQL

کاربر ترجیح اتصال مستقیم را اعلام کرده است. در وضعیت فعلی اتصال داخل برنامه فقط در Debug و فقط برای Metadata است؛ فرمِ موجود **ورود کاربر آتیران یا اتصال عملیاتی مالی نیست**. هر credential فقط برای درخواست جاری استفاده می‌شود، در فایل/SharedPreferences/Log ذخیره نمی‌شود و پس از تلاش فیلد رمز پاک می‌شود. Release اتصال را مسدود می‌کند.

به‌علت قرارداد واقعی `dbo.Create_Login` در backup، اجرای این procedure در اپ ممنوع است: تعریف آن SQL login را با نقش‌های `sysadmin` و `db_owner` ایجاد می‌کند. اتصال مستقیم عملیاتی فقط پس از provision جداگانهٔ SQL principal با حداقل دسترسی، آزمون مجوزهای سمت سرور و تطبیق هویت مجاز خواهد شد. Permission check سمت Android به‌تنهایی مرز امنیتی نیست.

## اجرای تست‌های قابل‌دسترس

از ریشهٔ پروژهٔ Android:

```bash
./gradlew :finance:testDebugUnitTest
./gradlew :finance:assembleDebug
```

`testDebugUnitTest`، `assembleDebug` و smoke نصب/اجرای Activity در Android 35 Emulator در GitHub Actions run `37149830102` موفق شدند. APK خروجی فقط Debug است و در Git commit نشده؛ نصب روی گوشی فیزیکی، اتصال جداول مالی یا آزمون روی دیتابیس واقعی انجام نشده است.

## کلید امضای Release

ماژول Finance هیچ کلید fallback قدیمی را استفاده نمی‌کند. امضای Release باید با keystore جدید و secrets خارج از مخزن تأمین شود: `FINANCE_RELEASE_STORE_FILE`، `FINANCE_RELEASE_STORE_PASSWORD`، `FINANCE_RELEASE_KEY_ALIAS` و `FINANCE_RELEASE_KEY_PASSWORD`. هیچ مقدار secret را در Source، Git، Log یا گفتگو قرار ندهید.

## اقدام امنیتی فوری خارج از ماژول Finance

مخزن GitHub در حال حاضر **public** است و شامل آرشیو backup و آرشیو برنامهٔ قدیمی است. آرشیوهای عمومی را دادهٔ افشاشده فرض کنید: دسترسی مخزن را محدود کنید، backup و secretها را از تاریخچهٔ Git پاک‌سازی کنید، همهٔ SQL/application credentialهای داخل نسخهٔ قدیمی را rotate/revoke کنید و keystore قدیمی APK را بازنشسته و کلید جدید صادر کنید. این بررسی هیچ ردیف حساب یا مالی را استخراج نکرده است؛ اما عمومی‌بودن خود backup به‌تنهایی ریسک جدی است.

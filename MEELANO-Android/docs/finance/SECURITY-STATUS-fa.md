# وضعیت امنیتی آتیران مالی

## کنترل‌های موجود در پوستهٔ Finance

- ماژول مستقل `:finance` است و credential و منطق ورود نسخهٔ قدیمی `:app` را وارد Finance نمی‌کند.
- هیچ SQL username/password، test credential یا secret امضایی در Finance Source، resource یا Gradle config قرار داده نشده است.
- اتصال مستقیم در Activity فقط در Debug باز است؛ credentialهای فرم در فایل، `SharedPreferences` و Log نوشته نمی‌شوند، فیلد رمز پس از تلاش پاک می‌شود و فرم/اتصال با `FLAG_SECURE` پوشانده می‌شود. پاک‌شدن قطعی نسخه‌های موقت String در JVM تضمین‌پذیر نیست.
- Manifest، Backup برنامه را غیرفعال و cleartext را رد می‌کند.
- Queryهای Inspector ثابت و Catalog-only هستند. `FinanceAuthorizationRepository` از SQL Server به‌عنوان احراز هویت، principal جاری را به یک `sys_users` فعال/باز و Role منطبق می‌بندد و password/hash نمی‌خواند؛ این مسیر fail-closed است اما هنوز به UI وصل یا روی سرور زنده آزموده نشده است.
- Release signing از keystore fallback قدیمی استفاده نمی‌کند؛ بدون secretهای خارج از مخزن، APK قابل‌توزیع امضاشده نداریم.

## یافته‌های مهم از تعریف‌های SQL (بدون اجرا)

1. `dbo.Create_Login` با dynamic SQL، SQL login ایجاد می‌کند و آن را به نقش‌های `sysadmin` و `db_owner` اضافه می‌کند. در اپلیکیشن Finance نباید اجرا شود.
2. `dbo.ChangePassword` رشتهٔ `ALTER LOGIN` را با الحاق مستقیم username/password می‌سازد. در Finance نباید اجرا یا الگو قرار گیرد.
3. `dbo.ProcMenuPermission` نتیجهٔ Menu را از وجود grant مستقیم فرم و زیرسیستم می‌سازد اما PermissionId را فیلتر نمی‌کند؛ منو به‌تنهایی مجوز Read/Write نیست.
4. Source قدیمی Android ورود را با مقایسهٔ چند قالب password از `sys_users.user_password` انجام می‌دهد. این Finance آن منطق را کپی نمی‌کند؛ روش امن و رسمی ورود/نگاشت هویت روی سرور هنوز تأیید نشده است.
5. در نسخهٔ پشتیبان عمومی، آرشیو SQL و آرشیو برنامهٔ قدیمی در repository عمومی قابل‌دسترسی‌اند. باید آن‌ها را افشاشده فرض کرد: repository را محدود، تاریخچه را پاک‌سازی، credentialها را rotate/revoke و keystore قدیمی را بازنشسته کنید. کلید امضای جدید باید خارج از مخزن نگه‌داری شود. این کار از سوی Finance هنوز انجام/قابل تأیید نیست.

## موارد آزموده‌نشده یا باز

- GitHub Actions run `37149665009`، `:finance:testDebugUnitTest` و `:finance:assembleDebug` را موفق اجرا کرد؛ نصب یا آزمون روی دستگاه/Emulator، اجرای UI و بررسی APK هنوز انجام نشده است.
- جفت‌شدن TLS واقعی با jTDS 1.3.1 و گواهی SQL Server آزمایش نشده؛ اتصال عملیاتی Release مسدود است.
- Prototype نگاشت SQL principal به `sys_users.user_id` و Role با `dbo.get_role_id` وجود دارد، اما روی سرور زنده تأیید و به session/UI وصل نشده؛ timeout/renewal و semantics `active`/`IsLocked` نیز نیازمند آزمون/تأیید است.
- `FinanceAuthorizationRepository` سیاست ترکیب permissionهای فرم و زیرسیستم را حدس نمی‌زند؛ معنای مؤثر هر action باید از مالک سیستم و رفتار server-side تأیید شود.
- چک‌های مجوز داخل APK **مرز امنیتی نیستند**. کاربر می‌تواند client را دست‌کاری یا مستقیم به SQL متصل شود؛ SQL principal باید per-user/least-privilege و server-side data access محدود داشته باشد.
- امنیت direct SQL، access به شبکه عمومی، VPN/firewall، certificate pinning، lockout و audit دسترسی هنوز تأیید نشده‌اند.
- Write/Edit/Delete، transaction، audit log، جلوگیری از replay/idempotency و بازیابی خطا اصلاً پیاده‌سازی نشده‌اند.

## Release gate

نسخهٔ عملیاتی یا `COMPLETE` صادر نمی‌شود تا: repository/credential/keystore افشاشده مهار شوند؛ SQL principalهای امن و least-privilege تأیید شوند؛ ورود و Permission مؤثر کاربر روی سرور واقعی آزموده شود؛ Schema زنده با backup تطبیق داده شود؛ semantics و محاسبات هر ماژول تأیید شوند؛ TLS نصب‌شده معتبر باشد؛ و Build، نصب، تست منفی دسترسی، سناریوهای مالی/Audit و hash APKها واقعاً ثبت شوند.

# قرارداد Access Control آتیران مالی

## منبع و دامنهٔ شواهد

این یادداشت از snapshot رمز‌شدهٔ metadata مربوط به backup عمومی `14050603.zip` تهیه شده است. backup در `2026-08-25` پایان یافته و snapshot در Workflow ایزوله SQL Server ساخته شده است. هیچ حساب کاربری، password/hash، ردیف مجوز یک کاربر مشخص یا دادهٔ مالی خوانده نشده است؛ هیچ procedure اجرا نشده است. بنابراین «مجوز مؤثر کاربر جاری» همچنان `UNKNOWN` است.

## Catalog واقعی ACL

در این backup:

- `security.Permission`: پنج نام ثبت‌شده: `1 Execute`، `2 Read`، `3 Write`، `4 Edit`، `5 Delete`.
- `security.Form`: 498 فرم؛ `security.Menu`: 557 ردیف؛ `security.SubSystem`: 17؛ `security.Field`: 252.
- `security.RoleFormPermission`: صفر ردیف؛ `security.RoleFieldPermission`: یک ردیف. این آمار، مجوزهای مستقیم کاربر را پوشش نمی‌دهد.
- نقش‌های کاتالوگ‌شده در `dbo.Roles`: مدیر، مدیر فروش، مدیر حسابداری، حسابدار، ویزیتور، کاربر و مدیر شعبه. هیچ دسترسی از نام Role استنباط نمی‌شود.
- `security.UserFormPermission` ستون‌های `user_id`, `PermissionId`, `FormId` دارد و FKهای کاتالوگ‌شدهٔ آن به `dbo.sys_users`, `security.Permission` و `security.Form` ثبت شده‌اند.
- `security.SubSystemPermission` ستون‌های `user_id`, `PermissionID`, `SubSystemID` دارد و FKهای کاتالوگ‌شده به کاربر، Permission و SubSystem وجود دارند؛ بعضی FKها در metadata به‌عنوان `is_not_trusted` علامت خورده‌اند.
- `dbo.sys_users` دارای `user_id`, `user_name`, `role_id`, `active`, `IsLocked` است. خود فیلد `user_password` عمداً از snapshot و queryهای Finance حذف شده است.

## قراردادهای SQL که واقعاً مشاهده شدند

### `dbo.get_role_id(@user__)`

تابع، `role_id` را از `dbo.sys_users` برای تطبیق `user_name = @user__` برمی‌گرداند و اگر ردیفی/نقشی پیدا نشود `-1` می‌دهد. این تابع Role واقعی دیتابیس را از متن Username حدس نمی‌زند؛ اما احراز هویت نیست و به‌تنهایی مجوز هیچ فرم/عملیاتی را ثابت نمی‌کند.

### `dbo.ProcMenuPermission(@id_user)`

Procedure منوهایی را برمی‌گرداند که `security.Menu.FormID` آن‌ها به ردیف `security.UserFormPermission.FormId` همان کاربر وصل شود و `Menu.SubSystemID` نیز به ردیف `security.SubSystemPermission.SubSystemID` همان کاربر وصل شود. سپس شناسهٔ والد منو را اضافه می‌کند.

**محدودیت قطعی:** این procedure ستون `PermissionId/PermissionID` را فیلتر نمی‌کند؛ پس نتیجه‌اش فقط قرارداد نمایش/ناوبری منو است، نه مجوز `Read`، `Write`، `Edit` یا `Delete`. همچنین منوهای دارای `SubSystemID = NULL` از `INNER JOIN` آن عبور نمی‌کنند.

### تعریف Permission و trigger

`security.FormAndFieldPermissions` برای درج در `security.PermissionTemp`، بسته به `Type`، مجوز مستقیم فرم یا فیلد با `PermissionId = 1` می‌سازد. در Catalog همین backup، نام Permission شمارهٔ 1 برابر `Execute` است. این رفتار را نباید به مجوز Read/نوشتن مالی تعمیم داد.

## نگاشت واقعی Menu ↔ Form در backup

شناسه‌ها و نام کلاس‌ها عیناً از ردیف‌های `security.Menu`، `security.SubSystem` و `security.Form` استخراج شده‌اند. موارد زیر فقط **کاندید نگاشت** هستند؛ هیچ‌کدام مجوز کاربر جاری، منبع دادهٔ تراکنشی یا روش محاسبه را اثبات نمی‌کنند. ممکن است سرور زنده از backup جدیدتر متفاوت باشد.

| حوزه | MenuID / SubSystemID / FormID | کلاس فرم ثبت‌شده | نکته |
|---|---:|---|---|
| فروش | `324 / 1 / 295` | `Invoice` | عنوان منو/فرم «فروش» |
| زمان‌بندی تسویه | `33 / 1 / 37` | `ZamanBandiTasviehFactorha` | عنوان «زمانبندی تسویه فاکتورها» |
| تسویه فاکتور | `248 / 1 / 240` | `TasvieFactor` | عنوان «تسویه فاکتورها» |
| نهایی‌سازی دریافت/پرداخت | `379 / 1 / 333` | `DaryaftSubmit` | فرم نهایی‌سازی؛ semantics عملیاتی جداگانه لازم است |
| دریافت وجه | `20 / NULL / 21` | `DaryaftVajh` | `SubSystemID` در backup تهی است؛ در نتیجه از قرارداد `ProcMenuPermission` قابل اثبات/نمایش نیست |
| دریافت حسابداری | `400 / 5 / 354` | `DaryaftInAccounting` | فرم مجزای حسابداری؛ با `DaryaftVajh` یکی فرض نمی‌شود |
| پرداخت حسابداری | `19 / 5 / 22` | `PardakhtInAccounting` | فرم مجزای حسابداری |
| مغایرت صندوق | `148 / 3 / 137` | `MoghayeratSandugh` | عنوان «بررسی مغایرت صندوق» |
| مغایرت بانک | `146 / 3 / 135` | `MoghayeratBank` | عنوان «بررسی مغایرت بانک» |
| گزارش عملکرد صندوق | `207 / 3 / 189` | `ReportAmalkardSandoogh` | گزارش؛ نه عملیات ثبت صندوق |
| گزارش عملکرد بانک | `205 / 3 / 188` | `ReportAmalkardBank` | گزارش؛ نه عملیات ثبت بانک |
| موجودی بانک | `181 / 3 / 169` | `ReportMojudiBank` | نام فرم به‌تنهایی تعریف مانده را اثبات نمی‌کند |
| POS دریافت/پرداخت | `484 / 3 / 434` | `ReportReceiveAndPayPos` | گزارش POS ثبت‌شده در منو |
| چک‌های دریافتی | `69 / 3 / 65` | `ReportCheckhayeDaryafti` | گزارش چک دریافتی |
| چک‌های پرداختی | `180 / 3 / 168` | `ReportChekhayePardakhti` | گزارش چک پرداختی |
| مدیریت چک صیادی | `431 / 1 / 384` | `ReportCheckSayad` | نام کلاس نشان‌دهندهٔ گزارش است؛ گردش‌های چک جدا هستند |
| پیگیری مطالبات | `493 / 1 / 442` | `frmReportPeygiriMotalebat` | گزارش/فهرست پیگیری |
| فاکتورهای معوق | `88 / 3 / 83` | `ReportFactorhayeMoavagh` | گزارش؛ معیار «معوق» هنوز تأیید نشده |
| بررسی تسویه فاکتور | `66 / 3 / 64` | `ReportBarrasiTasviyeFactorha` | گزارش وضعیت تسویه؛ معنای Status تأیید نشده |
| اسناد حذف/ویرایش‌شده | `542 / 3 / 483` | `frmReportDeleteOrEditedDocuments` | کاندید گزارش تغییرات سند؛ کامل‌بودن Audit اثبات نشده |
| گزارش ورود کاربران | `558 / 3 / 497` | `frmReportLoginDetails` | کاندید گزارش login؛ مجوز و دادهٔ آن خوانده نشده‌اند |

## منطق آداپتور Android

`finance/src/main/java/ir/atiran/finance/FinanceAuthorizationRepository.java` یک آداپتور read-only است و در UI استفاده نمی‌شود. ابتدا هویت SQL جاری را از `SUSER_SNAME()` و `ORIGINAL_LOGIN()` می‌گیرد، SQL principal privileged را رد می‌کند و تطبیق دقیق DB با یک `sys_users` فعال/باز را می‌طلبد؛ سپس نتیجهٔ `dbo.get_role_id` را با `sys_users.role_id` مقایسه می‌کند. نگاشت SQL principal ↔ `user_name` هنوز روی سرور زنده تأیید نشده است. پس از آن آداپتور:

1. شناسه‌های منو را از `dbo.ProcMenuPermission(?)` می‌گیرد؛
2. نام‌های فرم و زیرسیستم را از `dbo.vw_MenuInfo` می‌خواند و فقط MenuIDهای برگشتی را نگه می‌دارد؛
3. ردیف‌های مجوز را فقط برای همان `user_id` از `UserFormPermission` و `SubSystemPermission` و نام‌های واقعی `Permission` می‌خواند؛
4. Permissionهای سطح فرم و زیرسیستم را جداگانه گزارش می‌کند و **قاعدهٔ ترکیب/مجوز مؤثر را حدس نمی‌زند**.

آداپتور password/hash یا ردیف تجاری نمی‌خواند و چیزی را ذخیره نمی‌کند. آزمون‌هایش فقط fail-closed بودن ساختار محلی را می‌سنجند؛ هنوز با SQL Server یا کاربر واقعی اجرا نشده است.

## محدودیت ورود و اتصال مستقیم

کد SQL منتخب نشان می‌دهد `dbo.Create_Login` با dynamic SQL، SQL login می‌سازد و آن را به `sysadmin` و `db_owner` اضافه می‌کند؛ `dbo.ChangePassword` نیز عبارت `ALTER LOGIN` را با الحاق رشته می‌سازد. هیچ‌کدام در Workflow یا Finance اجرا نشده‌اند و در برنامه نباید فراخوانی شوند.

اتصال مستقیم تنها زمانی قابل بررسی برای Release است که مدیر SQL برای هر هویت، SQL principal جداگانه و least-privilege، کنترل سمت‌سرور و نگاشت هویت قابل‌آزمون فراهم کند. ورود کاربر نهایی، نگاشت SQL principal به `sys_users.user_id`, بررسی `active`/`IsLocked`, نحوهٔ ترکیب Permissionها، grantهای واقعی، field-level policy و policy عملیات مالی فعلاً `UNKNOWN` هستند.

## قاعدهٔ ادامهٔ کار

تا تأیید موارد بالا، منوها عملیاتی نمی‌شوند و هیچ مبلغ/مانده/تراکنش از Android قابل ثبت یا تغییر نیست. برای Write/Edit/Delete، فقط مخفی‌کردن دکمه یا بررسی سمت کلاینت کافی نیست؛ مجوز، Audit، Transaction و idempotency باید سمت SQL/سرویس قابل‌اعتماد enforce و آزموده شوند.

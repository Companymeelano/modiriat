# نگاشت اسکیمای انبار آتیران (WarehouseSchemaMapping)

> سند زندهٔ نگاشت داده برای ماژول «آتیران انبار». هر ردیف منبع اعتبار خود را دارد.
> قانون: «وجود نام جدول به معنی اجازهٔ استفاده از تمام ستون‌های آن نیست.» بنابراین فقط ستون‌هایی
> که در SQLِ اجراییِ تولید (Production) یا در کاوش متادیتا دیده شده‌اند ثبت می‌شوند؛ بقیه Unknown.
>
> منابع:
> - **[SQL-PROD]** = عبارت SQL که در خود برنامه/اسکریپت‌های اجرایی تولید وجود دارد (`res/raw/*.sql`، `MainActivity.java`) و در محیط واقعی اجرا شده است.
> - **[BAK-PRESENT]** = نام شیء/ستون در بکاپ `14050603` (بکاپ واقعی SQL Server از پایگاه Atiran2 روی سرور HAMDANI) دیده شده است — فقط تأیید *وجود*، نه نوع/نابودی.
> - **[UNKNOWN]** = هنوز متادیتای معتبر گرفته نشده؛ نباید استفاده شود.

## ۱) فاکتور فروش — سربرگ

| شیء | نقش | کلید | ستون‌های معتبر | منبع |
|---|---|---|---|---|
| `sailfact` | سربرگ فاکتور فروش | `shfacfo` (+`rdf__`) | shfacfo, rdf__, active, Status, UniqueID, [all], panevis, rdf_tahbarg, nah_par, moname, vis_rdf, tax, vazn, tafif, SumTafifAghlam, modpar, DocumentSourceID, TaeedUser, [date], done_date, shmo | [SQL-PROD] atiran_sale_invoice.sql + MainActivity |
| `FactorConfirmation` | قطعی‌سازی فاکتور | – | Date, UserName, Shfacfo, SysID, UserID | [SQL-PROD] |
| رویه `dbo.AddInvoice` | ساخت سربرگ | خروجی `@id_en` | – | [SQL-PROD] |
| رویه `dbo.FactorConfirmation` | Status=1 + پورسانت | – | – | [SQL-PROD] |

## ۲) فاکتور فروش — اقلام

| شیء | نقش | کلید/خارجی | ستون‌های معتبر | منبع |
|---|---|---|---|---|
| `subsailtemp` | جدول موقت ورود اقلام (تریگر آن را به subsailfact می‌برد) | shfacfo→sailfact | rdf__, shfacfo, shka, rdf_anbar, tedvah, tedjoz, vahprice, jozprice, bastebandi, tedbastebandi, linesum, pertafif, RDF, pervis, litakhma, active, naka, ptax, tax, avarez, PAvarez, gift, [date], done_date, UserID, vis_rdf, sysid, tafifAghlam, TafifLine, ProductionSeriesID, TEDVAHMain, TEDJOZMain, PerPromotion, PromotionValue, MultiPishFactor, TafifPos, TafifNaghd, VarietyID | [SQL-PROD] |
| `subsailfact` | اقلام فروش نهایی | shfacfo→sailfact, shka→inventory | همان ستون‌های subsailtemp (خوانده در MainActivity: SHKA, TEDVAH, TEDJOZ, LINESUM, naka, rdf_anbar, active) | [SQL-PROD] |
| `InvoiceTrigger` | کپی subsailtemp→subsailfact + ثبت گردش ka_act (act_id=20) | – | – | [SQL-PROD] (کامنت) |

## ۳) کالا و موجودی

| شیء | نقش | کلید | ستون‌های معتبر | منبع |
|---|---|---|---|---|
| `inventory` | کالا | `shka` | shka, naka, mohvah, bastebandi, mojkavah, mojkajoz | [SQL-PROD] + [BAK-PRESENT] |
| `ka_act` | دفتر گردش کالا (مبنای موجودی) | – | shka, tedvah, tedjoz, active, act_id | [SQL-PROD] |
| `anbars` | انبارها | `rdf_anbar` | rdf_anbar, Active, Base | [SQL-PROD] |
| `kagroup` | گروه کالا | – | (نام جدول تأیید) | [BAK-PRESENT] |
| `variety` | تنوع/بسته‌بندی | – | VarietyID در اقلام دیده شد | [BAK-PRESENT] |
| `forosh_price` | قیمت فروش | – | (نام جدول تأیید) | [BAK-PRESENT] |
| **موجودی (mojodi)** | **ستون نیست!** | – | موجودی از SUM روی `ka_act` × `mohvah` + لایهٔ تطبیقی محاسبه می‌شود | [SQL-PROD] |

> نکتهٔ مهم: هیچ ستون `mojodi`/`minstock`/`expiry`/`batch` قابل اتکایی پیدا نشد. بنابراین
> «کالای کم‌موجود»، «انقضا»، «FEFO» و «Lot» فعلاً **Unknown** هستند و UI جعلی برای آن‌ها ساخته نمی‌شود (§۵۳–۵۷ پرامپت).

## ۴) خرید و دریافت

| شیء | نقش | کلید | ستون‌های معتبر | منبع |
|---|---|---|---|---|
| `buyfact` | سربرگ خرید | shmo | shmo (ستون‌های دیگر Unknown) | [SQL-PROD] (EXISTS) + [BAK-PRESENT] |
| `subbuyfact` | اقلام خرید | shfac→buyfact | (نام جدول تأیید؛ ستون‌ها Unknown) | [BAK-PRESENT] |

> دریافت خرید باید پس از گرفتن متادیتای معتبر (کاوش `sys.columns`) تکمیل شود. فعلاً فقط «فهرست و مشاهدهٔ خواندنی» مجاز است.

## ۵) مشتری و سازمان

| شیء | نقش | کلید | ستون‌های معتبر | منبع |
|---|---|---|---|---|
| `CUSTOMERS` | مشتری | `SHMO` | SHMO, MONAME, man, RDF_masir | [SQL-PROD] |
| `visitors` | ویزیتور | vis_rdf | vis_rdf | [SQL-PROD] |
| `sys_users` | کاربران | – | (خواندنی) | [SQL-PROD] |
| `masir`, `Quarter`, `regions`, `CITYS` | زنجیرهٔ مسیر مشتری | rdf_masir→… | (نام‌ها تأیید) | [SQL-PROD] |

## ۶) جدول‌های متعلق به برنامه (الگوی موجود برای ماژول انبار)

برنامهٔ فعلی همین الگو را برای قابلیت‌های خود ساخته است؛ ماژول انبار باید همان الگو را با پیشوند `meelano_wh_` ادامه دهد:

| شیء | نقش | منبع |
|---|---|---|
| `meelano_access_users` | کاربران + role_key + permissions | [SQL-PROD] |
| `meelano_access_roles` | نقش‌ها + permissions | [SQL-PROD] |
| `meelano_delivery` | تحویل/دیسپچ (status, assignee, signature, receiver…) | [SQL-PROD] |
| `meelano_delivery_item` | اقلام تحویل (state, reason, changed_by) | [SQL-PROD] |
| `meelano_delivery_log` | رویداد/ممیزی تحویل | [SQL-PROD] |
| `meelano_prefactors`, `meelano_prefactor_items` | پیش‌فاکتور | [SQL-PROD] |
| `meelano_attendance`, `meelano_hr_*` | پرسنل/حضورغیاب | [SQL-PROD] |

## ۷) اشیا ناشناخته (Unknown — استفاده نشود)

`mojodi`, `minstock`, `minimum`, `expiry`, `batch`(به‌عنوان Lot معتبر), `lot`, `location/bin/shelf/rack` به‌عنوان ساختار انبارگردانی، `vw_customer` (در کاوش بکاپ غیبت کاذب داشت), `CheckTypes` (غیبت کاذب). برای هرکدام پیش از استفاده باید `sys.columns` خوانده شود.

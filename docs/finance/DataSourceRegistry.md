# DataSource Registry — «آتیران مالی» (Atiran Finance)

هر عددی که در اپ دیده می‌شود، در این سند یک ردیف دارد: جدول، ستون، فیلتر، محاسبه، سیاست
به‌روزرسانی و وضعیت اعتبار. هر موردی که با بررسی مستقیم روی دیتابیس واقعی (۱۴۰۵/۰۷/۱۲، فقط
خواندنی) تأیید نشده باشد، **UNKNOWN** علامت خورده و در اپ نمایش داده نمی‌شود.

منابع JSON خام این بررسی‌ها در همین پوشه است:
`AtiranFinanceDetail.json` (فاز ۲)، `AtiranFinanceValidation.json` (فاز ۳)،
`AtiranFinancialSchema.json`، `FinancialSchemaMapping.md` و لاگ‌های `probe-*-log.txt`.

## قواعد ثابت کل اپ

| قاعده | مقدار |
|---|---|
| تاریخ «امروز» | `CONVERT(char(10), dbo.ReturnDateServer())` — هرگز ساعت دستگاه |
| نوع تاریخ‌ها | `char(10)` شمسی (`1405/07/12`) ⇒ مقایسه رشته‌ای روی سرور معتبر است |
| واحد پول | ریال؛ ستون‌های money با scale ۴ اما مقادیر صحیح |
| نوشتن | فقط جدول‌های `dbo.meelano_fin_*`؛ نوشتن در جدول‌های اصلی آتیران ممنوع |
| محاسبه مانده | فقط خواندن `CUSTOMERS.man`؛ هیچ‌گاه محاسبه/بازنویسی در اپ |
| خطای SQL | هرگز در UI؛ فقط کد رویداد شش‌رقمی (SHA-256) |
| هر عملیات نویسنده | `FinDb.runGuarded` ⇒ Transaction + Audit + Idempotency (`meelano_fin_ops`) + Permission |

## خانه — «مرکز کنترل مالی» (`FinQueries.home`)

| # | KPI | منبع | محاسبه | فیلتر | Refresh |
|---|---|---|---|---|---|
| 1 | فروش امروز | `sailfact` | `SUM([all])`, `COUNT(*)` | `active='t' AND [date]=امروز` | ۶۰ ثانیه |
| 2 | وصول امروز | `dar` | `SUM(mab)`, `COUNT(*)` | `p=0 AND ISNULL(Active,1)=1 AND [date]=امروز` | ۶۰ ثانیه |
| 3 | نقد امروز | `dar` | `SUM(naghd)` | همان فیلتر | ۶۰ ثانیه |
| 4 | چک امروز | `dar` | `SUM(mabcheck)` | همان فیلتر | ۶۰ ثانیه |
| 5 | POS امروز | `PosDetails`⋈`dar.ghno` | `SUM(MabPos)`, `COUNT(*)` | `dar.p=0 AND dar.[date]=امروز` | ۶۰ ثانیه |
| 6 | مطالبات | `CUSTOMERS.man` | `SUM(man)>0` و `COUNT(*)` | `ISNULL(man,0)>0` | ۶۰ ثانیه |
| 7 | بانک‌ها | `BANK.MAN` | `SUM(MAN)`, `COUNT(*)` | `ISNULL(Active,1)=1` | ۳۰۰ ثانیه |
| 8 | مغایرت‌ها | `dar` + `PosDetails` | `ABS(mab − (naghd + mabcheck + POS)) > 1`, `COUNT(*)` | ماه جاری | ۶۰ ثانیه |

نمودار ۱۴ روز: به‌ازای هر روز `SUM(sailfact.[all])` و `SUM(dar.mab)` — ۲۸ پرس‌وجوی ساده با کلید روز.

هشدارها (`FinQueries.alerts`) همه شرطی‌اند؛ اگر کوئری صفر برگرداند، هشدار ساخته نمی‌شود:
سررسید امروز / معوق دریافتی / ۷ روز آینده / معوق پرداختی / مغایرت اجزای قبض / POS بدون قبض /
مانده منفی مشتری / پرونده مغایرت باز.

## فروش و وصول (`FinQueries.salesVsCollection`)

| قلم | منبع | محاسبه | وضعیت |
|---|---|---|---|
| فروش دوره | `sailfact` | `SUM([all])` | ✔ ۹۷۰ فاکتور فعال |
| وصول دوره | `dar` | `SUM(mab)` | ✔ ۸۱۲ قبض |
| نقد | `dar` | `SUM(naghd)` | ✔ ۲۷ قبض دارای نقد |
| چک | `dar` | `SUM(mabcheck)` و `SUM(ted_chk)` تعداد برگه | ✔ ۵۳ قبض دارای چک، ۷۰ ردیف چک |
| POS | `PosDetails`⋈`dar` | `SUM(MabPos)` | ✔ ۱۰۸۳ ردیف |
| وصول‌نشده | `sailfact` | `SUM([all] − MabDaryaftFactor)` | ✔ ۹۶۷ فاکتور با `bamandeh<>0` |
| مغایرت اجزای قبض | `dar` + `PosDetails` | `ABS(تفاوت) > 1` | ✔ ۶۹۴ از ۸۱۲ قبض دقیقاً می‌خواند؛ ۱۱۸ مورد اختلاف واقعی |

`agree_invoice_lines` (فاز ۳) نشان داد ارتباط خط فاکتور ↔ قبض فقط در ۴۵۸ از ۱۱۶۸ مورد برقرار است؛
به همین دلیل اپ هیچ تطبیق «فاکتور به قبض» اختراع نمی‌کند و مغایرت‌ها را به‌صورت صف مستقل نشان می‌دهد.

## مرکز چک (`FinQueries.cheques`, `chequeStatusCensus`, `paidCheques`)

| مورد | منبع | توضیح |
|---|---|---|
| نام وضعیت دریافتی | `getcheckhistorystatus.StatusName` | متن واقعی سیستم، بدون hard-code |
| نام وضعیت پرداختی | `putchk.putchk_status` + `putchkdis` | کد وضعیت + شرح |
| سررسید امروز/معوق/۷ روز | `getchk.sardate` با `?` و تاریخ سرور | ✔ all=118، overdue=29، future=89، due_today=0 |
| در خزانه | `getchk.chk_satus IN (8,9,10)` | ✔ کد ۸ = «اخذ چک از مشتری در خزانه داری»؛ ۱ = دریافت خرید/فروش (۲۱ ردیف) |
| چک باز پرداختی | `putchk_status IN (1,7)` | ✔ کد ۱ = ۶۳ ردیف (۴۹ معوق)، کد ۷ = ۱ |
| سپرده در بانک | `getchk.our_bankrdf > 0` | ✔ ۱۰ ردیف دارای شناسه صیاد |
| برگشتی | `getchk.back IN ('t','T')` | فیلد واقعی جدول |
| تقویم | اجتماع `getchk.sardate`, `putchk.sardate`, `sailfact.[date]`, `meelano_fin_followup.promise_date`, `meelano_fin_settlement.jalali_date`, `meelano_fin_recon.jalali_date` | همه تاریخ‌ها یک قالب دارند |

**UNKNOWN:** انتساب چک به اپراتور — `getchk.vis_rdf` برای همه ۱۱۸ ردیف صفر است (`with_visitor=0`).

## مطالبات (`FinQueries.receivableTotals`, `agingBands`, `receivables`)

| قلم | منبع | توضیح |
|---|---|---|
| مانده هر مشتری | `CUSTOMERS.man` | ✔ ۲۷۲۴ از ۲۷۲۴ مشتری با جمع دفتر `cust_act` مطابق (۲۱۸ مانده منفی) |
| اعتبار | `CUSTOMERS.cred`، `Sys_Mandeh_Customer.Etebar/Mandeh` | می‌تواند صفر باشد؛ در UI «بدون اعتبار ثبت‌شده» |
| فاکتور باز | `sailfact` | `bamandeh <> 0` و `MabDaryaftFactor` برای مبلغ وصول‌شده |
| سنی‌بندی ۰-۷/۸-۳۰/۳۱-۶۰/۶۱-۹۰/۹۰+ | `sailfact.[date]` با تاریخ سرور | پنجره داده فعلی ≈۱ ماه ⇒ بازه‌های بالاتر صفر و همان‌طور نمایش داده می‌شوند |
| آخرین وصول | `dar.[date]` | `p=0 AND ISNULL(Active,1)=1` |
| پیگیری/وعده | `meelano_fin_followup` | جدول اختصاصی اپ (نوشتن با Audit) |
| شرایط پرداخت مشتری | `CUSTOMERS.checkdays` | **UNKNOWN** — برای هیچ‌یک از ۲۷۲۴ مشتری مقدار ندارد ⇒ نمایش داده نمی‌شود |

## بانک‌ها (`FinQueries.banks`, `bankMovements`, `bankDaily`)

| قلم | منبع | توضیح |
|---|---|---|
| موجودی | `BANK.MAN` | فقط خواندن |
| حساب/شعبه/مالک | `BANK.SHHE`, `SHOBE`, `OWNER`, `BANKNAME` | ✔ ۸ حساب، ۷ فعال |
| شبا | `BANK.ShabaNumber` | **UNKNOWN/غیرقابل استفاده**: مقدار واقعی همه ردیف‌ها `"IR"` است ⇒ نمایش داده نمی‌شود |
| چک الکترونیک | `BANK.HaveEChecks` | برای همه false ⇒ قابلیت ارائه نمی‌شود |
| گردش | `ban_act` | `act_bed` واریز، `act_bes` برداشت، `act_date` شمسی |
| کارت‌خوان | `BANK.IsPos`, `PosDetails.PosBankRdf` | PosDetails ۱۰۸۳ ردیف |

## POS و تسویه (`FinQueries.posSummary`, `userSettlement`)

| قلم | منبع | توضیح |
|---|---|---|
| مبلغ/تعداد/کارمزد | `PosDetails.MabPos`, `Karmozd` | متصل به `dar.ghno` |
| اپراتور | `PosDetails.UserID = visitors.vis_rdf` | ✔ تأییدشده (۶ جواد لطیفی، ۳ فاطمه محمودی، ۵ elham و …) |
| به تفکیک بانک | `PosBankRdf` ⋈ `BANK.RDF` | ۷ بانک |
| بدون قبض | `NOT EXISTS (SELECT 1 FROM dar …)` | بخش مستقل مغایرت |
| پایانه | `TerminalPos` | `BankPos` خالی است |
| تسویه اپراتور (Expected) | `dar.rdf_vis` (نقد/چک/تلاش) + `PosDetails.UserID` | فقط مبالغ واقعی همان اپراتور |
| تحویل‌شده (Actual) | `meelano_fin_settlement` | فقط رکوردهای همین اپ |
| تفاوت | Expected − Actual | نمایش مستقیم، بدون تعدیل دستی |

## صندوق (`FinQueries.cashSummary`, `cashMovements`)

| قلم | منبع |
|---|---|
| موجودی | `SUM(COW.BED − COW.BES)` با `isActive=1` ✔ ۷۷ ردیف، ۴ صندوق |
| ورود/خروج روز | `COW.BED`/`COW.BES` با `DATE = تاریخ سرور` |
| نوع گردش | `COW.act_id` ⇒ {1 قبض دریافت، 2 قبض پرداخت، 50 واریز به بانک، 62 ابطال، 71 موجودی اولیه} |
| سند | `DocNumber`, `AccDocNumber`, `Ghno`, `bank_rdf` |

## مغایرت بانکی (`FinQueries.reconCases`) — جدول اختصاصی اپ

`dbo.meelano_fin_recon(case_key, kind, bank_rdf, jalali_date, amount, system_ref, bank_ref, reason, status, assigned_to, resolution, approved_by, …)`

چرخه: `open → review → resolved → confirmed`. منبع هر پرونده یکی از سه صف واقعی است:
مغایرت اجزای قبض، POS بدون قبض، گردش بانکی بدون قبض. هر گام با `runGuarded` ثبت می‌شود
(مجوز `finance_reconcile`؛ تأیید نهایی `finance_approve`).

## Audit و Idempotency

`dbo.meelano_fin_audit(op_key, username, role_key, module, action, reference, amount, before_json, after_json, device, app_version, created_at)`
`dbo.meelano_fin_ops(op_key PRIMARY KEY, module, username, result_json, created_at)`

`op_key = SHA-256(module|action|پارامترهای کلیدی)` ⇒ ارسال دوباره یک عملیات، همان نتیجه قبلی را
برمی‌گرداند و رکورد دوم ثبت نمی‌شود.

## UNKNOWN / نمایش‌داده‌نشده (خلاصه)

| مورد | دلیل |
|---|---|
| شبا (IBAN) | `ShabaNumber` فقط `"IR"` |
| چک الکترونیک | `HaveEChecks=false` در همه بانک‌ها |
| اپراتور چک | `getchk.vis_rdf=0` |
| انبار/حمل سند | `accounting_docs` (Document/DocumentDetails) صفر ردیف |
| زمان‌بندی پرداخت مشتری (`checkdays`) | برای همه مشتریان خالی |
| قالب split خط فاکتور ↔ قبض | فقط ۴۵۸/۱۱۶۸ تطابق ⇒ قاعده اختراع نمی‌شود |
| چک «بسته‌شده» پرداختی | کد ۰ = ۱۰۹ ردیف؛ معنی دقیق کد نیاز به تأیید مالک داده دارد و فعلاً فقط شمارش خام نمایش داده می‌شود |

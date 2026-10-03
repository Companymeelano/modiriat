# مماری و داده — Audit (فاز ۱ و ۲ سند مدیریتی)

> قانون: هیچ ستونی حدس زده نشده. هر ردیف زیر یا از Queryهای production موجود (ستون‌های Resolve‌شده در Runtime) آمده یا «استفاده‌نشده/نامعتبر» علامت خورده است.

## ۱) Architecture Audit
| لایه | پیاده‌سازی فعلی | وضعیت/ارتقا |
|---|---|---|
| UI | تک‌Activity (`MainActivity`) با رندر برنامه‌ای RTL، داک ۵تایی، صفحه‌ها با `showApp(page)` | حفظ؛ صفحه‌های هوشمند مدیر افزوده شد |
| Navigation | `dockKeyFor` + `canOpenPage` + whitelist نقش | حفظ |
| Data Access | متدهای `query*` داخل MainActivity با `Connection` مستقیم (jdbc) | **لایهٔ جدید `ManagerAnalytics`** = Repository خواندنی برای KPIهای جدید |
| Metadata Validation | `columns()/resolve()/resolveFlexible()` در Runtime مقابل `sys.columns` | در `ManagerAnalytics` هم‌ارزش تکرار شد (زیرساخت، نه SQL تجاری) |
| Dedupe فاکتور | `dedupeFactorSource` (ROW_NUMBER روی شماره فاکتور) | تنها منبع جمع‌های فروش/خرید — KPI جدید همان را مصرف می‌کند (برابری Dashboard↔Reports) |
| Cache/Offline | `KEY_CACHE_DASHBOARD/REPORTS` + بنر آفلاین | برای گزارش‌های مدیر افزوده شد (`cache_manager_reports`) |
| PDF | `MeelanoDailyReportPdf` (Canvas) | سربرگ برند مدیر + خلاصهٔ بازه |
| Charts | View سفارشی (`ManagerTrendChartView`) | توسعه به BarChart چندسری در فاز UI |
| Access | `canUsePermission/managerEditionPermissionAllowed` | حفظ |
| Theme | پالت‌های ثابت + `espresso_gold` مدیر | حفظ |

## ۲) Data Audit — منبع هر KPI
| KPI | جدول | ستون‌های معتبر (Runtime-resolved) | Query |
|---|---|---|---|
| فروش بازه/قبل/اسناد/طرف‌حساب/دریافتی | sailfact | date, all, shfacfo, shmo, MabDaryaftFactor/Daryaft | `ManagerAnalytics.rangeBlock` |
| خرید بازه | buyfact | DATE/date, all, shfackh, MablaghPardakht |同上 |
| روند ۷روز | sailfact | date+all (group by) | `trend` |
| مطالبات کل/تعداد | CUSTOMERS | man>0, SHMO, MONAME | `receivables` |
| بدهکاران TOP | CUSTOMERS | man desc | `debtors` |
| معوق | sailfact | tasvieh='f', t_date + dbo.dif_date_alan (تابع با hasFunction چک می‌شود) | موجود در MainActivity |
| سطل چک دریافتی | getchk | getchkmab, sarresid/getchkdate | `checkBuckets` |
| مشتری فعال/هرگز-خرید-نکرده/غیرفعال۶روز | CUSTOMERS×sailfact | shmo↔SHMO, date | `customerCategories` |
| عملکرد ویزیتور | sailfact×visitors (+vis_goals هدف) | vis_rdf↔rdf, name; vis_goals: vis_rdf/target/done (همه Resolve) | `visitorPerformance` |
| کالای پرفروش/بدون‌فروش‌دوره | subsailfact×sailfact | SHKA, naka, LINESUM, shfacfo, active='t' | `products` |
| فید فعالیت | sailfact, getchk | date, shfacfo, all, getchkmab | `activityFeed` |

## ۳) وضعیت متادیتا — به‌روزرسانی probe (CI run 37090109252)
- **تأییدشده با probe واقعی:** `Visit(VisitID,VisRdf,Shmo,Duration,DateCreated,TimeCreated,SaveLat,SaveLng,…)` → صفحهٔ «ویزیت میدانی» فعال شد. `vis_goals(rdf,baze_rdf,vis_rdf,mab,ted,Active,…)` → ستون هدف **mab** است و ستون «تحقق» وجود ندارد ⇒ تحقق = فروش واقعی ویزیتور (بدون دادهٔ ساختگی). `vw_customer(Lng,Lat,last_fact_date,…)`، `cust_act(act_bed,act_bes,date,…)`، `Sys_Mandeh_Customer(Etebar,Mandeh,…)`، `TellBook`، `sailfact_pish(gainall,taeed,Rejected,…)` نیز تأیید شدند.
- **سود تفصیلی — آزاد شد (اسکن سراسری probe)**: ستون‌های بهای خرید واقعی در `inventory` تأیید شد (`pure_buy_price`, `buy_price`, `inventory_price`, `ProductionPrice`) و `ka_act.gain` نیز وجود دارد. پیاده‌سازی: سود کالا = `subsailfact.LINESUM − TEDVAH × inventory.buy_price` (همان الگوی اثبات‌شدهٔ loadMonthlyProfit) — کارت «سود واقعی کالاها» در اتاق فروش.
- **انبارها — آزاد شد (probe)**: `anbars(rdf_anbar,name,anbardar,…)` و `inventory_anbars(rdf_anbars,shka,mojkavah,…)` و `ka_act.RdfAnbar` تأیید شد → کارت انبارها در اتاق فروش.
- **`baze(rdf,name,sta,end_,Active)` تأیید شد** → هدف دوره‌ای ویزیتور فعال شد (vis_goals.baze_rdf ⋈ بازهٔ پوشش‌دهندهٔ آخرین تاریخ فروش).
- **برنامهٔ ویزیت/تقویم: جدولی وجود ندارد** (جستجوی plan/barname/gharar/meet/taghvim/calendar در probe) → Planned-vs-Actual و تقویم زمانی همچنان مسدود و صادقانه غیرفعال.
- **تاریخ ساخت مشتری (New)**: ستون created در CUSTOMERS تأیید نشده → دستهٔ «جدید» از درخواست‌های مشتری میلو (meelano_customer_requests) می‌آید نه حدس.
- **Forecast واقعی**: وجود ندارد → عنوان «وضعیت فشار نقدینگی» (محاسبهٔ شفاف چک/وصول) حفظ می‌شود.

## ۴) خطاهای شناخته‌شدهٔ Runtime
- سندباکس به SQL Server دسترسی مستقیم ندارد (فقط CI)؛ بنابراین تأیید متادیتا = Runtime-Resolution + Usageهای production.

# -*- coding: utf-8 -*-
"""Renders the Persian RTL engineering report to PDF using persian_pdf.py.

    python3 build_report.py [out.pdf]
"""
import os, sys, hashlib
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from persian_pdf import PDF, GOLD, INK

ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
REG = '/tmp/Vazirmatn-Regular.ttf'; BOLD = '/tmp/Vazirmatn-Bold.ttf'; MED = '/tmp/Vazirmatn-Medium.ttf'
ICON = os.path.join(ROOT, 'tools', 'icon', 'atiran-warehouse-icon.jpg')
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, 'docs', 'warehouse', 'ATIRAN-WAREHOUSE-REPORT-fa.pdf')

def sha(p):
    try: return hashlib.sha256(open(p, 'rb').read()).hexdigest()
    except Exception: return '—'

p = PDF(REG, BOLD, MED)

# ---------- cover ----------
p.rect(0, 0, p.W, p.H, (0.05, 0.06, 0.09))
p.rect(0, p.H-10, p.W, 10, GOLD)
p.image(ICON, 150)
p.para('آتیران انبار', fnt='bold', size=26, rgb=(0.98, 0.92, 0.75), align='center', space_after=2)
p.para('Atiran Warehouse & Dispatch Management', fnt='med', size=11, rgb=(0.75, 0.78, 0.84), align='center', space_after=14)
p.line(180, p.y, p.W-180, p.y, GOLD, 1.0)
p.y -= 26
p.para('گزارش مهندسی، نگاشت اسکیمای واقعی و طرح اجرایی', fnt='med', size=12, rgb=(0.9, 0.9, 0.93), align='center', space_after=8)
p.para('کاربر اصلی: اسما حمدانی — نقش: سرپرست/اپراتور انبار', size=10.5, rgb=(0.85, 0.85, 0.88), align='center', space_after=6)
p.para('محیط کسب‌وکار: پخش آجیل، خشکبار و شکلات (FMCG)', size=10.5, rgb=(0.85, 0.85, 0.88), align='center', space_after=6)
p.para('تاریخ: ۱۴۰۵/۰۷/۱۱ — نسخهٔ سند 1.0.0', size=10, rgb=(0.7, 0.72, 0.78), align='center', space_after=4)
p.new_page()

# ---------- 0 executive ----------
p.heading('۰) خلاصهٔ مدیریتی', 1)
for b in [
 'پروژهٔ موجود جاوا خالص است؛ android.useAndroidX=false یعنی نه Compose، نه ViewModel/Room/Coroutines. پرامپت «Kotlin+Compose» با واقعیت ناسازگار است و طبق بند ۷ خود پرامپت، معماری فعلی ملاک است.',
 'MainActivity.java یک تک‌فایل ۲,۰۸۹,۱۴۹ بایت / ۲۶,۹۹۴ خط است. قابلیت‌های زیادی موجودند: تحویل با امضا، کنترل دسترسی نقش‌محور، ۱۳+ تم رانتایم، PDF روزانه، جستجو، نمودار، جلالی.',
 'اسما حمدانی کاربر واقعی است: login=asma، vis=4، uid=4، حساب پرسنل shmo=2701؛ همین حالا در ماژول تحویل حضور دارد.',
 'موجودی ستون نیست؛ از دفتر گردش ka_act × mohvah + لایهٔ تطبیقی کشف ستون (sys.columns) محاسبه می‌شود.',
 'ساخت APK در این sandbox غیرممکن است (نه JDK/Gradle/SDK؛ مخازن Maven/Google مسدود؛ apt بدون root). بنابراین APK واقعی این‌جا ساخته/تست نمی‌شود و وانمود هم نمی‌شود.',
 'دادهٔ نمایشی در لایهٔ designPreview و با مهار isDebuggableBuild است و همهٔ اتصال‌های DB در آن رد می‌شود؛ قانون «NO MOCK در Production» رعایت شده است.',
]: p.bullet(b)

# ---------- 1 corrections ----------
p.heading('۱) اصلاح فرض‌های پرامپت', 1)
p.table(['فرض پرامپت', 'واقعیت مشاهده‌شده', 'منبع'],
 [['Kotlin/Compose/MVVM/Room', 'جاوا خالص، useAndroidX=false، View سنتی', 'gradle.properties, app/build.gradle'],
  ['چهار تم XML', 'یک AppTheme + موتور تم رانتایم ۱۳+ شناسه', 'styles.xml, MainActivity:1235'],
  ['اسکنر موجود', 'نه مجوز CAMERA، نه ZXing/ML Kit', 'AndroidManifest.xml'],
  ['Credential امن', 'رمز SQL آرایه int و رمز keystore hard-code', 'MainActivity:211, build.gradle'],
  ['جدول mojodi/انقضا/مکان', 'وجود ندارد (Unknown)', 'کاوش بکاپ + SQL تولید']],
 [0.30, 0.42, 0.28], rtl_cols=[0, 1, 2])

# ---------- 2 build blocked ----------
p.heading('۲) وضعیت ساخت — مسدود (Build blocked)', 1)
p.code('java -version      -> command not found\n'
       'which gradle sdkmanager -> not found\n'
       'apt-get update     -> E: Permission denied (13)\n'
       'curl repo1.maven.org/maven2/            -> 000 SSL_ERROR_SYSCALL\n'
       'curl dl.google.com/dl/android/maven2/   -> 000\n'
       'curl services.gradle.org/distributions/ -> 000\n'
       'curl api.github.com                     -> 200\n'
       'ls MEELANO-Android/gradle/wrapper -> No such file or directory')
p.para('نتیجه: بدون JDK 17 + Android SDK 36 + دسترسی به مخازن، APK ساخته نمی‌شود. دستورالعمل ماشین واقعی در بخش ۹ آمده است.', size=9.5)

# ---------- 3 schema ----------
p.heading('۳) نگاشت اسکیمای واقعی', 1)
p.para('سند کامل: docs/warehouse/WAREHOUSE-SCHEMA-MAPPING-fa.md. خلاصهٔ ستون‌های معتبر (تأییدشده با SQLِ اجراییِ تولید):', size=9.5)
p.table(['جدول', 'کلید', 'ستون‌های معتبر'],
 [['sailfact', 'shfacfo', 'shfacfo, rdf__, active, Status, UniqueID, [all], moname, vis_rdf, tax, vazn, tafif, modpar, TaeedUser'],
  ['subsailfact', 'shfacfo,shka', 'shka, TEDVAH, TEDJOZ, LINESUM, naka, rdf_anbar, active, VarietyID, ProductionSeriesID'],
  ['inventory', 'shka', 'shka, naka, mohvah, bastebandi, mojkavah, mojkajoz (موjodi نیست)'],
  ['ka_act', '-', 'shka, tedvah, tedjoz, active, act_id=20 (فروش)'],
  ['anbars', 'rdf_anbar', 'rdf_anbar, Active, Base'],
  ['CUSTOMERS', 'SHMO', 'SHMO, MONAME, man, RDF_masir'],
  ['buyfact/subbuyfact', 'shmo', 'فقط نام جدول تأیید؛ ستون‌ها Unknown']],
 [0.22, 0.18, 0.60], rtl_cols=[0, 1, 2])
p.bullet('کاوش رشته‌ای بکاپ 14050603 غیبت کاذب دارد (UniqueID را absent نشان داد)؛ پس منبع معتبر متادیتا اجرای atiran-bak-probe.yml یا کوئری sys.columns روی کپی بازیابی‌شده است.')

# ---------- 4 stock ----------
p.heading('۴) مدل موجودی (مهم‌ترین کشف)', 1)
p.bullet('منبع اصلی: SUM(ka_act.tedvah*mohvah + ka_act.tedjoz) با علامت per act_id.')
p.bullet('منبع جایگزین: جدول قابل‌تنظیم که ستون‌هایش در رانتایم از sys.columns کشف می‌شود (resolveWarehouseColumn).')
p.bullet('پیامد: هر تغییر موجودی باید از رویه‌های آتیران + Transaction + Permission + Audit عبور کند؛ هرگز عدد UI مستقیم نوشته نشود.')

# ---------- 5 reuse ----------
p.heading('۵) استفادهٔ مجدد — نساز، وصل کن', 1)
p.table(['نیاز', 'موجود در پروژه', 'فایل'],
 [['تحویل/دیسپچ/امضا', 'meelano_delivery(+item,log) + MeelanoSignatureView', 'MeelanoDelivery.java'],
  ['کنترل دسترسی', 'meelano_access_users/roles', 'MainActivity'],
  ['PDF روزانه', 'MeelanoDailyReportPdf', 'MainActivity'],
  ['نمودار/جلالی/جستجو/تم/آیکون', 'MeelanoCharts/Jalali/Search/موتور تم/Icons', 'متعدد'],
  ['اتصال jTDS', 'DriverManager + hidden()', 'MainActivity:4378']],
 [0.30, 0.45, 0.25], rtl_cols=[0, 1, 2])

# ---------- 6 architecture ----------
p.heading('۶) معماری پیشنهادی (بدون تخریب)', 1)
p.bullet('افزودن flavor پنجم warehouse با applicationId «ir.meelano.atiran.warehouse» مطابق الگوی فعلی تا جدا نصب شود.')
p.bullet('بستهٔ جدید ir.meelano.android.warehouse با زیربسته‌های data/domain/presentation/scanner/… ولی با سبک جاوای فعلی.')
p.bullet('جدول‌های عملیاتی با پیشوند meelano_wh_ (task/receive/count/audit) بدون لمس اسناد حسابداری آتیران.')
p.bullet('اسکنر: zxing:core (بدون AndroidX) + Camera1، وگرنه ورود دستی barcode.')

# ---------- 7 workflow ----------
p.heading('۷) جریان کاری منطبق بر دادهٔ واقعی', 1)
p.para('Purchase(buyfact) → Receiving(meelano_wh_receive) → Inventory(ka_act فقط با رویه‌های آتیران) → Sales(sailfact/subsailfact خواندنی) → Picking(meelano_wh_task با اعتبارسنجی) → Control → Dispatch(meelano_delivery) → Delivery(امضا/مختصات) → Audit(meelano_wh_audit).', size=10)

# ---------- 8 security ----------
p.heading('۸) یافته‌های امنیتی', 1)
for b in ['رمز SQL hard-code (MainActivity:211-215) → به Keystore/سرور منتقل شود.',
          'رمز keystore در build.gradle → به متغیر محیطی/CI منتقل شود.',
          'مخزن عمومی حاوی دادهٔ مشتری و رمزهاست → خصوصی‌سازی و چرخش رمز.',
          'همهٔ کوئری‌های جدید parameterized (الگوی فعلی «?»).']: p.bullet(b)

# ---------- 9 build real machine ----------
p.heading('۹) دستورالعمل ساخت روی ماشین واقعی', 1)
p.code('JDK 17 + Android SDK(platform 36) + دسترسی mavenCentral/google\n'
       'cd MEELANO-Android\n'
       './gradlew :app:assembleVisitorRelease   # sanity\n'
       './gradlew :app:assembleWarehouseDebug   # پس از افزودن flavor\n'
       'sha256sum app/build/outputs/apk/*/*.apk')

# ---------- 10 tests ----------
p.heading('۱۰) گزارش تست (آنچه واقعاً اجرا شد)', 1)
p.table(['آزمون', 'روش', 'نتیجه'],
 [['استخراج جدول/ستون واقعی', 'parse SQL تولید + کاوش بکاپ', 'قبول (با ثبت Unknown)'],
 ['صحت shaping/Bidi موتور PDF', 'unit-check سلام/آتیران/فاکتور', 'قبول'],
 ['ساخت APK', 'تلاش در sandbox', 'مسدود (§۲)'],
 ['اتصال DB', '-', 'بدون SQL Server/شبکه'],
 ['UI/RTL/تم', 'بازبینی ایستا', 'فقط بررسی کد']],
 [0.35, 0.40, 0.25], rtl_cols=[0, 1, 2])

# ---------- 12 fingerprints ----------
p.heading('۱۲) نسخه و اثرانگشت SHA-256', 1)
files = [
 ('docs/warehouse/WAREHOUSE-SCHEMA-MAPPING-fa.md',), ('docs/warehouse/ATIRAN-WAREHOUSE-REPORT-fa.md',),
 ('tools/warehouse/persian_pdf.py',), ('tools/warehouse/build_report.py',),
 ('tools/icon/atiran-warehouse-icon-1024.png',),
]
rows = []
for (f,) in files:
    rows.append([os.path.basename(f), sha(os.path.join(ROOT, f))[:32] + '…'])
p.table(['فایل', 'SHA-256 (بریده)'], rows, [0.45, 0.55], rtl_cols=[0])
p.para('SHA-256 کامل PDF همین فایل پس از تولید در انتهای گزارش نهایی درج می‌شود.', size=8.5, rgb=(0.45, 0.48, 0.53))

n = p.save(OUT)
print('pages:', n)
print('pdf sha256:', sha(OUT))
print('out:', OUT)

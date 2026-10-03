#!/usr/bin/env bash
# Live manager-app UI review: installs the manager debug APK, signs in against the REAL Atiran2
# database (GitHub runners can reach 37.143.147.19:1433), walks every manager screen, taps the
# primary buttons, and dumps the UI hierarchy for every capture. Large-font and narrow-screen
# passes are included, because "everything must be visible" is a layout claim, not a data claim.
set -u
PKG=ir.meelano.manager.debug
ACT=ir.meelano.android.MainActivity
OUT=${1:-review-manager}
mkdir -p "$OUT/xml"
W=$(adb shell wm size | tail -1 | sed 's/.*: //' | cut -dx -f1)
H=$(adb shell wm size | tail -1 | sed 's/.*: //' | cut -dx -f2)
D=$(adb shell wm density | tail -1 | sed 's/.*: //' | tr -dc 0-9)
echo "screen ${W}x${H} density ${D}" | tee "$OUT/env.txt"

adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global hide_error_dialogs 1 || true
adb install -r -g app-manager-debug.apk 2>&1 | tail -2 | tee -a "$OUT/env.txt"

dismiss_sys () {
  local i f b
  for i in 1 2 3; do
    f=$(adb shell dumpsys window | grep -m1 mCurrentFocus)
    echo "$f" | grep -qi "not responding" || return 0
    adb shell uiautomator dump /sdcard/anr.xml >/dev/null 2>&1
    b=$(adb exec-out cat /sdcard/anr.xml | grep -o 'text="Wait"[^>]*bounds="[^"]*"' | grep -o 'bounds="[^"]*"' | grep -o '[0-9]\+' | tr '\n' ' ')
    set -- $b
    if [ $# -ge 4 ]; then adb shell input tap $(( ($1 + $3) / 2 )) $(( ($2 + $4) / 2 )); else adb shell input keyevent KEYCODE_BACK; fi
    sleep 2
  done
}

dump () {  # name
  local k
  for k in 1 2 3; do
    adb shell rm -f /sdcard/u.xml
    adb shell uiautomator dump /sdcard/u.xml >/dev/null 2>&1
    adb pull /sdcard/u.xml "$OUT/xml/$1.xml" >/dev/null 2>&1 && return 0
    sleep 1
  done
  echo "dump failed: $1" >> "$OUT/env.txt"
}

cap () { dismiss_sys; adb exec-out screencap -p > "$OUT/$1.png"; dump "$1"; echo "captured $1"; }

tap_text () {  # substring [required]
  dump _tap
  local xy
  xy=$(python3 - "$OUT/xml/_tap.xml" "$1" <<'PY'
import sys, re, xml.etree.ElementTree as ET
root = ET.parse(sys.argv[1]).getroot()
for n in root.iter('node'):
    if sys.argv[2] in (n.get('text', '') + ' ' + n.get('content-desc', '')):
        x1, y1, x2, y2 = map(int, re.findall(r'\d+', n.get('bounds')))
        print((x1 + x2) // 2, (y1 + y2) // 2); break
PY
)
  if [ -n "$xy" ]; then adb shell input tap $xy; sleep 3; echo "tapped '$1' at $xy" >> "$OUT/env.txt"; return 0; fi
  echo "NOT FOUND '$1'" >> "$OUT/env.txt"; return 1
}

tap_text_exact () {  # text [min-y] -> taps the first clickable node with EXACTLY this text below min-y
  dump _tape
  local xy
  xy=$(python3 - "$OUT/xml/_tape.xml" "$1" "${2:-0}" <<'PYEOF'
import sys, re, xml.etree.ElementTree as ET
root = ET.parse(sys.argv[1]).getroot()
mins = int(sys.argv[3])
for n in root.iter('node'):
    if (n.get('text') or '').strip() == sys.argv[2] and n.get('clickable') == 'true':
        x1, y1, x2, y2 = map(int, re.findall(r'\d+', n.get('bounds')))
        if y1 >= mins and (y2 - y1) > 40:
            print((x1 + x2) // 2, (y1 + y2) // 2); break
PYEOF
)
  if [ -n "$xy" ]; then adb shell input tap $xy; sleep 3; echo "tapped exact '$1' at $xy" >> "$OUT/env.txt"; return 0; fi
  echo "NOT FOUND exact '$1'" >> "$OUT/env.txt"; return 1
}

type_into () {  # text -> focuses the first EditText, clears and types
  dump _edit
  local xy
  xy=$(python3 - "$OUT/xml/_edit.xml" <<'PY'
import sys, re, xml.etree.ElementTree as ET
root = ET.parse(sys.argv[1]).getroot()
for n in root.iter('node'):
    if 'EditText' in (n.get('class') or ''):
        x1, y1, x2, y2 = map(int, re.findall(r'\d+', n.get('bounds')))
        print((x1 + x2) // 2, (y1 + y2) // 2); break
PY
)
  [ -n "$xy" ] || { echo "no EditText for '$1'" >> "$OUT/env.txt"; return 1; }
  adb shell input tap $xy; sleep 1
  adb shell input keyevent KEYCODE_MOVE_END
  for i in $(seq 1 24); do adb shell input keyevent KEYCODE_DEL; done
  adb shell input text "$1"; sleep 1; return 0
}

# ---------------------------------------------------------------- sign in against the live server
adb shell am start -S -W -n "$PKG/$ACT" >/dev/null
sleep 8
cap 00-login
if tap_text "نام کاربری" || true; then type_into "Modir"; fi
# second EditText is the password
dump _edit2
PXY=$(python3 - "$OUT/xml/_edit2.xml" <<'PY'
import sys, re, xml.etree.ElementTree as ET
fields = []
for n in ET.parse(sys.argv[1]).getroot().iter('node'):
    if 'EditText' in (n.get('class') or ''):
        x1, y1, x2, y2 = map(int, re.findall(r'\d+', n.get('bounds')))
        fields.append(((x1 + x2) // 2, (y1 + y2) // 2))
print("%d %d" % fields[1] if len(fields) > 1 else "")
PY
)
if [ -n "$PXY" ]; then adb shell input tap $PXY; sleep 1; adb shell input text "123"; sleep 1; fi
cap 01-credentials-filled
# The password field triggers the login on IME_ACTION_DONE; a raw tap on the word «ورود» is unreliable
# because the header badge carries the same word (the first audit run tapped that badge instead).
adb shell input keyevent KEYCODE_ENTER
sleep 3
tap_text_exact "ورود" 700 || true
adb shell input keyevent 111 >/dev/null 2>&1 || true   # hide the soft keyboard
LOGIN_OK=0
for i in $(seq 1 18); do
  sleep 5
  dump _after
  if grep -q "تأییدها" "$OUT/xml/_after.xml" 2>/dev/null && grep -q "خانه" "$OUT/xml/_after.xml" 2>/dev/null; then LOGIN_OK=1; break; fi
  if grep -q "رمز عبور" "$OUT/xml/_after.xml" 2>/dev/null && [ "$i" = "6" ]; then
    tap_text_exact "ورود" 700 || true
    adb shell input keyevent 111 >/dev/null 2>&1 || true
  fi
done
echo "login_ok=$LOGIN_OK" >> "$OUT/env.txt"
cap 02-manager-home
sleep 3
cap 03-manager-home-settled

# ---------------------------------------------------------------- walk every manager screen
for name in "تأییدها" "گزارش‌ها" "پرسنل" "بیشتر"; do
  tap_text "$name" || true
  sleep 5
  cap "10-$(echo "$name" | tr ' ' '-')"
  sleep 2
  cap "11-$(echo "$name" | tr ' ' '-')-settled"
done
tap_text "خانه" || true; sleep 4

# the «بیشتر» toolbox rows -> intelligence screens
tap_text "بیشتر" || true; sleep 4
for row in "اتاق فروش" "مرکز وصول" "عملکرد ویزیتور" "هوش کالا" "اعتبار مشتریان" "مشتری‌شناسی" "دفتر حساب مشتری" "ویزیت میدانی" "سلامت اتصال" "مشتریان" "کالاها" "تحویل بار" "اتاق فرمان" "حضور و مرخصی" "گفتگو" "دستیار میلو" "مدیریت دسترسی کاربران"; do
  tap_text "بیشتر" || true; sleep 3
  if tap_text "$row"; then
    sleep 6
    cap "20-$(echo "$row" | tr ' ' '-')"
    sleep 2
    cap "21-$(echo "$row" | tr ' ' '-')-settled"
    adb shell input keyevent KEYCODE_BACK; sleep 2
  fi
done

# ---------------------------------------------------------------- large font + narrow screen pass
tap_text "خانه" || true; sleep 2
adb shell settings put system font_scale 1.30
adb shell wm size 360x740
adb shell wm density 340
adb shell am force-stop "$PKG"
adb shell am start -S -W -n "$PKG/$ACT" >/dev/null
sleep 12
cap "30-narrow-1.3x-home"
for name in "بیشتر" "گزارش‌ها"; do tap_text "$name" || true; sleep 5; cap "31-narrow-$(echo "$name" | tr ' ' '-')"; done
tap_text "بیشتر" || true; sleep 4
for row in "اتاق فروش" "مرکز وصول" "هوش کالا"; do
  tap_text "بیشتر" || true; sleep 3
  tap_text "$row" && { sleep 6; cap "32-narrow-$(echo "$row" | tr ' ' '-')"; adb shell input keyevent KEYCODE_BACK; sleep 2; }
done
adb shell wm size reset
adb shell wm density reset
adb shell settings put system font_scale 1.0

adb shell input keyevent KEYCODE_BACK || true
echo "manager review done: $(ls "$OUT"/*.png 2>/dev/null | wc -l) screenshots, $(ls "$OUT"/xml/*.xml 2>/dev/null | wc -l) dumps"

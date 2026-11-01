#!/usr/bin/env bash
# Emülatörde LifeOS'u açar, her ekrana gider ve ekran görüntüsü alır (CI için).
# Kullanım: bash scripts/screenshots.sh <çıktı_klasörü>
set -u
OUT="${1:-screenshots}"
PKG="com.lifeos.app.debug"
mkdir -p "$OUT"

./gradlew installDebug -q || exit 1
adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS 2>/dev/null || true
adb shell am start -W -n "$PKG/com.lifeos.app.MainActivity" >/dev/null
sleep 5

tap_desc() {
  local desc="$1"
  adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
  local xml
  xml=$(adb exec-out cat /sdcard/ui.xml)
  local b
  b=$(printf '%s' "$xml" | grep -o "content-desc=\"$desc\"[^>]*bounds=\"\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]\"" | head -1 \
      | grep -o '\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]')
  if [ -z "$b" ]; then
    echo "Bulunamadı: $desc"
    return 1
  fi
  local nums
  nums=$(echo "$b" | tr -c '0-9' ' ')
  set -- $nums
  adb shell input tap $(( ($1 + $3) / 2 )) $(( ($2 + $4) / 2 ))
  sleep 2
}

shot() {
  adb exec-out screencap -p > "$OUT/$1.png"
  echo "Kaydedildi: $OUT/$1.png ($(stat -c %s "$OUT/$1.png") bayt)"
}

shot 01_ana_sayfa
i=2
for d in "Görevler" "Alışkanlık" "Notlar" "Takvim" "İstatistikler" "Ayarlar"; do
  if tap_desc "$d"; then
    shot "$(printf '%02d' $i)_$(echo "$d" | tr 'A-ZÇĞİÖŞÜ' 'a-zçğiöşü')"
  fi
  i=$((i + 1))
done
exit 0

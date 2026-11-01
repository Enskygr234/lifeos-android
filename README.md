# LifeOS – Kişisel Yaşam Yönetimi (Android)

[![Android CI](https://github.com/Enskygr234/lifeos-android/actions/workflows/android.yml/badge.svg)](https://github.com/Enskygr234/lifeos-android/actions/workflows/android.yml)

Görevlerinizi, günlük hedeflerinizi, alışkanlıklarınızı ve notlarınızı tek yerde yöneten,
**tamamen çevrimdışı** çalışan, Türkçe arayüzlü modern bir Android uygulaması.
Hesap, internet bağlantısı veya ücretli servis gerekmez; tüm veriler cihazınızda kalır.

## İndir

En güncel APK: **[Releases sayfası](https://github.com/Enskygr234/lifeos-android/releases/latest)**

| Dosya | Açıklama |
|---|---|
| `LifeOS-vX.Y.Z.apk` | Release anahtarıyla imzalı, küçültülmüş (R8) sürüm – **önerilen** |
| `LifeOS-vX.Y.Z-debug.apk` | Hata ayıklama sürümü (ayrı paket adıyla yan yana kurulabilir) |
| `SHA256SUMS.txt` | Dosya doğrulama özetleri |

Kurulum: APK'yı telefona indirin → açın → "Bilinmeyen kaynaklardan yüklemeye izin ver" onayını verin.
Gereksinim: **Android 8.0 (API 26) ve üzeri.**

## Özellikler

| Bölüm | İçerik |
|---|---|
| **Ana ekran** | Günün tamamlanma yüzdesi (animasyonlu halka), günün özeti, bugünkü görevler, günlük hedefler, hızlı notlar |
| **Görevler** | Ekleme / düzenleme / silme (onaylı), öncelik (Düşük/Orta/Yüksek), son tarih + saat, kategoriler (özel kategori eklenebilir), tamamlandı işaretleme, Bugün / Yaklaşan / Gecikmiş / Tamamlanan filtreleri |
| **Alışkanlıklar** | Alışkanlık oluşturma (renk seçimi), günlük takip, mevcut ve en iyi seri (streak), 7/30 günlük oran, haftalık şerit ve aylık takvim görünümü (geçmiş günler işaretlenebilir) |
| **Not defteri** | Tam ekran düzenleyici, arama, kategoriler, favoriler, silme onayı |
| **Takvim** | Aylık ve haftalık görünüm, günlere göre görev listesi, seçili güne görev ekleme, hatırlatıcı göstergesi |
| **İstatistik** | Günlük başarı oranı, son 7 gün çubuk grafiği, son 4 hafta grafiği, kategori bazında görev ilerlemesi, alışkanlık oranları |
| **Ayarlar** | Koyu / Açık / Sistem teması, Türkçe dil, görev hatırlatıcıları ve günlük özet bildirimi (saat seçimi), JSON yedekleme ve geri yükleme |

### Günlük başarı oranı nasıl hesaplanır?
`(o gün tamamlanan görev + tamamlanan hedef + yapılan alışkanlık) / (o güne ait görev + hedef + aktif alışkanlık)`

### Bildirimler
Hatırlatıcılar **WorkManager** ile planlanır; cihaz yeniden başlatılsa da korunur. Saat seçilmeyen görevler
için hatırlatıcı 09:00'da gelir. Android 13+ cihazlarda bildirim izni ilk kullanımda istenir.
WorkManager, pil optimizasyonu nedeniyle bildirimleri birkaç dakika geciktirebilir (Android kısıtı).

### Yedekleme
Ayarlar → Yedekleme → **JSON olarak dışa aktar** ile tüm verileriniz (görev, hedef, alışkanlık, kayıtlar,
notlar, ayarlar) seçtiğiniz konuma kaydedilir. **Yedekten geri yükle** onay sonrası mevcut verileri yedektekilerle değiştirir.
Geçersiz veya başka uygulamaya ait dosyalar reddedilir.

## Teknoloji

- Kotlin 2.0, Jetpack Compose, Material 3
- Room (SQLite) – görev, hedef, alışkanlık, alışkanlık kaydı, not tabloları
- DataStore Preferences – tema ve bildirim ayarları
- WorkManager – görev hatırlatıcıları ve günlük özet
- Navigation Compose, alt navigasyon çubuğu, animasyonlu geçişler
- Grafikler Compose `Canvas` ile çizilir (harici kütüphane yok)

```
app/src/main/java/com/lifeos/app/
├── data/        Room varlıkları, DAO'lar, Repository, DataStore, JSON yedek kodlayıcı
├── domain/      Saf iş kuralları (seri, başarı oranı, bugünün görevleri)
├── work/        WorkManager worker'ları ve bildirim planlayıcı
└── ui/          Tema, ortak bileşenler, ekranlar, ViewModel
```

## Derleme

Android Studio (Ladybug veya üzeri) ile klasörü açın ya da komut satırından:

```bash
./gradlew testDebugUnitTest      # birim testleri
./gradlew assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease        # imza anahtarı yoksa app-release-unsigned.apk
./gradlew connectedDebugAndroidTest   # bağlı cihaz/emülatörde arayüz testleri
```

JDK 17 ve Android SDK 35 gerekir.

## Testler ve CI

GitHub Actions (`.github/workflows/android.yml`) her push'ta:

1. **Birim testleri** – seri hesabı, başarı oranı, bugünün görevleri, JSON yedeğin gidiş-dönüş doğruluğu, bozuk dosya reddi
2. **Debug + Release APK** derlemesi ve lint raporu
3. **Emülatör testi (API 30)** – uygulamanın açıldığını, tüm ekranların gezilebildiğini, görev/alışkanlık/not/hedef eklenebildiğini doğrular
4. `v*` etiketiyle push edildiğinde APK'ları **GitHub Releases**'e yükler

## Release imzalama

Release APK, yalnızca depo sırlarında anahtar varsa imzalanır (sahte imza kullanılmaz):

| Secret | İçerik |
|---|---|
| `LIFEOS_KEYSTORE_BASE64` | `base64 -w0 release.jks` çıktısı |
| `LIFEOS_KEYSTORE_PASSWORD` | Keystore parolası |
| `LIFEOS_KEY_ALIAS` | Anahtar takma adı |
| `LIFEOS_KEY_PASSWORD` | Anahtar parolası |

Sırlar tanımlı değilse iş akışı `LifeOS-…-release-unsigned.apk` üretir ve bunu uyarı olarak belirtir.
**Önemli:** Uygulamayı güncelleyebilmek için aynı anahtarla imzalamaya devam etmelisiniz; keystore dosyasını güvenli bir yerde yedekleyin.

## Lisans

MIT – ayrıntılar için [LICENSE](LICENSE).

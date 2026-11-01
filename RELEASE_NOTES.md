## LifeOS – kişisel yaşam yönetimi (Android 8.0+)

Görevler, günlük hedefler, alışkanlık takibi (seri/streak), not defteri, takvim, istatistikler ve JSON yedekleme.
Tamamen çevrimdışı çalışır; tüm veriler cihazda kalır.

### Hangi dosyayı indirmeliyim?

| Dosya | Açıklama |
|---|---|
| `LifeOS-vX.Y.Z-debug.apk` | **Telefona doğrudan kurulabilir.** Android hata ayıklama anahtarıyla imzalıdır. |
| `LifeOS-vX.Y.Z-release-unsigned.apk` | Küçültülmüş (R8) sürüm, **imzasızdır** – kendi anahtarınızla imzalanmadan kurulamaz. |
| `LifeOS-vX.Y.Z.apk` | Yalnızca depo sırlarına release anahtarı eklendiğinde üretilir (imzalı, önerilen). |
| `SHA256SUMS.txt` | Dosya doğrulama özetleri |

**İmza durumu:** Bu sürümde release imzalama anahtarı depo sırlarında tanımlı değildir; bu yüzden release APK imzasız yayınlanmıştır
(sahte imza kullanılmaz). Anahtar eklendiğinde iş akışı imzalı `LifeOS-vX.Y.Z.apk` dosyasını otomatik üretir – ayrıntılar README'de.

Kurulum: APK'yı açın → "Bilinmeyen kaynaklardan yüklemeye izin ver" onayını verin.

### Doğrulama
- Birim testleri (seri hesabı, başarı oranı, JSON yedek gidiş-dönüş ve bozuk dosya reddi)
- Android 11 (API 30) emülatöründe arayüz testleri: uygulama açılır, tüm ekranlar gezilir, görev/alışkanlık/not/hedef eklenir

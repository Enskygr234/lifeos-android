# LifeOS R8 kuralları
# Room, WorkManager ve Compose kendi tüketici kurallarını getirir.
# WorkManager worker sınıfları yansıma ile oluşturulur:
-keep class com.lifeos.app.work.** extends androidx.work.ListenableWorker { <init>(...); }
-keepattributes *Annotation*

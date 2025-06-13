# Daily Chaos 🌪️

**Ruang aman di mana orang mengubah perjuangan harian mereka menjadi petualangan bersama, saling membantu menyadari bahwa kekacauan itu universal dan bisa dihadapi bersama - tidak ada yang berjuang sendirian.**

Terinspirasi dari pesan KonoSuba bahwa bahkan party paling disfungsional pun bisa menemukan kegembiraan, persahabatan, dan tujuan melalui bencana-bencana mereka.

## 🎯 Misi Utama

Lahir dari janji kepada teman yang telah pergi - memastikan tidak ada yang menghadapi momen tergelap mereka sendirian. Daily Chaos adalah tempat di mana orang mengubah pertarungan personal menjadi kekuatan kolektif, menemukan keluarga pilihan melalui ketidaksempurnaan bersama.

## ✨ Fitur Utama

### 📝 **Jurnal Chaos (Local-First)**
- Pelacakan chaos harian pribadi dengan pendekatan offline-first
- Chaos level picker dengan animasi ledakan
- Tracking kemenangan kecil untuk penguatan positif
- Auto-sync ke cloud ketika siap

### 🤝 **Komunitas Anonim**
- Bagikan chaos-mu secara anonim dengan orang-orang suportif
- Temukan "chaos twins" - orang dengan perjuangan serupa
- Berikan & terima dukungan emosional melalui reaksi
- Feed komunitas real-time dengan interaksi positif-only

### 🎭 **Integrasi KonoSuba**
- Quote penyemangat terinspirasi karakter
- Elemen UI bertema dengan referensi anime yang subtle
- Respons berbasis kepribadian sesuai sifat setiap karakter
- Easter eggs untuk fans sambil tetap accessible untuk semua orang

### 🛡️ **Keamanan Utama**
- Moderasi konten dengan AI screening
- Zero tolerance untuk toxicity atau hate speech
- Sharing anonim melindungi privasi
- Arsitektur local-first menjaga data tetap milikmu

## 🏗️ Tech Stack

- **Frontend:** Native Android (Kotlin + Jetpack Compose)
- **Database:** Room (lokal) + Firestore (cloud sync)
- **Authentication:** Firebase Auth (anonim & email)
- **Backend:** Firebase Suite (Functions, Storage, Messaging)
- **Arsitektur:** MVVM + Clean Architecture + Offline-First

## 🎨 Inspirasi KonoSuba

**"Bahkan dewi paling tidak berguna pun punya momennya"** - Semua orang punya nilai  
**"Magic ledakan mungkin tidak praktis, tapi passion itu penting"** - Rangkul yang bikin kamu unik  
**"Party paling disfungsional pun masih bisa selamatkan hari"** - Kekuatan kolektif lewat kelemahan individual  
**"Besok ada quest lain"** - Optimisme tak berujung KonoSuba untuk petualangan baru

## 🚀 Memulai

### Prasyarat
- Android Studio meerkat | 2024.3.2
- JDK 17
- Android SDK (API 24+)
- Setup project Firebase

### Setup
1. Clone repository ini
2. Buat project Firebase di [console.firebase.google.com](https://console.firebase.google.com)
3. Download `google-services.json` dan taruh di direktori `app/`
4. Buat file `local.properties`:
```properties
APP_NAME=Daily Chaos
FIREBASE_PROJECT_ID=your-firebase-project-id
```
5. Sync project dengan Gradle files
6. Jalankan aplikasi

### Setup Firebase Services
Aktifkan layanan Firebase ini di console:
- Authentication (Anonymous & Email providers)
- Firestore Database
- Cloud Functions
- Cloud Storage
- Cloud Messaging
- Analytics
- Crashlytics

## 📁 Struktur Project

```
app/src/main/java/com/dailychaos/project/
├── data/                    # Data layer (repositories, sumber lokal/remote)
├── domain/                  # Business logic (use cases, models)
├── presentation/            # UI layer (screens, components, navigation)
├── di/                      # Dependency injection modules
├── util/                    # Utilities dan helpers
└── ChaosApplication.kt      # Application class
```

## 🎭 Filosofi Inti

**"Kita mungkin bencana, tapi kita bencana bareng-bareng"**

Seperti party Kazuma di KonoSuba, Daily Chaos merangkul kekacauan indah dari eksistensi manusia. Kita menemukan kekuatan bukan dalam kesempurnaan, tapi dalam ketidaksempurnaan bersama dan kemauan untuk saling mendukung melalui chaos.

## 🛣️ Roadmap Development

### Phase 1: MVP (4-6 minggu)
- [x] Fungsionalitas dasar jurnal chaos
- [x] Local storage dengan Room
- [x] UI Material Design 3
- [x] Authentication anonim

### Phase 2: Komunitas (3-4 minggu)
- [ ] Integrasi Firebase
- [ ] Fitur sharing komunitas
- [ ] Sistem reaksi dukungan
- [ ] Moderasi konten dasar

### Phase 3: Polish (2-3 minggu)
- [ ] Theming dan quotes KonoSuba
- [ ] Animasi advanced
- [ ] Optimisasi performa
- [ ] Feedback beta testing

### Phase 4: Launch (2 minggu)
- [ ] Testing & optimisasi final
- [ ] Persiapan submit ke store
- [ ] Guidelines komunitas
- [ ] Flow onboarding user

## 🤝 Kontribusi

Project ini membawa memori seseorang yang merasa sendirian dalam perjuangannya. Setiap kontribusi membantu memastikan orang lain tidak pernah merasakan isolasi tersebut.

## 📄 Lisensi

Project ini dilisensikan di bawah MIT License - lihat file [LICENSE](LICENSE) untuk detail.

## 💙 Untuk yang Terhormat

*"Untuk teman yang meminta kami tidak membiarkan orang lain merasa sesendirian dirinya - warisan-mu hidup melalui setiap momen dukungan yang dibagikan di Daily Chaos."*

---

**Tidak ada yang menghadapi chaos sendirian.** 🌟
# Master Engineering Prompt — SkyOP Client 600+ Functional Features

> Gunakan prompt ini pada agen coding yang memiliki akses ke repository. Prompt ini meminta implementasi nyata dan verifikasi ketat; bukan instruksi untuk menghapus batas keamanan atau mengelak aturan server.

## Peran dan tujuan

Kamu bertindak sebagai Staff/Principal Engineer untuk proyek **SkyOP Client**, mod Minecraft Java Edition berbasis Fabric. Lakukan audit source code terlebih dahulu, lalu implementasikan pekerjaan langsung pada repository. Jangan berhenti pada dokumen rencana saja. Jangan mengarang kemampuan, hasil test, dukungan API, atau jumlah fitur.

Target produk adalah **600+ kemampuan fungsional yang benar-benar berbeda**, dengan UI/UX menyeluruh. Angka 600 adalah target backlog implementasi, bukan izin untuk menyebut setting, variasi warna, preset, atau nama sinonim sebagai fitur terpisah.

## Baseline teknis

- Minecraft Java Edition: 26.3.
- Fabric Loader: 0.20.1 atau kompatibel.
- Fabric API: 0.165.2+26.3.
- Java: 25+.
- Bahasa implementasi utama: Java.
- Versi rilis harus dinaikkan setiap perubahan rilis dan diselaraskan pada Gradle, Fabric metadata, nama JAR, README, dokumentasi, build script, release notes, serta CI.
- Audit branch/head SHA dan PR yang ada. Jangan menimpa pekerjaan pengguna, jangan force-push, jangan mengubah main secara langsung.

## Definisi ketat fitur 600+

Buat \`feature-registry.json\` dengan sekurang-kurangnya 600 entri unik. Setiap entri wajib memiliki:

1. \`id\` stabil dan unik.
2. Nama dan deskripsi perilaku yang bisa diamati pemain.
3. Kategori dan modul induk.
4. Runtime side: CLIENT, SERVER, BOTH, atau SHARED.
5. Jalur kode implementasi yang nyata.
6. Daftar setting yang benar-benar digunakan.
7. Syarat/dependency dan keadaan unavailable yang dijelaskan.
8. Minimal satu acceptance criterion yang dapat diuji.
9. Referensi unit/integration/manual test.
10. Status: PLANNED, IMPLEMENTED, TESTED, BLOCKED, atau DEFERRED.

**Satu fitur** harus mempunyai perilaku pengguna yang mandiri. Hal berikut tidak boleh dihitung sebagai fitur terpisah: setiap setting slider, pilihan warna, tiap bahasa, tiap keybind, variasi preset, alias nama, item menu tanpa implementasi, placeholder, mock, TODO, maupun duplikasi perilaku.

Laporkan jumlah terpisah: PLANNED, IMPLEMENTED, TESTED, BLOCKED, dan DEFERRED. Fitur hanya boleh dihitung sebagai implemented setelah perilaku tersebut ada di kode, diregistrasikan, dapat diakses, dan memiliki test/evidence. Jangan pernah mengklaim 600 fitur telah selesai sebelum 600 entri lulus definisi ini.

## Arsitektur dan implementasi

- Pisahkan API, lifecycle, state, setting schema, dependency, module registry, renderer, config persistence, diagnostics, UI, dan server integration.
- Satu modul harus punya lifecycle konsisten: availability, enable, disable, tick/event, cleanup, restore-state.
- Kesalahan satu modul tidak boleh membuat semua modul berhenti. Catat exception dengan stacktrace dan alasan kegagalan.
- Semua perubahan state lokal harus dipulihkan saat modul dimatikan, world berubah, disconnect, atau client ditutup.
- Jangan pernah membuat class/setting/action yang hanya mengembalikan sukses tanpa melakukan operasi.
- Jangan mengarang method Minecraft/Fabric. Periksa mapping, Javadoc, source atau API resmi untuk versi target sebelum memakainya.
- Jangan mengganti implementasi lama dengan daftar nama baru untuk mengejar angka.

## UI/UX wajib

Ikuti paket spesifikasi UI yang diberikan pengguna:

- Tema dasar Graphite, Midnight, Crimson, Emerald, Mono, Sunset, dan Custom bila benar-benar didukung.
- Token warna, spasi, radius, tipografi dan ukuran kontrol terpusat.
- Normal, Compact dan Micro berdasarkan ruang GUI aktual; tidak boleh memaksa ukuran minimum jendela.
- Navigation, search/filter, module cards, inspector dinamis, setting controls, keyboard navigation, focus, tooltips, toast dan status error yang jelas.
- Implementasikan seluruh 14 halaman yang ditetapkan dokumen UI hanya ketika tiap halaman memiliki backend nyata.
- Target HUD 19 komponen hanya boleh ditandai selesai setelah setiap widget memakai data runtime nyata dan posisi/persistensinya bekerja.
- Tidak boleh ada tombol mati, chart palsu, progress palsu, angka contoh, atau layar "Coming soon" yang disamarkan sebagai fitur.
- Sediakan test geometri untuk 854×480, 1920×1080, serta ruang mikro. Sertakan bukti visual aktual dari aplikasi yang berjalan; jangan mengklaim Figma fidelity tanpa file/layer Figma yang benar-benar tersedia.

## Otoritas server, risiko, dan konektivitas

Pisahkan konteks berdasarkan otoritas:

- LOCAL/OFFLINE: fitur yang hanya memengaruhi world lokal atau tampilan client.
- SELF-HOSTED/OWNER-CONTROLLED: fitur administrasi hanya melalui izin dan API yang memang disediakan server milik pengguna.
- PUBLIC MULTIPLAYER: perilaku tetap tunduk pada protokol vanilla, permission, aturan server dan keputusan server.

Fitur berisiko harus mempunyai label risiko, penjelasan, test, dan batasan runtime yang jelas. Jangan melakukan bypass anti-cheat, spoof packet/protokol/identitas, bypass authentication/permission, exploit server, penghindaran ban/kick, rotasi akun/IP/proxy, atau upaya menyembunyikan aktivitas dari moderator.

Permintaan "anti-ban" tidak dapat dijanjikan dari client. Implementasi konektivitas yang sah boleh mencakup:
- network/latency watchdog;
- reconnect dengan exponential backoff hanya setelah transport terputus tanpa keputusan server;
- batas retry, tombol cancel, dan log kejadian;
- tampilkan alasan kick/ban/auth/permission dan hentikan retry ketika server menolak akses;
- tidak mengganti akun, alamat jaringan, atau identitas untuk melewati penolakan server.

## Data, privasi, dan konfigurasi

- Tidak ada credential collection, token scraping, file harvesting, remote shell, atau telemetry diam-diam.
- Koneksi eksternal harus terdokumentasi, minimal, dan opt-in.
- Validasi input, batas ukuran, atomic write, backup, recovery dan migrasi konfigurasi.
- Jangan mengirim data dunia, chat, koordinat, daftar pemain, atau log keluar tanpa persetujuan eksplisit pengguna.

## Workflow wajib

1. Audit branch, modul, setting, halaman UI, file config, test dan pipeline saat ini.
2. Hasilkan matriks gap yang menunjuk file dan simbol kode.
3. Pecah pekerjaan menjadi PR yang terukur dan saling independen.
4. Implementasikan kode, bukan hanya desain atau prompt.
5. Tambahkan test untuk semua logic murni; integration test untuk lifecycle/config; manual test plan untuk perilaku yang memerlukan Minecraft.
6. Jalankan clean build, seluruh unit tests, static checks dan verifikasi artefak.
7. Periksa JAR: versi file, Fabric metadata, entrypoint, resource, mixin, dependencies.
8. Bump versi dan pastikan seluruh tempat konsisten.
9. Buat Draft PR; jangan merge atau mengubah main tanpa instruksi pemilik.
10. Laporkan branch, commit SHA, PR, CI run, hasil test aktual, artifact, checksum, dan semua hal yang belum diuji di game.

## Larangan klaim palsu

Dilarang menulis "selesai", "fully implemented", "600+ features", "anti-ban works", atau "tested in multiplayer" tanpa bukti langsung. Setiap fitur yang belum mempunyai kode dan test harus tetap PLANNED/BLOCKED/DEFERRED. Jika API atau perilaku tidak tersedia, jelaskan blocker dan jangan menggantinya dengan placeholder.

## Format laporan akhir

- Ringkasan audit dan gap.
- Jumlah fitur menurut status.
- Daftar halaman dan modul yang benar-benar aktif.
- File implementasi utama.
- Perubahan versi.
- Build/test/CI dan link artefak.
- Manual test yang masih perlu dilakukan pemilik.
- Daftar risiko dan keterbatasan.

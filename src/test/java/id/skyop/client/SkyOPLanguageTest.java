package id.skyop.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkyOPLanguageTest {
    @Test
    void allSkyOpFourHudModulesHaveIndonesianTitlesAndDescriptions() {
        SkyOPLanguage.set("id");
        try {
            assertModule(SkyOPModuleId.WORLD_INFO_HUD, "Info Dunia",
                    "Menampilkan dimensi, bioma, dan kondisi cuaca saat ini.");
            assertModule(SkyOPModuleId.CHUNK_INFO_HUD, "Info Chunk",
                    "Menampilkan koordinat chunk dan koordinat blok lokal.");
            assertModule(SkyOPModuleId.PERFORMANCE_HUD, "HUD Performa",
                    "Menampilkan FPS dan penggunaan memori JVM secara langsung.");
            assertModule(SkyOPModuleId.SESSION_TIMER_HUD, "Timer Sesi",
                    "Menampilkan lama waktu sesi di dunia saat ini.");
            assertModule(SkyOPModuleId.FOOD_HUD, "HUD Makanan",
                    "Menampilkan tingkat lapar dan saturasi saat ini.");
            assertModule(SkyOPModuleId.AIR_HUD, "HUD Udara",
                    "Menampilkan sisa pasokan udara.");
            assertModule(SkyOPModuleId.EXPERIENCE_HUD, "HUD Pengalaman",
                    "Menampilkan level pengalaman dan progres.");
            assertModule(SkyOPModuleId.ITEM_INFO_HUD, "Info Item",
                    "Menampilkan nama item tangan utama, jumlah, dan daya tahan.");
            assertModule(SkyOPModuleId.OFFHAND_HUD, "Info Offhand",
                    "Menampilkan item dan jumlah di tangan kedua.");
            assertModule(SkyOPModuleId.BLOCK_INFO_HUD, "Info Blok",
                    "Menampilkan blok yang ditargetkan dan koordinatnya.");
            assertModule(SkyOPModuleId.ARMOR_DETAIL_HUD, "Detail Armor",
                    "Menampilkan bagian armor yang dipakai dan sisa daya tahannya.");
            assertModule(SkyOPModuleId.AUTO_HUNT, "Berburu Otomatis",
                    "Menyerang mob agresif terdekat yang sudah dimuat tanpa menggerakkan kamera.");
            assertModule(SkyOPModuleId.AUTO_BRIDGE, "Jembatan Otomatis",
                    "Menempatkan blok hotbar terpilih di bawah pemain saat bergerak.");
            assertModule(SkyOPModuleId.INFINITE_JUMP, "Lompat Berulang",
                    "Memungkinkan satu lompatan tambahan saat tombol lompat ditekan kembali di udara.");
            assertModule(SkyOPModuleId.JUMP_BOOST, "Peningkat Lompatan",
                    "Mengatur pengali dorongan lompatan lokal.");
            assertModule(SkyOPModuleId.ITEM_ESP, "ESP Item",
                    "Memasukkan entitas item yang terjatuh ke filter sorotan Entity ESP.");
            assertModule(SkyOPModuleId.WAYPOINTS, "Titik Koordinat",
                    "Menampilkan koordinat tersimpan dan jarak; tekan V untuk menyimpan titik.");
            assertModule(SkyOPModuleId.MINIMAP, "Peta Mini",
                    "Menampilkan entitas sekitar yang sudah dimuat oleh klien.");
            assertModule(SkyOPModuleId.NIGHT_VISION, "Penglihatan Malam",
                    "Meningkatkan visibilitas lokal melalui pengaturan gamma klien.");
        } finally {
            SkyOPLanguage.set("id");
        }
    }

    @Test
    void everyRegisteredModuleHasIndonesianTitleAndDescriptionTranslations() {
        for (SkyOPModuleId module : SkyOPModuleId.values()) {
            String titleKey = "module." + module.name() + ".title";
            String descriptionKey = "module." + module.name() + ".description";
            org.junit.jupiter.api.Assertions.assertTrue(
                    SkyOPLanguage.hasTranslation("id", titleKey),
                    module.name() + " Indonesian title is missing");
            org.junit.jupiter.api.Assertions.assertTrue(
                    SkyOPLanguage.hasTranslation("id", descriptionKey),
                    module.name() + " Indonesian description is missing");
        }
    }

    private static void assertModule(SkyOPModuleId module, String expectedTitle,
                                     String expectedDescription) {
        assertEquals(expectedTitle, module.title(), module.name() + " title");
        assertEquals(expectedDescription, module.description(), module.name() + " description");
    }
}

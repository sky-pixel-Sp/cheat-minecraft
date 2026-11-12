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
        } finally {
            SkyOPLanguage.set("id");
        }
    }

    private static void assertModule(SkyOPModuleId module, String expectedTitle,
                                     String expectedDescription) {
        assertEquals(expectedTitle, module.title(), module.name() + " title");
        assertEquals(expectedDescription, module.description(), module.name() + " description");
    }
}

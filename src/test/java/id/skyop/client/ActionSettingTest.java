package id.skyop.client;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ActionSettingTest {
    @Test
    void invokesRegisteredOperation() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        ActionSetting setting = new ActionSetting(
                "saveProfile1", "Save", "Save profile", "1", calls::incrementAndGet
        );

        assertEquals("1", setting.value());
        setting.execute();

        assertEquals(1, calls.get());
    }
}

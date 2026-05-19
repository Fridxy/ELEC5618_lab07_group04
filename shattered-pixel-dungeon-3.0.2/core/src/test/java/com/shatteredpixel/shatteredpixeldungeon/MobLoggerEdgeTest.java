package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.utils.MobLogger;
import com.watabou.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;

public class MobLoggerEdgeTest {

    @Test
    public void testLoggingDoesNotCrashOnIOFailure() {
        // mock FileUtils to simulate a catastrophic disk failure
        try (MockedStatic<FileUtils> mockedFileUtils = Mockito.mockStatic(FileUtils.class)) {

            // Force the system to throw an exception whenever it tries to get the log file
            mockedFileUtils.when(() -> FileUtils.getFileHandle(anyString()))
                    .thenThrow(new RuntimeException("Simulated Read-Only File System or Disk Full"));

            // attempt to log a spawn event.
            // If the try/catch in MobLogger.write() fails to swallow the error, this test will fail.
            assertDoesNotThrow(() -> {
                MobLogger.logSpawn(99, "TestRat", 5, 123456789L, 100);
            }, "MobLogger must swallow I/O exceptions so game performance and flow are not disrupted.");
        }
    }
}
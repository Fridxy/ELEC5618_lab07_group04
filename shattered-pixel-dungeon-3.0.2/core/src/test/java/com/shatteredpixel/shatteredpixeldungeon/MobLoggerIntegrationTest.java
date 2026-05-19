package com.shatteredpixel.shatteredpixeldungeon;

import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.utils.MobLogger;
import com.watabou.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MobLoggerIntegrationTest {

    @Test
    public void testLogFormatAndPersistence() {
        // Create a fake FileHandle to catch the data
        FileHandle mockFileHandle = mock(FileHandle.class);

        // Intercept LibGDX's FileUtils so it doesn't crash the test
        try (MockedStatic<FileUtils> mockedFileUtils = Mockito.mockStatic(FileUtils.class)) {

            // Whenever the logger asks for a file, give it the mock instead
            mockedFileUtils.when(() -> FileUtils.getFileHandle(anyString())).thenReturn(mockFileHandle);

            // Trigger the logger
            MobLogger.logStateTransition(1, "Rat", 2, "SLEEPING", "HUNTING");

            // Capture the exact string the logger tried to write to the file
            ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

            // Verify that writeString() was called at least once with "true" (append mode)
            verify(mockFileHandle, atLeastOnce()).writeString(logCaptor.capture(), eq(true));

            String capturedLog = logCaptor.getValue();

            // Validate the formatting rules
            assertTrue(capturedLog.contains("STATE"), "Log should contain the correct event tag.");
            assertTrue(capturedLog.contains("id=1 "), "Log should contain the formatted mob ID.");
            assertTrue(capturedLog.contains("Rat"), "Log should contain the mob type.");
            assertTrue(capturedLog.contains("SLEEPING -> HUNTING"), "Log should contain the exact state transition.");

            System.out.println("Test Passed! Intercepted Log String: " + capturedLog.trim());
        }
    }

    @Test
    public void testLoggingPerformanceUnderHighLoad() {
        // Simulate a room with 10,000 events happening at once
        int massiveMobVolume = 10000;
        long startTime = System.currentTimeMillis();

        // Spam the logger as fast as possible
        for (int i = 0; i < massiveMobVolume; i++) {
            // passing different IDs to simulate many different mobs
            MobLogger.logStateTransition(i, "SwarmRat", 15, "SLEEPING", "HUNTING");
        }

        long endTime = System.currentTimeMillis();
        long executionTimeMs = endTime - startTime;

        // 10,000 logs should be written to disk in well under 1 second (1000ms).
        // 16.6ms is the budget for a 60FPS frame. want to prove that writing a few dozen
        // logs per turn takes less than 1ms.
        assertTrue(executionTimeMs < 1000,
                "Writing 10,000 logs took " + executionTimeMs + "ms. " +
                        "It must complete in under 1000ms to prove standard turn logging (<50 mobs) " +
                        "takes less than 1ms and will not drop frame rates.");

        System.out.println("SQA Metric: 10,000 log events processed in " + executionTimeMs + "ms.");
    }
}
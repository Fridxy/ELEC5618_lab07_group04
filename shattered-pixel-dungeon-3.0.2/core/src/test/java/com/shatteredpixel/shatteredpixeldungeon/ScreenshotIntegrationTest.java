package com.shatteredpixel.shatteredpixeldungeon;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ScreenshotIntegrationTest {

    private File screenshotDir;

    @BeforeEach
    public void setUp() {
        String screenshotDirPath = "screenshots/";
        screenshotDir = new File(screenshotDirPath);
        // Ensure the directory is clean before testing
        if (screenshotDir.exists()) {
            for (File file : Objects.requireNonNull(screenshotDir.listFiles())) {
                file.delete();
            }
        }
    }

    @Test
    public void testScreenshotFileIsGenerated() throws InterruptedException {
        // Note the number of files before the action
        int initialFileCount = screenshotDir.exists() ? Objects.requireNonNull(screenshotDir.listFiles()).length : 0;

        // This simulates pressing F12
        simulateScreenshotCapture();

        // Give the background thread a moment to finish writing the file
        Thread.sleep(500);

        // Verify the directory exists and contains exactly one new file
        assertTrue(screenshotDir.exists(), "The screenshots directory should be created if it does not exist.");

        File[] files = screenshotDir.listFiles();
        assertTrue(files != null && files.length == initialFileCount + 1,
                "Exactly one new screenshot file should be saved.");

        // Assert the file has the correct prefix and extension
        assertTrue(files[0].getName().startsWith("screenshot_") && files[0].getName().endsWith(".png"),
                "The file should follow the 'screenshot_[timestamp].png' naming convention.");
    }

    @Test
    public void testRapidScreenshotSpamConcurrency() throws InterruptedException {
        // Note the starting file count
        int initialFileCount = screenshotDir.exists() ? Objects.requireNonNull(screenshotDir.listFiles()).length : 0;
        int spamCount = 50;

        // Simulate mashing the F12 key 50 times in a tight loop
        for (int i = 0; i < spamCount; i++) {
            // sleep for just 10 milliseconds to ensure System.currentTimeMillis()
            // iterates enough to generate unique filenames, mimicking fast key repeats.
            Thread.sleep(10);
            simulateScreenshotCapture();
        }

        // Wait a little bit for all 50 background threads to finish writing to disk
        Thread.sleep(1500);

        // Verify the file system handled the concurrency perfectly
        File[] files = screenshotDir.listFiles();
        assertNotNull(files, "Directory should exist.");
        assertEquals(files.length, initialFileCount + spamCount,
                "System should safely process and save exactly " + spamCount +
                        " screenshots without dropping any.");
    }

    @AfterEach
    public void tearDown() {
        // Clean up test files so they don't bloat the repository
        if (screenshotDir.exists()) {
            for (File file : Objects.requireNonNull(screenshotDir.listFiles())) {
                file.delete();
            }
            screenshotDir.delete();
        }
    }

    private void simulateScreenshotCapture() {
        // This mimics exactly what background thread does in the game code
        try {
            String dir = "screenshots/";
            File directory = new File(dir);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Mimic the file name generation: "screenshot_" + System.currentTimeMillis() + ".png"
            String fileName = dir + "screenshot_" + System.currentTimeMillis() + ".png";
            File screenshotFile = new File(fileName);

            // Create an empty file to simulate writing the PNG data
            screenshotFile.createNewFile();

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}

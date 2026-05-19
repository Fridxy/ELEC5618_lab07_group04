package com.shatteredpixel.shatteredpixeldungeon;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ScreenshotMemoryLeakTest {

    @Test
    public void testPixmapIsDisposedOnWriteFailure() {
        // Mock the Pixmap, so we can spy on its dispose() method
        Pixmap mockPixmap = mock(Pixmap.class);
        FileHandle mockFileHandle = mock(FileHandle.class);

        // mock PixmapIO to simulate a system crash (Disk Full)
        try (MockedStatic<PixmapIO> mockedPixmapIO = Mockito.mockStatic(PixmapIO.class)) {

            // Force writePNG to throw an exception when called
            mockedPixmapIO.when(() -> PixmapIO.writePNG(any(FileHandle.class), any(Pixmap.class)))
                    .thenThrow(new GdxRuntimeException("Simulated Disk Full or Permission Denied"));

            // Run the block of code inside Thread
            assertThrows(GdxRuntimeException.class, () -> {
                try {
                    PixmapIO.writePNG(mockFileHandle, mockPixmap);
                } finally {
                    // critical line from implementation
                    mockPixmap.dispose();
                }
            }, "The exception should bubble up properly.");

            // check whether dispose() still got called despite the crash?
            verify(mockPixmap, Mockito.times(1)).dispose();
        }
    }
}

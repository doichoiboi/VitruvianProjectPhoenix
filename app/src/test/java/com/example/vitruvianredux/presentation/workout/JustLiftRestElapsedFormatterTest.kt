package com.example.vitruvianredux.presentation.workout

import org.junit.Assert.assertEquals
import org.junit.Test

class JustLiftRestElapsedFormatterTest {
    @Test
    fun `formats elapsed rest seconds`() {
        assertEquals("Resting 0:00", JustLiftRestElapsedFormatter.label(0))
        assertEquals("Resting 0:09", JustLiftRestElapsedFormatter.label(9))
        assertEquals("Resting 1:05", JustLiftRestElapsedFormatter.label(65))
        assertEquals("Resting 12:00", JustLiftRestElapsedFormatter.label(720))
    }

    @Test
    fun `negative elapsed values are clamped to zero`() {
        assertEquals("Resting 0:00", JustLiftRestElapsedFormatter.label(-3))
    }
}

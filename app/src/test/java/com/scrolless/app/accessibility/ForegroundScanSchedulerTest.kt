/*
 * Copyright (C) 2026 Scrolless
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.scrolless.app.accessibility

import android.os.Handler
import android.os.Looper
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
@LooperMode(LooperMode.Mode.PAUSED)
class ForegroundScanSchedulerTest {
    private val looper = shadowOf(Looper.getMainLooper())
    private var scans = 0
    private val scheduler = ForegroundScanScheduler(Handler(Looper.getMainLooper()), { scans++ })

    @Test
    fun `continuous events cannot postpone scan deadline`() {
        repeat(6) {
            scheduler.request()
            looper.idleFor(Duration.ofMillis(10))
        }
        assertEquals(1, scans)
        scheduler.stop()
    }

    @Test
    fun `foreground recovery rescans without new events and stops on app exit`() {
        scheduler.setForeground(true)
        looper.idleFor(Duration.ofMillis(750))
        assertEquals(3, scans)
        scheduler.setForeground(false)
        looper.idleFor(Duration.ofSeconds(2))
        assertEquals(3, scans)
        scheduler.stop()
    }

    @Test
    fun `stop cancels queued events and recovery on screen off or destroy`() {
        scheduler.setForeground(true)
        scheduler.request()
        scheduler.stop()
        looper.idleFor(Duration.ofSeconds(2))
        assertEquals(0, scans)
    }

    @Test
    fun `recovery does not duplicate a pending event scan`() {
        scheduler.setForeground(true)
        looper.idleFor(Duration.ofMillis(220))
        scheduler.request()
        looper.idleFor(Duration.ofMillis(100))
        assertEquals(1, scans)
        scheduler.stop()
    }
}

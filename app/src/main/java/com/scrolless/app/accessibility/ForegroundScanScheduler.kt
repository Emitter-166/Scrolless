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

/** Coalesce event storms without sliding the deadline; poll only while a target is foreground. */
internal class ForegroundScanScheduler(
    private val handler: Handler,
    private val scan: () -> Unit,
    private val eventDelayMillis: Long = 60L,
    private val recoveryDelayMillis: Long = 250L,
) {
    private var pending = false
    private var foreground = false
    private val eventScan = Runnable {
        pending = false
        scan()
    }
    private val recoveryScan = object : Runnable {
        override fun run() {
            if (!foreground) return
            // One scan replaces both recovery and queued events; never scan twice per tick.
            handler.removeCallbacks(eventScan)
            pending = false
            scan()
            if (foreground) handler.postDelayed(this, recoveryDelayMillis)
        }
    }

    fun request() {
        if (pending) return
        pending = true
        handler.postDelayed(eventScan, eventDelayMillis)
    }

    fun setForeground(active: Boolean) {
        if (foreground == active) return
        foreground = active
        handler.removeCallbacks(recoveryScan)
        if (active) handler.postDelayed(recoveryScan, recoveryDelayMillis)
    }

    fun stop() {
        foreground = false
        pending = false
        handler.removeCallbacks(eventScan)
        handler.removeCallbacks(recoveryScan)
    }
}

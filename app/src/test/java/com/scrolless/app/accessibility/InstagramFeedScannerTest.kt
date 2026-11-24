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

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.scrolless.app.core.model.BlockableApp
import com.scrolless.app.core.model.ContentBlockAction
import com.scrolless.app.core.model.ResolvedBlockableApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28, 35], shadows = [IndexedTestNode::class])
class InstagramFeedScannerTest {
    private val service = Robolectric.buildService(TestAccessibilityService::class.java).create().get()
    private val instagram = ResolvedBlockableApp(BlockableApp.REELS, "com.instagram.android")
    private val scanner = ContentScanner(service, { false }, { true }, { false })

    @Test
    fun `Home cover never gains DM exemption and is removed when conversation opens`() {
        showHome()
        val detected = requireNotNull(scanner.findVisibleBlockedContent(instagram))
        assertEquals(ContentBlockAction.CoverVideoRegion, detected.blockAction)
        assertFalse(detected.blockingSuppressed)
        assertEquals(ContentBounds(0, 100, 1080, 2100), detected.cover!!.target.bounds)

        addNode("row_thread_composer_edittext", Rect(0, 1800, 1080, 2000))
        assertNull(scanner.findVisibleBlockedContent(instagram, detected.cover))
    }

    @Test
    fun `full screen Reels take priority over retained Home feed and cover`() {
        showHome()
        val home = requireNotNull(scanner.findVisibleBlockedContent(instagram))
        addNode("clips_viewer_view_pager", Rect(0, 0, 1080, 2200))
        val reels = requireNotNull(scanner.findVisibleBlockedContent(instagram, home.cover))
        assertNull(reels.cover)
        assertEquals(instagram.getBlockAction(), reels.blockAction)
    }

    @Test
    fun `profiles remain accessible even with retained Home views`() {
        showHome()
        addNode("profile_header", Rect(0, 100, 1080, 400))
        assertNull(scanner.findVisibleBlockedContent(instagram))
    }

    @Test
    fun `Reels still use Back and unrelated packages cannot trigger Instagram cover`() {
        service.showTestWindow(instagram.packageId, viewId = "clips_viewer_view_pager")
        val reels = scanner.findVisibleBlockedContent(instagram)
        assertNotNull(reels)
        assertEquals(instagram.getBlockAction(), reels!!.blockAction)
        assertNull(reels.cover)

        showHome("com.example.other")
        assertNull(scanner.findVisibleBlockedContent(instagram))
    }

    @Test
    fun `window and package events find same Home cover and app exit removes detection`() {
        showHome()
        val scan = scanner.scan(instagram.packageId, false, null, null)
        val windowScan = scanner.scan(null, true, null, null)
        assertNotNull(scan.content?.cover)
        assertEquals(scan.content, windowScan.content)

        service.showTestWindow("launcher")
        val exit = scanner.scan("launcher", true, instagram, instagram, scan.content!!.cover)
        assertEquals(true, exit.trackedAppExited)
        assertNull(exit.content)
    }

    private fun showHome(packageId: String = instagram.packageId) {
        service.showTestWindow(packageId)
        addNode("main_feed_recycler_view", Rect(0, 100, 1080, 2200), packageId = packageId)
        addNode("feed_tab", Rect(0, 2100, 200, 2200), selected = true, packageId = packageId)
        addNode("action_bar", Rect(0, 0, 1080, 100), packageId = packageId)
    }

    @Suppress("DEPRECATION")
    private fun addNode(id: String, bounds: Rect, selected: Boolean = false, packageId: String = instagram.packageId) {
        val node = AccessibilityNodeInfo.obtain().apply {
            packageName = packageId
            viewIdResourceName = "$packageId:id/$id"
            isVisibleToUser = true
            isSelected = selected
            setBoundsInScreen(bounds)
        }
        shadowOf(service.rootInActiveWindow).addChild(node)
    }
}

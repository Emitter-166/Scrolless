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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Synthetic layouts, not captures of a particular Instagram release. */
class InstagramScreenDetectorTest {
    private val detector = InstagramScreenDetector()
    private val feed = node("main_feed_recycler_view", ContentBounds(0, 100, 1080, 2200))
    private val home = node("feed_tab", ContentBounds(0, 2100, 200, 2200), selected = true)
    private val header = node("action_bar", ContentBounds(0, 0, 1080, 100))
    private val expected = ContentBounds(0, 100, 1080, 2100)
    private val nodes = listOf(feed, home, header)

    @Test
    fun `Home feed covered without covering header messaging or bottom navigation`() {
        assertEquals(expected, detector.coverBounds(nodes))
    }

    @Test
    fun `cover includes story tray and blanks space above scrolled feed beneath header`() {
        assertEquals(expected, detector.coverBounds(nodes.map { if (it == feed) it.copy(bounds = feed.bounds.copy(top = 500)) else it }))
    }

    @Test
    fun `Explore grid is covered while search header stays accessible`() {
        val explore = nodes.map {
            when (it) {
                feed -> it.copy(viewId = "explore_grid")
                home -> it.copy(viewId = "search_tab")
                else -> it
            }
        }
        assertEquals(expected, detector.coverBounds(explore))
    }

    @Test
    fun `conversations and profiles override retained Home tab and feed`() {
        val pushedScreenIds = listOf(
            "direct_inbox_recycler_view",
            "row_thread_composer_edittext",
            "profile_header_username",
            "clips_viewer_view_pager",
            "reel_viewer_root",
        )
        pushedScreenIds.forEach { id ->
            assertNull(detector.coverBounds(nodes + node(id, ContentBounds(0, 0, 100, 100)), expected))
        }
        listOf("profile_tab", "direct_tab").forEach { id ->
            assertNull(detector.coverBounds(nodes + node(id, ContentBounds(800, 2100, 1080, 2200), selected = true), expected))
        }
    }

    @Test
    fun `Home tab without a recognized feed cannot cover a profile or inbox list`() {
        assertNull(detector.coverBounds(listOf(home, header, feed.copy(viewId = "recycler_view"))))
        assertNull(detector.coverBounds(listOf(home, header), expected))
        assertNull(detector.coverBounds(nodes.map { if (it == home) it.copy(isSelected = false) else it }))
    }

    @Test
    fun `Pixel Instagram 457 android list layout is covered`() {
        assertEquals(
            ContentBounds(0, 283, 1080, 2211),
            detector.coverBounds(
                listOf(
                    node("list", ContentBounds(0, 136, 1080, 2211)).copy(isScrollable = true),
                    home.copy(bounds = ContentBounds(0, 2211, 216, 2337)),
                    header.copy(viewId = "main_feed_action_bar", bounds = ContentBounds(0, 136, 1080, 283)),
                ),
            ),
        )
    }

    @Test
    fun `renamed IDs use selected navigation role and scrollable geometry`() {
        val renamed = listOf(
            feed.copy(viewId = "unknown_list", isScrollable = true),
            home.copy(viewId = "unknown_tab", navigationRole = "home"),
            header,
        )
        assertEquals(expected, detector.coverBounds(renamed))
        assertNull(detector.coverBounds(renamed.map { if (it.navigationRole == "home") it.copy(isSelected = false) else it }))
        assertNull(detector.coverBounds(renamed + home.copy(navigationRole = "messages")))
    }

    @Test
    fun `covered feed stays blocked but offscreen cached feed cannot start cover`() {
        val occluded = nodes.map { if (it == feed) it.copy(isVisible = false) else it }
        assertNull(detector.coverBounds(occluded))
        assertEquals(expected, detector.coverBounds(occluded, expected))
        assertNull(detector.coverBounds(occluded, expected.copy(right = 2000)))
        assertNull(detector.coverBounds(nodes.map { if (it == home) it.copy(isVisible = false) else it }, expected))
    }

    @Test
    fun `missing navigation invalid bounds and landscape geometry are handled safely`() {
        assertNull(detector.coverBounds(listOf(feed, header)))
        assertNull(detector.coverBounds(nodes.map { if (it == feed) it.copy(bounds = ContentBounds(0, 0, 0, 0)) else it }))
        assertEquals(
            ContentBounds(100, 60, 1900, 900),
            detector.coverBounds(
                listOf(
                    feed.copy(bounds = ContentBounds(100, 60, 1900, 1000)),
                    home.copy(bounds = ContentBounds(100, 900, 300, 1000)),
                    header.copy(bounds = ContentBounds(100, 0, 1900, 60)),
                ),
            ),
        )
    }

    private fun node(id: String, bounds: ContentBounds, selected: Boolean = false) =
        ContentCoverNode(id, bounds, isVisible = true, isSelected = selected)
}

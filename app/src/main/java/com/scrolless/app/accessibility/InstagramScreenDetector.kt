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

import com.scrolless.app.R
import com.scrolless.app.core.model.BlockableApp

/**
 * Covers known Instagram Home/Explore content, never an arbitrary scrolling list.
 * Resource IDs are version-dependent: unknown layouts deliberately stay usable rather than
 * covering a conversation or profile. No labels, usernames, or message text are inspected.
 */
internal class InstagramScreenDetector : ContentCoverDetector {
    override val app = BlockableApp.REELS
    override val titleRes = R.string.instagram_blocked_title
    override val descriptionRes = R.string.instagram_blocked_description

    private val homeContentIds = setOf("main_feed_recycler_view", "feed_list", "feed_recycler_view")
    private val exploreContentIds = setOf("explore_grid", "explore_recycler_view", "explore_feed")
    private val homeTabIds = setOf("feed_tab")
    private val exploreTabIds = setOf("search_tab", "explore_tab")
    private val bottomBarIds = setOf("tab_bar", "tab_bar_container", "bottom_navigation_bar")
    private val headerIds = setOf("action_bar", "action_bar_container", "action_bar_wrapper")
    private val allowedScreenIds = setOf(
        "direct_inbox_recycler_view",
        "direct_inbox_layout",
        "direct_thread_recycler_view",
        "direct_thread_messages_list",
        "row_thread_composer_edittext",
        "direct_thread_input",
        "profile_header",
        "profile_header_container",
        "profile_header_username",
    )
    private val allowedTabIds = setOf("profile_tab", "direct_tab", "inbox_tab")
    private val fullScreenViewerIds = setOf("clips_viewer_view_pager", "reel_viewer_root")

    override val viewIds = homeContentIds + exploreContentIds + homeTabIds + exploreTabIds +
        bottomBarIds + headerIds + allowedScreenIds + allowedTabIds + fullScreenViewerIds

    override fun coverBounds(nodes: List<ContentCoverNode>, activeCoverBounds: ContentBounds?): ContentBounds? {
        val visible = nodes.filter { it.isVisible && it.bounds.isVisible }
        // Instagram can retain a selected Home tab underneath a pushed conversation/profile.
        // Positive allowed-screen evidence must win, including while a feed cover is active.
        if (visible.any {
                it.viewId in allowedScreenIds || it.viewId in fullScreenViewerIds ||
                    (it.viewId in allowedTabIds && it.isSelected)
            }
        ) {
            return null
        }

        val homeSelected = visible.any { it.viewId in homeTabIds && it.isSelected }
        val exploreSelected = visible.any { it.viewId in exploreTabIds && it.isSelected }
        val contentIds = when {
            homeSelected -> homeContentIds
            exploreSelected -> exploreContentIds
            else -> return null
        }
        // A tab alone is not sufficient: it can remain selected on an unrelated pushed screen.
        // Retain an occluded feed only while that feed ID and selected tab both still exist.
        val content = nodes.filter {
            it.viewId in contentIds && it.bounds.isVisible && (it.isVisible || activeCoverBounds != null)
        }.maxByOrNull { it.bounds.width.toLong() * it.bounds.height } ?: return null

        val bottomBarTop = visible.filter { it.viewId in bottomBarIds + homeTabIds + exploreTabIds + allowedTabIds }
            .map { it.bounds.top }.filter { it > content.bounds.top }.minOrNull() ?: return null
        val headerBottom = visible.filter { it.viewId in headerIds && it.bounds.bottom <= bottomBarTop }
            .maxOfOrNull { it.bounds.bottom } ?: content.bounds.top
        // Keep Instagram's header (DM button) and bottom tabs reachable. Never guess pixel offsets.
        return content.bounds.copy(
            top = headerBottom,
            bottom = minOf(content.bounds.bottom, bottomBarTop),
        ).takeIf { it.isVisible && (content.isVisible || it == activeCoverBounds) }
    }
}

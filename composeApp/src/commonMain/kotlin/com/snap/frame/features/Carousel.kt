package com.snap.frame.features

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AutoCarousel(
    itemCount: Int,
    modifier: Modifier = Modifier,
    autoScrollEnabled: Boolean = true,
    autoScrollIntervalMs: Long = 3_000L,
    dotsPaddingTop: Dp = 10.dp,
    pageContent: @Composable (itemIndex: Int) -> Unit
) {
    if (itemCount <= 0) return

    // Use virtual pages to support circular manual scrolling.
    //2147483647
    val virtualPageCount = Int.MAX_VALUE / 1000
    val initialPage = virtualPageCount / 2 -
        (virtualPageCount / 2) % itemCount

    // Tracks the current virtual pager position.
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { virtualPageCount }
    )

    // Pause auto-scroll while the user is dragging.
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    // Auto-scroll after each page settles.
    LaunchedEffect(
        autoScrollEnabled,
        isDragged,
        pagerState.settledPage,
        autoScrollIntervalMs,
        itemCount
    ) {
        if (!autoScrollEnabled || isDragged || itemCount <= 1) {
            return@LaunchedEffect
        }

        delay(autoScrollIntervalMs)

        if (!pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(
                pagerState.settledPage + 1
            )
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Displays one full carousel page at a time.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSize = PageSize.Fill,
                contentPadding = PaddingValues(0.dp),
                pageSpacing = 0.dp
            ) { page ->
                val itemIndex = page % itemCount

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    pageContent(itemIndex)
                }
            }
        }

        Spacer(Modifier.height(dotsPaddingTop))

        // Displays the current real page indicator.
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentIndex = pagerState.settledPage % itemCount

            repeat(itemCount) { index ->
                Box(
                    modifier = Modifier
                        .size(
                            width = if (index == currentIndex) 18.dp else 8.dp,
                            height = 8.dp
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (index == currentIndex) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(
                                    // 35% opaque or 65% transparent
                                    alpha = 0.35f
                                )
                            }
                        )
                )
            }
        }
    }
}

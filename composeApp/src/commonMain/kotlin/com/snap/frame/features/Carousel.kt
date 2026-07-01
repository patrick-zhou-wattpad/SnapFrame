package com.snap.frame.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    autoScrollEnabled: Boolean = false,
    autoScrollIntervalMs: Long = 3_000L,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pageSpacing: Dp = 0.dp,
    dotsPaddingTop: Dp = 10.dp,
    pageContent: @Composable (itemIndex: Int) -> Unit
) {
    val virtualCount = Int.MAX_VALUE
    val startPage = virtualCount / 2 - (virtualCount / 2) % itemCount

    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { virtualCount }
    )

    LaunchedEffect(pagerState.currentPage, autoScrollEnabled) {
        if (!autoScrollEnabled) return@LaunchedEffect
        delay(autoScrollIntervalMs)
        if (pagerState.isScrollInProgress) return@LaunchedEffect
        pagerState.animateScrollToPage(pagerState.currentPage + 1)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSize = PageSize.Fill,
                contentPadding = contentPadding,
                pageSpacing = pageSpacing
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentIndex = pagerState.currentPage % itemCount

            var index = 0
            while (index < itemCount) {
                Box(
                    modifier = Modifier
                        .size(
                            width = if (index == currentIndex) 18.dp else 8.dp,
                            height = 8.dp
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (index == currentIndex)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                )
                index++
            }
        }
    }
}

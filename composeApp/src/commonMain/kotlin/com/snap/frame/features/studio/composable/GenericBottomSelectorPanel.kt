package com.snap.frame.features.studio.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource


sealed class SelectorCircleItem {
    data class Default(
        val id: String,
        val preview: DrawableResource
    ) : SelectorCircleItem()


    data object Add : SelectorCircleItem()


    data class Custom(
        val id: String,
        val preview: DrawableResource
    ) : SelectorCircleItem()
}


@Composable
fun GenericBottomSelectorPanel(
    modifier: Modifier,
    title: String,
    items: List<SelectorCircleItem>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    onAddClicked: () -> Unit,
    onDeleteCustom: (id: String) -> Unit
) {
    val scroll = rememberScrollState()


    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .height(150.dp)
            .padding(top = 10.dp, bottom = 14.dp)
    ) {
        ColumnHeader(title = title)


        Row(
            modifier = Modifier
                .padding(top = 34.dp)
                .horizontalScroll(scroll)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val size = if (isSelected) 76.dp else 54.dp
                val scale = if (isSelected) 1.0f else 0.92f

                when (item) {
                    is SelectorCircleItem.Default -> {
                        CirclePreview(
                            modifier = Modifier,
                            size = size,
                            scale = scale,
                            preview = item.preview,
                            label = "Default",
                            selectable = true,
                            onClick = { onSelectedIndexChange(index) },
                            onLongPress = null
                        )
                    }

                    SelectorCircleItem.Add -> {
                        AddCircle(
                            size = size,
                            scale = scale,
                            onClick = onAddClicked
                        )
                    }

                    is SelectorCircleItem.Custom -> {
                        CirclePreview(
                            modifier = Modifier,
                            size = size,
                            scale = scale,
                            preview = item.preview,
                            label = "",
                            selectable = true,
                            onClick = { onSelectedIndexChange(index) },
                            onLongPress = { onDeleteCustom(item.id) }
                        )
                    }
                }


                Spacer(Modifier.width(14.dp))
            }
        }
    }
}


@Composable
private fun ColumnHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
private fun AddCircle(
    size: androidx.compose.ui.unit.Dp,
    scale: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(
                onClick = onClick,
                onLongClick = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
private fun CirclePreview(
    modifier: Modifier,
    size: androidx.compose.ui.unit.Dp,
    scale: Float,
    preview: DrawableResource,
    label: String,
    selectable: Boolean,
    onClick: () -> Unit,
    onLongPress: (() -> Unit)?
) {
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(Color.Black)
            .combinedClickable(
                onClick = { if (selectable) onClick() },
                onLongClick = onLongPress
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(preview),
            contentDescription = "preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )


        if (label.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}

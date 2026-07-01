package com.snap.frame.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.DrawableResource
import com.snap.frame.features.clickableNoRipple

@Composable
fun IconTitleRow(
    icon: DrawableResource,
    title: String,
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 18.dp,
    spacing: Dp = 8.dp,
    titleColor: Color = Color.White,
    titleSize: TextUnit = 18.sp,
    titleWeight: FontWeight = FontWeight.SemiBold
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = title,
            modifier = Modifier
                .size(iconSize)
                .clickableNoRipple { onIconClick() }
        )

        Spacer(Modifier.width(spacing))

        androidx.compose.material3.Text(
            text = title,
            color = titleColor,
            fontWeight = titleWeight,
            fontSize = titleSize
        )
    }
}

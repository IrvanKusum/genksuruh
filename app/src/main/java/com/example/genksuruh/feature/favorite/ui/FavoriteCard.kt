package com.example.genksuruh.feature.favorite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.genksuruh.feature.favorite.data.FavoriteService

@Composable
fun FavoriteCard(
    service: FavoriteService,
    onClick: () -> Unit,
    onHeartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visual = categoryStyle(service.category)
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = FavColors.Primary500.copy(alpha = 0.25f),
                spotColor = FavColors.Primary500.copy(alpha = 0.25f)
            )
            .clip(shape)
            .background(Color.White)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(visual.background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = visual.icon,
                contentDescription = null,
                tint = visual.iconTint,
                modifier = Modifier.size(52.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.92f))
                    .clickable(onClick = onHeartClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "Hapus dari favorit",
                    tint = FavColors.Coral500,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = service.title,
                style = FavType.title14,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = service.providerName,
                style = FavType.caption12,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = FavColors.Sand500,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.size(4.dp))
                Text(text = ratingText(service.rating), style = FavType.captionMain12)
                Spacer(Modifier.size(4.dp))
                Text(text = "(${service.reviewCount})", style = FavType.caption12)
            }
            Spacer(Modifier.height(8.dp))
            Text(text = "mulai dari", style = FavType.caption12)
            Text(text = rupiah(service.price), style = FavType.price14)
        }
    }
}

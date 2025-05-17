package com.rakuishi.nreader.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakuishi.nreader.R
import com.rakuishi.nreader.model.Novel
import com.rakuishi.nreader.ui.theme.NReaderTheme
import com.rakuishi.nreader.util.LocaleUtil
import com.rakuishi.nreader.util.SampleDataProvider
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NovelListItem(
    novel: Novel,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onClickNovel: (novel: Novel) -> Unit,
    onLongClickNovel: (novel: Novel) -> Unit,
) {

    val topCorner = if (isFirst) 16.dp else 4.dp
    val bottomCorner = if (isLast) 16.dp else 4.dp
    val roundedCornerShape = RoundedCornerShape(
        topStart = topCorner,
        topEnd = topCorner,
        bottomStart = bottomCorner,
        bottomEnd = bottomCorner
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp)
            .clip(roundedCornerShape)
            .background(color = MaterialTheme.colorScheme.surface)
            .combinedClickable(
                onClick = { onClickNovel.invoke(novel) },
                onLongClick = { onLongClickNovel.invoke(novel) },
            ),
        tonalElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = novel.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        R.string.novel_episode_and_updated_at,
                        novel.currentEpisodeNumber,
                        novel.latestEpisodeNumber,
                        SimpleDateFormat(
                            if (LocaleUtil.isJa()) "yyyy年M月d日" else "d LLLL yyyy",
                            if (LocaleUtil.isJa()) Locale.JAPAN else Locale.US
                        ).format(novel.latestEpisodeUpdatedAt)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (novel.hasNewEpisode) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_new_episode_24),
                    modifier = Modifier.size(18.dp),
                    contentDescription = stringResource(R.string.has_new_episode),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun NovelListItemPreview() {
    NReaderTheme {
        NovelListItem(
            novel = SampleDataProvider.novel(),
            onClickNovel = {},
            onLongClickNovel = {},
        )
    }
}

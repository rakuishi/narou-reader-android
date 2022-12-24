package com.rakuishi.narou.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.rakuishi.narou.R
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.ui.theme.NarouReaderTheme
import com.rakuishi.narou.util.LocaleUtil
import com.rakuishi.narou.util.SampleDataProvider
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NovelListItem(
    novel: Novel,
    onClickNovel: (novel: Novel) -> Unit,
    onLongClickNovel: (novel: Novel) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
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
                    .padding(8.dp)
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
                        R.string.novel_latest_episode_meta,
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

@Preview
@Composable
fun NovelListItemPreview() {
    NarouReaderTheme {
        NovelListItem(
            novel = SampleDataProvider.novel(),
            onClickNovel = {},
            onLongClickNovel = {},
        )
    }
}

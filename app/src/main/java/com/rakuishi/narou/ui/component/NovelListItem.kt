package com.rakuishi.narou.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakuishi.narou.R
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.ui.theme.NarouReaderTheme
import com.rakuishi.narou.util.SampleDataProvider
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NovelListItem(novel: Novel, onNovelClicked: (novel: Novel) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNovelClicked.invoke(novel) },
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
            )
            Text(
                text = stringResource(
                    R.string.novel_latest_episode_meta,
                    novel.currentEpisodeNumber,
                    novel.latestEpisodeNumber,
                    SimpleDateFormat(
                        "MM/dd HH:mm",
                        Locale.JAPAN
                    ).format(novel.latestEpisodeUpdatedAt)
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.secondary,
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

@Preview
@Composable
fun NovelListItemPreview() {
    NarouReaderTheme {
        NovelListItem(
            novel = SampleDataProvider.novel(),
            onNovelClicked = {}
        )
    }
}

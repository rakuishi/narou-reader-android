package com.rakuishi.nreader.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.rakuishi.nreader.R
import com.rakuishi.nreader.ui.theme.NarouReaderTheme

@Composable
fun IconDropdownMenuItem(
    @StringRes textResId: Int,
    @DrawableRes iconResId: Int,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = stringResource(textResId),
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = stringResource(textResId),
            )
        },
        onClick = onClick,
    )
}

@Preview
@Composable
fun IconDropdownMenuItemPreview() {
    NarouReaderTheme {
        DropdownMenu(
            expanded = true,
            onDismissRequest = {},
        ) {
            IconDropdownMenuItem(
                textResId = R.string.move_to_latest_episode,
                iconResId = R.drawable.ic_new_episode_24
            ) {}
        }
    }
}
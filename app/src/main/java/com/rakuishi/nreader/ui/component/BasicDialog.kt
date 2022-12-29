package com.rakuishi.nreader.ui.component

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.rakuishi.nreader.ui.theme.NarouReaderTheme

@ExperimentalMaterial3Api
@Composable
fun BasicDialog(
    @StringRes title: Int? = null,
    @StringRes message: Int? = null,
    openDialog: MutableState<Boolean>,
    @StringRes onPositiveText: Int = android.R.string.ok,
    onPositiveClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { openDialog.value = false },
        title = if (title != null) {
            { Text(text = stringResource(title)) }
        } else {
            null
        },
        text = if (message != null) {
            { Text(text = stringResource(message)) }
        } else {
            null
        },
        confirmButton = {
            TextButton(onClick = {
                openDialog.value = false
                onPositiveClick.invoke()
            }) {
                Text(text = stringResource(onPositiveText))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                openDialog.value = false
            }) {
                Text(text = stringResource(android.R.string.cancel))
            }
        },
    )
}

@ExperimentalMaterial3Api
@Preview
@Composable
fun BasicDialogPreview() {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(true) }

    NarouReaderTheme {
        BasicDialog(
            title = android.R.string.dialog_alert_title,
            message = android.R.string.dialog_alert_title,
            openDialog = openDialog,
            onPositiveClick = {
                Toast.makeText(context, android.R.string.ok, Toast.LENGTH_SHORT).show()
            }
        )
    }
}

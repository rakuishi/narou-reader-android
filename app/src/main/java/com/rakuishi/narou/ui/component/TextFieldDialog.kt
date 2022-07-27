package com.rakuishi.narou.ui.component

import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.rakuishi.narou.ui.theme.NarouReaderTheme

@ExperimentalMaterial3Api
@Composable
fun TextFieldDialog(
    title: String,
    placeholder: String,
    openDialog: MutableState<Boolean>,
    onPositiveClick: (String) -> Unit,
) {
    val text = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { openDialog.value = false },
        title = {
            Text(text = title)
        },
        text = {
            TextField(
                value = text.value,
                placeholder = {
                    Text(
                        text = placeholder,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                    )
                },
                singleLine = true,
                onValueChange = { text.value = it },
            )
        },
        confirmButton = {
            TextButton(onClick = {
                openDialog.value = false
                onPositiveClick.invoke(text.value)
            }) {
                Text(text = stringResource(android.R.string.ok))
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
fun TextFieldDialogPreview() {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(true) }

    NarouReaderTheme {
        TextFieldDialog(
            title = "Title",
            placeholder = "Placeholder",
            openDialog = openDialog,
            onPositiveClick = {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        )
    }
}

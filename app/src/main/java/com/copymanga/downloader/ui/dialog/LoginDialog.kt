package com.copymanga.downloader.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.copymanga.downloader.R
import com.copymanga.downloader.data.remote.dto.UserProfileRespData
import com.copymanga.downloader.di.AppContainer
import kotlinx.coroutines.launch

@Composable
fun LoginDialog(
    container: AppContainer,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val copyRepository = container.copyRepository
    val loginFailedText = stringResource(R.string.login_failed)

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var profile by remember { mutableStateOf<UserProfileRespData?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        copyRepository.getUserProfile()
            .onSuccess { profile = it }
            .onFailure { /* 未登录或 token 过期，留空 */ }
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.login)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                profile?.let {
                    Text(
                        text = stringResource(
                            R.string.current_user,
                            it.nickname.ifEmpty { it.username },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.username)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        errorMessage = stringResource(R.string.username_password_required)
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        copyRepository.login(username, password)
                            .onSuccess {
                                copyRepository.getUserProfile()
                                    .onSuccess { p -> profile = p }
                                    .onFailure { e -> errorMessage = e.message }
                                onDismiss()
                            }
                            .onFailure { e ->
                                errorMessage = e.message ?: loginFailedText
                            }
                        isLoading = false
                    }
                },
                enabled = !isLoading,
            ) {
                Text(stringResource(R.string.login))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

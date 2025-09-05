package com.romeodev.features.trancription.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.romeodev.core.presentation.TranscribeViewModel
import org.koin.compose.getKoin
@Composable
fun TranscribeScreen(
    vm: TranscribeViewModel = getKoin().get()
) {
    TranscribeContent(
        log = vm.log,
        onStartRecording = vm::startRecording,
        onStopAndTranscribe = vm::stopAndTranscribe
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranscribeContent(
    log: String,
    onStartRecording: () -> Unit,
    onStopAndTranscribe: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transcriber") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onStartRecording) {
                    Text("Start recording")
                }
                Button(onClick = onStopAndTranscribe) {
                    Text("Stop + Transcribe")
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Log:")
            SelectionContainer {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    text = log
                )
            }
        }
    }
}
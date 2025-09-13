package com.romeodev.features.trancription.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.romeodev.core.TranscriptResult
import com.romeodev.features.trancription.presentation.viewModels.TranscribeViewModel
import org.koin.compose.getKoin
@Composable
fun TranscribeScreen(
    vm: TranscribeViewModel
) {
    val transcription by vm.transcriber.collectAsState()
    val live by vm.live.collectAsState()

    TranscribeContent(
        onStartRecording = vm::startRecording,
        onStopAndTranscribe = vm::stopAndTranscribe,
        onStartStreaming = vm::startStreaming,
        onStopStreaming = vm::stopStreaming,
        liveText = live,
        transcriber = transcription
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranscribeContent(
    onStartRecording: () -> Unit,
    onStopAndTranscribe: () -> Unit,
    onStartStreaming: () -> Unit,
    onStopStreaming: () -> Unit,
    liveText: String,
    transcriber: TranscriptResult?
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
            // Controls: Recording + Streaming
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onStartRecording) {
                    Text("Start recording")
                }
                Button(onClick = onStopAndTranscribe) {
                    Text("Stop + Transcribe")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onStartStreaming) {
                    Text("Start stream")
                }
                Button(onClick = onStopStreaming) {
                    Text("Stop stream")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live box
            Text("Live (stream):")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                SelectionContainer {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                        text = liveText.ifBlank { "—" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            Text("Final transcription:")
            SelectionContainer {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp),
                    text = transcriber?.text ?: ""
                )
            }
        }
    }
}
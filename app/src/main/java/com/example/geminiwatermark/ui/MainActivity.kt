package com.example.geminiwatermark.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.geminiwatermark.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> viewModel.addImages(uris) }
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Gemini 水印去除器", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isProcessing.value) {
            LinearProgressIndicator(
                progress = viewModel.progress.value,
                modifier = Modifier.fillMaxWidth()
            )
            Text(text = viewModel.statusText.value, modifier = Modifier.padding(top = 8.dp))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f).padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.selectedImages) { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.weight(1f),
                enabled = !viewModel.isProcessing.value
            ) {
                Text("選擇圖片")
            }
            
            Button(
                onClick = { viewModel.processImages(context) },
                modifier = Modifier.weight(1f),
                enabled = viewModel.selectedImages.isNotEmpty() && !viewModel.isProcessing.value
            ) {
                Text("開始處理")
            }
        }
        
        if (viewModel.selectedImages.isNotEmpty() && !viewModel.isProcessing.value) {
            TextButton(
                onClick = { viewModel.clearImages() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("清除選擇")
            }
        }
    }
}

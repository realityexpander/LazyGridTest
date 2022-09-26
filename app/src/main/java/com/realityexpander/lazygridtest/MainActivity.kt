package com.realityexpander.lazygridtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.realityexpander.lazygridtest.ui.theme.LazyGridTestTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LazyGridTestTheme {
                val viewModel by viewModels<CodingViewModel>()

                val uiState = viewModel.uiState.collectAsState()

                val scope = rememberCoroutineScope()

//                val listState = rememberLazyListState()
//
//                LaunchedEffect(uiState.value.data) {
//                    snapshotFlow { uiState.value.data}
//                        .distinctUntilChanged()
//                        .collect {
//                            listState.animateScrollToItem(0)
//                        }
//                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    LazyColumn {
                        items(uiState.value.data ?: emptyList()) { item ->
                            Text(text = item.name +":"+ item.share)
                        }

                        item {
                            Button(onClick = {
                                scope.launch {
                                    viewModel.startSim()
                                }
                            }) {
                                Text(text = "Start Sim")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LazyGridTestTheme {
        Greeting("Android")
    }
}
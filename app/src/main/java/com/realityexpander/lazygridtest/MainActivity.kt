package com.realityexpander.lazygridtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColor
import androidx.core.graphics.toColorLong
import com.realityexpander.lazygridtest.ui.theme.LazyGridTestTheme
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LazyGridTestTheme {
                val viewModel by viewModels<CodingViewModel>()

                val uiState = viewModel.uiState.collectAsState()

                val scope = rememberCoroutineScope()


                // Sort the list in the UI
                val itemsSorted = uiState.value.data
                    ?.map { it } //.copy() }
                    ?.sortedBy {
                        it.share
                    }
                    ?.reversed()
                    ?: emptyList()


                // Testing snapshotFlow (allows use of flow operators)
                LaunchedEffect(key1 = uiState) {
                    println("LaunchedEffect")

                    // Create a flow from a Compose State
                    snapshotFlow { uiState }
                        .mapNotNull { it.value.data }
                        .collect {
                            it.forEach { item ->
                                println("snapshotFlow item: ${item.name}")
                            }
                        }
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    // LazyVerticalGrid
                    if (false) {
                        LazyVerticalGrid(
                            cells = GridCells.Fixed(2),
                        ) {
                            items(itemsSorted) { item ->
                                Row {
                                    Text(
                                        text = item.name + ":" + item.share,
                                        modifier = Modifier.animateItemPlacement(),
                                        color = MaterialTheme.colors.onBackground
                                    )
                                }
                            }
                            item {
                                Button(onClick = {
                                    scope.launch {
                                        viewModel.startSim()
                                    }
                                }) {
                                    Text(text = "Start Sim")
                                }

                                Button(onClick = {
                                    scope.launch {
                                        viewModel.stopSim()
                                    }
                                }) {
                                    Text(text = "Stop Sim")
                                }
                            }
                        }
                    }


                    // LazyColumn
                    if (true) {
                        LazyColumn() {
                            items(
                                itemsSorted,
                                //uiState.value.data ?: emptyList(),
                                key = { item -> item.id },
                            ) { item ->
                                Text(
                                    text = item.name + ":" + item.share,
                                    modifier = Modifier.animateItemPlacement(),
                                    color = MaterialTheme.colors.onBackground
                                )
                            }

                            item {
                                Button(onClick = {
                                    scope.launch {
                                        viewModel.startSim()
                                    }
                                }) {
                                    Text(text = "Start Sim")
                                }

                                Button(onClick = {
                                    scope.launch {
                                        viewModel.stopSim()
                                    }
                                }) {
                                    Text(text = "Stop Sim")
                                }
                            }
                        }
                    }

                    // AnimatedVerticalGrid
                    if (false) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        ) {
                            AnimatedVerticalGrid(
                                items = itemsSorted,
                                itemKey = Coding::id,
                                columns = 2,
                                rows = itemsSorted.size / 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1F)
                            ) { item ->
                                //Coding(it.id)
                                Text(
                                    text = item.name + ":" + item.share,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .background(color = Color(item.color))
                                        .padding(8.dp)
                                )
                            }

                            Button(onClick = {
                                scope.launch {
                                    viewModel.startSim()
                                }
                            }) {
                                Text(text = "Start Sim")
                            }

                            Button(onClick = {
                                scope.launch {
                                    viewModel.stopSim()
                                }
                            }) {
                                Text(text = "Stop Sim")
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

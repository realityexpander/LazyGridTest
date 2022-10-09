package com.realityexpander.lazygridtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.unit.dp
import com.realityexpander.lazygridtest.ui.theme.LazyGridTestTheme
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LazyGridTestTheme {
                val viewModel by viewModels<ItemsViewModel>()
                val uiState = viewModel.uiState.collectAsState()
                val scope = rememberCoroutineScope()

                //val updateSortTriggerState = viewModel.updateSortTriggerState.collectAsState()
                val updateValuesTriggerState = viewModel.updateValuesTriggerState.collectAsState()

                var itemsSorted by remember { mutableStateOf(emptyList<Item>()) }

//                // Update the sorted list (usually infrequently) (using StateFlow)
//                LaunchedEffect(updateSortTriggerState.value) {
//                    try {
//                        itemsSorted = uiState.value.data
//                            ?.sortedBy {
//                                it.share
//                            }
//                            ?.reversed()
//                            ?: emptyList()
//                    } catch (e: Exception) {
//                        println("updateSortTriggerState sort Error: $e")
//                        cancel()
//                    }
//                }

                // Update the sorted list (usually infrequently) (using SharedFlow)
                LaunchedEffect(Unit) {
                    viewModel.updateSortTriggerState.collect {
                        try {
                            itemsSorted = uiState.value.data
                                ?.sortedBy {
                                    it.share
                                }
                                ?.reversed()
                                ?: emptyList()
                        } catch (e: Exception) {
                            println("updateSortTriggerState sort Error: $e")
                            cancel()
                        }
                    }
                }


                // Update just the `share` values (usually often)
                LaunchedEffect(updateValuesTriggerState.value) {
                    try {
                        // Update the `share` values for each item in the list from the latest data
                        itemsSorted.forEach { itemSorted ->
                            uiState.value.data?.forEach { coding ->
                                if (coding.id == itemSorted.id) {
                                    itemSorted.share = coding.share
                                }
                            }
                        }

                        // Force update by making a copy of the list
                        val newItemsSorted = mutableListOf<Item>()
                        itemsSorted.forEach {
                            newItemsSorted += it.copy(
                                forceUpdateId = abs(Random.nextInt())
                            )
                        }
                        itemsSorted = newItemsSorted

                    } catch (e: Exception) {
                        println("updateValuesTriggerState exception " +
                                "size:${uiState.value.data?.size}, " +
                                "indices:${uiState.value.data?.indices}")
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    // LazyVerticalGrid
                    if (false) {
                        Column {
                            LazyVerticalGrid(
                                cells = GridCells.Fixed(2),
                            ) {
                                items(itemsSorted) { item ->
                                    Row {
                                        Text(
                                            text = item.name + ":" + item.share,
                                            color = Color.Black,
                                            modifier = Modifier
                                                .background(color = Color(item.color))
                                                .padding(8.dp)
                                                .animateItemPlacement() // currently ignored
                                        )
                                    }
                                }
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


                    // LazyColumn
                    if (false) {
                        LazyColumn() {
                            items(
                                itemsSorted,
                                //uiState.value.data ?: emptyList(),
                                key = { item -> item.id },
                            ) { item ->
                                Text(
                                    text = item.name + ":" + item.share,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .background(color = Color(item.color))
                                        .padding(8.dp)
                                        .animateItemPlacement()
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
                    if (true) {
                        Column(
                            modifier = Modifier
                                .background(Color.White)
                                .padding(4.dp)
                        ) {

                            if(uiState.value is Response.Loading) {
                                Text(text = "Loading...")
                            }
                            if(uiState.value is Response.Error) {
                                Text(text = "Error: ${(uiState.value as Response.Error).error?.message}")
                            }

                            AnimatedVerticalGrid(
                                items = itemsSorted,
                                itemKey = Item::id,
                                columns = 2,
                                rows = itemsSorted.size / 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(1.dp)
                                    .weight(1F)
                            ) { item ->
                                Text(
                                    text = item.name + ":" + item.share.toString(),
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

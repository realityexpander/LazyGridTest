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
import kotlinx.coroutines.delay
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

                val updateSortState = viewModel.updateSortTriggerState.collectAsState()
                val updateValuesState = viewModel.updateValuesTriggerState.collectAsState()

                // Sort the list in the UI
                var itemsSorted by remember { mutableStateOf(emptyList<Coding>()) }
//                var itemsSorted = uiState.value.data
//                    ?.map {
//                        it
//                    }
////                    ?.map { it.copy() }
//                    ?.sortedBy {
//                        it.share
//                    }
//                    ?.reversed()
//                    ?: emptyList()

//                val sorted = try {
//                    uiState.value.data
//                        ?.sortedBy {
//                            it.share
//                        }
//                        ?.reversed()
//                        ?.subList(0, 10)
//                        ?.joinToString { it.name + "->" + it.share }
//                } catch (e: Exception) {
//                    println("Sorted exception: $e")
//                }
//                println("final data (activity): $sorted")

                // Update for sorted list
//                LaunchedEffect(key1 = uiState.value.data?.get(0)?.forceUpdateId) {
                LaunchedEffect(updateSortState.value) {

                    //if(uiState.value.data?.get(0)?.forceUpdateId == 0) return@LaunchedEffect

                    //println("LaunchedEffect forceUpdateId: ${uiState.value.data?.get(0)?.forceUpdateId}")

                        try {
                            println("LaunchedEffect collecting...")

                            itemsSorted = uiState.value.data
                                ?.sortedBy {
                                    it.share
                                }
                                ?.reversed()
                                ?: emptyList()
                        } catch (e: Exception) {
                            println("LaunchedEffect sort Error: $e")
                            cancel()
                        } finally {
                            println("LaunchedEffect sort complete")
                        }
                }

                // Update just the `share` value
//                LaunchedEffect(key1 = true) {
                //LaunchedEffect(key1 = uiState.value.data?.get(1)?.forceUpdateId) {
                LaunchedEffect(updateValuesState.value) {
                    // Create a "snapshotFlow" : Convert a hot value to a flow
                    snapshotFlow { uiState.value.data }
                        .mapNotNull {
                            it
                        }
                        .collect { codings ->
                            println("snapshotFlow collecting...")
                            try {
//                                for (index in codings.indices) {
//                                    if (codings.indices.last >= codings.size) {
//                                        break
//                                        //return@collect
//                                    }
//                                    val coding = codings[index]
//                                    //println("coding $index: ${coding.name}")
//                                }

//                                itemsSorted = codings
//                                    .sortedBy {
//                                        it.share
//                                    }
//                                    .reversed()

                                itemsSorted.forEach { itemSorted ->
                                    codings.forEachIndexed { index, coding ->
                                        if (coding.id == itemSorted.id) {
                                            itemSorted.share = coding.share
                                        }
                                    }
                                }

                                //delay(100)
                                delay(1)

                            } catch (e: Exception) {
                                println("exception size:${codings.size}, indices:${codings.indices}")
                                cancel()
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
                            AnimatedVerticalGrid(
                                items = itemsSorted,
                                itemKey = Coding::id,
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

package com.realityexpander.lazygridtest

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.abs
import kotlin.random.Random

// ConcurrentModificationException resources
// https://stackoverflow.com/questions/39479909/synchronize-methods-to-prevent-concurrentmodificationexception
// https://stackoverflow.com/questions/5151956/java-util-concurrentmodificationexception-in-android-animation
// https://stackoverflow.com/questions/67827951/android-compose-lazycolum-rendering-issue-when-changing-list-sorting
// https://stackoverflow.com/questions/70457309/modifying-a-snapshotstatelist-throws-concurrentmodificationexception
// https://stackoverflow.com/questions/6866238/concurrent-modification-exception-adding-to-an-arraylist

// Youtube that started this project
// https://www.youtube.com/watch?v=RPpXYxwo2yI&t=11s

// Source code to SnapShotStateList
// https://androidx.tech/artifacts/compose.runtime/runtime/1.0.0-alpha06-source/androidx/compose/runtime/snapshots/SnapshotStateList.kt.html

// About SnapShotFlow
// https://medium.com/mobile-app-development-publication/jetpack-compose-side-effects-made-easy-a4867f876928

class ItemsViewModel(
    private val itemRepository: ItemRepository = ItemRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<Response<List<Item>>>(Response.Loading())
    val uiState = _uiState.asStateFlow()

    private val _updateSortTriggerState = MutableStateFlow(-1)
    val updateSortTriggerState = _updateSortTriggerState.asStateFlow()

    private val _updateValuesTriggerState = MutableStateFlow(-1)
    val updateValuesTriggerState = _updateValuesTriggerState.asStateFlow()

    init {
        // Update the current list of data
        viewModelScope.launch {
            itemRepository
                .getItemsData()
                .also {// to show inline hint about type
                }
                .collect { listCoding ->
                    _uiState.value = Response.Success(listCoding)
                }
        }

        // Update the sorted list  (usually slowly)
        viewModelScope.launch {
            itemRepository
                .onUpdateSortedTrigger()
                .also {// to show inline hint about type
                }
                .collect { updateSortKey ->
                    _updateSortTriggerState.value = updateSortKey
                }
        }

        // Update just the `share` values (usually rapidly)
        viewModelScope.launch {
            itemRepository
                .onUpdateValuesTrigger()
                .also { // to show inline hint about type
                }
                .collect { updateValuesKey ->
                    _updateValuesTriggerState.value = updateValuesKey
                }
        }
    }

    fun addItem(item: Item): Boolean = itemRepository.addCoding(item)
    fun removeItem(item: Item): Boolean = itemRepository.removeCoding(item)

    suspend fun startSim() = viewModelScope.launch {
        itemRepository.startSimulation()
    }

    suspend fun stopSim() = viewModelScope.launch {
        itemRepository.stopSimulation()
    }
}

class ItemRepository {
    private var isSimRunning: Boolean = false

    // Setup Data for items
    private val data =
        ArrayList(
            ItemListEnum.values().map {
                Item(it)
            }
        )
        .toMutableStateList()
        .also {// to show inline hint about type
        }


    fun getItemsData(): Flow<List<Item>> = snapshotFlow {
        data
    }

    private var updateSortTrigger: Int by mutableStateOf(-1)
    fun onUpdateSortedTrigger() =
        snapshotFlow {
            updateSortTrigger
        }

    private var updateValuesTrigger by mutableStateOf(-1)
    fun onUpdateValuesTrigger() =
        snapshotFlow {
            updateValuesTrigger
        }

    fun addCoding(item: Item): Boolean {
        data.add(item)
        return true
    }

    fun removeCoding(item: Item): Boolean {
        data.remove(item)
        return true
    }

    suspend fun stopSimulation() {
        isSimRunning = false

        // Reset the simulation to zero with new values
        data.forEach {
            it.share = 0
            it.trend = abs(Random.nextInt()) % 50
        }

        updateSortTrigger = Random.nextInt()
    }

    suspend fun startSimulation() = withContext(Dispatchers.IO) {
        if (isSimRunning) return@withContext

        isSimRunning = true
        var counter = 0
        var counter2 = 0
        val maxReps = 100_000

        repeat(maxReps) {
            if (!isSimRunning) return@repeat

            // Run the simulation data
            data.forEach { coding ->
                coding.share += coding.trend
                coding.trend = abs(Random.nextInt()) % 50
            }

            // Update the values in the items in the UI
            counter2++
            if (counter2 > 5) {
                counter2 = 0

                updateValuesTrigger = Random.nextInt()
            }

            // Update the sort order in the UI
            counter++
            if (counter >= maxReps / 5) {
                counter = 0

                updateSortTrigger = Random.nextInt()
            }
        }
        isSimRunning = false

        // Update the UI with final data
        updateValuesTrigger = Random.nextInt()
        updateSortTrigger = Random.nextInt()


        // Show final data in log
        val sorted = data
            .sortedBy {
                it.share
            }
            .reversed()
            .subList(0, 10)
            .joinToString { it.name + "->" + it.share }
        println("final data           : $sorted")

    }
}


data class Item(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    var share: Int = 0,
    var trend: Int = 0,
    val color: Long = Random(id).nextLong(),
    var forceUpdateId: Int = 0
) {
    constructor(item: ItemListEnum) : this(
        item.id,
        item.languageName,
        item.description,
        0,
        abs(Random.nextInt()) % 300,
    )
}

enum class ItemListEnum(
    val id: Int,
    val languageName: String,
    val description: String
) {
    ANDROID(1, "Android", "Android is a mobile operating system developed by Google"),
    IOS(2, "iOS", "iOS is a mobile operating system created and developed by Apple Inc."),
    WEB(3, "Web", "Web development is the work involved in developing a Web site for the Internet"),
    OTHER(4, "Other", "Other"),
    JAVA(
        5,
        "Java",
        "Java is a general-purpose computer-programming language that is concurrent, class-based, object-oriented, and specifically designed to have as few implementation dependencies as possible."
    ),
    KOTLIN(
        6,
        "Kotlin",
        "Kotlin is a cross-platform, statically typed, general-purpose programming language with type inference."
    ),
    SWIFT(
        7,
        "Swift",
        "Swift is a general-purpose, multi-paradigm, compiled programming language developed by Apple Inc."
    ),
    JAVASCRIPT(
        8,
        "JavaScript",
        "JavaScript, often abbreviated as JS, is a programming language that conforms to the ECMAScript specification."
    ),
    PYTHON(
        9,
        "Python",
        "Python is an interpreted, high-level, general-purpose programming language."
    ),
    RUBY(
        10,
        "Ruby",
        "Ruby is a dynamic, reflective, object-oriented, general-purpose programming language."
    ),
    C_SHARP(
        11,
        "C#",
        "C# is a general-purpose, multi-paradigm programming language encompassing static typing, strong typing, lexically scoped, imperative, declarative, functional, generic, object-oriented, and component-oriented programming disciplines."
    ),
    C_PLUS_PLUS(
        12,
        "C++",
        "C++ is a general-purpose programming language created by Bjarne Stroustrup as an extension of the C programming language, or \"C with Classes\"."
    ),
    C(
        13,
        "C",
        "C is a general-purpose, procedural computer programming language supporting structured programming, lexical variable scope, and recursion, with a static type system."
    ),
    GO(
        14,
        "Go",
        "Go is a statically typed, compiled programming language designed at Google by Robert Griesemer, Rob Pike, and Ken Thompson."
    ),
    RUST(
        15,
        "Rust",
        "Rust is a multi-paradigm programming language focused on performance and safety, especially safe concurrency."
    ),
    PHP(
        16,
        "PHP",
        "PHP is a general-purpose scripting language especially suited to web development."
    ),
    SCALA(
        17,
        "Scala",
        "Scala is a general-purpose programming language providing support for functional programming and a strong static type system."
    ),
    R(
        18,
        "R",
        "R is a programming language and software environment for statistical computing and graphics supported by the R Foundation for Statistical Computing."
    ),
    DART(
        19,
        "Dart",
        "Dart is a client-optimized programming language for apps on multiple platforms."
    ),
    OBJECTIVE_C(
        20,
        "Objective-C",
        "Objective-C is a general-purpose, object-oriented programming language that adds Smalltalk-style messaging to the C programming language."
    ),
    PERL(
        21,
        "Perl",
        "Perl is a family of high-level, general-purpose, interpreted, dynamic programming languages."
    ),
    HASKELL(
        22,
        "Haskell",
        "Haskell is a standardized, general-purpose purely functional programming language, with non-strict semantics and strong static typing."
    ),
    CLOJURE(
        23,
        "Clojure",
        "Clojure is a dialect of the Lisp programming language, with a focus on functional programming."
    ),
    COBOL(
        24,
        "COBOL",
        "COBOL is a compiled English-like computer programming language designed for business use."
    ),

}









































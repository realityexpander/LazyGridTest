package com.realityexpander.lazygridtest

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import kotlin.random.Random

@OptIn(InternalCoroutinesApi::class)
class CodingViewModel(
    private val codingRepository: CodingRepository = CodingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<Response<List<Coding>>>(Response.Loading())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            codingRepository.getCoding().collect { listCoding ->

//                 _uiState.value = Response.Success(
//                     listCoding.sortedBy { it.share }.reversed()
//                 )

                _uiState.value = Response.Success(
                    listCoding
                )
            }
        }
    }

    fun addCoding(coding: Coding): Boolean = codingRepository.addCoding(coding)
    fun removeCoding(coding: Coding): Boolean = codingRepository.removeCoding(coding)
    suspend fun startSim() = viewModelScope.launch {
        codingRepository.startSimulation()
    }

    suspend fun stopSim() = viewModelScope.launch {
        codingRepository.stopSimulation()
    }
}

class CodingRepository {
    private var isSimRunning: Boolean = false

    private val data: SnapshotStateList<Coding> = CodingType.values()
        .map {
            // `with` changes `it` to `this`
            with(it) {
                Coding(
                    it.id,
                    languageName,
                    description,
                    0,
                    abs(Random.nextInt()) % 300
                )
            }
        }
        .toMutableStateList()

    fun getCoding(): Flow<List<Coding>> = snapshotFlow {

//        var list: ArrayList<Coding>
//        synchronized(data) {
//            list = ArrayList(data.toList())
//        }
//        list


//        val data2 = CopyOnWriteArrayList<Coding>()
//        synchronized(data) {
//            data.forEach(data2::add)
//        }
//        data2


        synchronized(data) {
            data.toList()
        }
    }

    fun addCoding(coding: Coding): Boolean {
        data.add(coding)
        return true
    }

    fun removeCoding(coding: Coding): Boolean {
        data.remove(coding)
        return true
    }

    suspend fun stopSimulation() {
        isSimRunning = false
    }

    suspend fun startSimulation() = withContext(Dispatchers.IO) {
        if (isSimRunning) return@withContext

        isSimRunning = true
        repeat(1000) {

            if (!isSimRunning) return@repeat

            ///////////// DOESNT WORK /////////////

//            // Causes ConcurrentModificationException (cant use forEachIndexed)
//            synchronized(data) {
//                data.forEachIndexed { index, coding ->
//                    data[index] = coding.copy(
//                        share = coding.share + coding.trend,
//                        trend = abs(Random.nextInt()) % 300
//                    )
//                }
//            }

//            // Causes ConcurrentModificationException (cant use forEach)
//            synchronized(data) {
//                data.forEach { coding ->
//                    data.remove(coding)
//                    data.add(coding.copy(
//                        share = coding.share + coding.trend,
//                        trend = abs(Random.nextInt()) % 300
//                    ))
//                }
//            }


//            // Does NOT update the UI (requires an update function, below)
//            data[0] = data[0].copy(share = data[0].share)

//              // Simply modifying the items Does NOT update the UI
//              data[index].share = data[index].share + data[index].trend
//              data[index].trend = data[index].trend + abs(Random.nextInt()) % 50


//            // Causes ConcurrentModificationException & requires synchronized VERSION_CODE N
//            synchronized(data) {
//                data.replaceAll {
//                    it.copy(
//                        share = it.share + it.trend,
//                        trend = it.trend + abs(Random.nextInt()) % 50
//                    )
//                }
//            }

//            // using iterator - remove & add, even w/ synchronized, causes CME
//            synchronized(data) {
//                val iterator = data.iterator()
//                while (iterator.hasNext()) {
//                    val coding = iterator.next()
//                    iterator.remove()
//                    data.add(coding.copy(
//                        share = coding.share + coding.trend,
//                        trend = coding.trend + abs(Random.nextInt()) % 50
//                    ))
//                }
//            }

            //////////////// WORKS ///////////////////////

//            // works, but requires the force update hack
              // Doesn't need synchronized for AnimatedVerticalGrid, LazyColumn
//            synchronized(data) { // needed for LazyVerticalGrid
                data.forEach { coding ->
                    coding.share += coding.trend
                    coding.trend = abs(Random.nextInt()) % 50
                }

                // forces update on each item (but interferes with animation)
                data.add(data.removeAt(0))
//            }


//            // Works well - doesn't need synchronized on AnimatedVerticalGrid, LazyVerticalGrid
//            //synchronized(data) { // needed for LazyColumn with AnimatedItemPlacement
//                val itr = data.listIterator()
//                while (itr.hasNext()) {
//                    val coding = itr.next()
//                    itr.set(
//                        coding.copy(
//                            share = coding.share + coding.trend,
//                            trend = coding.trend + abs(Random.nextInt()) % 50
//                        )
//                    )
//                }
//            //}


            // works well - - doesn't need synchronized on AnimatedVerticalGrid, LazyVerticalGrid
//            synchronized(data) { // needed for LazyColumn with AnimatedItemPlacement
//                for (index in data.indices) {
//                    data[index] = with(data[index]) {
//                        val randomInt = abs(Random.nextInt()) % 10
//                        copy(
//                            share = share + trend,
//                            trend = trend + randomInt
//                        )
//                    }
//
//                }
//            }


            delay(Random.nextLong(10, 500))

//            println("data: ${
//                data
//                    .sortedBy { it.share }
//                    .reversed()
//                    .subList(0, 10)
//                    .joinToString { it.name + "->" + it.share }
//            }")
        }

        isSimRunning = false
    }
}


data class Coding(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    var share: Int = 0,
    var trend: Int = 0,
    val color: Long = Random(id).nextLong() //and 0xBB000000
)

enum class CodingType(
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









































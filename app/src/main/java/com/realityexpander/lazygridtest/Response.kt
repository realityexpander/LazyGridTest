package com.realityexpander.lazygridtest

import kotlin.random.Random

sealed class Response<T: Any>(
    val data: T? = null,
    val error: Throwable? = null,
    val UUID: String = Random.nextLong().toString()
) {
    class Success<T: Any>(data: T) : Response<T>(data = data, UUID = Random.nextLong().toString())
    class Error<T: Any>(error: Throwable) : Response<T>(error = error)
    class Loading<T: Any>: Response<T>()
}
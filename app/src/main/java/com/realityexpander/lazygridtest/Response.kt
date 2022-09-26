package com.realityexpander.lazygridtest

import kotlin.random.Random

sealed class Response<T: Any>(
    val data: T? = null,
    val error: Throwable? = null
) {
    class Success<T: Any>(data: T, var id:Int = Random(200).nextInt()) : Response<T>(data = data)
    class Error<T: Any>(error: Throwable) : Response<T>(error = error)
    class Loading<T: Any>: Response<T>()
}


//data class Resource<out T>(val status: Status, val payload: T?, val message: String?) {
//
//    companion object {
//        fun <T> success(payload: T?) = Resource(Status.SUCCESS, payload, null)
//
//        fun <T> error(message: String, payload: T?) = Resource(Status.ERROR, payload, message)
//
//        fun <T> loading(payload: T?) = Resource(Status.LOADING, payload, null)
//    }
//}
//
//enum class Status {
//    SUCCESS,
//    ERROR,
//    LOADING
//}
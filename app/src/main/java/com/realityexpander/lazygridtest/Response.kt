package com.realityexpander.lazygridtest

import kotlin.random.Random

sealed class Response<T: Any>(
    val data: T? = null,
    val error: Throwable? = null,
    var updateSortKey: Int = 0,
    var updateValuesKey: Int = 0
) {
    class Success<T: Any>(data: T, updateSortKey: Int = 0) : Response<T>(data = data, updateSortKey = updateSortKey)
    class Error<T: Any>(error: Throwable) : Response<T>(error = error)
    class Loading<T: Any>: Response<T>()
    class UpdateSort<T: Any>(updateKey: Int): Response<T>(updateSortKey = updateKey)
    class UpdateValues<T: Any>(updateKey: Int): Response<T>(updateSortKey = updateKey)
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
package com.si.gymmanager.common


sealed class Result<out T>{
    data class success<T>(val data: T) : Result<T>()
    data class error(val message: String) : Result<Nothing>()
    object Loading:Result<Nothing>()

}
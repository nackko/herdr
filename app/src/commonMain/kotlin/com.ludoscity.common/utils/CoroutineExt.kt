package com.ludoscity.common.utils

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

//Heavily inspired by
//https://github.com/jarroyoesp/KotlinMultiPlatform/blob/master/app/src/main/java/com/jarroyo/sharedcode/utils/CoroutineExt.kt

/**
 * Equivalent to [launch] but return [Unit] instead of [Job].
 *
 * Mainly for usage when you want to lift [launch] to return. Example:
 *
 * ```
 * override fun loadData() = launchSilent {
 *     // code
 * }
 * ```
 */
fun launchSilent(
    context: CoroutineContext = Dispatchers.Main,
    exceptionHandler: CoroutineExceptionHandler? = null,
    job: Job,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) {
    val coroutineScope = if (exceptionHandler != null) {
        CoroutineScope(context + job + exceptionHandler)
    } else {
        CoroutineScope(context + job)
    }
    coroutineScope.launch(context, start, block)
}
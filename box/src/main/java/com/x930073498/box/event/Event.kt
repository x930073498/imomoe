@file:Suppress("FunctionName")

package com.x930073498.box.event

import com.x930073498.box.core.BoxOwner
import kotlinx.coroutines.flow.Flow


interface Event<V> {
    val sticky: Boolean
        get() = false
    val buffer: Int
        get() = Int.MAX_VALUE
}
fun <V> Event(
    sticky: Boolean = false,
    @androidx.annotation.IntRange(from = 1, to = Int.MAX_VALUE.toLong())
    buffer: Int = Int.MAX_VALUE
): Event<V> =
    DefaultEventImpl(sticky, buffer)


fun <T, V> T.pushEvent(event: Event<V>, value: V) where T : BoxOwner {
    pushEventInternal(event, value)
}

fun <T, V> T.eventFlow(event: Event<V>): Flow<V> where T : BoxOwner {
    return eventFlowInternal(event)
}

package com.x930073498.box.event

import com.x930073498.box.core.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

internal class DefaultEventImpl< V>(override val sticky: Boolean, override val buffer: Int) :
    Event<V>

private class EventFlowEmitterHolder : FlowEmitter<Event< *>, Any?> {
    private val flowMap =
        mutableMapOf<Event< *>, MutableSharedFlow<DataAction<Event<*>, Any?>>>()

    override fun getFlow(key: Event< *>): MutableSharedFlow<DataAction<Event< *>, Any?>> {
        var result = flowMap[key]
        if (result != null) return result
        result = if (key.sticky) MutableSharedFlow(
            1,
            extraBufferCapacity = key.buffer,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        ) else MutableSharedFlow(
            extraBufferCapacity = key.buffer,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        flowMap[key] = result
        return result

    }

    override fun emit(key: Event< *>, value: DataAction<Event< *>, Any?>) {
        getFlow(key).tryEmit(value)
    }

    override fun reset() {
//        flowMap.clear()
    }

}

private object EventFactory : BoxFactory<Event< *>, Any?> {
    override fun create(owner: BoxOwner, id: ID<Event<*>, Any?>): Box<Event<*>, Any?> {
        return mapBox(EventFlowEmitterHolder())
    }
}

private object EventID : ID<Event<*>, Any?> {
    override val factory: BoxFactory<Event< *>, Any?>
        get() = EventFactory
}

private fun <T> T.eventBox(): Box<Event<*>, Any?> where T : BoxOwner {
    return getBox(EventID)
}

internal fun <T,  V> T.pushEventInternal(event: Event<V>, value: V) where T : BoxOwner {
    eventBox()[event] = value
}

@Suppress("UNCHECKED_CAST")
internal fun <T,  V> T.eventFlowInternal(event: Event<V>): Flow<V> where T : BoxOwner {
    val box = eventBox()
    return box.flowHolder.getFlow(event)
        .mapNotNull {
            when (it) {
                is DataAction.Initial -> it.value as V
                is DataAction.Modify -> it.current as V
                is DataAction.Remove -> null
            }
        }

}

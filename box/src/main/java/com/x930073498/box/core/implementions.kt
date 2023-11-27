@file:Suppress("UNCHECKED_CAST")

package com.x930073498.box.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

internal class DefaultFlowEmitter<K, V> : FlowEmitter<K, V>,
    CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val flow = MutableSharedFlow<DataAction<K, V>>(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun getFlow(key: K): SharedFlow<DataAction<K, V>> {
        return flow.filter { it.key == key }.shareIn(this, SharingStarted.Lazily, 1)
    }

    override fun emit(key: K, value: DataAction<K, V>) {
        flow.tryEmit(value)
    }

    override fun reset() {

    }

}

internal open class MapBox<K, V>(override val flowHolder: FlowEmitter<K, V>) : Box<K, V> {
    private val map = mutableMapOf<K, V>()
    override fun get(initializer: Initializer<K, V>): V {
        synchronized(this) {
            val key = initializer.key
            return if (map.containsKey(key)) map[key] as V
            else {
                val result = initializer.initial()
                map[key] = result
                flowHolder.emit(key, DataAction.Initial(key, result, false))
                result
            }
        }
    }

    override fun set(key: K, value: V) {
        synchronized(this) {
            if (map.containsKey(key)) {
                val last = map[key] as V
                map[key] = value
                flowHolder.emit(key, DataAction.Modify(key, last, value))
            } else {
                map[key] = value
                flowHolder.emit(key, DataAction.Initial(key, value, true))
            }
        }
    }

    override val keys: Collection<K>
        get() = map.keys

    override fun containsKey(key: K): Boolean {
        return map.containsKey(key)
    }

    override fun remove(key: K) {
        synchronized(this) {
            if (map.containsKey(key)) {
                val value = map.remove(key)
                flowHolder.emit(key, DataAction.Remove(key, value as V))
            }
        }
    }

}


internal class MapBoxFactory<K,V> : BoxFactory<K,V>


private class DefaultBoxOwner : BoxOwner {
    val map = mutableMapOf<ID<*, *>, Box<*, *>>()
    override fun <K, V> getBox(id: ID<K, V>): Box<K, V> {
        synchronized(this) {
            val result = map[id]
            if (map.containsKey(id)) {
                if (result is Box<*, *>) {
                    return result as Box<K, V>
                }
            }
            val temp = id.factory.create(this, id)
            map[id] = temp
            return temp
        }


    }
}

internal fun defaultBoxOwner(): BoxOwner = DefaultBoxOwner()


internal class DataInitializer<K, V>(override val key: K, private val value: V) :
    Initializer<K, V> {
    override fun initial(): V {
        return value
    }
}



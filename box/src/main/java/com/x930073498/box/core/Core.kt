package com.x930073498.box.core

import kotlinx.coroutines.flow.SharedFlow


sealed interface DataAction<K, V> {
    val key: K

    data class Remove<K, V>(override val key: K, val value: V) : DataAction<K, V>

    data class Modify<K, V>(override val key: K, val last: V, val current: V) : DataAction<K, V>

    /**
     * @param keep 初始值是否被存在容器中
     */
    data class Initial<K, V>(override val key: K, val value: V, val keep: Boolean) :
        DataAction<K, V>

}

interface Initializer<K, V> {
    val key: K
    fun initial(): V
}


//键值对存储容器
interface Box<K, V> {
    val keys: Collection<K>
    operator fun get(initializer: Initializer<K, V>): V
    operator fun set(key: K, value: V)
    fun remove(key: K)
    fun containsKey(key: K):Boolean
    val flowHolder: FlowHolder<K, V>
}

interface FlowHolder<K, V> {
    fun getFlow(key: K): SharedFlow<DataAction<K, V>>
}

interface FlowEmitter<K, V> : FlowHolder<K, V> {
    fun emit(key: K, value: DataAction<K, V>)
    fun reset()
}

//创建box的factory
interface BoxFactory<K,V> {
    fun  create(owner: BoxOwner, id: ID<K, V>): Box<K, V> = mapBox()
}

interface ID<K, V> {
    val factory: BoxFactory<K,V>
        get() = MapBoxFactory()
}

//box持有者
interface BoxOwner {
    fun <K, V> getBox(id: ID<K, V>): Box<K, V>
}




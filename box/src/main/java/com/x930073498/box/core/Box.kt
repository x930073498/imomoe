@file:Suppress("FunctionName")

package com.x930073498.box.core

fun <K, V> boxInitial(key: K, value: V): Initializer<K, V> = DataInitializer(key, value)
infix fun <K, V> K.initial(value: V): Initializer<K, V> = boxInitial(this, value)

fun <K, V> Box<K, V>.get(key: K, value: V) = get(boxInitial(key, value))

fun <K, V> Box<K, V?>.get(key: K) = get(boxInitial(key, null))

fun BoxOwner(): BoxOwner = defaultBoxOwner()

fun <K, V> mapBox(emitter: FlowEmitter<K, V> = DefaultFlowEmitter()): Box<K, V> =
    MapBox(emitter)



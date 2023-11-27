@file:Suppress("UNCHECKED_CAST")

package com.x930073498.box.property

import com.x930073498.box.core.*
import kotlinx.coroutines.flow.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

enum class DataSignal {
    SET, GET
}

//信号
data class PropertyData<T, V>(
    val target: T,
    val property: KProperty<*>,
    val value: V,
    val box: Box<String, Any?>,
    val signal: DataSignal
)

fun <T, V> T.subscribe(property: KProperty0<V>): Flow<V> where T : BoxOwner =
    subscribeInternal(property)

 fun <T, V> T.property(
     initializer: T.(KProperty<*>) -> Initializer<String?, V>,
     callback: (PropertyData<T, V>.() -> Unit)? = null,
): ReadWriteProperty<T, V> where T : BoxOwner =
    propertyInternal( initializer, callback)

fun <T, V> T.property(
    default: V,
    callback: (PropertyData<T, V>.() -> Unit)? = null,
): ReadWriteProperty<T, V> where T : BoxOwner =
    propertyInternal(
        initializer = { boxInitial(null, default) },
        callback
    )

fun <T, V> T.property(
    callback: (PropertyData<T, V?>.() -> Unit)? = null,
): ReadWriteProperty<T, V?> where T : BoxOwner =
    propertyInternal(
        initializer = { boxInitial(null, null) },
        callback
    )

fun <T> T.resetProperty(vararg properties: KProperty<*>) where T : BoxOwner =
    resetPropertyInternal(*properties)
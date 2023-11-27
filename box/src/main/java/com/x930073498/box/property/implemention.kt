@file:Suppress("UNCHECKED_CAST")

package com.x930073498.box.property

import com.x930073498.box.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onSubscription
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible

//可訂閱的
internal interface SubscribableProperty<T> {
    fun T.getKey(property: KProperty<*>): String
    val target: T
}


private object PropertyFactoryID : ID<String, Any?>


private fun <T> T.propertyBox(): Box<String, Any?> where T : BoxOwner {
    return getBox(PropertyFactoryID)
}

private class BoxProperty<V, T>(
    override val target: T,
    private val initializer: T.(KProperty<*>) -> Initializer<String?, V>,
    private val callback: (PropertyData<T, V>.() -> Unit)? = null,
) :
    SubscribableProperty<T>,
    ReadWriteProperty<T, V> where T : BoxOwner {
    override fun getValue(thisRef: T, property: KProperty<*>): V {
        val box = thisRef.propertyBox()
        val result = with(thisRef) {
            box.get(getKey(property), initializer.invoke(thisRef, property).initial()) as V
        }
        return result.also {
            callback?.invoke(PropertyData(thisRef, property, result, box, DataSignal.GET))
        }
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        val box = thisRef.propertyBox()
        with(thisRef) {
            box[getKey(property)] = value
        }
        callback?.invoke(PropertyData(thisRef, property, value, box, DataSignal.SET))
    }

    override fun T.getKey(property: KProperty<*>): String {
        val key = initializer.invoke(this, property).key
        return if (key.isNullOrEmpty()) property.name else key
    }


}

internal fun <T, V> T.propertyInternal(
    initializer: T.(KProperty<*>) -> Initializer<String?, V>,
    callback: (PropertyData<T, V>.() -> Unit)? = null,
): ReadWriteProperty<T, V> where T : BoxOwner =
    BoxProperty(this, initializer, callback)


private fun <T> T.getDelegate(property: KProperty<*>): SubscribableProperty<T>? {
    property.isAccessible = true
    fun checkDelegate(delegate: Any?): SubscribableProperty<T>? {
        if (delegate is SubscribableProperty<*> && delegate.target == this) {
            return delegate as SubscribableProperty<T>
        }
        return null
    }
    if (property is KProperty0<*>) {
        return checkDelegate(property.getDelegate())
    }
    if (property is KProperty1<*, *>) {
        property as KProperty1<T, *>
        return checkDelegate(property.getDelegate(this))
    }
    return null
}

internal fun <T> T.resetPropertyInternal(vararg properties: KProperty<*>) where T : BoxOwner {
    val box = propertyBox()
    if (properties.isEmpty()) {
        val keys = box.keys.toList()
        keys.forEach {
            box.remove(it)
        }
    } else {
        properties.forEach { kProperty ->
            getDelegate(kProperty)?.run {
                getKey(kProperty)
            }?.let {
                box.remove(it)
            }
        }
    }
}

internal fun <T, V> T.subscribeInternal(property: KProperty0<V>): Flow<V> where T : BoxOwner {
    property.isAccessible = true
    val delegate = property.getDelegate()
    if (delegate !is SubscribableProperty<*>) {
        throw SubscribableException()
    }
    if (delegate.target != this) {
        throw PropertyException()
    }
    delegate as SubscribableProperty<T>
    return propertyBox()
        .flowHolder
        .getFlow(with(delegate) {
            getKey(property)
        })
        .onSubscription {
            property.invoke()
        }
        .mapNotNull {
            when (it) {
                is DataAction.Initial -> it.value as? V
                is DataAction.Modify -> it.current as? V
                is DataAction.Remove -> {
                    property.get()
                    null
                }
            }
        }

}


private class SubscribableException : RuntimeException("该属性不是可观察的属性,请采用代理模式声明变量")
private class PropertyException : RuntimeException("订阅的属性不是指定对象的属性,请检查订阅的属性书否属于目标对象所有")
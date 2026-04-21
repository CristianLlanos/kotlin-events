package com.cristianllanos.events.coroutines

import com.cristianllanos.container.Resolver

class FakeResolver : Resolver {
    private val instances = mutableMapOf<Class<*>, Any>()

    fun <T : Any> bind(type: Class<T>, instance: T) {
        instances[type] = instance
    }

    inline fun <reified T : Any> bind(instance: T) = bind(T::class.java, instance)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> resolve(type: Class<T>): T {
        return instances[type] as? T
            ?: throw IllegalArgumentException("No binding for [${type.simpleName}]")
    }
}

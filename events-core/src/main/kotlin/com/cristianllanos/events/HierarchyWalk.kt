package com.cristianllanos.events

fun <V> collectEntries(registry: Map<out Class<out Event>, Collection<V>>, type: Class<*>): List<V> {
    if (registry.isEmpty()) return emptyList()
    val result = mutableListOf<V>()
    val visited = mutableSetOf<Class<*>>()
    collectRecursive(registry, type, result, visited)
    return result
}

private fun <V> collectRecursive(
    registry: Map<out Class<out Event>, Collection<V>>,
    type: Class<*>,
    result: MutableList<V>,
    visited: MutableSet<Class<*>>,
) {
    if (!visited.add(type)) return
    registry[type]?.let { result.addAll(it) }
    for (iface in type.interfaces) {
        collectRecursive(registry, iface, result, visited)
    }
    type.superclass?.let { superclass ->
        if (superclass != Any::class.java) {
            collectRecursive(registry, superclass, result, visited)
        }
    }
}

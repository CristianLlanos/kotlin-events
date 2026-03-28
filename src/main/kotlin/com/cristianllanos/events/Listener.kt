package com.cristianllanos.events

interface Listener<T : Event> {
    fun handle(event: T)
}

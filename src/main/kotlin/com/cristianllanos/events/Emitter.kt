package com.cristianllanos.events

interface Emitter {
    fun <T : Event> emit(event: T)
}

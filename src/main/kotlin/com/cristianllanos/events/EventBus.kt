package com.cristianllanos.events

import com.cristianllanos.container.Resolver

interface EventBus : Emitter, Subscriber

fun EventBus(resolver: Resolver): EventBus = EventDispatcher(resolver)

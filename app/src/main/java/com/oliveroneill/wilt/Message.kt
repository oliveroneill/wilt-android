package com.oliveroneill.wilt

import java.util.concurrent.atomic.AtomicBoolean

/**
 * An interface for messages to be sent through LiveData. This message will have some content but
 * may return null if the content has already been consumed, depending on the implementations
 * use-case. This is specifically useful for separating [Event]s and [Data], since [Event]s
 * can only be consumed once and will return null after that.
 */
interface Message<out T> {
    /**
     * Called to retrieve the content of the message. This may be null if messages are only intended to be
     * read once
     */
    fun getContent(): T?
}

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 * This is useful for navigation or dialogs that should only appear once.
 *
 * Taken from: https://gist.github.com/JoseAlcerreca/5b661f1800e1e654f07cc54fe87441af#file-event-kt
 * and modified
 */
open class Event<out T>(private val content: T): Message<T> {
    private var hasBeenHandled = AtomicBoolean(false)

    /**
     * Returns the content and prevents its use again.
     */
    override fun getContent(): T? {
        return if (hasBeenHandled.get()) {
            null
        } else {
            hasBeenHandled.set(true)
            content
        }
    }
}

/**
 * Data is a message that can be read multiple times
 */
open class Data<out T>(private val content: T): Message<T> {
    override fun getContent(): T? = content
}


package com.oliveroneill.wilt

import androidx.lifecycle.Observer

/**
 * An [Observer] for [Message]s, simplifying the pattern of checking if an [Event] has already been
 * handled and always sending on the content for [Data]
 *
 * [onEventUnhandledContent] is *only* called if the [Event]'s contents has not been handled.
 *
 * Taken from: https://gist.github.com/JoseAlcerreca/e0bba240d9b3cffa258777f12e5c0ae9
 * and modified
 */
class MessageObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Message<T>> {
    override fun onChanged(message: Message<T>?) {
        message?.getContent()?.let { value ->
            onEventUnhandledContent(value)
        }
    }
}

/*
 * MIT License
 *
 * Copyright (c) 2025 Yvan Mazy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.transferproxy.api.event;

import net.transferproxy.api.event.listener.EventListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface EventManager {

    /**
     * Calls all event listeners associated with the specified event type.
     * <p><p>
     * This method ensures that the provided event object matches the expected type
     * for the given event type.
     * It retrieves the list of registered listeners for
     * the event type and invokes each {@link EventListener#handle(Object)} method with the event.
     * If no specific listeners are found, the default listener is used (Check {@link EventType#buildDefaultListener()}).
     * <p>
     *
     * @param eventType the event type for which the listeners are called
     * @param event     the event object to be passed to the listeners
     *
     * @throws IllegalArgumentException if the event object does not match the expected type
     *                                  for the given event type
     * @throws NullPointerException     if either the eventType or the event is null
     */
    void call(final @NotNull EventType eventType, final @NotNull Object event);

    /**
     * Adds the specified {@link EventListener} to the list of listeners for the specified event type.
     *
     * @param eventType     the event type to which the event listener should be added
     * @param eventListener the event listener to add
     */
    <T extends EventListener<?>> void addListener(final @NotNull EventType eventType, final @NotNull T eventListener);

    /**
     * Removes the specified {@link EventListener} from the list of listeners for the specified event type.
     * <p>
     * If the event type is null, or the event listener is null, this method does nothing and returns false.
     * <p>
     * If the event listener is not found in the list of listeners, this method does nothing and returns false.
     * <p>
     * Otherwise, this method removes the specified event listener and returns true.
     *
     * @param eventType     the event type for which the listener is removed
     * @param eventListener the event listener to remove
     * @param <T>           the type of the event
     *
     * @return true if the event listener was found and removed, false otherwise
     */
    @Contract("null, _ -> false; _, null -> false; _, _ -> _")
    <T extends EventListener<?>> boolean removeListener(final EventType eventType, final T eventListener);

    /**
     * Retrieves the array of event listeners associated with the specified event type.
     *
     * @param eventType the event type for which listeners are retrieved
     * @param <T>       the type of the event listener
     *
     * @return an array of listeners for the specified event type, or null if the event type is null
     */
    @Contract(value = "null -> null; !null -> _", pure = true)
    <T extends EventListener<?>> T[] getListeners(final EventType eventType);

}
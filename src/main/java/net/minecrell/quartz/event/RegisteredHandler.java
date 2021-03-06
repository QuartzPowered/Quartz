/*
 * Quartz
 * Copyright (c) 2015, Minecrell <https://github.com/Minecrell>
 *
 * Based on Sponge and SpongeAPI, licensed under the MIT License (MIT).
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.minecrell.quartz.event;

import static java.util.Objects.requireNonNull;

import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.event.Cancellable;
import org.spongepowered.api.util.event.Event;
import org.spongepowered.api.util.event.Order;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

class RegisteredHandler implements EventHandler, Comparable<RegisteredHandler> {

    private final PluginContainer plugin;

    private final Class<? extends Event> eventClass;
    private final Order order;

    private final Method handle;
    private final EventHandler handler;

    private final boolean ignoreCancelled;

    RegisteredHandler(PluginContainer plugin, Class<? extends Event> eventClass, Order order, EventHandler handler, Method handle,
            boolean ignoreCancelled) {
        this.plugin = requireNonNull(plugin, "plugin");
        this.eventClass = requireNonNull(eventClass, "eventClass");
        this.order = requireNonNull(order, "order");
        this.handle = requireNonNull(handle, "handle");
        this.handler = requireNonNull(handler, "handler");
        this.ignoreCancelled = ignoreCancelled;
    }

    public PluginContainer getPlugin() {
        return plugin;
    }

    public Class<? extends Event> getEventClass() {
        return eventClass;
    }

    @Override
    public Object getHandle() {
        return handler.getHandle();
    }

    @Override
    public void handle(Event event) throws Throwable {
        if (ignoreCancelled && event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
            return;
        }

        handler.handle(event);
    }

    @Override
    public int compareTo(RegisteredHandler handler) {
        return order.compareTo(handler.order);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RegisteredHandler that = (RegisteredHandler) o;
        return handle.equals(that.handle);
    }

    @Override
    public int hashCode() {
        return handle.hashCode();
    }

}

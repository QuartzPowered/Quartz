/*
 * Ice
 * Copyright (c) 2015, Minecrell <https://github.com/Minecrell>
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

package net.minecrell.ice.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import net.minecrell.ice.Ice;
import net.minecrell.ice.IceGame;
import net.minecrell.ice.IceGameRegistry;
import net.minecrell.ice.event.IceEventManager;
import net.minecrell.ice.plugin.IcePluginManager;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.SimpleServiceManager;
import org.spongepowered.api.service.event.EventManager;

public class IceGuiceModule extends AbstractModule {

    private final Ice ice;

    public IceGuiceModule(Ice ice) {
        this.ice = ice;
    }

    @Override
    protected void configure() {
        bind(Ice.class).toInstance(ice);
        bind(Game.class).to(IceGame.class).in(Scopes.SINGLETON);
        bind(PluginManager.class).to(IcePluginManager.class).in(Scopes.SINGLETON);
        bind(EventManager.class).to(IceEventManager.class).in(Scopes.SINGLETON);
        bind(GameRegistry.class).to(IceGameRegistry.class).in(Scopes.SINGLETON);
        bind(ServiceManager.class).to(SimpleServiceManager.class).in(Scopes.SINGLETON);
    }

}

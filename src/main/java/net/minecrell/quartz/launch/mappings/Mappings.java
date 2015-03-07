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

package net.minecrell.quartz.launch.mappings;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableBiMap;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.IOException;

public class Mappings {

    private static Mappings instance;

    public static Mappings getInstance() {
        checkState(instance != null, "Not initialized yet");
        return instance;
    }

    public static void initialize(LaunchClassLoader loader) throws IOException {
        if (instance != null) return;
        instance = new Mappings();
        loader.registerTransformer(Mappings.class.getPackage().getName() + ".MappingsTransformer");
    }

    private final ImmutableBiMap<String, String> classes;
    private final ImmutableBiMap<String, String> methods;
    private final ImmutableBiMap<String, String> fields;

    public Mappings() throws IOException {
        ImmutableBiMap.Builder<String, String> classes = ImmutableBiMap.builder();
        ImmutableBiMap.Builder<String, String> methods = ImmutableBiMap.builder();
        ImmutableBiMap.Builder<String, String> fields = ImmutableBiMap.builder();

        loadMappings(classes, methods, fields);

        this.classes = classes.build();
        this.methods = methods.build();
        this.fields = fields.build();
    }

    private static void loadMappings(ImmutableBiMap.Builder<String, String> classes,
            ImmutableBiMap.Builder<String, String> methods, ImmutableBiMap.Builder<String, String> fields) throws IOException {
        System.out.println(Mappings.class.getProtectionDomain().getCodeSource().getLocation());
    }

}

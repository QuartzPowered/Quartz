package net.minecrell.quartz.guice;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.service.config.DefaultConfig;

import java.lang.annotation.Annotation;

class ConfigFileAnnotation implements DefaultConfig {

    private final boolean shared;

    ConfigFileAnnotation(boolean shared) {
        this.shared = shared;
    }

    @Override
    public boolean sharedRoot() {
        return shared;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return DefaultConfig.class;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultConfig)) {
            return false;
        }

        DefaultConfig that = (DefaultConfig) o;
        return sharedRoot() == that.sharedRoot();
    }

    @Override
    public int hashCode() {
        return (127 * "sharedRoot".hashCode()) ^ Boolean.valueOf(sharedRoot()).hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper('@' + getClass().getName())
                .add("shared", shared)
                .toString();
    }

}

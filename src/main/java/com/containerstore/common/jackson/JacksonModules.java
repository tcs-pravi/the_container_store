package com.containerstore.common.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.cfg.PackageVersion;
import com.fasterxml.jackson.databind.module.SimpleModule;

public final class JacksonModules {

    private JacksonModules() {
        throw new UnsupportedOperationException();
    }

    public static Module emptyToNullStringModule() {
        return new SimpleModule(PackageVersion.VERSION)
                .addDeserializer(String.class, new EmptyToNullStringDeserializer());
    }
}

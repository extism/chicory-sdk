package org.extism.chicory.sdk;

import java.util.EnumSet;
import java.util.List;

public class Manifest {

    public enum Validation {
        Import, Type, All;
    }

    public static class Options {
        boolean aot;
        EnumSet<Validation> validationFlags = EnumSet.noneOf(Validation.class);

        public Options withAoT() {
            this.aot = true;
            return this;
        }

        public Options withValidation(Validation... vs) {
            this.validationFlags.addAll(List.of(vs));
            return this;
        }
    }


    public static Builder ofWasms(ManifestWasm... wasms) {
        return new Builder(wasms);
    }

    public static class Builder {
        final ManifestWasm[] wasms;
        private Options options;
        private String name;

        private Builder(ManifestWasm[] manifestWasms) {
            this.wasms = manifestWasms;
        }

        public Builder withOptions(Options opts) {
            this.options = opts;
            return this;
        }

        public Manifest build() {
            return new Manifest(wasms, name, options);
        }

    }

    final ManifestWasm[] wasms;
    final Manifest.Options options;

    Manifest(ManifestWasm[] wasms, String name, Manifest.Options opts) {
        this.wasms = wasms;
        this.options = opts;
    }
}

package org.extism.sdk.chicory;

import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Machine;
import com.dylibso.chicory.wasi.WasiOptions;
import org.extism.sdk.chicory.http.HttpConfig;

import com.dylibso.chicory.wasm.types.MemoryLimits;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Manifest {

    public enum Validation {
        Import, Type, All;
    }

    public static class Options {
        boolean aot = false;
        Function<Instance, Machine> machineFactory;
        EnumSet<Validation> validationFlags = EnumSet.noneOf(Validation.class);
        ConfigProvider config = ConfigProvider.empty();
        WasiOptions wasiOptions = WasiOptions.builder().build();
        String[] allowedHosts = new String[0];
        public boolean enableHttpResponseHeaders;
        HttpConfig httpConfig = null;
        MemoryLimits memoryLimits = null;

        public Options withAoT() {
            return withAoT(true);
        }

        public Options withAoT(boolean enabled) {
            this.aot = enabled;
            return this;
        }

        public Options withMachineFactory(Function<Instance, Machine> machineFactory) {
            this.machineFactory = machineFactory;
            return this;
        }

        public Options withConfig(Map<String, String> config) {
            return withConfigProvider(ConfigProvider.ofMap(config));
        }

        public Options withConfigProvider(ConfigProvider config) {
            this.config = config;
            return this;
        }


        public Options withValidation(Validation... vs) {
            this.validationFlags.addAll(List.of(vs));
            return this;
        }

        public Options withAllowedHosts(String... allowedHosts) {
            for (String allowedHost : allowedHosts) {
                // Wildcards are only allowed at starting position and may occur only once.
                if (allowedHost.indexOf('*') > 0 || allowedHost.indexOf('*', 1) != -1) {
                    throw new ExtismException("Illegal pattern " + allowedHost);
                }
            }
            this.allowedHosts = allowedHosts;
            return this;
        }

        public Options withHttpConfig(HttpConfig httpConfig) {
            this.httpConfig = httpConfig;
            return this;
        }

        public Options withHttpResponseHeaders(boolean enabled) {
            this.enableHttpResponseHeaders = enabled;
            return this;
        }

        public Options withWasi(WasiOptions wasiOptions) {
            this.wasiOptions = wasiOptions;
            return this;
        }

        public Options withMemoryLimits(int initial, int max) {
            this.memoryLimits = new MemoryLimits(initial, max);
            return this;
        }

    }


    public static Builder ofWasms(ManifestWasm... wasms) {
        return new Builder(wasms);
    }

    public static class Builder {
        final ManifestWasm[] wasms;
        private Options options = new Manifest.Options();
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

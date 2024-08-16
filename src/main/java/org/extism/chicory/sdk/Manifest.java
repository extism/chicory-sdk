package org.extism.chicory.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

class ManifestWasm {
}

class ManifestWasmBytes extends ManifestWasm {
    final byte[] bytes;

    public ManifestWasmBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}

class ManifestWasmFile extends ManifestWasm {
    final Path filePath;

    public ManifestWasmFile(Path path) {
        this.filePath = path;
    }
}

class ManifestWasmPath extends ManifestWasm {
    final String path;

    public ManifestWasmPath(String path) {
        this.path = path;
    }
}

class ManifestWasmUrl extends ManifestWasm {
    final String url;

    public ManifestWasmUrl(String url) {
        this.url = url;
    }

    public InputStream getUrlAsStream() {
        try {
            var url = new URL(this.url);
            return url.openStream();
        } catch (MalformedURLException e) {
            throw new ExtismException(e);
        } catch (IOException e) {
            throw new ExtismException(e);
        }

    }
}

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

    public static class Builder {
        final ManifestWasm[] wasms;
        private Options options;

        private Builder(ManifestWasm[] manifestWasms) {
            this.wasms = manifestWasms;
        }

        public static Builder fromPath(String path) {
            var wasm = new ManifestWasmPath(path);
            return new Builder(new ManifestWasm[]{wasm});
        }

        public static Builder fromUrl(String url) {
            var wasm = new ManifestWasmUrl(url);
            return new Builder(new ManifestWasm[]{wasm});
        }

        public static Builder fromFilePath(Path path) {
            var wasm = new ManifestWasmFile(path);
            return new Builder(new ManifestWasm[]{wasm});
        }

        public static Builder fromBytes(byte[] bytes) {
            var wasm = new ManifestWasmBytes(bytes);
            return new Builder(new ManifestWasm[]{wasm});
        }

        public Builder withOptions(Options opts) {
            this.options = opts;
            return this;
        }

        public Manifest build() {
            return new Manifest(wasms, options);
        }

    }

    final ManifestWasm[] wasms;
    final Manifest.Options options;

    Manifest(ManifestWasm[] wasms, Manifest.Options opts) {
        this.wasms = wasms;
        this.options = opts;
    }
}

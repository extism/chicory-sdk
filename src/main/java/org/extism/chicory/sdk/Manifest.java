package org.extism.chicory.sdk;

class ManifestWasm {
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
}

class Manifest {
    final ManifestWasm[] wasms;

    public static Manifest fromPath(String path) {
        var wasm = new ManifestWasmPath(path);
        return new Manifest(new ManifestWasm[]{wasm});
    }

    public static Manifest fromUrl(String url) {
        var wasm = new ManifestWasmUrl(url);
        return new Manifest(new ManifestWasm[]{wasm});
    }

    Manifest(ManifestWasm[] wasms) {
        this.wasms = wasms;
    }
}

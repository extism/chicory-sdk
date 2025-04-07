package org.extism.sdk.chicory.core.e2e;

import com.dylibso.chicory.wasi.WasiOptions;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import junit.framework.TestCase;
import org.extism.sdk.chicory.core.ExtismFunction;
import org.extism.sdk.chicory.core.ExtismFunctionException;
import org.extism.sdk.chicory.core.ExtismHostFunction;
import org.extism.sdk.chicory.core.ExtismValType;
import org.extism.sdk.chicory.core.Manifest;
import org.extism.sdk.chicory.core.ManifestWasm;
import org.extism.sdk.chicory.core.Plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginTest extends TestCase {

    public void testRollDice() {
        var path = Path.of("./src/test/resources/void-return/void-return.wasm");
        var wasm = ManifestWasm.fromFilePath(path).build();
        var manifest = Manifest.ofWasms(wasm).build();
        var plugin = Plugin.ofManifest(manifest).build();
        ExtismFunctionException ex = null;
        try {
            var out = plugin.call("dummy", new byte[0]);
        } catch (ExtismFunctionException e) {
            ex = e;
        }
        assertTrue(ex.getMessage().contains("i32 return code"));
    }

    public void testGreet() {
        var url = "https://github.com/extism/plugins/releases/download/v1.1.1/greet.wasm";
        var wasm = ManifestWasm.fromUrl(url).build();
        var manifest = Manifest.ofWasms(wasm).build();
        var plugin = Plugin.ofManifest(manifest).build();
        var input = "Benjamin";
        var result = new String(plugin.call("greet", input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        assertEquals("Hello, Benjamin!", result);
    }

    public void testGreetAoT() {
        var url = "https://github.com/extism/plugins/releases/download/v1.1.1/greet.wasm";
        var wasm = ManifestWasm.fromUrl(url).build();
        var manifest = Manifest.ofWasms(wasm).withOptions(new Manifest.Options().withAoT()).build();
        var plugin = Plugin.ofManifest(manifest).build();
        var input = "Benjamin";
        var result = new String(plugin.call("greet", input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        assertEquals("Hello, Benjamin!", result);
    }

    public void testCountVowels() {
        var url = "https://github.com/extism/plugins/releases/download/v1.1.1/count_vowels.wasm";
        var wasm = ManifestWasm.fromUrl(url).build();
        var manifest = Manifest.ofWasms(wasm).build();
        var plugin = Plugin.ofManifest(manifest).build();

        {
            var output = plugin.call("count_vowels", "Hello, World!".getBytes(StandardCharsets.UTF_8));
            var result = new String(output, StandardCharsets.UTF_8);
            assertEquals("{\"count\":3,\"total\":3,\"vowels\":\"aeiouAEIOU\"}", result);
        }

        {
            var output = plugin.call("count_vowels", "Hello, World!".getBytes(StandardCharsets.UTF_8));
            var result = new String(output, StandardCharsets.UTF_8);
            assertEquals("{\"count\":3,\"total\":6,\"vowels\":\"aeiouAEIOU\"}", result);
        }

        {
            var output = plugin.call("count_vowels", "Hello, World!".getBytes(StandardCharsets.UTF_8));
            var result = new String(output, StandardCharsets.UTF_8);
            assertEquals("{\"count\":3,\"total\":9,\"vowels\":\"aeiouAEIOU\"}", result);
        }
    }

    public void testCountVowelsWithConfig() {
        var url = "https://github.com/extism/plugins/releases/download/v1.1.1/count_vowels.wasm";
        var wasm = ManifestWasm.fromUrl(url).build();
        var config = Map.of("vowels", "aeiouyAEIOUY");
        var manifest = Manifest.ofWasms(wasm)
                .withOptions(new Manifest.Options().withConfig(config)).build();
        var plugin = Plugin.ofManifest(manifest).build();

        {
            var output = plugin.call("count_vowels", "Yellow, World!".getBytes(StandardCharsets.UTF_8));
            var result = new String(output, StandardCharsets.UTF_8);
            assertEquals("{\"count\":4,\"total\":4,\"vowels\":\"aeiouyAEIOUY\"}", result);
        }
    }

    public void testCountVowelsKVStore() {
        var url = "https://github.com/extism/plugins/releases/download/v1.1.1/count_vowels_kvstore.wasm";
        var wasm = ManifestWasm.fromUrl(url).build();
        var manifest = Manifest.ofWasms(wasm).build();

        // Our application KV store
        // Pretend this is redis or a database :)
        var kvStore = new HashMap<String, byte[]>();

        ExtismFunction kvWrite = (plugin, params, returns) -> {
            System.out.println("Hello from kv_write Java Function!");
            var key = plugin.memory().readString(params.getRaw(0));
            var value = plugin.memory().readBytes(params.getRaw(1));
            System.out.println("Writing to key " + key);
            kvStore.put(key, value);
        };

        ExtismFunction kvRead = (plugin, params, returns) -> {
            System.out.println("Hello from kv_read Java Function!");
            var key = plugin.memory().readString(params.getRaw(0));
            System.out.println("Reading from key " + key);
            var value = kvStore.get(key);
            if (value == null) {
                // default to zeroed bytes
                var zero = new byte[]{0, 0, 0, 0};
                returns.setRaw(0, plugin.memory().writeBytes(zero));
            } else {
                returns.setRaw(0, plugin.memory().writeBytes(value));
            }
        };

        var kvWriteHostFn = ExtismHostFunction.of(
                "kv_write",
                List.of(ExtismValType.I64, ExtismValType.I64),
                List.of(),
                kvWrite
        );

        var kvReadHostFn = ExtismHostFunction.of(
                "kv_read",
                List.of(ExtismValType.I64),
                List.of(ExtismValType.I64),
                kvRead
        );

        var plugin = Plugin.ofManifest(manifest).withHostFunctions(kvReadHostFn, kvWriteHostFn).build();
        var output = plugin.call("count_vowels", "Yellow, World!".getBytes(StandardCharsets.UTF_8));
        var result = new String(output, StandardCharsets.UTF_8);
        assertEquals("{\"count\":3,\"total\":3,\"vowels\":\"aeiouAEIOU\"}", result);
    }


    public void testWasi() throws IOException {
        var url = "https://github.com/extism/plugins/releases/download/v1.1.1/read_write.wasm";
        var wasm = ManifestWasm.fromUrl(url).build();
        try (var fs = Jimfs.newFileSystem(
                Configuration.unix().toBuilder().setAttributeViews("unix").build())) {

            Path tmp = fs.getPath("tmp");
            Files.createDirectory(tmp);
            Path path = tmp.resolve("hello.txt");

            var config = Map.of("path", path.toString());
            var manifest = Manifest.ofWasms(wasm)
                    .withOptions(new Manifest.Options()
                            .withConfig(config)
                            .withWasi(WasiOptions.builder()
                                    .withArguments(List.of("read_write"))
                                    .withDirectory("/tmp", tmp).build())).build();
            var plugin = Plugin.ofManifest(manifest).build();


            Files.writeString(path, "hello world", StandardOpenOption.CREATE);
            var output = plugin.call("try_read", new byte[0]);

            var result = new String(output, StandardCharsets.UTF_8);
            assertEquals("hello world", result);
        }
    }

}

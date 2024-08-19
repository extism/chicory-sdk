# Chicory SDK

This is an experimental Java SDK using the [JVM-native Chicory runtime](https://github.com/dylibso/chicory).
This library and the runtime are still very experimental, however, there should be enough there
to complete a full Extism SDK. If anyone would like to work on it feel free to reach out to
@bhelx on the [Extism Discord](https://extism.org/discord).

> **Note**: If you are interested in a solid and working Java SDK, see our [Extism Java SDK](https://github.com/extism/java-sdk).
> But if you have a need for pure Java solution, please reach out!

## Example

```java
        var manifest =
        Manifest.ofWasms(
                ManifestWasm.fromUrl(
                                "https://github.com/extism/plugins/releases/download/v1.1.0/greet.wasm")
                        .build()).build();
var plugin = Plugin.Builder.ofManifest(manifest).build();
var input = "Benjamin";
var result = new String(plugin.call("greet", input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
assertEquals("Hello, Benjamin!", result);
```
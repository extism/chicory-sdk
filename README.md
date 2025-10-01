# Chicory SDK

This is an experimental Java SDK using the [JVM-native Chicory runtime](https://github.com/dylibso/chicory).
This library and the runtime are still very experimental, however, there should be enough there
to complete a full Extism SDK. If anyone would like to work on it feel free to reach out to
@bhelx on the [Extism Discord](https://extism.org/discord).

> **Note**: If you are interested in a solid and working Java SDK, see our [Extism Java SDK](https://github.com/extism/java-sdk).
> But if you have a need for pure Java solution, please reach out!

## Installation

### Maven

To use the Chicory java-sdk with maven you need to add the following dependency to your `pom.xml` file:
```xml
<dependency>
    <groupId>org.extism.sdk</groupId>
    <artifactId>chicory-sdk-core</artifactId>
    <version>999-SNAPSHOT</version>
</dependency>
```


### Gradle

To use the Chicory java-sdk with maven you need to add the following dependency to your `build.gradle` file:

```
implementation 'org.extism.sdk:chicory-sdk:999-SNAPSHOT'
```

## Getting Started

The primary concept in Extism is the [plug-in](https://extism.org/docs/concepts/plug-in). You can think of a plug-in as a code module stored in a `.wasm` file.
Since you may not have a Extism plug-in on hand to test, let's load a demo plug-in from the web:

```java
var url = "https://github.com/extism/plugins/releases/latest/download/count_vowels.wasm";
var wasm = ManifestWasm.fromUrl(url).build();
var manifest = Manifest.ofWasms(wasm).build();
var plugin = Plugin.ofManifest(manifest).build();
```

> **Note**: See [the Manifest docs](https://www.javadoc.io/doc/org.extism.sdk/extism/latest/org/extism/sdk/manifest/Manifest.html) as it has a rich schema and a lot of options.

### Calling A Plug-in's Exports

This plug-in was written in Rust and it does one thing, it counts vowels in a string. As such, it exposes one "export" function: `count_vowels`. 
We can call exports using [Plugin#call](https://www.javadoc.io/doc/org.extism.sdk/extism/latest/org/extism/sdk/Plugin.html#call(java.lang.String,byte[]))

```java
var output = plugin.call("count_vowels", "Hello, World!".getBytes(StandardCharsets.UTF_8));
System.out.println(new String(output, StandardCharsets.UTF_8));
// => "{"count": 3, "total": 3, "vowels": "aeiouAEIOU"}"
```

All exports have a simple interface of bytes-in and bytes-out.
This plug-in happens to take a string and return a JSON encoded string with a report of results.


### Plug-in State

Plug-ins may be stateful or stateless. Plug-ins can maintain state b/w calls by the use of variables.
Our count vowels plug-in remembers the total number of vowels it's ever counted in the "total" key in the result.
You can see this by making subsequent calls to the export:

```java
var output = plugin.call("count_vowels","Hello, World!".getBytes(StandardCharsets.UTF_8));
System.out.println(output);
// => "{"count": 3, "total": 6, "vowels": "aeiouAEIOU"}"

var output = plugin.call("count_vowels", "Hello, World!".getBytes(StandardCharsets.UTF_8));
System.out.println(output);
// => "{"count": 3, "total": 9, "vowels": "aeiouAEIOU"}"
```

These variables will persist until this plug-in is freed or you initialize a new one.

### Configuration

Plug-ins may optionally take a configuration object. This is a static way to configure the plug-in.
Our count-vowels plugin takes an optional configuration to change out which characters are considered vowels. Example:

```java
var plugin = new Plugin(manifest, false, null);
var output = plugin.call("count_vowels", "Yellow, World!");
System.out.println(output);
// => {"count": 3, "total": 3, "vowels": "aeiouAEIOU"}

// Let's change the vowels config it uses to determine what is a vowel:
var config = Map.of("vowels", "aeiouyAEIOUY");
var manifest2 = Manifest.ofWasms(wasm)
        .withOptions(new Manifest.Options().withConfig(config)).build();
var plugin = Plugin.ofManifest(manifest2).build();
var result = new String(plugin.call("count_vowels", "Yellow, World!".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
System.out.println(output);
// => {"count": 4, "total": 4, "vowels": "aeiouyAEIOUY"}
// ^ note count changed to 4 as we configured Y as a vowel this time
```

### Host Functions

Let's extend our count-vowels example a little bit: Instead of storing the `total` in an ephemeral plug-in var,
let's store it in a persistent key-value store!

Wasm can't use our app's KV store on its own. This is where [Host Functions](https://extism.org/docs/concepts/host-functions) come in.

[Host functions](https://extism.org/docs/concepts/host-functions) allow us to grant new capabilities to our plug-ins from our application.
They are simply some java methods you write which can be passed down and invoked from any language inside the plug-in.

Let's load the manifest like usual but load up this `count_vowels_kvstore` plug-in:

```java
var url = "https://github.com/extism/plugins/releases/latest/download/count_vowels_kvstore.wasm";
var manifest = new Manifest(List.of(UrlWasmSource.fromUrl(url)));
var plugin = new Plugin(manifest, false, null);
```

> *Note*: The source code for this plug-in is [here](https://github.com/extism/plugins/blob/main/count_vowels_kvstore/src/lib.rs)
> and is written in rust, but it could be written in any of our PDK languages.

Unlike our previous plug-in, this plug-in expects you to provide host functions that satisfy its import interface for a KV store.
We want to expose two functions to our plugin, `kv_write(String key, Bytes value)` which writes a bytes value to a key and `Bytes kv_read(String key)` which reads the bytes at the given `key`.

```java
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
```

> *Note*: In order to write host functions you should get familiar with the methods on the [ExtismCurrentPlugin](https://www.javadoc.io/doc/org.extism.sdk/extism/latest/org/extism/sdk/ExtismCurrentPlugin.html) class.
> The `plugin` parameter is an instance of this class.

Now we just need to pass in these function references when creating the plugin:.

```java
var plugin = Plugin.ofManifest(manifest).withHostFunctions(kvReadHostFn, kvWriteHostFn).build();
var output = plugin.call("count_vowels", "Yellow, World!".getBytes(StandardCharsets.UTF_8));
var result = new String(output, StandardCharsets.UTF_8);
// => Hello from kv_read Java Function!
// => Reading from key count-vowels
// => Hello from kv_write Java Function!
// => Writing to key count-vowels
System.out.println(output);
// => {"count": 3, "total": 3, "vowels": "aeiouAEIOU"}
```

## Development

# Build

To build the Extism chicory-sdk run the following command:

```
mvn clean verify
```


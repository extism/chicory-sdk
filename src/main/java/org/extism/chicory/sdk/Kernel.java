package org.extism.chicory.sdk;

import static com.dylibso.chicory.wasm.types.Value.*;

import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Module;
import com.dylibso.chicory.wasm.types.Value;
import com.dylibso.chicory.wasm.types.ValueType;
import com.dylibso.chicory.runtime.Memory;
import java.util.HashMap;
import java.util.List;

public class Kernel {
    private static final String IMPORT_MODULE_NAME = "extism:host/env";
    private final Memory memory;
    private final ExportFunction alloc;
    private final ExportFunction free;
    private final ExportFunction length;
    private final ExportFunction lengthUnsafe;
    private final ExportFunction loadU8;
    private final ExportFunction loadU64;
    private final ExportFunction inputLoadU8;
    private final ExportFunction inputLoadU64;
    private final ExportFunction storeU8;
    private final ExportFunction storeU64;
    private final ExportFunction inputSet;
    private final ExportFunction inputLen;
    private final ExportFunction inputOffset;
    private final ExportFunction outputLen;
    private final ExportFunction outputOffset;
    private final ExportFunction outputSet;
    private final ExportFunction reset;
    private final ExportFunction errorSet;
    private final ExportFunction errorGet;
    private final ExportFunction memoryBytes;

    public Kernel() {
        var kernelStream = getClass().getClassLoader().getResourceAsStream("extism-runtime.wasm");
        Instance kernel = Module.builder(kernelStream).build().instantiate();
        memory = kernel.memory();
        alloc = kernel.export("alloc");
        free = kernel.export("free");
        length = kernel.export("length");
        lengthUnsafe = kernel.export("length_unsafe");
        loadU8 = kernel.export("load_u8");
        loadU64 = kernel.export("load_u64");
        inputLoadU8 = kernel.export("input_load_u8");
        inputLoadU64 = kernel.export("input_load_u64");
        storeU8 = kernel.export("store_u8");
        storeU64 = kernel.export("store_u64");
        inputSet = kernel.export("input_set");
        inputLen = kernel.export("input_length");
        inputOffset = kernel.export("input_offset");
        outputLen = kernel.export("output_length");
        outputOffset = kernel.export("output_offset");
        outputSet = kernel.export("output_set");
        reset = kernel.export("reset");
        errorSet = kernel.export("error_set");
        errorGet = kernel.export("error_get");
        memoryBytes = kernel.export("memory_bytes");
    }

    public void setInput(byte[] input) {
        var ptr = alloc.apply(i64(input.length))[0];
        memory.write(ptr.asInt(), input);
        inputSet.apply(ptr, i64(input.length));
    }

    public byte[] getOutput() {
        var ptr = outputOffset.apply()[0];
        var len = outputLen.apply()[0];
        return memory.readBytes(ptr.asInt(), len.asInt());
    }

    public HostFunction[] toHostFunctions() {
        var hostFunctions = new HostFunction[23];
        int count = 0;

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> alloc.apply(args),
                        IMPORT_MODULE_NAME,
                        "alloc",
                        List.of(ValueType.I64),
                        List.of(ValueType.I64));

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> free.apply(args),
                        IMPORT_MODULE_NAME,
                        "free",
                        List.of(ValueType.I64),
                        List.of());

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> length.apply(args),
                        IMPORT_MODULE_NAME,
                        "length",
                        List.of(ValueType.I64),
                        List.of(ValueType.I64));

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> lengthUnsafe.apply(args),
                        IMPORT_MODULE_NAME,
                        "length_unsafe",
                        List.of(ValueType.I64),
                        List.of(ValueType.I64));

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> loadU8.apply(args),
                        IMPORT_MODULE_NAME,
                        "load_u8",
                        List.of(ValueType.I64),
                        List.of(ValueType.I32));

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> loadU64.apply(args),
                        IMPORT_MODULE_NAME,
                        "load_u64",
                        List.of(ValueType.I64),
                        List.of(ValueType.I64));

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> inputLoadU8.apply(args),
                        IMPORT_MODULE_NAME,
                        "input_load_u8",
                        List.of(ValueType.I64),
                        List.of(ValueType.I32));

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> inputLoadU64.apply(args),
                        IMPORT_MODULE_NAME,
                        "input_load_u64",
                        List.of(ValueType.I64),
                        List.of(ValueType.I64));

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> storeU8.apply(args),
                        IMPORT_MODULE_NAME,
                        "store_u8",
                        List.of(ValueType.I64, ValueType.I32),
                        List.of());

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> storeU64.apply(args),
                        IMPORT_MODULE_NAME,
                        "store_u64",
                        List.of(ValueType.I64, ValueType.I64),
                        List.of());

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> inputSet.apply(args),
                        IMPORT_MODULE_NAME,
                        "input_set",
                        List.of(ValueType.I64, ValueType.I64),
                        List.of());

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> inputLen.apply(args),
                        IMPORT_MODULE_NAME,
                        "input_length",
                        List.of(),
                        List.of(ValueType.I64));

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> inputOffset.apply(args),
                        IMPORT_MODULE_NAME,
                        "input_offset",
                        List.of(),
                        List.of(ValueType.I64));

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> outputSet.apply(args),
                        IMPORT_MODULE_NAME,
                        "output_set",
                        List.of(),
                        List.of(ValueType.I64));

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> outputLen.apply(args),
                        IMPORT_MODULE_NAME,
                        "output_length",
                        List.of(),
                        List.of(ValueType.I64));

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> outputOffset.apply(args),
                        IMPORT_MODULE_NAME,
                        "output_offset",
                        List.of(),
                        List.of(ValueType.I64));

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> reset.apply(args),
                        IMPORT_MODULE_NAME,
                        "reset",
                        List.of(),
                        List.of());

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> errorSet.apply(args),
                        IMPORT_MODULE_NAME,
                        "error_set",
                        List.of(ValueType.I64),
                        List.of());

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> errorGet.apply(args),
                        IMPORT_MODULE_NAME,
                        "error_get",
                        List.of(),
                        List.of(ValueType.I64));

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> memoryBytes.apply(args),
                        IMPORT_MODULE_NAME,
                        "memory_bytes",
                        List.of(),
                        List.of(ValueType.I64));

        var vars = new HashMap<String, byte[]>();

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> {
                            // System.out.println("_var_get " + args);
                            //                    var keyLen = Length.apply(args[0])[0];
                            //                    var key = memory.getString(args[0].asInt(),
                            // keyLen.asInt());
                            //                    var value = vars.get(key);
                            return new Value[] {i64(0)};
                        },
                        IMPORT_MODULE_NAME,
                        "var_get",
                        List.of(ValueType.I64, ValueType.I64),
                        List.of(ValueType.I64));

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> {
                            // System.out.println("_var_set" + args);
                            //                    var keyLen = Length.apply(args[0])[0];
                            //                    var key = memory.getString(args[0].asInt(),
                            // keyLen.asInt());
                            //                    var value = vars.get(key);
                            return null;
                        },
                        IMPORT_MODULE_NAME,
                        "var_set",
                        List.of(ValueType.I64, ValueType.I64),
                        List.of());

        hostFunctions[count++] =
                new HostFunction(
                        (Instance instance, Value... args) -> {
                            // System.out.println("_config_get" + args);
                            //                    var keyLen = Length.apply(args[0])[0];
                            //                    var key = memory.getString(args[0].asInt(),
                            // keyLen.asInt());
                            //                    var value = vars.get(key);
                            return new Value[] {i64(0)};
                        },
                        IMPORT_MODULE_NAME,
                        "config_get",
                        List.of(ValueType.I64),
                        List.of(ValueType.I64));

        return hostFunctions;
    }
}

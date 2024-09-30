package org.extism.chicory.sdk;

import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Module;
import com.dylibso.chicory.wasm.types.Value;
import com.dylibso.chicory.wasm.types.ValueType;

import java.util.List;

import static com.dylibso.chicory.wasm.types.Value.i64;

public class Kernel {
    static final String IMPORT_MODULE_NAME = "extism:host/env";
    final com.dylibso.chicory.runtime.Memory instanceMemory;
    final ExportFunction alloc;
    final ExportFunction free;
    final ExportFunction length;
    final ExportFunction lengthUnsafe;
    final ExportFunction loadU8;
    final ExportFunction loadU64;
    final ExportFunction inputLoadU8;
    final ExportFunction inputLoadU64;
    final ExportFunction storeU8;
    final ExportFunction storeU64;
    final ExportFunction inputSet;
    final ExportFunction inputLen;
    final ExportFunction inputOffset;
    final ExportFunction outputLen;
    final ExportFunction outputOffset;
    final ExportFunction outputSet;
    final ExportFunction reset;
    final ExportFunction errorSet;
    final ExportFunction errorGet;
    final ExportFunction memoryBytes;

    public Kernel() {
        Instance kernel = module().instantiate();
        instanceMemory = kernel.memory();
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

    public static Module module() {
        var kernelStream = Kernel.class.getClassLoader().getResourceAsStream("extism-runtime.wasm");
        return Module.builder(kernelStream).build();
    }

    public void setInput(byte[] input) {
        var ptr = alloc.apply(i64(input.length))[0];
        instanceMemory.write(ptr.asInt(), input);
        inputSet.apply(ptr, i64(input.length));
    }

    byte[] getOutput() {
        var ptr = outputOffset.apply()[0];
        var len = outputLen.apply()[0];
        return instanceMemory.readBytes(ptr.asInt(), len.asInt());
    }


    HostFunction[] toHostFunctions() {
        var hostFunctions = new HostFunction[20];
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
                        List.of(ValueType.I64, ValueType.I64),
                        List.of());

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
        return hostFunctions;
    }
}

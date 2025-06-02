package org.extism.sdk.chicory;

import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import com.dylibso.chicory.wasm.types.FunctionType;
import com.dylibso.chicory.wasm.types.ValType;

import java.util.List;

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
        this(null);
    }

    Kernel(CachedAotMachineFactory machineFactory) {
        Instance kernel = instance(machineFactory);
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

    private static Instance instance(CachedAotMachineFactory machineFactory) {
        var kernelStream = Kernel.class.getClassLoader().getResourceAsStream("extism-runtime.wasm");
        WasmModule module = Parser.parse(kernelStream);
        if (machineFactory != null) {
            machineFactory.compile(module);
        }
        return Instance.builder(module).withMachineFactory(machineFactory).build();
    }

    public void setInput(byte[] input) {
        reset.apply();
        var ptr = alloc.apply(input.length)[0];
        instanceMemory.write((int) ptr, input);
        inputSet.apply(ptr, input.length);
    }

    byte[] getOutput() {
        var ptr = outputOffset.apply()[0];
        var len = outputLen.apply()[0];
        return instanceMemory.readBytes((int) ptr, (int) len);
    }

    public String getError() {
        long ptr = errorGet.apply()[0];
        long len = length.apply(ptr)[0];
        return instanceMemory.readString((int) ptr, (int) len);
    }

    HostFunction[] toHostFunctions() {
        var hostFunctions = new HostFunction[20];
        int count = 0;

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "alloc",
                        FunctionType.of(List.of(ValType.I64), List.of(ValType.I64)),
                        (Instance instance, long... args) -> alloc.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "free",
                        FunctionType.of(List.of(ValType.I64), List.of()),
                        (Instance instance, long... args) -> free.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "length",
                        FunctionType.of(List.of(ValType.I64), List.of(ValType.I64)),
                        (Instance instance, long... args) -> length.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "length_unsafe",
                        FunctionType.of(List.of(ValType.I64), List.of(ValType.I64)),
                        (Instance instance, long... args) -> lengthUnsafe.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "load_u8",
                        FunctionType.of(List.of(ValType.I64), List.of(ValType.I32)),
                        (Instance instance, long... args) -> loadU8.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "load_u64",
                        FunctionType.of(List.of(ValType.I64), List.of(ValType.I64)),
                        (Instance instance, long... args) -> loadU64.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "input_load_u8",
                        FunctionType.of(List.of(ValType.I64), List.of(ValType.I32)),
                        (Instance instance, long... args) -> inputLoadU8.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "input_load_u64",
                        FunctionType.of(List.of(ValType.I64), List.of(ValType.I64)),
                        (Instance instance, long... args) -> inputLoadU64.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "store_u8",
                        FunctionType.of(List.of(ValType.I64, ValType.I32), List.of()),
                        (Instance instance, long... args) -> storeU8.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "store_u64",
                        FunctionType.of(List.of(ValType.I64, ValType.I64), List.of()),
                        (Instance instance, long... args) -> storeU64.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "input_set",
                        FunctionType.of(List.of(ValType.I64, ValType.I64), List.of()),
                        (Instance instance, long... args) -> inputSet.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "input_length",
                        FunctionType.of(List.of(), List.of(ValType.I64)),
                        (Instance instance, long... args) -> inputLen.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "input_offset",
                        FunctionType.of(List.of(), List.of(ValType.I64)),
                        (Instance instance, long... args) -> inputOffset.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "output_set",
                        FunctionType.of(List.of(ValType.I64, ValType.I64), List.of()),
                        (Instance instance, long... args) -> outputSet.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "output_length",
                        FunctionType.of(List.of(), List.of(ValType.I64)),
                        (Instance instance, long... args) -> outputLen.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "output_offset",
                        FunctionType.of(List.of(), List.of(ValType.I64)),
                        (Instance instance, long... args) -> outputOffset.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "reset",
                        FunctionType.of(List.of(), List.of()),
                        (Instance instance, long... args) -> reset.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "error_set",
                        FunctionType.of(List.of(ValType.I64), List.of()),
                        (Instance instance, long... args) -> errorSet.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "error_get",
                        FunctionType.of(List.of(), List.of(ValType.I64)),
                        (Instance instance, long... args) -> errorGet.apply(args)
                );

        hostFunctions[count++]
                = new HostFunction(
                        IMPORT_MODULE_NAME,
                        "memory_bytes",
                        FunctionType.of(List.of(), List.of(ValType.I64)),
                        (Instance instance, long... args) -> memoryBytes.apply(args)
                );
        return hostFunctions;
    }
}

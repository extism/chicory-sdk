package org.extism.chicory.sdk;

import com.dylibso.chicory.aot.AotMachine;
import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.runtime.*;
import com.dylibso.chicory.runtime.Memory;
import com.dylibso.chicory.wasm.Module;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.types.*;

import java.util.List;

import static com.dylibso.chicory.wasm.types.Value.i64;

public class Kernel {
    public static final String IMPORT_MODULE_NAME = "extism:host/env";
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
    private final Module module;

    public Kernel(Logger logger, Manifest.Options opts) {
        var kernelStream = getClass().getClassLoader().getResourceAsStream("extism-runtime.wasm");
        this.module = Parser.parse(kernelStream);

        var instanceBuilder = Instance.builder(module);

        if (opts != null) {
            if (opts.aot) {
                instanceBuilder = instanceBuilder.withMachineFactory(AotMachine::new);
            }
        }

        var kernel = instanceBuilder.build().initialize(true);
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

    public Module module() {
        return module;
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

    public HostModule toHostModule() {
        FunctionSection functionSection = module.functionSection();
        for (int i = 0; i < module.exportSection().exportCount(); i++) {

            Export export = module.exportSection().getExport(i);
            if (export.exportType() == ExternalType.FUNCTION) {
                throw new UnsupportedOperationException("todo");
            }

            FunctionType functionType = functionSection.getFunctionType(i, module.typeSection());
        }

        return HostModule.builder(IMPORT_MODULE_NAME)
        .withFunctionSignature(
                        "alloc",
                        List.of(ValueType.I64),
                        List.of(ValueType.I64))
        .withFunctionSignature(
                        "free",
                        List.of(ValueType.I64),
                        List.of())
        .withFunctionSignature(
                        "length",
                        List.of(ValueType.I64),
                        List.of(ValueType.I64))
        .withFunctionSignature(
                        "length_unsafe",
                        List.of(ValueType.I64),
                        List.of(ValueType.I64))
        .withFunctionSignature(
                        "load_u8",
                        List.of(ValueType.I64),
                        List.of(ValueType.I32))
        .withFunctionSignature(
                        "load_u64",
                        List.of(ValueType.I64),
                        List.of(ValueType.I64))
        .withFunctionSignature(
                        "input_load_u8",
                        List.of(ValueType.I64),
                        List.of(ValueType.I32))
        .withFunctionSignature(
                        "input_load_u64",
                        List.of(ValueType.I64),
                        List.of(ValueType.I64))
        .withFunctionSignature(
                        "store_u8",
                        List.of(ValueType.I64, ValueType.I32),
                        List.of())
        .withFunctionSignature(
                        "store_u64",
                        List.of(ValueType.I64, ValueType.I64),
                        List.of())
        .withFunctionSignature(
                        "input_set",
                        List.of(ValueType.I64, ValueType.I64),
                        List.of())
        .withFunctionSignature(
                        "input_length",
                        List.of(),
                        List.of(ValueType.I64))
        .withFunctionSignature(
                        "input_offset",
                        List.of(),
                        List.of(ValueType.I64))
        .withFunctionSignature(
                        "output_set",
                        List.of(ValueType.I64, ValueType.I64),
                        List.of())
        .withFunctionSignature(
                        "output_length",
                        List.of(),
                        List.of(ValueType.I64))
        .withFunctionSignature(
                        "output_offset",
                        List.of(),
                        List.of(ValueType.I64))
        .withFunctionSignature(
                        "reset",
                        List.of(),
                        List.of())
        .withFunctionSignature(
                        "error_set",
                        List.of(ValueType.I64),
                        List.of())
        .withFunctionSignature(
                        "error_get",
                        List.of(),
                        List.of(ValueType.I64))
        .withFunctionSignature(
                        "memory_bytes",
                        List.of(),
                        List.of(ValueType.I64))
        .withFunctionSignature(
                        "var_get",
                        List.of(ValueType.I64, ValueType.I64),
                        List.of(ValueType.I64))
        .withFunctionSignature(
                        "var_set",
                        List.of(ValueType.I64, ValueType.I64),
                        List.of())
        .withFunctionSignature(
                        "config_get",
                        List.of(ValueType.I64),
                        List.of(ValueType.I64)).build();
    }

    public HostFunction[] toHostFunctions() {

        return HostModuleInstance.builder(toHostModule())
                .bind("alloc", (Instance inst, Value[] args) -> alloc.apply(args))
                .bind("free", (Instance inst, Value[] args) -> free.apply(args))
                .bind("length", (Instance inst, Value[] args) -> length.apply(args))
                .bind("length_unsafe", (Instance inst, Value[] args) -> lengthUnsafe.apply(args))
                .bind("load_u8", (Instance inst, Value[] args) -> loadU8.apply(args))
                .bind("load_u64", (Instance inst, Value[] args) -> loadU64.apply(args))
                .bind("input_load_u8", (Instance inst, Value[] args) -> inputLoadU8.apply(args))
                .bind("input_load_u64", (Instance inst, Value[] args) -> inputLoadU64.apply(args))
                .bind("store_u8", (Instance inst, Value[] args) -> storeU8.apply(args))
                .bind("store_u64", (Instance inst, Value[] args) -> storeU64.apply(args))
                .bind("input_set", (Instance inst, Value[] args) -> inputSet.apply(args))
                .bind("input_len", (Instance inst, Value[] args) -> inputLen.apply(args))
                .bind("input_offset", (Instance inst, Value[] args) -> inputOffset.apply(args))
                .bind("output_len", (Instance inst, Value[] args) -> outputLen.apply(args))
                .bind("output_offset", (Instance inst, Value[] args) -> outputOffset.apply(args))
                .bind("output_set", (Instance inst, Value[] args) -> outputSet.apply(args))
                .bind("reset", (Instance inst, Value[] args) -> reset.apply(args))
                .bind("error_set", (Instance inst, Value[] args) -> errorSet.apply(args))
                .bind("error_set", (Instance inst, Value[] args) -> errorGet.apply(args))
                .bind("memory_bytes", (Instance inst, Value[] args) -> memoryBytes.apply(args)).build().hostFunctions();
    }
}

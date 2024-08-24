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

    public Kernel(Instance kernel) {
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

    public static Module module() {
        var kernelStream = Kernel.class.getClassLoader().getResourceAsStream("extism-runtime.wasm");
        return Parser.parse(kernelStream);
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
}

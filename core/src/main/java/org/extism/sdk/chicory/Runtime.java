package org.extism.sdk.chicory;

import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.TrapException;
import com.dylibso.chicory.wasi.WasiExitException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class Initializer {
    
    enum Type {
        WASI_COMMAND("WASI Command"),
        WASI_REACTOR("WASI Reactor"),
        WASI_CONSTRUCTORS("WASI Constructors"),
        HASKELL("Haskell");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    
    static class EntryPoint {
        final Initializer.Type type;
        final ExportFunction init;
        
        EntryPoint(Initializer.Type type, ExportFunction init) {
            this.type = type;
            this.init = init;
        }
    }
    
    final EntryPoint main;
    final List<EntryPoint> entryPoints;

    Initializer(EntryPoint main, List<EntryPoint> entryPoints) {
        this.main = main;
        this.entryPoints = entryPoints;
    }

    public void initialize() {
        for (var runtime : entryPoints) {
            safeInit(runtime);
        }
        safeInit(main);
    }


    private void safeInit(EntryPoint rt) {
        if (rt == null) {
            return;
        }
        try {
            rt.init.apply();
        } catch (WasiExitException ex) {
            // ProcExit always throws, but it's an error only if it's nonzero.
            if (ex.exitCode() != 0) {
                throw new ExtismException("WASI command exited with code: " + ex.exitCode(), ex);
            }
        } catch (TrapException e) {
            throw new ExtismException("Failed to initialize: " + e.getMessage(), e);
        }

    }

    static Initializer detect(Map<String, Instance> instances, Logger logger) {
        Instance mainInstance = instances.get(DependencyGraph.MAIN_MODULE_NAME);
        if (mainInstance == null) {
            throw new ExtismException("Main instance not found");
        }
        
        var mainRuntime = detectEntryPoint(mainInstance, logger);
        var entryPoints = new ArrayList<EntryPoint>();
        
        for (Map.Entry<String, Instance> entry : instances.entrySet()) {
            if (!DependencyGraph.MAIN_MODULE_NAME.equals(entry.getKey())) {
                var runtime = detectEntryPoint(entry.getValue(), logger);
                if (runtime != null) {
                    entryPoints.add(runtime);
                }
            }
        }

        return new Initializer(mainRuntime, entryPoints);
    }

    private static EntryPoint detectEntryPoint(Instance instance, Logger logger) {
        // Check for Haskell module
        ExportFunction hsInit = instance.export("hs_init");
        if (hsInit != null) {
            logger.debugf("Detected Haskell runtime");
            return new EntryPoint(Type.HASKELL, hsInit);
        }

        // Check for reactor module
        ExportFunction initialize = instance.export("_initialize");
        if (initialize != null) {
            logger.debugf("Detected WASI reactor module");
            return new EntryPoint(Type.WASI_REACTOR, initialize);
        }

        // Check for command module
        ExportFunction start = instance.export("_start");
        if (start != null) {
            logger.debugf("Detected WASI command module");
            return new EntryPoint(Type.WASI_COMMAND, start);
        }

        // Check for constructors
        ExportFunction ctors = instance.export("__wasm_call_ctors");
        if (ctors != null) {
            logger.debugf("Detected WASI module with constructors");
            return new EntryPoint(Type.WASI_CONSTRUCTORS, ctors);
        }

        logger.debugf("No entry point detected");
        return null;
    }
}
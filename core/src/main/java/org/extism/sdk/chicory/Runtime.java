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
    
    abstract static class EntryPoint {
        abstract void initialize();
    }
    
    static class WasiCommandEntryPoint extends EntryPoint {
        private final ExportFunction start;
        
        WasiCommandEntryPoint(ExportFunction start) {
            this.start = start;
        }
        
        @Override
        void initialize() {
            try {
                start.apply();
            } catch (WasiExitException ex) {
                if (ex.exitCode() != 0) {
                    throw new ExtismException("WASI command exited with code: " + ex.exitCode(), ex);
                }
            } catch (TrapException e) {
                throw new ExtismException("Failed to initialize WASI command: " + e.getMessage(), e);
            }
        }

    }
    
    static class WasiReactorEntryPoint extends EntryPoint {
        private final ExportFunction initialize;
        
        WasiReactorEntryPoint(ExportFunction initialize) {
            this.initialize = initialize;
        }
        
        @Override
        void initialize() {
            try {
                initialize.apply();
            } catch (TrapException e) {
                throw new ExtismException("Failed to initialize WASI reactor: " + e.getMessage(), e);
            }
        }

    }
    
    static class WasiConstructorsEntryPoint extends EntryPoint {
        private final ExportFunction ctors;
        
        WasiConstructorsEntryPoint(ExportFunction ctors) {
            this.ctors = ctors;
        }
        
        @Override
        void initialize() {
            try {
                ctors.apply();
            } catch (TrapException e) {
                throw new ExtismException("Failed to call constructors: " + e.getMessage(), e);
            }
        }

    }
    
    static class HaskellEntryPoint extends EntryPoint {
        private final ExportFunction hsInit;
        
        HaskellEntryPoint(ExportFunction hsInit) {
            this.hsInit = hsInit;
        }
        
        @Override
        void initialize() {
            try {
                hsInit.apply();
            } catch (TrapException e) {
                throw new ExtismException("Failed to initialize Haskell runtime: " + e.getMessage(), e);
            }
        }

    }
    
    final EntryPoint main;
    final List<EntryPoint> entryPoints;

    Initializer(EntryPoint main, List<EntryPoint> entryPoints) {
        this.main = main;
        this.entryPoints = entryPoints;
    }

    public void initialize() {
        for (var entryPoint : entryPoints) {
            if (entryPoint != null) {
                entryPoint.initialize();
            }
        }
        if (main != null) {
            main.initialize();
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
            return new HaskellEntryPoint(hsInit);
        }

        // Check for reactor module
        ExportFunction initialize = instance.export("_initialize");
        if (initialize != null) {
            logger.debugf("Detected WASI reactor module");
            return new WasiReactorEntryPoint(initialize);
        }

        // Check for command module
        ExportFunction start = instance.export("_start");
        if (start != null) {
            logger.debugf("Detected WASI command module");
            return new WasiCommandEntryPoint(start);
        }

        // Check for constructors
        ExportFunction ctors = instance.export("__wasm_call_ctors");
        if (ctors != null) {
            logger.debugf("Detected WASI module with constructors");
            return new WasiConstructorsEntryPoint(ctors);
        }

        logger.debugf("No entry point detected");
        return null;
    }
}
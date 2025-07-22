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
        protected final ExportFunction init;
        
        EntryPoint(ExportFunction init) {
            this.init = init;
        }
        
        abstract void initialize();
    }
    
    static class WasiCommandEntryPoint extends EntryPoint {
        WasiCommandEntryPoint(ExportFunction start) {
            super(start);
        }
        
        @Override
        void initialize() {
            try {
                init.apply();
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
        WasiReactorEntryPoint(ExportFunction initialize) {
            super(initialize);
        }
        
        @Override
        void initialize() {
            try {
                init.apply();
            } catch (TrapException e) {
                throw new ExtismException("Failed to initialize WASI reactor: " + e.getMessage(), e);
            }
        }

    }
    
    static class WasiConstructorsEntryPoint extends EntryPoint {
        WasiConstructorsEntryPoint(ExportFunction ctors) {
            super(ctors);
        }
        
        @Override
        void initialize() {
            try {
                init.apply();
            } catch (TrapException e) {
                throw new ExtismException("Failed to call constructors: " + e.getMessage(), e);
            }
        }

    }
    
    static class HaskellEntryPoint extends EntryPoint {
        HaskellEntryPoint(ExportFunction hsInit) {
            super(hsInit);
        }
        
        @Override
        void initialize() {
            try {
                init.apply();
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

    static Initializer find(Map<String, Instance> instances, Logger logger) {
        var entryPoints = new ArrayList<EntryPoint>();
        EntryPoint main = null;
        
        for (var entry : instances.entrySet()) {
            var runtime = findEntryPoint(entry.getValue(), logger);
            if (runtime != null) {
                if (DependencyGraph.MAIN_MODULE_NAME.equals(entry.getKey())) {
                    main = runtime;
                } else {
                    entryPoints.add(runtime);
                }
            }
        }

        if (main == null) {
            logger.debug("No entry point detected for main");
        }

        return new Initializer(main, entryPoints);
    }

    private static EntryPoint findEntryPoint(Instance instance, Logger logger) {
        // Check for Haskell module
        ExportFunction hsInit = instance.export("hs_init");
        if (hsInit != null) {
            logger.debug("Detected Haskell runtime");
            return new HaskellEntryPoint(hsInit);
        }

        // Check for reactor module
        ExportFunction initialize = instance.export("_initialize");
        if (initialize != null) {
            logger.debug("Detected WASI reactor module");
            return new WasiReactorEntryPoint(initialize);
        }

        // Check for command module
        ExportFunction start = instance.export("_start");
        if (start != null) {
            logger.debug("Detected WASI command module");
            return new WasiCommandEntryPoint(start);
        }

        // Check for constructors
        ExportFunction ctors = instance.export("__wasm_call_ctors");
        if (ctors != null) {
            logger.debug("Detected WASI module with constructors");
            return new WasiConstructorsEntryPoint(ctors);
        }

        logger.debug("No entry point detected");
        return null;
    }
}
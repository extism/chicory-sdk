package org.extism.chicory.sdk;

import com.dylibso.chicory.log.SystemLogger;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasi.WasiPreview1;
import com.dylibso.chicory.runtime.Module;
import com.dylibso.chicory.wasm.types.Value;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;

public class DependencyGraphTest extends TestCase {

    public void testCircularDeps() throws IOException {
        InputStream is1 = this.getClass().getResourceAsStream("/circular-import/circular-import-1.wasm");
        InputStream is2 = this.getClass().getResourceAsStream("/circular-import/circular-import-2.wasm");
        InputStream is3 = this.getClass().getResourceAsStream("/circular-import/circular-import-main.wasm");

        DependencyGraph dg = new DependencyGraph(new SystemLogger());

        dg.registerModule("env-1", parse(is1));
        dg.registerModule("env-2", parse(is2));
        dg.registerModule("main", parse(is3));

        Instance main = dg.instantiate();

        Value[] result = main.export("real_do_expr").apply();
        assertEquals(60, result[0].asInt());
    }


    public void testCircularDepsMore() throws IOException {
        InputStream addBytes = this.getClass().getResourceAsStream("/circular-import-more/circular-import-add.wasm");
        InputStream subBytes = this.getClass().getResourceAsStream("/circular-import-more/circular-import-sub.wasm");
        InputStream exprBytes = this.getClass().getResourceAsStream("/circular-import-more/circular-import-expr.wasm");
        InputStream mainBytes = this.getClass().getResourceAsStream("/circular-import-more/circular-import-main.wasm");


        Module add = parse(addBytes);
        Module sub = parse(subBytes);
        Module expr = parse(exprBytes);
        Module main = parse(mainBytes);

        {
            DependencyGraph dg = new DependencyGraph(new SystemLogger());
            dg.registerModule("add", add);
            dg.registerModule("sub", sub);
            dg.registerModule("expr", expr);
            dg.registerModule("main", main);

            Instance mainInst = dg.instantiate();

            Value[] result = mainInst.export("real_do_expr").apply();
            assertEquals(60, result[0].asInt());
        }

        // Let's try to register them in a different order:
        // it should never matter.
        {
            DependencyGraph dg = new DependencyGraph(new SystemLogger());
            dg.registerModule("expr", expr);
            dg.registerModule("main", main);
            dg.registerModule("sub", sub);
            dg.registerModule("add", add);

            Instance mainInst = dg.instantiate();

            Value[] result = mainInst.export("real_do_expr").apply();
            assertEquals(60, result[0].asInt());
        }
    }

    public void testHostFunctionDeps() throws IOException {
        InputStream requireWasi = this.getClass().getResourceAsStream("/host-functions/import-wasi.wasm");
        Module requireWasiM = parse(requireWasi);

        DependencyGraph dg = new DependencyGraph(new SystemLogger());
        dg.registerFunctions(wasiPreview1().toHostFunctions());
        dg.registerModule("main", requireWasiM);

        // The host functions should be found, thus the module should not be further searched in the DependencyGraph.
        // If the search did not stop, it would cause an error, because there is no actual module to instantiate.
        Instance mainInst = dg.instantiate();
        assertNotNull(mainInst);
    }

    public void testInstantiate() throws IOException {
        InputStream requireWasi = this.getClass().getResourceAsStream("/host-functions/import-wasi.wasm");
        Module requireWasiM = parse(requireWasi);

        DependencyGraph dg = new DependencyGraph(new SystemLogger());
        dg.registerFunctions(wasiPreview1().toHostFunctions());
        dg.registerModule("main", requireWasiM);

        Instance mainInst = dg.instantiate();
        Instance mainInst2 = dg.instantiate();
        assertSame("when invoked twice, instantiate() returns the same instance", mainInst, mainInst2);
    }

    private Module parse(InputStream is1) throws IOException {
        return Module.builder(is1.readAllBytes()).build();
    }

    private WasiPreview1 wasiPreview1() {
        return new WasiPreview1(new SystemLogger());
    }

}

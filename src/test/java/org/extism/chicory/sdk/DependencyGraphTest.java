package org.extism.chicory.sdk;

import com.dylibso.chicory.log.SystemLogger;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.Parser;
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

        dg.registerModule("env-1", Parser.parse(is1.readAllBytes()));
        dg.registerModule("env-2", Parser.parse(is2.readAllBytes()));
        dg.registerModule("main", Parser.parse(is3.readAllBytes()));

        Instance main = dg.instantiate();

        Value[] result = main.export("real_do_expr").apply();
        assertEquals(60, result[0].asInt());
    }

    public void testCircularDepsMore() throws IOException {
        InputStream add = this.getClass().getResourceAsStream("/circular-import-more/circular-import-add.wasm");
        InputStream sub = this.getClass().getResourceAsStream("/circular-import-more/circular-import-sub.wasm");
        InputStream expr = this.getClass().getResourceAsStream("/circular-import-more/circular-import-expr.wasm");
        InputStream main = this.getClass().getResourceAsStream("/circular-import-more/circular-import-main.wasm");

        DependencyGraph dg = new DependencyGraph(new SystemLogger());

        dg.registerModule("add", Parser.parse(add.readAllBytes()));
        dg.registerModule("sub", Parser.parse(sub.readAllBytes()));
        dg.registerModule("expr", Parser.parse(expr.readAllBytes()));
        dg.registerModule("main", Parser.parse(main.readAllBytes()));

        Instance mainInst = dg.instantiate();

        Value[] result = mainInst.export("real_do_expr").apply();
        assertEquals(60, result[0].asInt());
    }

}

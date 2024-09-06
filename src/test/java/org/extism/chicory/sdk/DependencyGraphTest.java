package org.extism.chicory.sdk;

import com.dylibso.chicory.log.SystemLogger;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.types.Value;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class DependencyGraphTest {

    public void testCircularDeps() throws IOException {
        InputStream is1 = this.getClass().getResourceAsStream("/circular-import-1.wasm");
        InputStream is2 = this.getClass().getResourceAsStream("/circular-import-2.wasm");
        InputStream is3 = this.getClass().getResourceAsStream("/circular-import-main.wasm");


        DependencyGraph dg = new DependencyGraph(new SystemLogger());


        dg.registerModule("wasm1", Parser.parse(is1.readAllBytes()));
        dg.registerModule("wasm2", Parser.parse(is2.readAllBytes()));
        dg.registerModule("main", Parser.parse(is3.readAllBytes()));

        Instance main = dg.instantiate();

        Value[] result = main.export("real_do_expr").apply();
        System.out.println(Arrays.toString(result));
    }
}

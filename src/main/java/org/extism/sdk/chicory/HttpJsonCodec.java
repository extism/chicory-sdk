package org.extism.sdk.chicory;

import java.util.Map;

public interface HttpJsonCodec {
    interface Request {
        String method();
        String uri();
        Map<String, String> headers();
        byte[] body();
    }

    Request decode(byte[] data);


}

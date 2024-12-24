package org.extism.sdk.chicory;

import com.dylibso.chicory.log.Logger;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.types.ValueType;
import jakarta.json.Json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HostEnv {

    private final Kernel kernel;
    private final Memory memory;
    private final Logger logger;
    private final Log log;
    private final Var var;
    private final Config config;
    private final Http http;

    public HostEnv(Kernel kernel, Map<String, String> config, String[] allowedHosts, Logger logger) {
        this.kernel = kernel;
        this.memory = new Memory();
        this.logger = logger;
        this.config = new Config(config);
        this.var = new Var();
        this.log = new Log();
        this.http = new Http(allowedHosts);
    }

    public Log log() {
        return log;
    }

    public Var var() {
        return var;
    }

    public Config config() {
        return config;
    }

    public Http http() {
        return http;
    }

    public HostFunction[] toHostFunctions() {
        return concat(
                kernel.toHostFunctions(),
                log.toHostFunctions(),
                var.toHostFunctions(),
                config.toHostFunctions(),
                http.toHostFunctions());
    }

    private HostFunction[] concat(HostFunction[]... hfs) {
        return Arrays.stream(hfs).flatMap(Arrays::stream).toArray(HostFunction[]::new);
    }

    public void setInput(byte[] input) {
        kernel.setInput(input);
    }

    public byte[] getOutput() {
        return kernel.getOutput();
    }

    public String getError() {
        return kernel.getError();
    }

    public Memory memory() {
        return this.memory;
    }


    public class Memory {

        public long length(long offset) {
            return kernel.length.apply(offset)[0];
        }

        public com.dylibso.chicory.runtime.Memory memory() {
            return kernel.instanceMemory;
        }

        public long alloc(long size) {
            return kernel.alloc.apply(size)[0];
        }

        byte[] readBytes(long offset) {
            long length = length(offset);
            return memory().readBytes((int) offset, (int) length);
        }

        String readString(long offset) {
            return new String(readBytes(offset), StandardCharsets.UTF_8);
        }

        long writeBytes(byte[] bytes) {
            long ptr = alloc(bytes.length);
            memory().write((int) ptr, bytes);
            return ptr;
        }

        long writeString(String s) {
            return writeBytes(s.getBytes(StandardCharsets.UTF_8));
        }
    }

    public class Log {
        private Log() {
        }

        public void log(LogLevel level, String message) {
            logger.log(level.toChicoryLogLevel(), message, null);
        }

        public void logf(LogLevel level, String format, Object args) {
            logger.log(level.toChicoryLogLevel(), String.format(format, args), null);
        }

        private long[] logTrace(Instance instance, long... args) {
            return log(LogLevel.TRACE, args[0]);
        }

        private long[] logDebug(Instance instance, long... args) {
            return log(LogLevel.DEBUG, args[0]);
        }

        private long[] logInfo(Instance instance, long... args) {
            return log(LogLevel.INFO, args[0]);
        }

        private long[] logWarn(Instance instance, long... args) {
            return log(LogLevel.WARN, args[0]);
        }

        private long[] logError(Instance instance, long... args) {
            return log(LogLevel.ERROR, args[0]);
        }


        private long[] log(LogLevel level, long offset) {
            String msg = memory().readString(offset);
            log(level, msg);
            return new long[0];
        }

        HostFunction[] toHostFunctions() {
            return new HostFunction[]{
                    new HostFunction(Kernel.IMPORT_MODULE_NAME, "log_trace", List.of(ValueType.I64), List.of(), this::logTrace),
                    new HostFunction(Kernel.IMPORT_MODULE_NAME, "log_debug", List.of(ValueType.I64), List.of(), this::logDebug),
                    new HostFunction(Kernel.IMPORT_MODULE_NAME, "log_info", List.of(ValueType.I64), List.of(), this::logInfo),
                    new HostFunction(Kernel.IMPORT_MODULE_NAME, "log_warn", List.of(ValueType.I64), List.of(), this::logWarn),
                    new HostFunction(Kernel.IMPORT_MODULE_NAME, "log_error", List.of(ValueType.I64), List.of(), this::logError)};
        }
    }

    public class Var {
        private final Map<String, byte[]> vars = new ConcurrentHashMap<>();

        private Var() {
        }

        public byte[] get(String key) {
            return vars.get(key);
        }

        public void set(String key, byte[] value) {
            this.vars.put(key, value);
        }

        private long[] varGet(Instance instance, long... args) {
            // FIXME: should check MaxVarBytes to see if vars are disabled.

            long ptr = args[0];
            String key = memory().readString(ptr);
            byte[] value = get(key);
            long result;
            if (value == null) {
                // Value not found
                result = 0;
            } else {
                long rPtr = memory().writeBytes(value);
                result = rPtr;
            }
            return new long[]{result};
        }

        private long[] varSet(Instance instance, long... args) {
            // FIXME: should check MaxVarBytes before committing.

            long keyPtr = args[0];
            long valuePtr = args[1];
            String key = memory().readString(keyPtr);

            // Remove if the value offset is 0
            if (valuePtr == 0) {
                vars.remove(key);
            } else {
                byte[] value = memory().readBytes(valuePtr);
                set(key, value);
            }
            return new long[0];
        }


        HostFunction[] toHostFunctions() {
            return new HostFunction[]{
                    new HostFunction(Kernel.IMPORT_MODULE_NAME, "var_get", List.of(ValueType.I64), List.of(ValueType.I64), this::varGet),
                    new HostFunction(Kernel.IMPORT_MODULE_NAME, "var_set", List.of(ValueType.I64, ValueType.I64), List.of(), this::varSet),
            };
        }
    }

    public class Config {

        private final Map<String, String> config;

        private Config(Map<String, String> config) {
            this.config = config;
        }

        public String get(String key) {
            return config.get(key);
        }

        private long[] configGet(Instance instance, long... args) {
            long ptr = args[0];
            String key = memory().readString(ptr);
            String value = get(key);
            long result;
            if (value == null) {
                // Value not found
                result = 0;
            } else {
                long rPtr = memory().writeString(value);
                result = rPtr;
            }
            return new long[]{result};
        }

        HostFunction[] toHostFunctions() {
            return new HostFunction[]{
                    new HostFunction(Kernel.IMPORT_MODULE_NAME, "config_get", List.of(ValueType.I64), List.of(ValueType.I64), this::configGet)
            };
        }

    }

    public class Http {
        private final HostPattern[] hostPatterns;
        HttpClient httpClient;
        HttpResponse<byte[]> lastResponse;

        public Http(String[] allowedHosts) {
            if (allowedHosts == null) {
                allowedHosts = new String[0];
            }
            this.hostPatterns = new HostPattern[allowedHosts.length];
            for (int i = 0; i < allowedHosts.length; i++) {
                this.hostPatterns[i] = new HostPattern(allowedHosts[i]);
            }
        }

        public HttpClient httpClient() {
            if (httpClient == null) {
                httpClient = HttpClient.newHttpClient();
            }
            return httpClient;
        }

        long[] request(Instance instance, long... args) {
            var result = new long[1];

            var requestOffset = args[0];
            var bodyOffset = args[1];

            var requestJson = memory().readBytes(requestOffset);
            kernel.free.apply(requestOffset);

            byte[] requestBody;
            if (bodyOffset == 0) {
                requestBody = new byte[0];
            } else {
                requestBody = memory().readBytes(bodyOffset);
                kernel.free.apply(bodyOffset);
            }

            var request = Json.createReader(new ByteArrayInputStream(requestJson))
                    .readObject();

            var method = request.getJsonString("method").getString();
            var uri = URI.create(request.getJsonString("url").getString());
            var headers = request.getJsonObject("headers");

            Map<String, String> headersMap = new HashMap<>();
            for (var key : headers.keySet()) {
                headersMap.put(key, headers.getString(key));
            }

            byte[] body = request(method, uri, headersMap, requestBody);
            if (body.length == 0) {
                result[0] = 0;
            } else {
                result[0] = memory().writeBytes(body);
            }

            return result;
        }

        byte[] request(String method, URI uri, Map<String, String> headers, byte[] requestBody) {
            HttpRequest.BodyPublisher bodyPublisher;
            if (requestBody.length == 0) {
                bodyPublisher = HttpRequest.BodyPublishers.noBody();
            } else {
                bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(requestBody);
            }

            var host = uri.getHost();
            if (Arrays.stream(hostPatterns).noneMatch(p -> p.matches(host))) {
                throw new ExtismException(String.format("HTTP request to '%s' is not allowed", host));
            }

            var reqBuilder = HttpRequest.newBuilder().uri(uri);
            for (var key : headers.keySet()) {
                reqBuilder.header(key, headers.get(key));
            }

            var req = reqBuilder.method(method, bodyPublisher).build();

            try {
                this.lastResponse =
                        httpClient().send(req, HttpResponse.BodyHandlers.ofByteArray());
                return lastResponse.body();
            } catch (IOException | InterruptedException e) {
                // FIXME gracefully handle the interruption
                throw new ExtismException(e);
            }
        }

        long[] statusCode(Instance instance, long... args) {
            return new long[]{statusCode()};
        }

        int statusCode() {
            return lastResponse == null ? 0 : lastResponse.statusCode();
        }

        long[] headers(Instance instance, long[] longs) {
            var result = new long[1];
            if (lastResponse == null) {
                return result;
            }

            // FIXME duplicated headers are effectively overwriting duplicate values!
            var objBuilder = Json.createObjectBuilder();
            for (var entry : lastResponse.headers().map().entrySet()) {
                for (var v : entry.getValue()) {
                    objBuilder.add(entry.getKey(), v);
                }
            }

            var bytes = objBuilder.build().toString().getBytes(StandardCharsets.UTF_8);
            result[0] = memory().writeBytes(bytes);
            return result;
        }


        public HostFunction[] toHostFunctions() {
            return new HostFunction[]{
                    new HostFunction(
                            Kernel.IMPORT_MODULE_NAME,
                            "http_request",
                            List.of(ValueType.I64, ValueType.I64),
                            List.of(ValueType.I64),
                            this::request),
                    new HostFunction(
                            Kernel.IMPORT_MODULE_NAME,
                            "http_status_code",
                            List.of(),
                            List.of(ValueType.I32),
                            this::statusCode),
                    new HostFunction(
                            Kernel.IMPORT_MODULE_NAME,
                            "http_headers",
                            List.of(),
                            List.of(ValueType.I64),
                            this::headers),

            };
        }
    }

    private static class HostPattern {
        private final String pattern;
        private final boolean exact;

        public HostPattern(String pattern) {
            if (pattern.indexOf('*', 1) != -1) {
                throw new ExtismException("Illegal pattern " + pattern);
            }
            int wildcard = pattern.indexOf('*');
            if (wildcard < 0) {
                this.exact = true;
                this.pattern = pattern;
            } else if (wildcard == 0) {
                this.exact = false;
                this.pattern = pattern.substring(1);
            } else {
                throw new ExtismException("Illegal pattern " + pattern);
            }
        }

        public boolean matches(String host) {
            if (exact) {
                return host.equals(pattern);
            } else {
                return host.endsWith(pattern);
            }
        }
    }


}

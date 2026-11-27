package at.klausbetz.provider;

import org.apache.http.NoHttpResponseException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppleRequestRetryTest {

    @Test
    void givenNoHttpResponseOnce_whenExecuting_thenSecondAttemptSucceeds() throws IOException {
        AtomicInteger calls = new AtomicInteger();
        String result = AppleRequestRetry.execute(() -> {
            if (calls.incrementAndGet() == 1) {
                throw new NoHttpResponseException("appleid.apple.com:443 failed to respond");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(2, calls.get());
    }

    @Test
    void givenPersistentConnectionFailure_whenExecuting_thenGivesUpAfterMaxAttempts() {
        AtomicInteger calls = new AtomicInteger();
        assertThrows(ConnectException.class, () -> AppleRequestRetry.execute(() -> {
            calls.incrementAndGet();
            throw new ConnectException("connection refused");
        }));
        assertEquals(AppleRequestRetry.MAX_ATTEMPTS, calls.get());
    }

    @Test
    void givenOtherIOException_whenExecuting_thenNotRetried() {
        AtomicInteger calls = new AtomicInteger();
        assertThrows(IOException.class, () -> AppleRequestRetry.execute(() -> {
            calls.incrementAndGet();
            throw new IOException("read timed out");
        }));
        assertEquals(1, calls.get());
    }
}

package at.klausbetz.provider;

import org.apache.http.NoHttpResponseException;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.ConnectException;

final class AppleRequestRetry {

    static final int MAX_ATTEMPTS = 3;
    static final long BACKOFF_MILLIS = 100;

    private static final Logger logger = Logger.getLogger(AppleRequestRetry.class);

    @FunctionalInterface
    interface HttpCall<T> {
        T execute() throws IOException;
    }

    private AppleRequestRetry() {
    }

    static <T> T execute(HttpCall<T> call) throws IOException {
        int attempt = 1;
        while (true) {
            try {
                return call.execute();
            } catch (NoHttpResponseException | ConnectException e) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw e;
                }
                logger.warnf("Request to apple failed (attempt %d/%d): %s. Retrying...", attempt, MAX_ATTEMPTS, e.getMessage());
                try {
                    Thread.sleep(BACKOFF_MILLIS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
                attempt++;
            }
        }
    }
}

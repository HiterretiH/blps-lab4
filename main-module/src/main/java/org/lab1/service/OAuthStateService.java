package org.lab1.service;

import org.lab.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OAuthStateService {
    private static final long STATE_TTL_MILLIS = TimeUnit.MINUTES.toMillis(5);
    private static final String STORE_STATE_LOG = "Stored Google auth state for user ID: ";
    private static final String NO_STATE_LOG = "No stored Google auth state found for user ID: ";
    private static final String VALIDATE_STATE_LOG = "Validated Google auth state for user ID: ";
    private static final String RESULT_LOG = ". Result: ";
    private static final String STATE_EXPIRED_LOG = "Google auth state expired. Creation time: ";
    private static final String TTL_LOG = ", TTL: ";
    private static final String MS_LOG = " ms.";
    private static final String CLEANUP_LOG = "Cleaned up ";
    private static final String EXPIRED_STATES_LOG = " expired Google auth states.";

    private final Map<Integer, StateInfo> stateStorage = new ConcurrentHashMap<>();

    @Autowired
    private Logger logger;

    public void storeGoogleAuthState(int userId, String state) {
        stateStorage.put(userId, new StateInfo(state, System.currentTimeMillis()));
        logger.info(STORE_STATE_LOG + userId);
        cleanupExpiredStates();
    }

    public boolean validateGoogleAuthState(int userId, String state) {
        StateInfo storedState = stateStorage.get(userId);
        if (storedState == null) {
            logger.error(NO_STATE_LOG + userId);
            return false;
        }

        stateStorage.remove(userId);
        boolean isValid = storedState.getState().equals(state) &&
                !isStateExpired(storedState.getCreationTime());
        logger.info(VALIDATE_STATE_LOG + userId + RESULT_LOG + isValid);
        return isValid;
    }

    private boolean isStateExpired(long creationTime) {
        boolean isExpired = System.currentTimeMillis() - creationTime > STATE_TTL_MILLIS;
        if (isExpired) {
            logger.error(STATE_EXPIRED_LOG + creationTime + TTL_LOG + STATE_TTL_MILLIS + MS_LOG);
        }
        return isExpired;
    }

    private void cleanupExpiredStates() {
        int initialSize = stateStorage.size();
        stateStorage.entrySet().removeIf(entry -> isStateExpired(entry.getValue().getCreationTime()));
        int removedCount = initialSize - stateStorage.size();

        if (removedCount > 0) {
            logger.info(CLEANUP_LOG + removedCount + EXPIRED_STATES_LOG);
        }
    }

    private static class StateInfo {
        private final String state;
        private final long creationTime;

        public StateInfo(String state, long creationTime) {
            this.state = state;
            this.creationTime = creationTime;
        }

        public String getState() {
            return state;
        }

        public long getCreationTime() {
            return creationTime;
        }
    }
}

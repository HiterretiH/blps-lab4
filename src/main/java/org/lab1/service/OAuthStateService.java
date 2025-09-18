package org.lab1.service;

import org.lab.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OAuthStateService {
    private final Map<Integer, StateInfo> stateStorage = new ConcurrentHashMap<>();
    private static final long STATE_TTL = TimeUnit.MINUTES.toMillis(5);
    @Autowired
    private Logger logger;

    public void storeGoogleAuthState(int userId, String state) {
        stateStorage.put(userId, new StateInfo(state, System.currentTimeMillis()));
        logger.info("Stored Google auth state for user ID: " + userId);
        cleanupExpiredStates();
    }

    public boolean validateGoogleAuthState(int userId, String state) {
        StateInfo storedState = stateStorage.get(userId);
        if (storedState == null) {
            logger.error("No stored Google auth state found for user ID: " + userId);
            return false;
        }

        stateStorage.remove(userId);
        boolean isValid = storedState.getState().equals(state) &&
                !isStateExpired(storedState.getCreationTime());
        logger.info("Validated Google auth state for user ID: " + userId + ". Result: " + isValid);
        return isValid;
    }

    private boolean isStateExpired(long creationTime) {
        boolean isExpired = System.currentTimeMillis() - creationTime > STATE_TTL;
        if (isExpired) {
            logger.error("Google auth state expired. Creation time: " + creationTime + ", TTL: " + STATE_TTL + " ms.");
        }
        return isExpired;
    }

    private void cleanupExpiredStates() {
        int initialSize = stateStorage.size();
        stateStorage.entrySet().removeIf(entry -> isStateExpired(entry.getValue().getCreationTime()));
        int removedCount = initialSize - stateStorage.size();
        if (removedCount > 0) {
            logger.info("Cleaned up " + removedCount + " expired Google auth states.");
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
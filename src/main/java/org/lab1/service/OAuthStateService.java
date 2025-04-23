package org.lab1.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OAuthStateService {
    private final Map<Integer, StateInfo> stateStorage = new ConcurrentHashMap<>();
    private static final long STATE_TTL = TimeUnit.MINUTES.toMillis(5);

    public void storeGoogleAuthState(int userId, String state) {
        stateStorage.put(userId, new StateInfo(state, System.currentTimeMillis()));
        cleanupExpiredStates();
    }

    public boolean validateGoogleAuthState(int userId, String state) {
        StateInfo storedState = stateStorage.get(userId);
        if (storedState == null) return false;

        stateStorage.remove(userId);
        return storedState.getState().equals(state) &&
                !isStateExpired(storedState.getCreationTime());
    }

    private boolean isStateExpired(long creationTime) {
        return System.currentTimeMillis() - creationTime > STATE_TTL;
    }

    private void cleanupExpiredStates() {
        stateStorage.entrySet().removeIf(entry ->
                isStateExpired(entry.getValue().getCreationTime())
        );
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
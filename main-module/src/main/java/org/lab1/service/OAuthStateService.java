package org.lab1.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.lab.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
  @Qualifier("correlationLogger")
  private Logger logger;

  public final void storeGoogleAuthState(final int userId, final String stateParam) {
    stateStorage.put(userId, new StateInfo(stateParam, System.currentTimeMillis()));
    logger.info(STORE_STATE_LOG + userId);
    cleanupExpiredStates();
  }

  public final boolean validateGoogleAuthState(final int userId, final String stateParam) {
    StateInfo storedState = stateStorage.get(userId);
    if (storedState == null) {
      logger.error(NO_STATE_LOG + userId);
      return false;
    }

    stateStorage.remove(userId);
    boolean isValid =
        storedState.getState().equals(stateParam) && !isStateExpired(storedState.getCreationTime());
    logger.info(VALIDATE_STATE_LOG + userId + RESULT_LOG + isValid);
    return isValid;
  }

  private boolean isStateExpired(final long creationTime) {
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

    StateInfo(final String stateParam, final long creationTimeParam) {
      this.state = stateParam;
      this.creationTime = creationTimeParam;
    }

    public String getState() {
      return state;
    }

    public long getCreationTime() {
      return creationTime;
    }
  }
}

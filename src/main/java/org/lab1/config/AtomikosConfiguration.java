package org.lab1.config;

import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

import jakarta.transaction.UserTransaction;

@Configuration
public class AtomikosConfiguration {
    private static final int TRANSACTION_TIMEOUT_SECONDS = 300;
    private static final String INIT_METHOD = "init";
    private static final String DESTROY_METHOD = "close";
    private static final String TRANSACTION_MANAGER_BEAN = "transactionManager";
    private static final String ATOMIKOS_TRANSACTION_MANAGER_BEAN = "atomikosTransactionManager";
    private static final String ATOMIKOS_USER_TRANSACTION_BEAN = "atomikosUserTransaction";
    private static final boolean FORCE_SHUTDOWN = true;

    @Bean(name = ATOMIKOS_TRANSACTION_MANAGER_BEAN, initMethod = INIT_METHOD, destroyMethod = DESTROY_METHOD)
    public UserTransactionManager atomikosTransactionManager() throws Throwable {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setTransactionTimeout(TRANSACTION_TIMEOUT_SECONDS);
        userTransactionManager.setForceShutdown(FORCE_SHUTDOWN);
        return userTransactionManager;
    }

    @Bean(name = ATOMIKOS_USER_TRANSACTION_BEAN)
    public UserTransaction atomikosUserTransaction() throws Throwable {
        return atomikosTransactionManager();
    }

    @Bean(name = TRANSACTION_MANAGER_BEAN)
    public PlatformTransactionManager transactionManager() throws Throwable {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(atomikosTransactionManager());
        jtaTransactionManager.setUserTransaction(atomikosUserTransaction());
        return jtaTransactionManager;
    }
}

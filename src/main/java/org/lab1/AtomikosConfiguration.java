package org.lab1;

import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

import jakarta.transaction.UserTransaction;

@Configuration
public class AtomikosConfiguration {

    @Bean(initMethod = "init", destroyMethod = "close")
    public UserTransactionManager atomikosTransactionManager() throws Throwable {
        UserTransactionManager utm = new UserTransactionManager();
        utm.setTransactionTimeout(300);
        utm.setForceShutdown(true);
        return utm;
    }

    @Bean
    public UserTransaction atomikosUserTransaction() throws Throwable {
        return atomikosTransactionManager();
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws Throwable {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(atomikosTransactionManager());
        jtaTransactionManager.setUserTransaction(atomikosUserTransaction());
        return jtaTransactionManager;
    }
}
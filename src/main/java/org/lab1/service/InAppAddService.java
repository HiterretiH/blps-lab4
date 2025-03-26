package org.lab1.service;

import org.lab1.json.InAppAddJson;
import org.lab1.model.InAppAdd;
import org.lab1.model.MonetizedApplication;
import org.lab1.repository.InAppAddRepository;
import org.lab1.repository.MonetizedApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InAppAddService {

    private final InAppAddRepository inAppAddRepository;
    private final MonetizedApplicationRepository monetizedApplicationRepository;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    public InAppAddService(InAppAddRepository inAppAddRepository, MonetizedApplicationRepository monetizedApplicationRepository, PlatformTransactionManager transactionManager) {
        this.inAppAddRepository = inAppAddRepository;
        this.monetizedApplicationRepository = monetizedApplicationRepository;
        this.transactionManager = transactionManager;
    }

    public InAppAdd createInAppAdd(InAppAddJson inAppAddJson) {
        MonetizedApplication monetizedApplication = monetizedApplicationRepository.findById(inAppAddJson.getMonetizedApplicationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monetized Application not found"));

        InAppAdd inAppAdd = new InAppAdd();
        inAppAdd.setMonetizedApplication(monetizedApplication);
        inAppAdd.setTitle(inAppAddJson.getTitle());
        inAppAdd.setDescription(inAppAddJson.getDescription());
        inAppAdd.setPrice(inAppAddJson.getPrice());

        return inAppAddRepository.save(inAppAdd);
    }

    public List<InAppAdd> createMultipleInAppAdds(List<InAppAddJson> inAppAddJsons) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        List<InAppAdd> inAppAdds = new ArrayList<>();
        try {
            if (inAppAddJsons == null || inAppAddJsons.isEmpty()) {
                return inAppAdds;
            }

            Integer monetizedApplicationId = inAppAddJsons.get(0).getMonetizedApplicationId();
            MonetizedApplication monetizedApplication = monetizedApplicationRepository.findById(monetizedApplicationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monetized Application not found"));

            for (InAppAddJson inAppAddJson : inAppAddJsons) {
                if (inAppAddJson.getMonetizedApplicationId() != monetizedApplicationId) {
                    transactionManager.rollback(status);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All ads must belong to the same Monetized Application for a single transaction.");
                }
                InAppAdd inAppAdd = new InAppAdd();
                inAppAdd.setMonetizedApplication(monetizedApplication);
                inAppAdd.setTitle(inAppAddJson.getTitle());
                inAppAdd.setDescription(inAppAddJson.getDescription());
                inAppAdd.setPrice(inAppAddJson.getPrice());
                inAppAdds.add(inAppAdd);
            }
            inAppAddRepository.saveAll(inAppAdds);
            transactionManager.commit(status);
        } catch (Exception ex) {
            transactionManager.rollback(status);
            throw ex;
        }
        return inAppAdds;
    }

    public List<InAppAdd> getAllInAppAds() {
        return inAppAddRepository.findAll();
    }

    public Optional<InAppAdd> getInAppAddById(int id) {
        return inAppAddRepository.findById(id);
    }

    public List<InAppAdd> getInAppAddByMonetizedApplication(int monetizedApplicationId) {
        return inAppAddRepository.findByMonetizedApplicationId(monetizedApplicationId);
    }
}
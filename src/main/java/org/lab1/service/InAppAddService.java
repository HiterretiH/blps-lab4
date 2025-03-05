package org.lab1.service;

import org.lab1.json.InAppAddJson;
import org.lab1.model.InAppAdd;
import org.lab1.model.MonetizedApplication;
import org.lab1.repository.InAppAddRepository;
import org.lab1.repository.MonetizedApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class InAppAddService {

    private final InAppAddRepository inAppAddRepository;
    private final MonetizedApplicationRepository monetizedApplicationRepository;

    @Autowired
    public InAppAddService(InAppAddRepository inAppAddRepository, MonetizedApplicationRepository monetizedApplicationRepository) {
        this.inAppAddRepository = inAppAddRepository;
        this.monetizedApplicationRepository = monetizedApplicationRepository;
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
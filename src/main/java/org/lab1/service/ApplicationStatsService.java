package org.lab1.service;

import org.lab1.json.ApplicationStatsJson;
import org.lab1.model.Application;
import org.lab1.model.ApplicationStats;
import org.lab1.repository.ApplicationRepository;
import org.lab1.repository.ApplicationStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationStatsService {

    @Autowired
    private ApplicationStatsRepository applicationStatsRepository;
    @Autowired
    private ApplicationRepository applicationRepository;

    public ApplicationStats save(ApplicationStatsJson applicationStatsJson) {
        Application application = applicationRepository.findById(applicationStatsJson.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Application not found"));

        ApplicationStats applicationStats = new ApplicationStats();
        applicationStats.setId(application.getId());
        applicationStats.setApplication(application);
        applicationStats.setDownloads(applicationStatsJson.getDownloads());
        applicationStats.setRating(applicationStatsJson.getRating());

        return applicationStatsRepository.save(applicationStats);
    }

    public Optional<ApplicationStats> findById(int id) {
        return applicationStatsRepository.findById(id);
    }

    public List<ApplicationStats> findAll() {
        return applicationStatsRepository.findAll();
    }

    public void delete(int id) {
        applicationStatsRepository.deleteById(id);
    }
}
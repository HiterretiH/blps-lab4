package org.lab1.controller;

import org.lab1.json.InAppAddJson;
import org.lab1.model.InAppAdd;
import org.lab1.model.MonetizedApplication;
import org.lab1.service.InAppAddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/in-app-ads")
public class InAppAddController {

    private final InAppAddService inAppAddService;

    @Autowired
    public InAppAddController(InAppAddService inAppAddService) {
        this.inAppAddService = inAppAddService;
    }

    @PostMapping
    public ResponseEntity<InAppAdd> createInAppAdd(@RequestBody InAppAddJson inAppAddJson) {
        InAppAdd inAppAdd = inAppAddService.createInAppAdd(inAppAddJson);
        return ResponseEntity.ok(inAppAdd);
    }

    @GetMapping
    public ResponseEntity<List<InAppAdd>> getAllInAppAds() {
        List<InAppAdd> inAppAdds = inAppAddService.getAllInAppAds();
        return ResponseEntity.ok(inAppAdds);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InAppAdd> getInAppAddById(@PathVariable int id) {
        Optional<InAppAdd> inAppAdd = inAppAddService.getInAppAddById(id);
        return inAppAdd.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/monetized/{monetizedApplicationId}")
    public ResponseEntity<List<InAppAdd>> getInAppAdsByMonetizedApplication(@PathVariable int monetizedApplicationId) {
        List<InAppAdd> inAppAdds = inAppAddService.getInAppAddByMonetizedApplication(monetizedApplicationId);
        return ResponseEntity.ok(inAppAdds);
    }
}
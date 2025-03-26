package org.lab1.controller;

import org.lab1.json.InAppAddJson;
import org.lab1.model.InAppAdd;
import org.lab1.service.InAppAddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAuthority('in_app_add.manage')")
    @PostMapping("/create")
    public ResponseEntity<InAppAdd> createInAppAdd(@RequestBody InAppAddJson inAppAddJson) {
        InAppAdd inAppAdd = inAppAddService.createInAppAdd(inAppAddJson);
        return ResponseEntity.ok(inAppAdd);
    }

    @PreAuthorize("hasAuthority('in_app_add.manage')")
    @PostMapping("/bulk")
    public ResponseEntity<List<InAppAdd>> createMultipleInAppAdds(@RequestBody List<InAppAddJson> inAppAddJsons) {
        try {
            List<InAppAdd> inAppAdds = inAppAddService.createMultipleInAppAdds(inAppAddJsons);
            return ResponseEntity.status(HttpStatus.CREATED).body(inAppAdds);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PreAuthorize("hasAuthority('in_app_add.read')")
    @GetMapping("/list")
    public ResponseEntity<List<InAppAdd>> getAllInAppAds() {
        List<InAppAdd> inAppAdds = inAppAddService.getAllInAppAds();
        return ResponseEntity.ok(inAppAdds);
    }

    @PreAuthorize("hasAuthority('in_app_add.read')")
    @GetMapping("get/{id}")
    public ResponseEntity<InAppAdd> getInAppAddById(@PathVariable int id) {
        Optional<InAppAdd> inAppAdd = inAppAddService.getInAppAddById(id);
        return inAppAdd.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('in_app_add.read')")
    @GetMapping("/monetized/{monetizedApplicationId}")
    public ResponseEntity<List<InAppAdd>> getInAppAdsByMonetizedApplication(@PathVariable int monetizedApplicationId) {
        List<InAppAdd> inAppAdds = inAppAddService.getInAppAddByMonetizedApplication(monetizedApplicationId);
        return ResponseEntity.ok(inAppAdds);
    }
}
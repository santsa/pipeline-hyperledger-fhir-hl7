package com.kafka.consumer.web;

import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.r5.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kafka.consumer.service.FabricService;

@RestController
@RequestMapping("/fabric")
public class FabricController {

    @Autowired
    private FabricService fabricService;

    // producer
    @PostMapping("/initLedger")
    public ResponseEntity<Map<String, Object>> initLedger() {
        Map<String, Object> response = new HashMap<>();
        try {
            fabricService.initLedger();
            response.put("status", "success");
            response.put("message", "Ledger initialized successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error initializing ledger: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAll() {
        Map<String, Object> response = new HashMap<>();
        try {
            String assets = fabricService.getAll();
            response.put("status", "success");
            response.put("data", assets);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error fetching assets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/createPatient")
    public ResponseEntity<Map<String, Object>> createPatient(@RequestBody String patient) {
        Map<String, Object> response = new HashMap<>();
        try {
            String asset = fabricService.createOrUpdatePatient(patient);
            response.put("status", "success");
            response.put("data", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error creating asset: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/updatePatientAsync")
    public ResponseEntity<Map<String, Object>> updatePatientAsync(@RequestBody String patient) {
        Map<String, Object> response = new HashMap<>();
        try {
            String asset = fabricService.updatePatientAsync(patient);
            response.put("status", "success");
            response.put("data", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error updatePatientAsync: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/readAssetById/{id}")
    public ResponseEntity<Map<String, Object>> readAssetById(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            String asset = fabricService.readPatientById(id);
            response.put("status", "success");
            response.put("data", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error readAssetById: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/updatePatient")
    public ResponseEntity<Map<String, Object>> updatePatient(@RequestBody String patient) {
        Map<String, Object> response = new HashMap<>();
        try {
            String asset = fabricService.createOrUpdatePatient(patient);
            response.put("status", "success");
            response.put("data", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error updatePatient: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/deletePatient/{id}")
    public ResponseEntity<Map<String, Object>> deletePatient(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            String asset = fabricService.deletePatient(id);
            response.put("status", "success");
            response.put("data", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error deletePatient: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

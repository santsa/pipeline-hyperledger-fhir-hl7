package com.kafka.consumer.web;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.hyperledger.fabric.client.CommitException;
import org.hyperledger.fabric.client.CommitStatusException;
import org.hyperledger.fabric.client.EndorseException;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.SubmitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.consumer.service.PatientService;

import ca.uhn.fhir.rest.api.MethodOutcome;

@RestController
@RequestMapping(value = "fhir/Patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    // producer
    @GetMapping("/initLedger")
    @ResponseStatus(HttpStatus.OK)
    public void initLedger() throws EndorseException, SubmitException, CommitException, CommitStatusException {
        patientService.initLedger();
    }

    @GetMapping(path="/getAll", produces = {"application/fhir+json"})
    @ResponseStatus(HttpStatus.OK)
    public Bundle search() throws EndorseException, SubmitException, CommitException, CommitStatusException, GatewayException, JsonProcessingException{
        return patientService.search();
    }

    @GetMapping(path="/{id}", produces = {"application/fhir+json"})
    @ResponseStatus(HttpStatus.OK)
    public Patient searchById(@PathVariable String id) throws EndorseException, SubmitException, CommitException, CommitStatusException, GatewayException, JsonProcessingException{
        return patientService.searchById(id);
    }

    @PostMapping(path = "/create",consumes = {MediaType.APPLICATION_JSON_VALUE, "application/fhir+json"}, produces = {"application/fhir+json"})
    @ResponseStatus(HttpStatus.CREATED)
    public MethodOutcome create(@RequestBody Patient patient) throws EndorseException, SubmitException, CommitException, CommitStatusException, GatewayException, Exception{
        return patientService.createOrUpdate(patient);
    }

    @PutMapping(path = "/updatePatientAsync", consumes = {MediaType.APPLICATION_JSON_VALUE, "application/fhir+json"}, produces = {"application/fhir+json"})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MethodOutcome updatePatientAsync(@RequestBody Patient patient) throws EndorseException, SubmitException, CommitException, CommitStatusException, GatewayException{
        return patientService.updatePatientAsync(patient);
    }

    @PutMapping(path = "/update",consumes = {MediaType.APPLICATION_JSON_VALUE, "application/fhir+json"}, produces = {"application/fhir+json"})
    @ResponseStatus(HttpStatus.CREATED)
    public MethodOutcome update(@RequestBody Patient patient) throws EndorseException, SubmitException, CommitException, CommitStatusException, GatewayException, Exception{
        return patientService.createOrUpdate(patient);
    }

    @DeleteMapping(path = "/{id}", produces = {"application/fhir+json"})
    @ResponseStatus(HttpStatus.OK)
    public MethodOutcome delete(@PathVariable String id) throws EndorseException, SubmitException, CommitException, CommitStatusException, GatewayException, JsonProcessingException{
        return patientService.deletePatient(id);
    }
}

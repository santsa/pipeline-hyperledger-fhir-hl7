/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hl7fhir;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(name = "PatientContract", info = @Info(title = "Patient contract", description = "Very basic Java Contract example", version = "0.0.1", license = @License(name = "SPDX-License-Identifier: Apache-2.0", url = ""), contact = @Contact(email = "santsa@gmail.com", name = "PatientContract", url = "http://PatientContract.me")))
@Default
public class PatientContract implements ContractInterface {

    public PatientContract() {

    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        createPatient(ctx, "asset1", "blue");
        createPatient(ctx, "asset2", "red");
        createPatient(ctx, "asset3", "green");

    }

    private final Genson genson = new Genson();

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean patientExists(Context ctx, String patientId) {
        byte[] buffer = ctx.getStub().getState(patientId);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void createPatient(Context ctx, String patientId, String value) {
        boolean exists = patientExists(ctx, patientId);
        if (exists) {
            throw new RuntimeException("The patient " + patientId + " already exists");
        }
        Patient patient = new Patient();
        patient.setValue(value);
        ctx.getStub().putState(patientId, patient.toJSONString().getBytes(UTF_8));
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Patient readPatient(Context ctx, String patientId) {
        boolean exists = patientExists(ctx, patientId);
        if (!exists) {
            throw new RuntimeException("The patient " + patientId + " does not exist");
        }

        Patient newAsset = Patient.fromJSONString(new String(ctx.getStub().getState(patientId), UTF_8));
        return newAsset;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void updatePatient(Context ctx, String patientId, String newValue) {
        boolean exists = patientExists(ctx, patientId);
        if (!exists) {
            throw new RuntimeException("The patient " + patientId + " does not exist");
        }
        Patient patient = new Patient();
        patient.setValue(newValue);

        ctx.getStub().putState(patientId, patient.toJSONString().getBytes(UTF_8));
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void deletePatient(Context ctx, String patientId) {
        boolean exists = patientExists(ctx, patientId);
        if (!exists) {
            throw new RuntimeException("The patient " + patientId + " does not exist");
        }
        ctx.getStub().delState(patientId);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllPatients(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Patient> queryResults = new ArrayList<>();
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result : results) {
            Patient patient = genson.deserialize(result.getStringValue(), Patient.class);
            System.out.println(patient);
            queryResults.add(patient);
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

}
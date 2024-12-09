/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hl7fhir;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

@DataType()
public class Patient {

    @Property()
    private String value;

    public Patient(){
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    public static Patient fromJSONString(String json) {
        String value = new JSONObject(json).getString("value");
        Patient asset = new Patient();
        asset.setValue(value);
        return asset;
    }
}

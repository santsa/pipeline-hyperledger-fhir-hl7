package org.hyperledger.fabric.hl7fhir;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Asset {

    @Property()
    private final String assetID;

    @Property()
    private final String jsonValue;

    public String getAssetID() {
        return assetID;
    }

    public String getJsonValue() {
        return jsonValue;
    }

    public Asset(@JsonProperty("assetID") final String assetID, @JsonProperty("jsonValue") final String jsonValue) {
        this.assetID = assetID;
        this.jsonValue = jsonValue;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Asset other = (Asset) obj;
        if (assetID == null) {
            if (other.assetID != null) {
                return false;
            }
        } else {
            if (!assetID.equals(other.assetID)) {
                return false;
            }
        }
        if (jsonValue == null) {
            if (other.jsonValue != null) {
                return false;
            }
        } else {
            if (!jsonValue.equals(other.jsonValue)) {
                return false;
            }
        }
        return true;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((assetID == null) ? 0 : assetID.hashCode());
        result = prime * result + ((jsonValue == null) ? 0 : jsonValue.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Asset [assetID=" + assetID + ", jsonValue=" + jsonValue + "]";
    }

}

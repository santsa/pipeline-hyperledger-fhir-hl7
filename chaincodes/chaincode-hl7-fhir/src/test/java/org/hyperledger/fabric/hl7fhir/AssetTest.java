package org.hyperledger.fabric.hl7fhir;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class AssetTest {

    @Nested
    class Equality {

        @Test
        public void isReflexive() {
            Asset asset = new Asset("asset1", "Blue");
            assertThat(asset).isEqualTo(asset);
        }

        @Test
        public void isSymmetric() {
            Asset assetA = new Asset("asset1", "Blue");
            Asset assetB = new Asset("asset1", "Blue");
            assertThat(assetA).isEqualTo(assetB);
            assertThat(assetB).isEqualTo(assetA);
        }

        @Test
        public void isTransitive() {
            Asset assetA = new Asset("asset1", "Blue");
            Asset assetB = new Asset("asset1", "Blue");
            Asset assetC = new Asset("asset1", "Blue");
            assertThat(assetA).isEqualTo(assetB);
            assertThat(assetB).isEqualTo(assetC);
            assertThat(assetA).isEqualTo(assetC);
        }

        @Test
        public void handlesInequality() {
            Asset assetA = new Asset("asset1", "Blue");
            Asset assetB = new Asset("asset2", "Red");
            assertThat(assetA).isNotEqualTo(assetB);
        }

        @Test
        public void handlesOtherObjects() {
            Asset assetA = new Asset("asset1", "Blue");
            String assetB = "not a asset";
            assertThat(assetA).isNotEqualTo(assetB);
        }

        @Test
        public void handlesNull() {
            Asset asset = new Asset("asset1", "Blue");
            assertThat(asset).isNotEqualTo(null);
        }
    }
}

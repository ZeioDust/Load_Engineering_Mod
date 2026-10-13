package com.loadbearing.advancement;

public final class LBCriteria {
    public static StructuralTrigger STRUCTURAL = new StructuralTrigger();

    private LBCriteria() {}

    public enum Kind {
        IT_HELD("it_held"),

        IT_DID_NOT_HOLD("it_did_not_hold"),

        SPANNED("spanned"),

        SUSPENDED("suspended"),

        SOLID_GROUND("solid_ground"),

        CONTROLLED_DEMOLITION("controlled_demolition"),

        OVERENGINEERED("overengineered");

        private final String id;

        Kind(String id) {
            this.id = id;
        }

        public String id() {
            return this.id;
        }
    }
}

package com.siems.entity.enums;

import java.util.EnumSet;
import java.util.Set;

public enum ShipmentStatusEnum {
    PENDING,
    PACKED,
    DISPATCHED,
    IN_TRANSIT,
    AT_CUSTOMS,
    DELIVERED,
    CANCELLED;

    public Set<ShipmentStatusEnum> allowedNextStatuses() {
        return switch (this) {
            case PENDING     -> EnumSet.of(PACKED, CANCELLED);
            case PACKED      -> EnumSet.of(DISPATCHED, CANCELLED);
            case DISPATCHED  -> EnumSet.of(IN_TRANSIT);
            case IN_TRANSIT  -> EnumSet.of(AT_CUSTOMS, DELIVERED);
            case AT_CUSTOMS  -> EnumSet.of(IN_TRANSIT, DELIVERED, CANCELLED);
            case DELIVERED   -> EnumSet.noneOf(ShipmentStatusEnum.class);
            case CANCELLED   -> EnumSet.noneOf(ShipmentStatusEnum.class);
        };
    }

    public boolean canTransitionTo(ShipmentStatusEnum target) {
        return allowedNextStatuses().contains(target);
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }

    public boolean restoresInventoryOnCancel() {
        return this == PENDING || this == PACKED || this == AT_CUSTOMS;
    }
}

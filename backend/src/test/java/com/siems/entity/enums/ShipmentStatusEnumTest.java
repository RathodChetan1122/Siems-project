package com.siems.entity.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ShipmentStatusEnum State Machine Tests")
class ShipmentStatusEnumTest {

    @ParameterizedTest(name = "{0} -> {1} should be {2}")
    @CsvSource({
            "PENDING, PACKED, true",
            "PENDING, CANCELLED, true",
            "PENDING, DELIVERED, false",
            "PENDING, IN_TRANSIT, false",
            "PACKED, DISPATCHED, true",
            "PACKED, CANCELLED, true",
            "PACKED, PENDING, false",
            "DISPATCHED, IN_TRANSIT, true",
            "DISPATCHED, DELIVERED, false",
            "DISPATCHED, CANCELLED, false",
            "IN_TRANSIT, AT_CUSTOMS, true",
            "IN_TRANSIT, DELIVERED, true",
            "IN_TRANSIT, CANCELLED, false",
            "AT_CUSTOMS, IN_TRANSIT, true",
            "AT_CUSTOMS, DELIVERED, true",
            "AT_CUSTOMS, CANCELLED, true",
            "DELIVERED, CANCELLED, false",
            "CANCELLED, PENDING, false"
    })
    @DisplayName("canTransitionTo() should correctly validate state machine transitions")
    void shouldValidateTransitions(ShipmentStatusEnum from, ShipmentStatusEnum to, boolean expected) {
        assertThat(from.canTransitionTo(to)).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(value = ShipmentStatusEnum.class, names = {"DELIVERED", "CANCELLED"})
    @DisplayName("isTerminal() should be true for DELIVERED and CANCELLED")
    void terminalStatesShouldReturnTrue(ShipmentStatusEnum status) {
        assertThat(status.isTerminal()).isTrue();
        assertThat(status.allowedNextStatuses()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ShipmentStatusEnum.class, names = {"PENDING", "PACKED", "DISPATCHED", "IN_TRANSIT", "AT_CUSTOMS"})
    @DisplayName("isTerminal() should be false for non-terminal states")
    void nonTerminalStatesShouldReturnFalse(ShipmentStatusEnum status) {
        assertThat(status.isTerminal()).isFalse();
        assertThat(status.allowedNextStatuses()).isNotEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "PENDING, true",
            "PACKED, true",
            "AT_CUSTOMS, true",
            "DISPATCHED, false",
            "IN_TRANSIT, false",
            "DELIVERED, false",
            "CANCELLED, false"
    })
    @DisplayName("restoresInventoryOnCancel() should only be true for PENDING, PACKED, AT_CUSTOMS")
    void shouldCorrectlyIdentifyInventoryRestoringStates(ShipmentStatusEnum status, boolean expected) {
        assertThat(status.restoresInventoryOnCancel()).isEqualTo(expected);
    }
}

package com.siems.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Inventory Entity Domain Logic Tests")
class InventoryTest {

    @Test
    @DisplayName("isLowStock() should return true when quantity equals threshold")
    void isLowStockShouldReturnTrueWhenEqualToThreshold() {
        Inventory inventory = Inventory.builder().quantity(50).reorderThreshold(50).build();
        assertThat(inventory.isLowStock()).isTrue();
    }

    @Test
    @DisplayName("isLowStock() should return false when quantity above threshold")
    void isLowStockShouldReturnFalseWhenAboveThreshold() {
        Inventory inventory = Inventory.builder().quantity(100).reorderThreshold(50).build();
        assertThat(inventory.isLowStock()).isFalse();
    }

    @Test
    @DisplayName("deduct() should reduce quantity correctly")
    void deductShouldReduceQuantity() {
        Inventory inventory = Inventory.builder().quantity(100).reorderThreshold(50)
                .product(Product.builder().sku("TEST-SKU").build()).build();

        inventory.deduct(30);

        assertThat(inventory.getQuantity()).isEqualTo(70);
    }

    @Test
    @DisplayName("deduct() should throw IllegalStateException when quantity exceeds available stock")
    void deductShouldThrowWhenInsufficient() {
        Inventory inventory = Inventory.builder().quantity(50).reorderThreshold(10)
                .product(Product.builder().sku("TEST-SKU").build()).build();

        assertThatThrownBy(() -> inventory.deduct(60))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient stock")
                .hasMessageContaining("TEST-SKU");

        assertThat(inventory.getQuantity()).isEqualTo(50);
    }

    @Test
    @DisplayName("add() should increase quantity correctly")
    void addShouldIncreaseQuantity() {
        Inventory inventory = Inventory.builder().quantity(50).reorderThreshold(10).build();
        inventory.add(25);
        assertThat(inventory.getQuantity()).isEqualTo(75);
    }
}

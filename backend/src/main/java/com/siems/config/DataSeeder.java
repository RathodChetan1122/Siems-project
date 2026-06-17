package com.siems.config;

import com.siems.entity.*;
import com.siems.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Seeds the four demo user accounts (and wires up dependent records) on application startup.
 *
 * <p>This runs AFTER Flyway migrations (Spring Boot applies Flyway during context
 * initialization, before CommandLineRunner beans execute). User passwords are hashed
 * via the actual injected {@link PasswordEncoder} bean, guaranteeing valid BCrypt
 * hashes regardless of BCrypt version/salt — unlike a hardcoded hash in a SQL migration.</p>
 *
 * <p>All seeded users share the password: <b>Password123</b></p>
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "Password123";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WarehouseRepository warehouseRepository;
    private final ShipmentRepository shipmentRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Users already seeded — skipping DataSeeder");
            return;
        }

        log.info("Seeding demo users with password '{}'", DEFAULT_PASSWORD);

        Map<String, String> seedUsers = Map.of(
                "admin", "ADMIN",
                "import_manager", "IMPORT_MANAGER",
                "export_manager", "EXPORT_MANAGER",
                "inventory_manager", "INVENTORY_MANAGER"
        );

        seedUsers.forEach((username, roleName) -> {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new IllegalStateException(
                            "Role " + roleName + " not found — ensure V2__seed_data.sql ran successfully"));

            User user = User.builder()
                    .username(username)
                    .email(username + "@siems.com")
                    .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .role(role)
                    .enabled(true)
                    .build();

            userRepository.save(user);
            log.info("Seeded user: {} / role: {}", username, roleName);
        });

        wireUpDependentRecords();
    }

    /**
     * Now that users exist, wire up records that reference them
     * (warehouse managers, shipment creators, demo notifications).
     */
    private void wireUpDependentRecords() {
        User inventoryManager = userRepository.findByUsername("inventory_manager").orElse(null);
        User importManager = userRepository.findByUsername("import_manager").orElse(null);
        User admin = userRepository.findByUsername("admin").orElse(null);

        if (inventoryManager != null) {
            List<Warehouse> warehouses = warehouseRepository.findAll();
            warehouses.forEach(w -> w.setManager(inventoryManager));
            warehouseRepository.saveAll(warehouses);
            log.info("Assigned inventory_manager as manager for {} warehouse(s)", warehouses.size());
        }

        if (importManager != null) {
            shipmentRepository.findByTrackingNumber("SIEMS-SHP-DEMO0001").ifPresent(shipment -> {
                shipment.setCreatedBy(importManager);
                shipmentRepository.save(shipment);
                log.info("Assigned import_manager as creator of demo shipment {}", shipment.getTrackingNumber());
            });
        }

        if (inventoryManager != null && admin != null && importManager != null) {
            notificationRepository.save(Notification.builder()
                    .user(inventoryManager)
                    .title("Low Stock Alert")
                    .message("Product 'Oil Filter' (SKU: AUTO-FLT-002) at warehouse 'European Distribution Center' "
                            + "has dropped to 45 units (threshold: 50).")
                    .type("WARNING")
                    .read(false)
                    .build());

            notificationRepository.save(Notification.builder()
                    .user(admin)
                    .title("Low Stock Alert")
                    .message("Product 'Oil Filter' (SKU: AUTO-FLT-002) at warehouse 'European Distribution Center' "
                            + "has dropped to 45 units (threshold: 50).")
                    .type("WARNING")
                    .read(false)
                    .build());

            notificationRepository.save(Notification.builder()
                    .user(importManager)
                    .title("Shipment Created")
                    .message("Shipment SIEMS-SHP-DEMO0001 has been created and inventory reserved.")
                    .type("SUCCESS")
                    .read(false)
                    .build());

            log.info("Seeded 3 demo notifications");
        }

        log.info("=========================================================");
        log.info(" SIEMS demo accounts ready — all use password: {}", DEFAULT_PASSWORD);
        log.info("   admin / {}", DEFAULT_PASSWORD);
        log.info("   import_manager / {}", DEFAULT_PASSWORD);
        log.info("   export_manager / {}", DEFAULT_PASSWORD);
        log.info("   inventory_manager / {}", DEFAULT_PASSWORD);
        log.info("=========================================================");
    }
}

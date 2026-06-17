package com.siems.service.impl;

import com.siems.dto.common.PageResponse;
import com.siems.dto.supplier.SupplierRequest;
import com.siems.dto.supplier.SupplierResponse;
import com.siems.entity.Supplier;
import com.siems.exception.DuplicateResourceException;
import com.siems.exception.ResourceNotFoundException;
import com.siems.mapper.SupplierMapper;
import com.siems.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierServiceImpl Unit Tests")
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier supplierEntity;
    private SupplierRequest supplierRequest;
    private SupplierResponse supplierResponse;

    @BeforeEach
    void setUp() {
        supplierEntity = Supplier.builder()
                .supplierId(1L)
                .name("Global Textiles Ltd.")
                .country("India")
                .contactEmail("contact@globaltextiles.com")
                .phone("+91-9876543210")
                .rating(BigDecimal.valueOf(4.5))
                .address("Surat, India")
                .build();

        supplierRequest = new SupplierRequest();
        supplierRequest.setName("Global Textiles Ltd.");
        supplierRequest.setCountry("India");
        supplierRequest.setContactEmail("contact@globaltextiles.com");
        supplierRequest.setPhone("+91-9876543210");
        supplierRequest.setRating(BigDecimal.valueOf(4.5));
        supplierRequest.setAddress("Surat, India");

        supplierResponse = SupplierResponse.builder()
                .supplierId(1L)
                .name("Global Textiles Ltd.")
                .country("India")
                .contactEmail("contact@globaltextiles.com")
                .phone("+91-9876543210")
                .rating(BigDecimal.valueOf(4.5))
                .address("Surat, India")
                .build();
    }

    @Nested
    @DisplayName("create()")
    class CreateSupplier {

        @Test
        @DisplayName("Should create supplier successfully when email is unique")
        void shouldCreateSupplierSuccessfully() {
            when(supplierRepository.existsByContactEmail(supplierRequest.getContactEmail())).thenReturn(false);
            when(supplierMapper.toEntity(supplierRequest)).thenReturn(supplierEntity);
            when(supplierRepository.save(supplierEntity)).thenReturn(supplierEntity);
            when(supplierMapper.toResponse(supplierEntity)).thenReturn(supplierResponse);

            SupplierResponse result = supplierService.create(supplierRequest);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Global Textiles Ltd.");
            verify(supplierRepository).save(supplierEntity);
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void shouldThrowExceptionWhenEmailDuplicate() {
            when(supplierRepository.existsByContactEmail(supplierRequest.getContactEmail())).thenReturn(true);

            assertThatThrownBy(() -> supplierService.create(supplierRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("contact@globaltextiles.com");

            verify(supplierRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetSupplierById {

        @Test
        @DisplayName("Should return supplier when found")
        void shouldReturnSupplierWhenFound() {
            when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplierEntity));
            when(supplierMapper.toResponse(supplierEntity)).thenReturn(supplierResponse);

            SupplierResponse result = supplierService.getById(1L);

            assertThat(result.getSupplierId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when supplier does not exist")
        void shouldThrowWhenSupplierNotFound() {
            when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> supplierService.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateSupplier {

        @Test
        @DisplayName("Should update supplier when email unchanged")
        void shouldUpdateSupplierWithSameEmail() {
            when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplierEntity));
            when(supplierRepository.save(supplierEntity)).thenReturn(supplierEntity);
            when(supplierMapper.toResponse(supplierEntity)).thenReturn(supplierResponse);

            SupplierResponse result = supplierService.update(1L, supplierRequest);

            assertThat(result).isNotNull();
            verify(supplierMapper).updateEntityFromRequest(supplierRequest, supplierEntity);
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when changing to an email used by another supplier")
        void shouldThrowWhenNewEmailBelongsToAnotherSupplier() {
            supplierRequest.setContactEmail("different@supplier.com");

            when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplierEntity));
            when(supplierRepository.existsByContactEmail("different@supplier.com")).thenReturn(true);

            assertThatThrownBy(() -> supplierService.update(1L, supplierRequest))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(supplierRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteSupplier {

        @Test
        @DisplayName("Should delete supplier when found")
        void shouldDeleteSupplier() {
            when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplierEntity));

            supplierService.delete(1L);

            verify(supplierRepository).delete(supplierEntity);
        }
    }

    @Nested
    @DisplayName("search()")
    class SearchSuppliers {

        @Test
        @DisplayName("Should return paginated supplier results")
        void shouldReturnPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Supplier> entityPage = new PageImpl<>(List.of(supplierEntity), pageable, 1);

            when(supplierRepository.search("Global", "India", pageable)).thenReturn(entityPage);
            when(supplierMapper.toResponse(supplierEntity)).thenReturn(supplierResponse);

            PageResponse<SupplierResponse> result = supplierService.search("Global", "India", pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
        }
    }
}

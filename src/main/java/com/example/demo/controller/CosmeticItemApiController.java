package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.CosmeticItemsService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/cosmetic")
@RequiredArgsConstructor
public class CosmeticItemApiController {

    private final CosmeticItemRepository cosmeticItemRepository;
    private final CosmeticItemsService cosmeticItemsService;
    private final BrandsRepository brandsRepository;
    private final CosmeticTypesRepository cosmeticTypesRepository;
    private final ManufactureRepository manufactureRepository;
    private final ProductStatusRepository productStatusRepository;
    private final QualityCertificRepository qualityCertificRepository;

    @Operation(summary = "Получить все косметические товары")
    @GetMapping
    public List<CosmeticItemDto> getCosmeticItems() {
        return cosmeticItemRepository.findAll()
                .stream().map(CosmeticItemDto::fromEntity)
                .toList();
    }

    @Operation(summary = "Получить товар по ID")
    @GetMapping("/{id}")
    public CosmeticItemDto getCosmeticItemById(@PathVariable Integer id) {
        return cosmeticItemRepository.findById(id)
                .map(CosmeticItemDto::fromEntity)
                .orElseThrow(() -> new RuntimeException("Товар с id=" + id + " не найден"));
    }

    @Operation(summary = "Создать новый товар")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Cosmetic_items createCosmetic(@Valid @RequestBody CosmeticItemDto dto) {
        if (dto.getBrandId() != null)
            brandsRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new IllegalArgumentException("Бренд не найден: id=" + dto.getBrandId()));
        if (dto.getCosmeticTypeId() != null)
            cosmeticTypesRepository.findById(dto.getCosmeticTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("Тип косметики не найден: id=" + dto.getCosmeticTypeId()));
        if (dto.getManufacturerId() != null)
            manufactureRepository.findById(dto.getManufacturerId())
                    .orElseThrow(() -> new IllegalArgumentException("Производитель не найден: id=" + dto.getManufacturerId()));

        Cosmetic_items entity = dto.toEntity();
        return cosmeticItemRepository.save(entity);
    }

    @Operation(summary = "Обновить существующий товар")
    @PutMapping("/{id}")
    public Cosmetic_items updateCosmetic(@PathVariable Integer id, @Valid @RequestBody CosmeticItemDto dto) {
        Cosmetic_items existing = cosmeticItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар с id=" + id + " не найден"));

        existing.setItemName(dto.getItemName());
        existing.setDescription(dto.getDescription());
        existing.setQuantity(dto.getQuantity());
        existing.setPrice(dto.getPrice());
        existing.setBrandId(dto.getBrandId());
        existing.setManufacturerId(dto.getManufacturerId());
        existing.setCosmeticTypeId(dto.getCosmeticTypeId());
        existing.setProductStatusId(dto.getProductStatusId());
        existing.setCertificateId(dto.getCertificateId());
        existing.setImagePath(dto.getImagePath());

        return cosmeticItemRepository.save(existing);
    }

    @Operation(summary = "Удалить товар")
    @DeleteMapping("/{id}")
    public void deleteCosmetic(@PathVariable Integer id) {
        if (!cosmeticItemRepository.existsById(id)) {
            throw new RuntimeException("Товар с id=" + id + " не найден");
        }
        cosmeticItemRepository.deleteById(id);
    }
}
package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.CosmeticItemsService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
    private final CosmeticViewRepository cosmeticViewRepository;

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
    @Operation(summary = "Экспорт шаблона товаров в CSV")
    @GetMapping(value = "/export/items-template.csv", produces = "text/csv; charset=UTF-8")
    public void exportItemsTemplate(HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"items-template.csv\"");

        try (var writer = new java.io.PrintWriter(resp.getWriter())) {

            writer.println("name;price;qty;category;brand;manufacturer;certificate;status");

            var items = cosmeticViewRepository.findAll();

            for (CosmeticView it : items) {
                String name = safe(it.getItemName());
                String price = it.getPrice() != null ? it.getPrice().toPlainString() : "0";
                String qty = it.getQuantity() != null ? it.getQuantity().toString() : "0";

                String category = safe(it.getCosmeticTypeName());
                String brand = safe(it.getBrandName());
                String manufacturer = safe(it.getManufacturerName());
                String certificate = safe(it.getCertificateName());
                String status = safe(it.getStatusName());

                writer.printf(
                        "%s;%s;%s;%s;%s;%s;%s;%s%n",
                        name, price, qty, category, brand, manufacturer, certificate, status
                );
            }
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.replace(";", ",");
    }

    @Operation(summary = "Импорт товаров из CSV-файла")
    @PostMapping(
            value = "/import/items",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> importItems(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл не выбран");
        }

        int imported = 0, skipped = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean first = true;

            while ((line = reader.readLine()) != null) {

                if (first) { first = false; continue; }
                if (line.isBlank()) continue;

                String[] p = line.split(";");
                if (p.length < 8) { skipped++; continue; }

                String name = p[0].trim();
                BigDecimal price;
                Integer qty;

                try {
                    price = new BigDecimal(p[1].trim().replace(",", "."));
                    qty = Integer.parseInt(p[2].trim());
                } catch (Exception ex) {
                    skipped++;
                    continue;
                }

                String cat = p[3].trim();
                String brand = p[4].trim();
                String manu = p[5].trim();
                String cert = p[6].trim();
                String status = p[7].trim();

                var typeOpt = cosmeticTypesRepository.findByCosmeticTypeName(cat);
                var brandOpt = brandsRepository.findByBrandName(brand);
                var manuOpt = manufactureRepository.findByManufacturerName(manu);
                var certOpt = qualityCertificRepository.findByCertificateName(cert);
                var statOpt = productStatusRepository.findByStatusName(status);

                if (typeOpt.isEmpty() || brandOpt.isEmpty() ||
                        manuOpt.isEmpty() || certOpt.isEmpty() || statOpt.isEmpty()) {
                    skipped++;
                    continue;
                }

                Cosmetic_items item = cosmeticItemRepository
                        .findByItemName(name)
                        .orElse(new Cosmetic_items());

                item.setItemName(name);
                item.setPrice(price);
                item.setQuantity(qty);

                item.setCosmeticTypeId(typeOpt.get().getCosmeticTypeId());
                item.setBrandId(brandOpt.get().getBrandId());
                item.setManufacturerId(manuOpt.get().getManufacturerId());
                item.setCertificateId(certOpt.get().getCertificateId());
                item.setProductStatusId(statOpt.get().getProductStatusId());

                cosmeticItemRepository.save(item);
                imported++;
            }

            String msg = "Импорт завершён. Успешно: " + imported + ", пропущено: " + skipped;
            return ResponseEntity.ok(msg);

        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("Ошибка импорта: " + ex.getMessage());
        }
    }
}
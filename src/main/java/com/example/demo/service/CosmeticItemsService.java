package com.example.demo.service;

import com.example.demo.Config.SecurityAuditor;
import com.example.demo.model.CosmeticItemForm;
import com.example.demo.model.Cosmetic_items;
import com.example.demo.model.Product_statuses;
import com.example.demo.model.User;
import com.example.demo.repository.CosmeticItemRepository;
import com.example.demo.repository.InventoryStats;
import com.example.demo.repository.ProductStatusRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CosmeticItemsService {

    private final CosmeticItemRepository cosmeticItemRepository;
    public InventoryStats getInventoryStats() {
        return cosmeticItemRepository.getInventoryStats();
    }
    private final ProductStatusRepository productStatusRepository;
    private final AuditLogService auditLog;
    private final SecurityAuditor securityAuditor;
    private final ObjectMapper om;

    private static final Path UPLOAD_DIR = Paths.get(
            System.getProperty("user.home"), "kurshax", "uploads", "images"
    );

    private String storeImageIfAny(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) return null;

        if (file.getSize() > 10 * 1024 * 1024)
            throw new IllegalArgumentException("Слишком большой файл (макс. 10 МБ)");
        if (file.getContentType() == null || !file.getContentType().startsWith("image/"))
            throw new IllegalArgumentException("Можно загружать только изображения");

        String ext = Optional.ofNullable(file.getOriginalFilename())
                .filter(n -> n.contains("."))
                .map(n -> n.substring(n.lastIndexOf('.')))
                .orElse(".png");

        String filename = java.util.UUID.randomUUID() + ext;
        Files.createDirectories(UPLOAD_DIR);
        file.transferTo(UPLOAD_DIR.resolve(filename).toFile());
        return "/images/" + filename;
    }

    private Integer getDefaultStatusId() {
        Product_statuses ps = productStatusRepository.findByStatusName("На рассмотренние")
                .orElseThrow(() -> new IllegalStateException("Статус 'На рассмотренние' не найден"));
        return ps.getProductStatusId();
    }
    private String statusNameOf(Integer statusId) {
        if (statusId == null) return null;
        return productStatusRepository.findById(statusId)
                .map(Product_statuses::getStatusName)
                .orElse(null);
    }

    @Transactional
    public Cosmetic_items create(CosmeticItemForm f) throws Exception {
        var e = new Cosmetic_items();
        e.setItemName(f.getItemName());
        e.setCosmeticTypeId(f.getCosmeticTypeId());
        e.setBrandId(f.getBrandId());
        e.setManufacturerId(f.getManufacturerId());
        e.setCertificateId(f.getCertificateId());
        e.setDescription(f.getDescription());
        e.setPrice(f.getPrice());
        e.setQuantity(f.getQuantity());

        String stored = storeImageIfAny(f.getImageFile());
        e.setImagePath(stored != null ? stored : f.getImagePath());

        e.setProductStatusId(
                f.getProductStatusId() != null ? f.getProductStatusId() : getDefaultStatusId()
        );

        cosmeticItemRepository.save(e);

        var actor = securityAuditor.getCurrentUserOrNull();
        var after = om.createObjectNode()
                .put("entity", "cosmetic_item")
                .put("id", e.getCosmeticItemId())
                .put("name", e.getItemName())
                .put("price", e.getPrice() == null ? null : e.getPrice().toPlainString())
                .put("qty", e.getQuantity())
                .put("status", statusNameOf(e.getProductStatusId()))
                .put("imagePath", e.getImagePath())
                .put("brandId", e.getBrandId())
                .put("typeId", e.getCosmeticTypeId())
                .put("manufacturerId", e.getManufacturerId())
                .put("certificateId", e.getCertificateId());

        auditLog.log("Создал товар", actor, null, after);

        return e;
    }

    @Transactional
    public Cosmetic_items update(Integer id, CosmeticItemForm f) throws Exception {
        var e = cosmeticItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        var before = om.createObjectNode()
                .put("entity", "cosmetic_item")
                .put("id", e.getCosmeticItemId())
                .put("name", e.getItemName())
                .put("price", e.getPrice() == null ? null : e.getPrice().toPlainString())
                .put("qty", e.getQuantity())
                .put("status", statusNameOf(e.getProductStatusId()))
                .put("imagePath", e.getImagePath())
                .put("brandId", e.getBrandId())
                .put("typeId", e.getCosmeticTypeId())
                .put("manufacturerId", e.getManufacturerId())
                .put("certificateId", e.getCertificateId());

        // --- APPLY CHANGES ---
        e.setItemName(f.getItemName());
        e.setCosmeticTypeId(f.getCosmeticTypeId());
        e.setBrandId(f.getBrandId());
        e.setManufacturerId(f.getManufacturerId());
        e.setCertificateId(f.getCertificateId());
        e.setDescription(f.getDescription());
        e.setPrice(f.getPrice());
        e.setQuantity(f.getQuantity());

        String stored = storeImageIfAny(f.getImageFile());
        if (stored != null) {
            e.setImagePath(stored);
        } else if (f.getImagePath() != null && !f.getImagePath().isBlank()) {
            e.setImagePath(f.getImagePath());
        }

        e.setProductStatusId(
                f.getProductStatusId() != null ? f.getProductStatusId() : getDefaultStatusId()
        );

        cosmeticItemRepository.save(e);

        var after = om.createObjectNode()
                .put("entity", "cosmetic_item")
                .put("id", e.getCosmeticItemId())
                .put("name", e.getItemName())
                .put("price", e.getPrice() == null ? null : e.getPrice().toPlainString())
                .put("qty", e.getQuantity())
                .put("status", statusNameOf(e.getProductStatusId()))
                .put("imagePath", e.getImagePath())
                .put("brandId", e.getBrandId())
                .put("typeId", e.getCosmeticTypeId())
                .put("manufacturerId", e.getManufacturerId())
                .put("certificateId", e.getCertificateId());

        var actor = securityAuditor.getCurrentUserOrNull();

        var details = om.createObjectNode();
        details.set("before", before);
        details.set("after",  after);

        auditLog.log("Изменил товар", actor, null, details);

        return e;
    }


    @Transactional
    public void delete(Integer id) {
        var e = cosmeticItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        var details = om.createObjectNode()
                .put("entity", "cosmetic_item")
                .put("id", e.getCosmeticItemId())
                .put("name", e.getItemName())
                .put("price", e.getPrice() == null ? null : e.getPrice().toPlainString())
                .put("qty", e.getQuantity())
                .put("status", statusNameOf(e.getProductStatusId()))
                .put("imagePath", e.getImagePath())
                .put("brandId", e.getBrandId())
                .put("typeId", e.getCosmeticTypeId())
                .put("manufacturerId", e.getManufacturerId())
                .put("certificateId", e.getCertificateId());

        var actor = securityAuditor.getCurrentUserOrNull();
        auditLog.log("Удалил товар", actor, null, details);

        cosmeticItemRepository.delete(e);
    }
}
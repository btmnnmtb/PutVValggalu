// controller/ReportsController.java
package com.example.demo.controller;

import com.example.demo.model.CosmeticView;
import com.example.demo.repository.CosmeticViewRepository;
import lombok.RequiredArgsConstructor;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/reports")
public class ReportsController {

    private final CosmeticViewRepository repo;

    @GetMapping(value = "/chart/by-type.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> chartByType() throws Exception {
        List<CosmeticView.NameCount> rows = repo.countSkuByType();

        CategoryChart chart = new CategoryChartBuilder()
                .width(900).height(500)
                .title("SKU по категориям")
                .xAxisTitle("Категория").yAxisTitle("SKU")
                .build();

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setYAxisMin(0.0);
        chart.addSeries(
                "SKU",
                rows.stream().map(CosmeticView.NameCount::getName).toList(),
                rows.stream().map(CosmeticView.NameCount::getCnt).toList()
        );

        return png(chart);
    }

    @GetMapping(value = "/chart/buckets.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> chartBuckets() throws Exception {
        Object row = repo.distributionBuckets();
        long z=0,l1=0,l6=0,g=0;
        if (row instanceof Object[] a) {
            z  = toLong(a[0]); l1 = toLong(a[1]); l6 = toLong(a[2]); g = toLong(a[3]);
        }

        PieChart chart = new PieChartBuilder()
                .width(700).height(500)
                .title("Распределение остатков (SKU)")
                .build();

        chart.getStyler().setLegendVisible(true);
        chart.addSeries("0", z);
        chart.addSeries("1–5", l1);
        chart.addSeries("6–10", l6);
        chart.addSeries(">10", g);

        return png(chart);
    }

    @GetMapping(value = "/chart/top-brands.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> chartTopBrands() throws Exception {
        List<CosmeticView.NameSum> rows = repo.topBrandsByStockValue();

        CategoryChart chart = new CategoryChartBuilder()
                .width(1000).height(600)
                .title("Топ-10 брендов по стоимости остатков")
                .xAxisTitle("Бренд").yAxisTitle("Стоимость (₽)")
                .build();

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setYAxisMin(0.0);
        chart.addSeries(
                "₽",
                rows.stream().map(CosmeticView.NameSum::getName).toList(),
                rows.stream().map(n -> n.getSum() == null ? BigDecimal.ZERO : n.getSum()).toList()
        );

        return png(chart);
    }

    @GetMapping("/export/by-category.csv")
    public ResponseEntity<byte[]> exportByCategoryCsv() {
        List<CosmeticView.NameCount> rows = repo.countSkuByType();
        String csv = "Категория;SKU\n" + rows.stream()
                .map(r -> esc(r.getName()) + ";" + r.getCnt())
                .collect(Collectors.joining("\n"));
        return csv(csv, "by-category.csv");
    }

    @GetMapping("/export/stock-buckets.csv")
    public ResponseEntity<byte[]> exportBucketsCsv() {
        Object row = repo.distributionBuckets();
        long z=0,l1=0,l6=0,g=0;
        if (row instanceof Object[] a) {
            z  = toLong(a[0]); l1 = toLong(a[1]); l6 = toLong(a[2]); g = toLong(a[3]);
        }
        String csv = "Диапазон;SKU\n"
                + "0;" + z + "\n"
                + "1–5;" + l1 + "\n"
                + "6–10;" + l6 + "\n"
                + ">10;" + g + "\n";
        return csv(csv, "stock-buckets.csv");
    }

    @GetMapping("/export/top-brands.csv")
    public ResponseEntity<byte[]> exportTopBrandsCsv() {
        List<CosmeticView.NameSum> rows = repo.topBrandsByStockValue();
        String csv = "Бренд;Стоимость_остатков\n" + rows.stream()
                .map(r -> esc(r.getName()) + ";" + (r.getSum()==null? "0" : r.getSum().toPlainString()))
                .collect(Collectors.joining("\n"));
        return csv(csv, "top-brands.csv");
    }

    private ResponseEntity<byte[]> png(org.knowm.xchart.internal.chartpart.Chart<?, ?> chart) throws Exception {
        byte[] bytes = BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(bytes);
    }

    private ResponseEntity<byte[]> csv(String text, String filename) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    private long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return 0L; }
    }
    private String esc(String s){
        if (s == null) return "";
        if (s.contains(";") || s.contains("\"")) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }
}
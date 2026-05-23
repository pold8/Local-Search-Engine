package org.example.widget;

import org.example.model.FileRecord;
import org.example.model.SearchResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImageGalleryWidget implements Widget {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp");
    private static final double ACTIVATION_THRESHOLD = 0.3;

    @Override
    public boolean isActivated(List<SearchResult> results, String query) {
        if (results.isEmpty()) return false;
        boolean queryMentionsImages = query != null
                && (query.toLowerCase().contains("image") || query.toLowerCase().contains("color:"));
        if (queryMentionsImages) return true;
        long imageCount = countImages(results);
        return (double) imageCount / results.size() >= ACTIVATION_THRESHOLD;
    }

    @Override
    public void display(List<SearchResult> results) {
        List<SearchResult> images = results.stream()
                .filter(r -> IMAGE_EXTENSIONS.contains(
                        r.getFileRecord().getExtension().toLowerCase()))
                .toList();

        Map<String, Long> colorCounts = new LinkedHashMap<>();
        for (SearchResult r : images) {
            String color = r.getFileRecord().getDominantColor();
            if (color != null) {
                colorCounts.merge(color, 1L, Long::sum);
            }
        }

        System.out.println("┌─ Image Gallery Widget ─────────────────────────┐");
        System.out.printf("│  %d image(s) found%n", images.size());

        images.forEach(r -> {
            FileRecord f = r.getFileRecord();
            String color = f.getDominantColor() != null ? " [" + f.getDominantColor() + "]" : "";
            System.out.printf("│  • %s%s%n", f.getName(), color);
        });

        if (!colorCounts.isEmpty()) {
            StringBuilder palette = new StringBuilder("│  Colors: ");
            colorCounts.forEach((c, n) -> palette.append(c).append("(").append(n).append(") "));
            System.out.println(palette);
        }

        System.out.println("└────────────────────────────────────────────────┘");
    }

    private long countImages(List<SearchResult> results) {
        return results.stream()
                .filter(r -> IMAGE_EXTENSIONS.contains(
                        r.getFileRecord().getExtension().toLowerCase()))
                .count();
    }
}

package org.example.indexing;

import org.example.model.FileRecord;
import org.example.util.HashUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

public class ImageFileStrategy implements FileIndexingStrategy {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp");

    private static final String[] COLOR_NAMES = {
        "red", "green", "blue", "yellow", "orange", "purple", "pink", "brown", "black", "white", "gray"
    };
    private static final int[][] COLOR_RGBS = {
        {255,   0,   0}, {  0, 128,   0}, {  0,   0, 255}, {255, 255,   0}, {255, 165,   0},
        {128,   0, 128}, {255, 192, 203}, {139,  69,  19}, {  0,   0,   0}, {255, 255, 255},
        {128, 128, 128}
    };

    @Override
    public boolean supports(String extension) {
        return IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }

    @Override
    public FileRecord process(File file) {
        String fileName = file.getName();
        String extension = getExtension(fileName);
        long size = 0;
        long lastModified = 0;
        String fileHash = "";
        String dominantColor = null;

        try {
            size = Files.size(file.toPath());
            lastModified = Files.getLastModifiedTime(file.toPath()).toMillis();
            fileHash = HashUtil.computeSHA256(file.toPath());
            dominantColor = extractDominantColor(file);
        } catch (Exception e) {
            System.err.println("[ImageStrategy] Error processing: " + file + " — " + e.getMessage());
        }

        FileRecord record = new FileRecord(
                file.getAbsolutePath(), fileName, extension, size, lastModified, fileHash, "", "");
        record.setDominantColor(dominantColor);
        return record;
    }

    private String extractDominantColor(File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        if (img == null) return null;

        int width = img.getWidth();
        int height = img.getHeight();
        // Sample at most ~2500 pixels by stepping through the image
        int step = Math.max(1, Math.min(width, height) / 50);

        long totalR = 0, totalG = 0, totalB = 0;
        int count = 0;

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int rgb = img.getRGB(x, y);
                totalR += (rgb >> 16) & 0xFF;
                totalG += (rgb >>  8) & 0xFF;
                totalB +=  rgb        & 0xFF;
                count++;
            }
        }

        if (count == 0) return null;

        int avgR = (int) (totalR / count);
        int avgG = (int) (totalG / count);
        int avgB = (int) (totalB / count);

        return nearestColorName(avgR, avgG, avgB);
    }

    private String nearestColorName(int r, int g, int b) {
        String nearest = COLOR_NAMES[0];
        long minDist = Long.MAX_VALUE;
        for (int i = 0; i < COLOR_NAMES.length; i++) {
            int[] c = COLOR_RGBS[i];
            long dist = (long)(c[0] - r) * (c[0] - r)
                      + (long)(c[1] - g) * (c[1] - g)
                      + (long)(c[2] - b) * (c[2] - b);
            if (dist < minDist) {
                minDist = dist;
                nearest = COLOR_NAMES[i];
            }
        }
        return nearest;
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot <= 0) return "";
        return fileName.substring(lastDot + 1).toLowerCase();
    }
}

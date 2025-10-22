package ru.vsu.cs.kiselev;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

    public static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "raw", "tiff", "tif"
    );

    public static boolean isImageFile(File file) {
        if (file.isDirectory()) return false;

        String name = file.getName().toLowerCase();
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (name.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    public static List<File> findImageFiles(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return new ArrayList<>();

        List<File> imageFiles = new ArrayList<>();
        for (File file : files) {
            if (isImageFile(file)) {
                imageFiles.add(file);
            }
        }
        return imageFiles;
    }

    public static List<File> findImageFilesRecursive(File directory) {
        List<File> imageFiles = new ArrayList<>();
        findImageFilesRecursive(directory, imageFiles);
        return imageFiles;
    }

    private static void findImageFilesRecursive(File directory, List<File> imageFiles) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                String dirName = file.getName();
                if (dirName.equals("_duplicates") || dirName.equals("_reports")) {
                    continue;
                }
                findImageFilesRecursive(file, imageFiles);
            } else if (isImageFile(file)) {
                imageFiles.add(file);
            }
        }
    }

    public static boolean createDirectoryIfNotExists(File directory) {
        if (!directory.exists()) {
            return directory.mkdirs();
        }
        return true;
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf);
    }

    public static String getFileNameWithoutExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return name;
        }
        return name.substring(0, lastIndexOf);
    }
}
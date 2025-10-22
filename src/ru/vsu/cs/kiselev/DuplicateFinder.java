package ru.vsu.cs.kiselev;

import java.awt.Desktop;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class DuplicateFinder {
    private Map<String, List<Photo>> hashMap;
    private Consumer<String> logConsumer;

    public DuplicateFinder(Consumer<String> logConsumer) {
        this.hashMap = new HashMap<>();
        this.logConsumer = logConsumer;
    }

    public Map<String, List<Photo>> getHashMap() {
        return hashMap;
    }

    public void addPhoto(Photo photo) {
        String hash = photo.getHash();
        hashMap.putIfAbsent(hash, new ArrayList<>());
        hashMap.get(hash).add(photo);
    }

    public List<List<Photo>> findDuplicates() {
        List<List<Photo>> duplicates = new ArrayList<>();

        for (List<Photo> photoGroup : hashMap.values()) {
            if (photoGroup.size() > 1) {
                duplicates.add(photoGroup);
            }
        }

        return duplicates;
    }

    public void removeDuplicates(File duplicatesFolder) {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.MOVE_TO_TRASH)) {
            logConsumer.accept("Ошибка: Перемещение в корзину не поддерживается в этой системе.");
            logConsumer.accept("Дубликаты не будут удалены.");
            return;
        }

        List<List<Photo>> allDuplicates = findDuplicates();
        int removedCount = 0;

        logConsumer.accept("Найдено групп дубликатов: " + allDuplicates.size());

        for (List<Photo> duplicateGroup : allDuplicates) {
            duplicateGroup.sort((p1, p2) -> Long.compare(p2.getFileSize(), p1.getFileSize()));

            logConsumer.accept("Группа дубликатов (" + duplicateGroup.size() + " файлов):");
            for (int i = 0; i < duplicateGroup.size(); i++) {
                Photo photo = duplicateGroup.get(i);
                logConsumer.accept("  " + (i == 0 ? "[ОРИГИНАЛ] " : "[ДУБЛИКАТ] ") +
                        photo.getFile().getName() + " (" + photo.getFileSize() + " bytes)");
            }

            for (int i = 1; i < duplicateGroup.size(); i++) {
                Photo duplicate = duplicateGroup.get(i);
                File originalFile = duplicate.getFile();

                try {
                    if (Desktop.getDesktop().moveToTrash(originalFile)) {
                        removedCount++;
                        logConsumer.accept("Перемещен в корзину: " + originalFile.getName());
                    } else {
                        logConsumer.accept("Не удалось переместить в корзину: " + originalFile.getName());
                    }
                } catch (Exception e) {
                    logConsumer.accept("Ошибка при перемещении в корзину: " + originalFile.getName() + " - " + e.getMessage());
                }
            }
        }

        logConsumer.accept("Всего перемещено дубликатов в корзину: " + removedCount);
    }

    public List<Photo> getUniquePhotos() {
        List<Photo> uniquePhotos = new ArrayList<>();

        for (List<Photo> photoGroup : hashMap.values()) {
            if (!photoGroup.isEmpty()) {
                uniquePhotos.add(photoGroup.get(0));
            }
        }

        return uniquePhotos;
    }

    public void printStatistics() {
        List<List<Photo>> duplicates = findDuplicates();
        int totalDuplicates = 0;
        long totalWastedSpace = 0;

        for (List<Photo> group : duplicates) {
            totalDuplicates += group.size() - 1;
            for (int i = 1; i < group.size(); i++) {
                totalWastedSpace += group.get(i).getFileSize();
            }
        }

        logConsumer.accept("Статистика дубликатов:");
        logConsumer.accept("  Групп дубликатов: " + duplicates.size());
        logConsumer.accept("  Всего дубликатов: " + totalDuplicates);
        logConsumer.accept("  Потрачено впустую: " + (totalWastedSpace / (1024 * 1024)) + " MB");
    }
}
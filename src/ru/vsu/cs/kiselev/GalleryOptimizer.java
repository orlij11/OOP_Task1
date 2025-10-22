package ru.vsu.cs.kiselev;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GalleryOptimizer {
    public List<Photo> photos;
    public File sourceDirectory;
    private File duplicatesFolder;
    private File reportsFolder;

    private Consumer<String> logConsumer;

    private MetadataProcessor metadataProcessor;
    private DuplicateFinder duplicateFinder;
    private DirectoryOrganizer organizer;


    public GalleryOptimizer(String sourcePath, Consumer<String> logConsumer) {
        this.sourceDirectory = new File(sourcePath);
        this.photos = new ArrayList<>();
        this.logConsumer = logConsumer;

        this.reportsFolder = new File(sourceDirectory, "_reports");

        FileUtils.createDirectoryIfNotExists(reportsFolder);

        this.metadataProcessor = new MetadataProcessor(logConsumer);
        this.duplicateFinder = new DuplicateFinder(logConsumer);
        this.organizer = new DirectoryOrganizer(sourceDirectory, logConsumer);
    }

    public void scanGallery() throws InterruptedException, ExecutionException {
        logConsumer.accept("Сканирование галереи: " + sourceDirectory.getAbsolutePath());
        photos.clear();

        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            logConsumer.accept("Ошибка: Директория не существует или не является папкой");
            return;
        }

        List<File> imageFiles = FileUtils.findImageFilesRecursive(sourceDirectory);
        int totalFiles = imageFiles.size();
        logConsumer.accept("Найдено файлов для обработки: " + totalFiles);


        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        AtomicInteger processed = new AtomicInteger(0);

        List<Callable<Photo>> tasks = imageFiles.stream().map(file -> (Callable<Photo>) () -> {
            try {
                Photo photo = new Photo(file);
                int count = processed.incrementAndGet();
                if (count % 50 == 0 || count == totalFiles) {
                    logConsumer.accept("Обработано: " + count + " / " + totalFiles + " файлов...");
                }
                return photo;
            } catch (Exception e) {
                logConsumer.accept("Ошибка при обработке файла: " + file.getName() + " - " + e.getMessage());
                return null;
            }
        }).collect(Collectors.toList());

        List<Future<Photo>> results = executor.invokeAll(tasks);
        executor.shutdown();

        photos.clear();
        for (Future<Photo> future : results) {
            Photo photo = future.get();
            if (photo != null) {
                photos.add(photo);
            }
        }

        logConsumer.accept("Сканирование завершено! Найдено фотографий: " + photos.size());

        if (!photos.isEmpty()) {
            generateScanReport();
        }
    }

    private void generateScanReport() {
        File reportFile = new File(reportsFolder, "scan_report_" + System.currentTimeMillis() + ".csv");
        metadataProcessor.generateReport(photos, reportFile);
    }

    public void optimizeGallery() throws Exception {
        if (photos.isEmpty()) {
            logConsumer.accept("Нет фотографий для обработки. Выполняется сканирование...");
            scanGallery();

            if (photos.isEmpty()) {
                logConsumer.accept("После сканирования фотографии не найдены");
                return;
            }
        }

        logConsumer.accept("\n=== НАЧАЛО ПОЛНОЙ ОПТИМИЗАЦИИ ===");
        logConsumer.accept("Будет обработано фотографий: " + photos.size());

        try {
            logConsumer.accept("\n=== ШАГ 1: Поиск и удаление дубликатов ===");
            removeDuplicates();

            logConsumer.accept("\n=== ШАГ 2: Стандартизация имен файлов ===");
            renamePhotos();

            logConsumer.accept("\n=== ШАГ 3: Организация по дате создания ===");
            organizeByDate();

            logConsumer.accept("\n=== ШАГ 4: Генерация итогового отчета ===");
            generateFinalReport();

            logConsumer.accept("\n=== ОПТИМИЗАЦИЯ ГАЛЕРЕИ УСПЕШНО ЗАВЕРШЕНА ===");
            logConsumer.accept("Итоговое количество фотографий: " + photos.size());

        } catch (Exception e) {
            logConsumer.accept("Критическая ошибка при оптимизации: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    public void removeDuplicates() {
        if (photos.isEmpty()) {
            logConsumer.accept("Нет фотографий для обработки. Сначала выполните сканирование.");
            return;
        }

        duplicateFinder.getHashMap().clear();
        for (Photo photo : photos) {
            duplicateFinder.addPhoto(photo);
        }

        int beforeCount = photos.size();

        duplicateFinder.removeDuplicates(null);

        photos = duplicateFinder.getUniquePhotos();
        int removedCount = beforeCount - photos.size();

        logConsumer.accept("Удалено дубликатов: " + removedCount);
        logConsumer.accept("Осталось уникальных фото: " + photos.size());
    }

    public void organizeByDate() {
        if (photos.isEmpty()) {
            logConsumer.accept("Нет фотографий для обработки. Сначала выполните сканирование.");
            return;
        }


        organizer.organizeByDate(photos);
        logConsumer.accept("Организация по дате завершена!");
    }

    private void renamePhotos() {
        metadataProcessor.renamePhotos(photos, "photo_{date}_{counter}");
    }

    private void generateFinalReport() {
        File reportFile = new File(reportsFolder, "optimization_report_" + System.currentTimeMillis() + ".csv");
        metadataProcessor.generateReport(photos, reportFile);

        long totalSize = 0;
        for (Photo photo : photos) {
            totalSize += photo.getFileSize();
        }

        logConsumer.accept("Общий размер фотографий: " + (totalSize / (1024 * 1024)) + " MB");
    }

    public List<Photo> getPhotos() {
        return photos;
    }
}
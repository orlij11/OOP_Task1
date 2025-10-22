package ru.vsu.cs.kiselev;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Consumer;

public class MetadataProcessor {

    private Consumer<String> logConsumer;

    public MetadataProcessor(Consumer<String> logConsumer) {
        this.logConsumer = logConsumer;
    }

    public void renamePhotos(List<Photo> photos, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        int counter = 1;

        for (Photo photo : photos) {
            try {
                if (!photo.getFile().exists()) {
                    logConsumer.accept("Файл не существует, пропускаем: " + photo.getFile().getName());
                    continue;
                }

                String newName = pattern
                        .replace("{date}", dateFormat.format(photo.getCreationDate()))
                        .replace("{counter}", String.format("%04d", counter))
                        .replace("{hash}", photo.getHash().substring(0, 8));


                String extension = FileUtils.getFileExtension(photo.getFile());
                newName += extension;

                File newFile = new File(photo.getFile().getParent(), newName);

                if (!newFile.getName().equals(photo.getFile().getName())) {


                    Path sourcePath = photo.getFile().toPath();
                    Path destPath = newFile.toPath();

                    try {
                        Files.move(sourcePath, destPath, StandardCopyOption.ATOMIC_MOVE);
                        logConsumer.accept("Переименован: " + photo.getFile().getName() + " -> " + newName);
                        photo.setFile(newFile); // Обновляем ссылку
                    } catch (IOException e) {
                        logConsumer.accept("Не удалось переименовать: " + photo.getFile().getName() + " - " + e.getMessage());
                    }
                }
                counter++;

            } catch (Exception e) {
                logConsumer.accept("Ошибка при переименовании: " + photo.getFile().getName() + " - " + e.getMessage());
            }
        }
    }

    public void generateReport(List<Photo> photos, File outputFile) {
        StringBuilder report = new StringBuilder();
        report.append("Имя файла,Размер,Дата создания,Хэш\n");

        for (Photo photo : photos) {
            report.append(String.format("%s,%d,%s,%s\n",
                    photo.getFile().getName(),
                    photo.getFileSize(),
                    photo.getCreationDate(),
                    photo.getHash()));
        }

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(report.toString());
            logConsumer.accept("Отчет сохранен: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            logConsumer.accept("Ошибка сохранения отчета: " + e.getMessage());
        }

        logConsumer.accept("Отчет сгенерирован для " + photos.size() + " файлов");
    }
}
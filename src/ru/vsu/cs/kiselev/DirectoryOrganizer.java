package ru.vsu.cs.kiselev;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class DirectoryOrganizer {
    private File baseDirectory;
    private Consumer<String> logConsumer;

    public DirectoryOrganizer(File baseDirectory, Consumer<String> logConsumer) {
        this.baseDirectory = baseDirectory;
        this.logConsumer = logConsumer;
    }

    public void organizeByDate(List<Photo> photos) {
        logConsumer.accept("Организация по дате...");

        for (Photo photo : photos) {
            try {
                if (!photo.getFile().exists()) {
                    logConsumer.accept("Файл не найден, пропуск: " + photo.getFile().getAbsolutePath());
                    continue;
                }

                Date date = photo.getCreationDate();
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
                SimpleDateFormat monthFormat = new SimpleDateFormat("MM - MMMM");
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd - EEEE");

                File yearDir = new File(baseDirectory, yearFormat.format(date));
                File monthDir = new File(yearDir, monthFormat.format(date));
                File dayDir = new File(monthDir, dayFormat.format(date));

                if (!dayDir.exists()) {
                    dayDir.mkdirs();
                }

                File destination = new File(dayDir, photo.getFile().getName());

                Path sourcePath = photo.getFile().toPath();
                Path destPath = destination.toPath();

                try {
                    Files.move(sourcePath, destPath, StandardCopyOption.ATOMIC_MOVE);
                    logConsumer.accept("Перемещен: " + photo.getFile().getName() + " -> " + dayDir.getPath());
                    photo.setFile(destination);
                } catch (IOException e) {
                    logConsumer.accept("Не удалось переместить: " + photo.getFile().getName() + " - " + e.getMessage());
                }


            } catch (Exception e) {
                logConsumer.accept("Ошибка при организации файла: " + photo.getFile().getName() + " - " + e.getMessage());
            }
        }

        logConsumer.accept("Организация по дате завершена!");
    }
}
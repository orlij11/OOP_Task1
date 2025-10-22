package ru.vsu.cs.kiselev;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class Photo {
    private File file;
    private String hash;
    private Date creationDate;
    private long fileSize;

    public Photo(File file) {
        this.file = file;
        this.fileSize = file.length();
        extractMetadata();
        calculateHash();
    }

    private void calculateHash() {
        try (InputStream fis = Files.newInputStream(file.toPath());
             DigestInputStream dis = new DigestInputStream(fis, MessageDigest.getInstance("MD5"))) {

            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {
            }

            MessageDigest digest = dis.getMessageDigest();
            byte[] hashBytes = digest.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            this.hash = sb.toString();

        } catch (NoSuchAlgorithmException | IOException e) {
            System.err.println("Ошибка при вычислении хэша: " + file.getName() + " - " + e.getMessage());
            this.hash = String.valueOf(file.length() + "_" + this.creationDate.getTime());
        }
    }

    private void extractMetadata() {
        try {
            Path path = file.toPath();
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            this.creationDate = new Date(attrs.creationTime().toMillis());
            return;
        } catch (IOException e) {
            System.err.println("Ошибка при чтении атрибутов файла: " + file.getName());
        }

        this.creationDate = new Date(file.lastModified());
    }

    public File getFile() { return file; }
    public String getHash() { return hash; }
    public Date getCreationDate() { return creationDate; }
    public long getFileSize() { return fileSize; }

    public void setFile(File file) {
        this.file = file;
        this.fileSize = file.length();
    }

    @Override
    public String toString() {
        return String.format("Photo{file=%s, hash=%s, date=%s}",
                file.getName(), hash != null ? hash.substring(0, 8) : "no_hash", creationDate);
    }
}
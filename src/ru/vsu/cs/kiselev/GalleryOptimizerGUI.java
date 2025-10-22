package ru.vsu.cs.kiselev;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GalleryOptimizerGUI extends JFrame {
    private JTextField folderPathField;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JButton selectFolderBtn, scanBtn, optimizeBtn, removeDuplicatesBtn, organizeByDateBtn;
    private GalleryOptimizer optimizer;

    public GalleryOptimizerGUI() {
        setTitle("Gallery Optimizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        initializeComponents();
        setupLayout();
        setupEventHandlers();


        setVisible(true);
    }

    private void initializeComponents() {
        folderPathField = new JTextField(30);
        folderPathField.setEditable(false);

        logArea = new JTextArea(20, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        selectFolderBtn = new JButton("Выбрать папку");
        scanBtn = new JButton("Сканировать");
        optimizeBtn = new JButton("Полная оптимизация");
        removeDuplicatesBtn = new JButton("Удалить дубликаты");
        organizeByDateBtn = new JButton("Организовать по дате");

        setButtonsEnabled(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Папка с фотографиями"));
        topPanel.add(new JLabel("Путь:"));
        topPanel.add(folderPathField);
        topPanel.add(selectFolderBtn);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Лог выполнения"));

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 5)); // 4 кнопки
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Операции"));
        buttonPanel.add(scanBtn);
        buttonPanel.add(removeDuplicatesBtn);
        buttonPanel.add(organizeByDateBtn);
        buttonPanel.add(optimizeBtn);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(progressBar, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        selectFolderBtn.addActionListener(e -> selectFolder());

        scanBtn.addActionListener(e -> scanGallery());
        optimizeBtn.addActionListener(e -> optimizeGallery());
        removeDuplicatesBtn.addActionListener(e -> removeDuplicates());
        organizeByDateBtn.addActionListener(e -> organizeByDate());
    }

    private void selectFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Выберите папку с фотографиями");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fileChooser.getSelectedFile();
            folderPathField.setText(selectedFolder.getAbsolutePath());

            optimizer = new GalleryOptimizer(selectedFolder.getAbsolutePath(), this::log);

            setButtonsEnabled(true);
            log("Выбрана папка: " + selectedFolder.getAbsolutePath());
        }
    }


    private void scanGallery() {
        if (optimizer == null) return;

        setButtonsEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true); // Пока без точного прогресса

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                log("Начинаем сканирование галереи...");
                optimizer.scanGallery();
                return optimizer.getPhotos().size();
            }

            @Override
            protected void done() {
                try {
                    int photoCount = get(); // Получаем результат из doInBackground
                    log("Сканирование завершено! Найдено: " + photoCount + " фото");
                } catch (Exception e) {
                    log("ОШИБКА (сканирование): " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    setButtonsEnabled(true);
                    progressBar.setVisible(false);
                }
            }
        }.execute();
    }

    private void optimizeGallery() {
        if (optimizer == null) return;

        setButtonsEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                log("=== НАЧАЛО ПОЛНОЙ ОПТИМИЗАЦИИ ===");
                optimizer.optimizeGallery();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    log("=== ОПТИМИЗАЦИЯ ЗАВЕРШЕНА ===");
                } catch (Exception e) {
                    log("ОШИБКА (оптимизация): " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    setButtonsEnabled(true);
                    progressBar.setVisible(false);
                }
            }
        }.execute();
    }

    private void removeDuplicates() {
        if (optimizer == null) return;

        setButtonsEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                log("Поиск и удаление дубликатов...");
                optimizer.removeDuplicates();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    log("Удаление дубликатов завершено!");
                } catch (Exception e) {
                    log("ОШИБКА (удаление дубликатов): " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    setButtonsEnabled(true);
                    progressBar.setVisible(false);
                }
            }
        }.execute();
    }

    private void organizeByDate() {
        if (optimizer == null) return;

        setButtonsEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                log("Организация по дате...");
                optimizer.organizeByDate();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    log("Организация по дате завершена!");
                } catch (Exception e) {
                    log("ОШИБКА (организация по дате): " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    setButtonsEnabled(true);
                    progressBar.setVisible(false);
                }
            }
        }.execute();
    }

    private void setButtonsEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            scanBtn.setEnabled(enabled);
            optimizeBtn.setEnabled(enabled);
            removeDuplicatesBtn.setEnabled(enabled);
            organizeByDateBtn.setEnabled(enabled);
            selectFolderBtn.setEnabled(enabled);
        });
    }

    private void log(String message) {
        String logMessage = message.endsWith("\n") ? message : message + "\n";

        SwingUtilities.invokeLater(() -> {
            logArea.append(logMessage);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        SwingUtilities.invokeLater(() -> new GalleryOptimizerGUI());
    }
}
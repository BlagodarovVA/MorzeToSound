package main.java.Morze;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static main.java.Morze.MorzeDictionary.stringToMorze;

public class TonePlayerGUI extends JFrame {

    private static final int SAMPLE_RATE = 22050; // Гц
    private static final int CHANNELS = 1;        // Моно
    private static final int FRAME_SIZE = 2;      // байт на сэмпл (16 бит)
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    private final JTextField freqField;
    private final JTextField durationField;
    private final JTextArea textArea;
    private final JTextArea morseOutputArea;
    private volatile boolean stopRequested = false;
    private SourceDataLine currentLine = null; // чтобы можно было прервать звук мгновенно
    private Thread playbackThread = null;      // чтобы можно было отслеживать активное воспроизведение

    // Внешний вид окна
    public TonePlayerGUI() {
        setTitle("Генератор морзянки");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new GridBagLayout());

        // Цвета для тёмной темы
        Color darkBackground = new Color(30, 30, 30);      // тёмно-серый фон
        Color lightText = new Color(220, 220, 220);        // светлый текст
        Color fieldBackground = new Color(50, 50, 50);     // фон полей
        Color buttonBackground = new Color(50, 50, 50);    // фон кнопок
        Color buttonForeground = Color.BLACK;

        // Применяем ко всему окну
        getContentPane().setBackground(darkBackground);
        getContentPane().setForeground(lightText);

        // Настройка UIManager (влияет на новые компоненты)
        UIManager.put("Panel.background", darkBackground);
        UIManager.put("Panel.foreground", lightText);
        UIManager.put("Label.foreground", lightText);
        UIManager.put("TextField.background", fieldBackground);
        UIManager.put("TextField.foreground", lightText);
        UIManager.put("TextArea.background", fieldBackground);
        UIManager.put("TextArea.foreground", lightText);
        UIManager.put("Button.background", buttonBackground);
        UIManager.put("Button.foreground", buttonForeground);
        UIManager.put("ScrollPane.background", darkBackground);
        UIManager.put("Viewport.background", fieldBackground);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6); // отступы: сверху, слева, снизу, справа

        // === Частота ===
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; // метка по левому краю
        add(new JLabel("Частота (Гц):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        freqField = new JTextField("700", 12);
        add(freqField, gbc);

        // === Длительность ===
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Длительность точки (мс):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        durationField = new JTextField("80", 12);
        add(durationField, gbc);

        // === Текст для ввода ===
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Текст:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        textArea = new JTextArea("привет", 4, 20);          // 4 строки, ~20 символов в ширину
        textArea.setFont(freqField.getFont());
        textArea.setLineWrap(true);                         // Перенос по словам
        textArea.setWrapStyleWord(true);                    // Переносить по границам слов, а не по символам
        JScrollPane scrollPane = new JScrollPane(textArea); // Добавляем прокрутку
        add(scrollPane, gbc);

        // === Преобразованный текст (Морзе) ===
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weighty = 0;                                    // не растягивать сильно
        add(new JLabel("Морзе:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        morseOutputArea = new JTextArea();
        morseOutputArea.setEditable(false); // только для чтения
        morseOutputArea.setFont(freqField.getFont());
        morseOutputArea.setLineWrap(true);
        morseOutputArea.setWrapStyleWord(true);
        JScrollPane outputScrollPane = new JScrollPane(morseOutputArea);
        add(outputScrollPane, gbc);

//        // === Кнопка "Воспроизвести" ===
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton playButton = new JButton("Старт");
        playButton.addActionListener(new PlayButtonListener());
        add(playButton, gbc);

        // === Кнопка "Прервать" ===
        gbc.gridy = 5;
        JButton stopButton = new JButton("Стоп");
        stopButton.addActionListener(new StopButtonListener());
        add(stopButton, gbc);

        pack();                     // автоматически оптимальный размер окна
//        setResizable(false);         // чтобы окно нельзя было растягивать
    }

    private void writeTone(SourceDataLine line, double frequency, int durationMs) {
        if (durationMs <= 0 || stopRequested) return;

        int sampleCount = (int) ((SAMPLE_RATE * durationMs) / 1000.0);
        byte[] buffer = new byte[sampleCount * FRAME_SIZE];

        final int FADE_MS = 6;
        final int fadeSamples = Math.min(sampleCount / 2, (int) ((SAMPLE_RATE * FADE_MS) / 1000.0));

        for (int i = 0; i < sampleCount; i++) {
            if (stopRequested) return;

            double time = i / (double) SAMPLE_RATE;
            double angle = 2.0 * Math.PI * frequency * time;
            double sine = Math.sin(angle);

            double gain = 1.0;
            if (i < fadeSamples) {
                gain = (double) i / fadeSamples;
            } else if (i >= sampleCount - fadeSamples) {
                gain = (double) (sampleCount - 1 - i) / fadeSamples;
            }

            short sample = (short) (Short.MAX_VALUE * sine * gain);
            buffer[i * 2] = (byte) (sample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        line.write(buffer, 0, buffer.length);
    }

    private void writeSilence(SourceDataLine line, int durationMs) {
        if (durationMs <= 0) return;
        int sampleCount = (int) ((SAMPLE_RATE * durationMs) / 1000.0);
        byte[] silence = new byte[sampleCount * FRAME_SIZE]; // по умолчанию нули
        line.write(silence, 0, silence.length);
    }

    private class PlayButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (playbackThread != null && playbackThread.isAlive()) {
                JOptionPane.showMessageDialog(TonePlayerGUI.this,
                        "Воспроизведение уже идёт. Нажмите «Прервать», чтобы остановить.",
                        "Информация", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            stopRequested = false;

            playbackThread = new Thread(() -> {
                try {
                    double frequency = Double.parseDouble(freqField.getText());
                    int duration = Integer.parseInt(durationField.getText());
                    String text = textArea.getText().toUpperCase();

                    if (frequency <= 0 || duration <= 0) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(TonePlayerGUI.this,
                                        "Частота и длительность должны быть положительными числами.",
                                        "Ошибка ввода", JOptionPane.ERROR_MESSAGE)
                        );
                        return;
                    }

                    if (!text.isEmpty()) {
                        String morse = stringToMorze(text).toString();
                        SwingUtilities.invokeLater(() -> morseOutputArea.setText(morse));

                        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, CHANNELS, SIGNED, BIG_ENDIAN);
                        SourceDataLine line = AudioSystem.getSourceDataLine(format);
                        line.open(format);
                        currentLine = line; // ← для прерывания
                        line.start();

                        for (int i = 0; i < morse.length() && !stopRequested; i++) {
                            char c = morse.charAt(i);
                            if (c == '.') {
                                writeTone(line, frequency, duration);
                            } else if (c == '-') {
                                writeTone(line, frequency, duration * 3);
                            } else if (c == ' ') {
                                writeSilence(line, duration * 3);
                            }
                            // Дополнительно: пауза между символами (например, 1 * duration)
                             writeSilence(line, 2 * duration);
                        }

                        if (!stopRequested) {
                            line.drain();
                        }
                        line.close();
                        currentLine = null;

                    }
                } catch (Exception ex) {
                    stopRequested = true;
                    currentLine = null;
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(TonePlayerGUI.this,
                                    "Ошибка: " + ex.getMessage(),
                                    "Ошибка воспроизведения", JOptionPane.ERROR_MESSAGE)
                    );
                }
            });

            playbackThread.setDaemon(true);
            playbackThread.start();
        }
    }

    private class StopButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            stopRequested = true;
            if (currentLine != null) {
                currentLine.close();        // немедленно остановить звук
            }
            if (playbackThread != null) {
                playbackThread.interrupt();
            }
        }
    }

    private void playTone(double frequency, int durationMs) throws LineUnavailableException {
        if (durationMs <= 0) return;

        int sampleCount = (int) ((SAMPLE_RATE * durationMs) / 1000.0);
        byte[] buffer = new byte[sampleCount * FRAME_SIZE];

        // Длительность fade in/out (в миллисекундах)
        final int FADE_MS = 8;
        final int fadeSamples = Math.min(sampleCount / 2, (int) ((SAMPLE_RATE * FADE_MS) / 1000.0));

        for (int i = 0; i < sampleCount; i++) {
            if (stopRequested) return;

            double time = i / (double) SAMPLE_RATE;
            double angle = 2.0 * Math.PI * frequency * time;
            double sine = Math.sin(angle);

            // Применяем fade in/out
            double gain = 1.0;
            if (i < fadeSamples) {
                // Fade in
                gain = (double) i / fadeSamples;
            } else if (i >= sampleCount - fadeSamples) {
                // Fade out
                gain = (double) (sampleCount - 1 - i) / fadeSamples;
            }

            short sample = (short) (Short.MAX_VALUE * sine * gain);

            buffer[i * 2] = (byte) (sample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, CHANNELS, SIGNED, BIG_ENDIAN);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
        }
    }

    static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            new TonePlayerGUI().setVisible(true);
        });
    }
}

package main.java.Morze;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static main.java.Morze.MorzeDictionary.stringToMorze;

public class TonePlayerGUI extends JFrame {

    private static final int SAMPLE_RATE = 8000;
    private static final int CHANNELS = 1;
    private static final int FRAME_SIZE = 2; // 16-bit
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    private final JTextField freqField;
    private final JTextField durationField;
    private final JTextArea textArea;

    public TonePlayerGUI() {
        setTitle("Генератор морзянки");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false); // чтобы окно нельзя было растягивать

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6); // отступы: сверху, слева, снизу, справа

        // === Частота ===
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; // метка по левому краю
        add(new JLabel("Частота (Гц):"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        freqField = new JTextField("650", 12);
        add(freqField, gbc);

        // === Длительность ===
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Длительность точки (мс):"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        durationField = new JTextField("80", 12);
        add(durationField, gbc);

        // === Текст ===
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Текст:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        textArea = new JTextArea("привет", 4, 20);          // 4 строки, ~20 символов в ширину
        textArea.setFont(freqField.getFont());
        textArea.setLineWrap(true);                         // Перенос по словам
        textArea.setWrapStyleWord(true);                    // Переносить по границам слов, а не по символам
        JScrollPane scrollPane = new JScrollPane(textArea); // Добавляем прокрутку
        add(scrollPane, gbc);

        // === Кнопка ===
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;                                  // занимает оба столбца
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JButton playButton = new JButton("Воспроизвести");
        playButton.addActionListener(new PlayButtonListener());
        add(playButton, gbc);

        pack(); // автоматически подбирает оптимальный размер окна
        //setSize(getWidth() * 2, getHeight() * 2); // затем удвоить
        setResizable(true); // разрешить изменение размера (опционально)
    }

    private class PlayButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                double frequency = Double.parseDouble(freqField.getText());
                int duration = Integer.parseInt(durationField.getText());
                String text = textArea.getText().toUpperCase();

                if (frequency <= 0 || duration <= 0) {
                    JOptionPane.showMessageDialog(TonePlayerGUI.this,
                            "Частота и длительность должны быть положительными числами.",
                            "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                System.out.println(text);
                System.out.println(text.length());

                if(!text.isEmpty()){
                    StringBuilder converted = new StringBuilder(stringToMorze(text));
                    System.out.println("converted: " + converted);          // строка морзе

                    for (int i = 0; i < converted.length(); i++) {
                        switch (converted.charAt(i)){
                            case ' ':
                                playTone(20000, duration);
                                break;
                            case '.':
                                playTone(frequency, duration);
                                break;
                            case '-':
                                playTone(frequency, duration*3);
                                break;
                        }
                    }
                    converted.delete(0, converted.length());
                }


            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(TonePlayerGUI.this,
                        "Пожалуйста, введите корректные числа.",
                        "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
            } catch (LineUnavailableException ex) {
                JOptionPane.showMessageDialog(TonePlayerGUI.this,
                        "Не удалось воспроизвести звук: " + ex.getMessage(),
                        "Ошибка аудио", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void playTone(double frequency, int durationMs) throws LineUnavailableException {
        int sampleCount = (int) ((SAMPLE_RATE * durationMs) / 1000.0);
        byte[] buffer = new byte[sampleCount * FRAME_SIZE];

        // Генерация синусоиды
        for (int i = 0; i < sampleCount; i++) {
            double time = i / (double) SAMPLE_RATE;
            double angle = 2.0 * Math.PI * frequency * time;
            short sample = (short) (Short.MAX_VALUE * Math.sin(angle));

            // Little-endian: младший байт сначала
            buffer[i * 2] = (byte) (sample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        // Аудиоформат и воспроизведение
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, CHANNELS, SIGNED, BIG_ENDIAN);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            new TonePlayerGUI().setVisible(true);
        });
    }
}
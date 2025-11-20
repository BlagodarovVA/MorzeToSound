package main.java.Morze;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;

public class TonePlayerGUI extends JFrame {

    protected final JTextField freqField;
    protected final JTextField durationField;
    protected final JTextArea textArea;
    protected final JTextArea morseOutputArea;
    protected volatile boolean stopRequested = false;
    protected SourceDataLine currentLine = null;        // чтобы можно было прервать звук мгновенно
    protected Thread playbackThread = null;             // чтобы можно было отслеживать активное воспроизведение

    // Конструктор и внешний вид окна
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
        gbc.weighty = 0;
        add(new JLabel("Текст:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
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
        gbc.weighty = 0;
        add(new JLabel("Морзе:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        morseOutputArea = new JTextArea();
        morseOutputArea.setEditable(false); // только для чтения
        morseOutputArea.setFont(freqField.getFont());
        morseOutputArea.setLineWrap(true);
        morseOutputArea.setWrapStyleWord(true);
        JScrollPane outputScrollPane = new JScrollPane(morseOutputArea);
        add(outputScrollPane, gbc);

        // === Кнопка "Воспроизвести" ===
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        //gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        JButton playButton = new JButton("Старт");
        playButton.addActionListener(new PlayButtonListener(this));
        add(playButton, gbc);

        // === Кнопка "Прервать" ===
        gbc.gridy = 5;
        JButton stopButton = new JButton("Стоп");
        stopButton.addActionListener(new StopButtonListener(this));
        add(stopButton, gbc);

        // === Параметры окна ===
        pack();                     // автоматически оптимальный размер окна
        // setResizable(false);         // чтобы окно нельзя было растягивать
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

package main.java.Morze;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class TonePlayerGUI extends JFrame {

    protected final JTextField freqField;
    protected final JTextField durationField;
    protected final JTextArea textArea;
    protected JTextPane morseOutputPane;
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
        Color caretColor = new Color(220, 220, 220);       // светло-серый курсор

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

        // === Строка Морзе ===
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
        morseOutputPane = new JTextPane();
        morseOutputPane.setEditable(false);
        morseOutputPane.setFont(freqField.getFont());
        morseOutputPane.setBackground(darkBackground);
        morseOutputPane.setForeground(lightText);
        morseOutputPane.setCaretColor(caretColor);                  // цвет мигающего курсора
        morseOutputPane.setSelectedTextColor(darkBackground);
        morseOutputPane.setSelectionColor(new Color(80, 120, 200)); // цвет выделения
        // Устанавливаем стиль по умолчанию
        StyledDocument doc = morseOutputPane.getStyledDocument();
        Style defaultStyle = doc.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setForeground(defaultStyle, lightText);
        StyleConstants.setBackground(defaultStyle, darkBackground);

        morseOutputPane.setEditorKit(new WrapEditorKit());

        JScrollPane outputScrollPane = new JScrollPane(morseOutputPane);
        outputScrollPane.setBackground(darkBackground);
        add(outputScrollPane, gbc);

        // === Кнопка "Воспроизвести" ===
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
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
        pack();                         // автоматически оптимальный размер окна
        // setResizable(false);         // чтобы окно нельзя было растягивать
    }


    private static class WrapEditorKit extends StyledEditorKit {
        @Override
        public ViewFactory getViewFactory() {
            return new StyledViewFactory();
        }

        private static class StyledViewFactory implements ViewFactory {
            @Override
            public View create(Element elem) {
                String kind = elem.getName();
                if (kind != null) {
                    switch (kind) {
                        case AbstractDocument.ContentElementName:
                            return new WrappedLabelView(elem);
                        case AbstractDocument.ParagraphElementName:
                            return new ParagraphView(elem);
                        case AbstractDocument.SectionElementName:
                            return new BoxView(elem, View.Y_AXIS);
                        case StyleConstants.ComponentElementName:
                            return new ComponentView(elem);
                        case StyleConstants.IconElementName:
                            return new IconView(elem);
                    }
                }
                return new LabelView(elem);
            }
        }

        private static class WrappedLabelView extends LabelView {
            public WrappedLabelView(Element elem) {
                super(elem);
            }

            @Override
            public float getMinimumSpan(int axis) {
                return switch (axis) {
                    case View.X_AXIS -> 0;
                    case View.Y_AXIS -> super.getMinimumSpan(axis);
                    default -> throw new IllegalArgumentException("Invalid axis: " + axis);
                };
            }
        }
    }

    void highlightMorse(int endPos) {
        try {
            StyledDocument doc = morseOutputPane.getStyledDocument();
            doc.removeStyle("green");
            doc.removeStyle("current");

            Style green = doc.addStyle("green", null);
            StyleConstants.setForeground(green, Color.GREEN);

            Style current = doc.addStyle("current", null);
            StyleConstants.setForeground(current, Color.YELLOW);
            StyleConstants.setBackground(current, new Color(60, 60, 60)); // фон

            // Очищаем всё форматирование
            doc.setCharacterAttributes(0, doc.getLength(), doc.getStyle(StyleContext.DEFAULT_STYLE), true);

            if (endPos <= 0) return;

            // Подсвечиваем воспроизведённые символы (зелёный)
            if (endPos > 1) {
                doc.setCharacterAttributes(0, endPos - 1, green, true);
            }

            // Подсвечиваем текущий символ (жёлтый)
            if (endPos - 1 < doc.getLength()) {
                doc.setCharacterAttributes(endPos - 1, 1, current, true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
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

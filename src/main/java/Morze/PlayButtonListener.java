package main.java.Morze;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static main.java.Morze.MorzeDictionary.stringToMorze;

public class PlayButtonListener implements ActionListener {

    private final TonePlayerGUI gui;

    public PlayButtonListener(TonePlayerGUI gui) {
        this.gui = gui;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gui.playbackThread != null && gui.playbackThread.isAlive()) {
            JOptionPane.showMessageDialog(gui,
                    "Воспроизведение уже идёт. Нажмите «Прервать», чтобы остановить.",
                    "Информация", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        gui.stopRequested = false;

        gui.playbackThread = new Thread(() -> {
            try {
                double frequency = Double.parseDouble(gui.freqField.getText());
                int duration = Integer.parseInt(gui.durationField.getText());
                String text = gui.textArea.getText().toUpperCase();

                if (frequency <= 0 || duration <= 0) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(gui,
                                    "Частота и длительность должны быть положительными числами.",
                                    "Ошибка ввода", JOptionPane.ERROR_MESSAGE)
                    );
                    return;
                }

                if (!text.isEmpty()) {
                    String morse = stringToMorze(text).toString();
                    SwingUtilities.invokeLater(() -> gui.morseOutputPane.setText(morse));
                    gui.highlightMorse(0);              // сброс

                    AudioFormat format = AudioPlayer.getAudioFormat();
                    SourceDataLine line = AudioSystem.getSourceDataLine(format);
                    line.open(format);
                    gui.currentLine = line;
                    line.start();

                    for (int i = 0; i < morse.length() && !gui.stopRequested; i++) {
                        char c = morse.charAt(i);

                        // Обновляем подсветку ДО воспроизведения символа
                        final int pos = i + 1;
                        SwingUtilities.invokeLater(() -> gui.highlightMorse(pos));

                        if (c == '.') {
                            AudioPlayer.writeTone(line, frequency, duration);
                        }
                        else if (c == '-') {
                            AudioPlayer.writeTone(line, frequency, duration * 3);
                        }
                        else if (c == ' ') {
                            AudioPlayer.writeSilence(line, duration * 5);
                        }
                        // Дополнительно: пауза между символами (например, 1 * duration)
                        AudioPlayer.writeSilence(line, 2 * duration);
                    }

                    if (!gui.stopRequested) {
                        line.drain();
                    }

                    line.close();
                    gui.currentLine = null;

                }
            } catch (Exception ex) {
                gui.stopRequested = true;
                gui.currentLine = null;
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(gui,
                                "Ошибка: " + ex.getMessage(),
                                "Ошибка воспроизведения", JOptionPane.ERROR_MESSAGE)
                );
            }
        });

        gui.playbackThread.setDaemon(true);
        gui.playbackThread.start();
    }
}

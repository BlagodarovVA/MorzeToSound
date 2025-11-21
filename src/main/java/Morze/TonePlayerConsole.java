// Черновик для консоли
package main.java.Morze;

import javax.sound.sampled.*;

public class TonePlayerConsole {

    private static final int SAMPLE_RATE = 8000;  // Гц
    private static final int CHANNELS = 1;        // Моно
    private static final int FRAME_SIZE = 2;      // байт на сэмпл (16 бит)
    private static final boolean BIG_ENDIAN = false;
    private static final boolean SIGNED = true;

    static void main(String[] args) throws LineUnavailableException, InterruptedException {
        // 510 Гц в течение 1 секунды
        playTone(510, 1000);
    }

    public static void playTone(double frequency, int durationMs) throws LineUnavailableException {
        int bufferSize = (int) ((SAMPLE_RATE * durationMs) / 1000);
        byte[] buffer = new byte[bufferSize * FRAME_SIZE];

        // Генерация синусоидального сигнала
        for (int i = 0; i < bufferSize; i++) {
            double angle = i / ((double) SAMPLE_RATE / frequency) * 2.0 * Math.PI;
            short sample = (short) (Short.MAX_VALUE * Math.sin(angle));
            buffer[i * 2] = (byte) (sample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        // Настройка аудиоформата
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, CHANNELS, SIGNED, BIG_ENDIAN);
        SourceDataLine line = AudioSystem.getSourceDataLine(format);
        line.open(format);
        line.start();

        line.write(buffer, 0, buffer.length);
        line.drain();
        line.close();
    }
}
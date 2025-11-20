package main.java.Morze;

import javax.sound.sampled.*;

public class AudioPlayer {

    private static final int SAMPLE_RATE = 22050;
    private static final int CHANNELS = 1;
    private static final int FRAME_SIZE = 2;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    // Генерация и запись тона в существующую линию
    public static void writeTone(SourceDataLine line, double frequency, int durationMs) {
        if (durationMs <= 0) return;

        int sampleCount = (int) ((SAMPLE_RATE * durationMs) / 1000.0);
        byte[] buffer = new byte[sampleCount * FRAME_SIZE];

        final int FADE_MS = 6;
        final int fadeSamples = Math.min(sampleCount / 2, (int) ((SAMPLE_RATE * FADE_MS) / 1000.0));

        for (int i = 0; i < sampleCount; i++) {
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

    // Запись тишины
    public static void writeSilence(SourceDataLine line, int durationMs) {
        if (durationMs <= 0) return;
        int sampleCount = (int) ((SAMPLE_RATE * durationMs) / 1000.0);
        byte[] silence = new byte[sampleCount * FRAME_SIZE];
        line.write(silence, 0, silence.length);
    }

    // Получить аудиоформат
    public static AudioFormat getAudioFormat() {
        return new AudioFormat(SAMPLE_RATE, 16, CHANNELS, SIGNED, BIG_ENDIAN);
    }
}
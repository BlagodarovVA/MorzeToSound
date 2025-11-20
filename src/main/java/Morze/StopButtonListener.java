package main.java.Morze;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StopButtonListener implements ActionListener {

    private final TonePlayerGUI gui;

    public StopButtonListener(TonePlayerGUI gui) {
        this.gui = gui;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gui.stopRequested = true;
        if (gui.currentLine != null) {
            gui.currentLine.flush();
            gui.currentLine.close();
            gui.currentLine = null;
        }
        if (gui.playbackThread != null) {
            gui.playbackThread.interrupt();
        }
    }
}
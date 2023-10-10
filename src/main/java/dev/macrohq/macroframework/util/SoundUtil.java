package dev.macrohq.macroframework.util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.util.Objects;

public class SoundUtil {

    /**
     * Plays a wav file.
     * @param file A wav file in the "resources" directory. Example /assets/example/sounds/ping.wav
     * @param volumePercent Integer, 0 - 100.
     */
    public void playSound(String file, int volumePercent) {
        new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(Objects.requireNonNull(SoundUtil.class.getResource(file)));
                clip.open(inputStream);
                FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                double volumePercentage = volumePercent / 100f;
                float dB = (float) (Math.log(volumePercentage) / Math.log(10.0) * 20.0);
                volumeControl.setValue(dB);
                clip.start();
            } catch (Exception exception) {
                System.out.println("A sound failed to play. " + exception.getMessage());
            }
        }).start();
    }
}

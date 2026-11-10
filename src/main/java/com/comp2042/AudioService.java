package com.comp2042;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

/**
 * Plays the short sound effects and applies the sound settings.
 *
 * <p>A single {@link MediaPlayer} is reused for every pickup: stopping and
 * replaying it avoids building a new player per coin, and keeps the sound
 * responsive when several coins are eaten in quick succession. If the clip
 * cannot be loaded the service stays silent instead of failing.</p>
 */
public final class AudioService {

    private static final String COIN_SOUND = "/resources/views/res/coinSound.wav";

    private MediaPlayer coinSound;

    public AudioService() {
        loadCoinSound();
    }

    private void loadCoinSound() {
        try {
            URL url = getClass().getResource(COIN_SOUND);
            if (url == null) {
                System.err.println("Sound resource not found: " + COIN_SOUND);
                return;
            }
            coinSound = new MediaPlayer(new Media(url.toExternalForm()));
        } catch (Exception e) {
            System.err.println("Sound unavailable: " + COIN_SOUND + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Plays the pickup sound, respecting the current sound settings.
     */
    public void playCoin() {
        if (coinSound == null) {
            return;
        }
        Settings settings = Settings.get();
        if (!settings.isSoundEnabled()) {
            return;
        }
        coinSound.setVolume(settings.getVolume());
        coinSound.stop();
        coinSound.play();
    }

    /**
     * Stops any sound that is currently playing.
     */
    public void stop() {
        if (coinSound != null) {
            coinSound.stop();
        }
    }
}

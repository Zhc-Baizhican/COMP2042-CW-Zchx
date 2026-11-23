package com.comp2042;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

/**
 * Plays the short sound effects and applies the sound settings.
 *
 * <p>One {@link MediaPlayer} per effect is prepared up front: stopping and
 * replaying an existing player avoids building a new one per coin, and keeps the
 * sound responsive when several coins are eaten in quick succession. All three
 * effects share the same clip; the death thud and the badge chime are the clip
 * played slower and faster, which gives three recognisable sounds without
 * shipping three files. If the clip cannot be loaded the service stays silent
 * instead of failing.</p>
 */
public final class AudioService {

    private static final String CLIP = "/resources/views/res/coinSound.wav";

    /** Playback rate of the pickup blip. */
    private static final double COIN_RATE = 1.0;
    /** Slowed down, the blip becomes a low thud for a lost run. */
    private static final double DEATH_RATE = 0.55;
    /** Sped up, the same blip becomes a bright chime for a new badge. */
    private static final double BADGE_RATE = 1.55;

    private MediaPlayer coinSound;
    private MediaPlayer deathSound;
    private MediaPlayer badgeSound;

    public AudioService() {
        loadSounds();
    }

    private void loadSounds() {
        try {
            URL url = getClass().getResource(CLIP);
            if (url == null) {
                System.err.println("Sound resource not found: " + CLIP);
                return;
            }
            String uri = url.toExternalForm();
            coinSound = new MediaPlayer(new Media(uri));
            coinSound.setRate(COIN_RATE);

            deathSound = new MediaPlayer(new Media(uri));
            deathSound.setRate(DEATH_RATE);

            badgeSound = new MediaPlayer(new Media(uri));
            badgeSound.setRate(BADGE_RATE);
        } catch (Exception e) {
            System.err.println("Sound unavailable: " + CLIP + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Plays the pickup sound, respecting the current sound settings.
     */
    public void playCoin() {
        play(coinSound);
    }

    /**
     * Plays the low thud used when a run ends.
     */
    public void playDeath() {
        play(deathSound);
    }

    /**
     * Plays the bright chime used when a badge is unlocked.
     */
    public void playBadge() {
        play(badgeSound);
    }

    private void play(MediaPlayer player) {
        if (player == null) {
            return;
        }
        Settings settings = Settings.get();
        if (!settings.isSoundEnabled()) {
            return;
        }
        player.setVolume(settings.getVolume());
        player.stop();
        player.play();
    }

    /**
     * Stops any sound that is currently playing.
     */
    public void stop() {
        for (MediaPlayer player : new MediaPlayer[]{coinSound, deathSound, badgeSound}) {
            if (player != null) {
                player.stop();
            }
        }
    }
}

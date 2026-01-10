package anightdazingzoroark.riftlib.sounds;

import anightdazingzoroark.riftlib.util.MathUtils;

public class RiftLibSoundEffect {
    public final String soundEffectName;
    private float[] volumeRange = new float[]{1f};
    private float[] pitchRange = new float[]{1f};

    /**
     * A RiftLibSoundEffect class contains information associated with a sound effect meant
     * for use in animations. It is meant to be used in the effect param in sound_effects.
     *
     * @param name The name of the sound effect. Note that this does not have to be the same as the associated identifier when registering.
     */
    public RiftLibSoundEffect(String name) {
        this.soundEffectName = name;
    }

    /**
     * Set the volume as a singular value
     *
     * @param value The volume of the sound effect. By default, it's 1.
     */
    public RiftLibSoundEffect setVolume(float value) {
        this.volumeRange = new float[]{value};
        return this;
    }

    /**
     * Set the volume as a random value within a range.
     *
     * @param min The minimum value for the range for volume.
     * @param max The maximum value for the range for volume.
     */
    public RiftLibSoundEffect setVolume(float min, float max) {
        this.volumeRange = new float[]{min, max};
        return this;
    }

    public float getVolume() {
        if (this.volumeRange.length == 1) return this.volumeRange[0];
        else if (this.volumeRange.length == 2) return MathUtils.randomInRange(this.volumeRange[0], this.volumeRange[1]);
        return 1f;
    }

    /**
     * Set the pitch as a singular value
     *
     * @param value The pitch of the sound effect. By default, it's 1.
     */
    public RiftLibSoundEffect setPitch(float value) {
        this.pitchRange = new float[]{value};
        return this;
    }

    /**
     * Set the pitch as a random value within a range.
     *
     * @param min The minimum value for the pitch for volume.
     * @param max The maximum value for the pitch for volume.
     */
    public RiftLibSoundEffect setPitch(float min, float max) {
        this.pitchRange = new float[]{min, max};
        return this;
    }

    public float getPitch() {
        if (this.pitchRange.length == 1) return this.pitchRange[0];
        else if (this.pitchRange.length == 2) return MathUtils.randomInRange(this.pitchRange[0], this.pitchRange[1]);
        return 1f;
    }
}

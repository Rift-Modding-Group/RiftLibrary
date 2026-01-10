package anightdazingzoroark.riftlib.core.keyframe;

public class EventKeyFrame {
	private final Double startTick;

	public EventKeyFrame(Double startTick) {
		this.startTick = startTick;
	}

	public Double getStartTick() {
		return this.startTick;
	}

    public static class ParticleEventKeyFrame extends EventKeyFrame {
        public final String effect;
        public final String locator;
        public final String script;

        public ParticleEventKeyFrame(Double startTick, String effect, String locator, String script) {
            super(startTick);
            this.script = script;
            this.locator = locator;
            this.effect = effect;
        }
    }

    public static class SoundEventKeyFrame extends EventKeyFrame {
        public final String effect;
        public final String locator;

        public SoundEventKeyFrame(Double startTick, String effect, String locator) {
            super(startTick);
            this.locator = locator;
            this.effect = effect;
        }
    }
}

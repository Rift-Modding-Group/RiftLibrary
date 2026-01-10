package anightdazingzoroark.riftlib.core.keyframe;

public class ParticleEventKeyFrame extends EventKeyFrame {
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

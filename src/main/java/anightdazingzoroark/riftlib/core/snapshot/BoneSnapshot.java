package anightdazingzoroark.riftlib.core.snapshot;

import anightdazingzoroark.riftlib.core.processor.IBone;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Vector3f;

public class BoneSnapshot {
	public final String name;
	private final IBone modelRenderer;

	@NotNull
	private final Vector3f scale = new Vector3f(1f, 1f, 1f);

	@NotNull
	private final Vector3f position = new Vector3f();

	@NotNull
	private final Vector3f rotation = new Vector3f();

	public float mostRecentResetRotationTick = 0;
	public float mostRecentResetPositionTick = 0;
	public float mostRecentResetScaleTick = 0;

	public boolean isCurrentlyRunningRotationAnimation = true;
	public boolean isCurrentlyRunningPositionAnimation = true;
	public boolean isCurrentlyRunningScaleAnimation = true;

	public BoneSnapshot(IBone modelRenderer) {
		this.rotation.set(modelRenderer.getRotation());
		this.position.set(modelRenderer.getPosition());
		this.scale.set(modelRenderer.getScale());

		this.modelRenderer = modelRenderer;
		this.name = modelRenderer.getName();
	}

	public BoneSnapshot(IBone modelRenderer, boolean dontSaveRotations) {
		if (dontSaveRotations) this.rotation.set(0f, 0f, 0f);

		this.rotation.set(modelRenderer.getRotation());
		this.position.set(modelRenderer.getPosition());
		this.scale.set(modelRenderer.getScale());

		this.modelRenderer = modelRenderer;
		this.name = modelRenderer.getName();
	}

	public BoneSnapshot(BoneSnapshot snapshot) {
		this.scale.set(snapshot.getScale());
		this.position.set(snapshot.getPosition());
		this.rotation.set(snapshot.getRotation());

		this.modelRenderer = snapshot.modelRenderer;
		this.name = snapshot.name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BoneSnapshot that = (BoneSnapshot) o;
		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@NotNull
	public Vector3f getRotation() {
		return this.rotation;
	}

	@NotNull
	public Vector3f getPosition() {
		return this.position;
	}

	@NotNull
	public Vector3f getScale() {
		return this.scale;
	}
}

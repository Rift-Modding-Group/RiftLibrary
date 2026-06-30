package anightdazingzoroark.riftlib.core.processor;

import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Vector3f;

public interface IBone {
	@NotNull
	Vector3f getRotation();

	@NotNull
	Vector3f getPosition();

	@NotNull
	Vector3f getScale();

	@NotNull
	Vector3f getPivot();

	boolean isHidden();

	boolean cubesAreHidden();

	boolean childBonesAreHiddenToo();

	void setHidden(boolean hidden);

	void setCubesHidden(boolean hidden);

	void setHidden(boolean selfHidden, boolean skipChildRendering);

	void setModelRendererName(String modelRendererName);

	void saveInitialSnapshot();

	BoneSnapshot getInitialSnapshot();

	default BoneSnapshot saveSnapshot() {
		return new BoneSnapshot(this);
	}

	String getName();
}

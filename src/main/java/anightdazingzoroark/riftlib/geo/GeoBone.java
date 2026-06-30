package anightdazingzoroark.riftlib.geo;

import java.util.ArrayList;
import java.util.List;

import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Vector3f;

public class GeoBone implements IBone {
	public GeoBone parent;

	public List<GeoBone> childBones = new ArrayList<>();
	public List<GeoCube> childCubes = new ArrayList<>();
	public List<GeoLocator> childLocators = new ArrayList<>();

	public String name;
	private BoneSnapshot initialSnapshot;

	public Boolean mirror;
	public Double inflate;
	public Boolean dontRender;
	public boolean isHidden;
	public boolean areCubesHidden = false;
	public boolean hideChildBonesToo;

	@NotNull
	private final Vector3f scale = new Vector3f(1f, 1f, 1f);
	@NotNull
	private final Vector3f position = new Vector3f();
	@NotNull
	private final Vector3f pivot = new Vector3f();
	@NotNull
	private final Vector3f rotation = new Vector3f();

	public Object extraData;

	@Override
	public void setModelRendererName(String modelRendererName) {
		this.name = modelRendererName;
	}

	@Override
	public void saveInitialSnapshot() {
		if (this.initialSnapshot == null) {
			this.initialSnapshot = new BoneSnapshot(this, true);
		}
	}

	@Override
	public BoneSnapshot getInitialSnapshot() {
		return this.initialSnapshot;
	}

	@Override
	public String getName() {
		return this.name;
	}

	// Boilerplate code incoming

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

	@NotNull
	public Vector3f getPivot() {
		return this.pivot;
	}

	@Override
	public boolean isHidden() {
		return this.isHidden;
	}

	@Override
	public void setHidden(boolean hidden) {
		this.setHidden(hidden, hidden);
	}

	@Override
	public boolean cubesAreHidden() {
		return areCubesHidden;
	}

	@Override
	public boolean childBonesAreHiddenToo() {
		return hideChildBonesToo;
	}

	@Override
	public void setCubesHidden(boolean hidden) {
		this.areCubesHidden = hidden;
	}

	@Override
	public void setHidden(boolean selfHidden, boolean skipChildRendering) {
		this.isHidden = selfHidden;
		this.hideChildBonesToo = skipChildRendering;
	}
}

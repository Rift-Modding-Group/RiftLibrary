package anightdazingzoroark.riftlib.geo;

import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Vector3f;

/**
 * GeoLocator is collected from raw model info and is just raw locator information.
 * */
public class GeoLocator implements IBone {
    public final GeoBone parent;
    public String name;
    private BoneSnapshot initialSnapshot;
    @NotNull
    private final Vector3f position = new Vector3f();
    @NotNull
    private final Vector3f rotation = new Vector3f();
    private boolean isHidden;
    private boolean areCubesHidden;
    private boolean hideChildBonesToo;

    public GeoLocator(GeoBone parent, String name) {
        this.parent = parent;
        this.name = name;
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
        return new Vector3f(1f, 1f, 1f);
    }

    @NotNull
    public Vector3f getPivot() {
        return new Vector3f();
    }

    @Override
    public boolean isHidden() {
        return this.isHidden;
    }

    @Override
    public boolean cubesAreHidden() {
        return this.areCubesHidden;
    }

    @Override
    public boolean childBonesAreHiddenToo() {
        return this.hideChildBonesToo;
    }

    @Override
    public void setHidden(boolean hidden) {
        this.setHidden(hidden, hidden);
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
}

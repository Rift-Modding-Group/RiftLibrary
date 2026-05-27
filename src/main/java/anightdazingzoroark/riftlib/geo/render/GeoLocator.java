package anightdazingzoroark.riftlib.geo.render;

import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;

/**
 * GeoLocator is collected from raw model info and is just raw locator information.
 * */
public class GeoLocator implements IBone {
    public final GeoBone parent;
    public String name;
    private BoneSnapshot initialSnapshot;
    private float positionX;
    private float positionY;
    private float positionZ;
    private float rotationX;
    private float rotationY;
    private float rotationZ;
    private boolean isHidden;
    private boolean areCubesHidden;
    private boolean hideChildBonesToo;

    public GeoLocator(GeoBone parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    @Override
    public float getPositionX() {
        return this.positionX;
    }

    @Override
    public float getPositionY() {
        return this.positionY;
    }

    @Override
    public float getPositionZ() {
        return this.positionZ;
    }

    @Override
    public void setPositionX(float value) {
        this.positionX = value;
    }

    @Override
    public void setPositionY(float value) {
        this.positionY = value;
    }

    @Override
    public void setPositionZ(float value) {
        this.positionZ = value;
    }

    @Override
    public float getRotationX() {
        return this.rotationX;
    }

    @Override
    public float getRotationY() {
        return this.rotationY;
    }

    @Override
    public float getRotationZ() {
        return this.rotationZ;
    }

    @Override
    public void setRotationX(float value) {
        this.rotationX = value;
    }

    @Override
    public void setRotationY(float value) {
        this.rotationY = value;
    }

    @Override
    public void setRotationZ(float value) {
        this.rotationZ = value;
    }

    @Override
    public float getScaleX() {
        return 1;
    }

    @Override
    public float getScaleY() {
        return 1;
    }

    @Override
    public float getScaleZ() {
        return 1;
    }

    @Override
    public void setScaleX(float value) {}

    @Override
    public void setScaleY(float value) {}

    @Override
    public void setScaleZ(float value) {}

    @Override
    public void setPivotX(float value) {}

    @Override
    public void setPivotY(float value) {}

    @Override
    public void setPivotZ(float value) {}

    @Override
    public float getPivotX() {
        return 0;
    }

    @Override
    public float getPivotY() {
        return 0;
    }

    @Override
    public float getPivotZ() {
        return 0;
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

    @Override
    public String toString() {
        return "[name="+this.name+", offset=("+this.positionX+", "+this.positionY+", "+this.positionZ+")]";
    }
}

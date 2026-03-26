package anightdazingzoroark.riftlibrary.main.geo.basic;

import anightdazingzoroark.riftlibrary.main.core.snapshot.BoneSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RiftLibBone {
    public RiftLibBone parent;

    public List<RiftLibBone> childBones = new ArrayList<>();
    public List<RiftLibCube> childCubes = new ArrayList<>();
    public List<RiftLibLocator> childLocators = new ArrayList<>();

    public String name;
    private BoneSnapshot initialSnapshot;

    public Boolean mirror;
    public Float inflate;
    public Boolean dontRender;
    public boolean isHidden;
    public boolean areCubesHidden = false;
    public boolean hideChildBonesToo;
    // I still have no idea what this field does, but its in the json file so
    // ¯\_(ツ)_/¯
    public Boolean reset;

    private float scaleX = 1;
    private float scaleY = 1;
    private float scaleZ = 1;

    private float positionX;
    private float positionY;
    private float positionZ;

    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;

    private float rotateX;
    private float rotateY;
    private float rotateZ;

    public Object extraData;

    public void setModelRendererName(String modelRendererName) {
        this.name = modelRendererName;
    }

    public void saveInitialSnapshot() {
        if (this.initialSnapshot == null) {
            this.initialSnapshot = new BoneSnapshot(this, true);
        }
    }

    public BoneSnapshot getInitialSnapshot() {
        return this.initialSnapshot;
    }

    public String getName() {
        return this.name;
    }

    // Boilerplate code incoming
    public float getRotationX() {
        return rotateX;
    }

    public float getRotationY() {
        return rotateY;
    }

    public float getRotationZ() {
        return rotateZ;
    }

    public float getPositionX() {
        return positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public float getPositionZ() {
        return positionZ;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public float getScaleZ() {
        return scaleZ;
    }

    public void setRotationX(float value) {
        this.rotateX = value;
    }

    public void setRotationY(float value) {
        this.rotateY = value;
    }

    public void setRotationZ(float value) {
        this.rotateZ = value;
    }

    public void setPositionX(float value) {
        this.positionX = value;
    }

    public void setPositionY(float value) {
        this.positionY = value;
    }

    public void setPositionZ(float value) {
        this.positionZ = value;
    }

    public void setScaleX(float value) {
        this.scaleX = value;
    }

    public void setScaleY(float value) {
        this.scaleY = value;
    }

    public void setScaleZ(float value) {
        this.scaleZ = value;
    }

    public boolean isHidden() {
        return this.isHidden;
    }

    public void setHidden(boolean hidden) {
        this.setHidden(hidden, hidden);
    }

    public void setPivotX(float value) {
        this.rotationPointX = value;
    }

    public void setPivotY(float value) {
        this.rotationPointY = value;
    }

    public void setPivotZ(float value) {
        this.rotationPointZ = value;
    }

    public float getPivotX() {
        return this.rotationPointX;
    }

    public float getPivotY() {
        return this.rotationPointY;
    }

    public float getPivotZ() {
        return this.rotationPointZ;
    }

    public boolean cubesAreHidden() {
        return areCubesHidden;
    }

    public boolean childBonesAreHiddenToo() {
        return hideChildBonesToo;
    }

    public void setCubesHidden(boolean hidden) {
        this.areCubesHidden = hidden;
    }

    public void setHidden(boolean selfHidden, boolean skipChildRendering) {
        this.isHidden = selfHidden;
        this.hideChildBonesToo = skipChildRendering;
    }
}

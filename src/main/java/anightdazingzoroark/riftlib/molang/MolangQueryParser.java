package anightdazingzoroark.riftlib.molang;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.util.MiscUtils;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class MolangQueryParser {
    private final HashMap<String, BiFunction<AbstractAnimationData<?>, MolangParser, Double>> dataQueryMap = new HashMap<>();
    private final HashMap<String, BiFunction<AbstractAnimationData<?>, MolangParser, Double>> animatableQueryMap = new HashMap<>();

    public MolangQueryParser() {
        //-----animation data queries-----
        this.dataQueryMap.put("query.anim_time", (animData, parser) -> {
            return animData.animTime;
        });
        this.dataQueryMap.put("query.delta_time", (animData, parser) -> {
            return animData.deltaTime;
        });
        this.dataQueryMap.put("query.life_time", (animData, parser) -> {
            return animData.lifeTime;
        });
        //-----ianimatable queries-----
        this.animatableQueryMap.put("query.modified_distance_moved", (animData, parser) -> {
            if (!(animData instanceof AnimationDataEntity animDataEntity)) return 0D;
            double speed = animDataEntity.getHorizontalSpeed();

            //the reason why modified_distance_moved is calculated as such is because
            //getEntitySpeed returns the speed of an entity within the tick, which is
            //as good as by how many blocks it was displaced within a tick, which is
            //good enough to add upon for this query
            if (speed > 0) {
                double oldValue = parser.getVariable("query.modified_distance_moved").get();
                return oldValue + speed;
            }
            return 0D;
        });

        this.animatableQueryMap.put("query.actor_count", (animData, parser) -> {
            World world = Minecraft.getMinecraft().world;
            if (world == null) return 0D;
            return (double) world.getLoadedEntityList().size();
        });
        this.animatableQueryMap.put("query.time_of_day", (animData, parser) -> {
            World world = Minecraft.getMinecraft().world;
            if (world == null) return 0D;
            return world.getTotalWorldTime() / 24000D;
        });
        this.animatableQueryMap.put("query.moon_phase", (animData, parser) -> {
            World world = Minecraft.getMinecraft().world;
            if (world == null) return 0D;
            return (double) world.getMoonPhase();
        });
        this.animatableQueryMap.put("query.distance_from_camera", (animData, parser) -> {
            if (!(animData instanceof AnimationDataEntity animDataEntity)) return 0D;

            Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
            float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();

            if (camera == null) return 0D;

            Vec3d entityCamera = new Vec3d(
                    Interpolations.lerp(camera.prevPosX, camera.posX, partialTick),
                    Interpolations.lerp(camera.prevPosY, camera.posY, partialTick),
                    Interpolations.lerp(camera.prevPosZ, camera.posZ, partialTick)
            );
            Vec3d entityPosition = new Vec3d(
                    Interpolations.lerp(animDataEntity.getHolder().prevPosX, animDataEntity.getHolder().posX, partialTick),
                    Interpolations.lerp(animDataEntity.getHolder().prevPosY, animDataEntity.getHolder().posY, partialTick),
                    Interpolations.lerp(animDataEntity.getHolder().prevPosZ, animDataEntity.getHolder().posZ, partialTick)
            );
            return entityCamera.add(ActiveRenderInfo.getCameraPosition()).distanceTo(entityPosition);
        });
        this.animatableQueryMap.put("query.is_on_ground", (animData, parser) -> {
            if (!(animData instanceof AnimationDataEntity animDataEntity)) return 0D;
            return MolangUtils.booleanToDouble(animDataEntity.getHolder().onGround);
        });
        this.animatableQueryMap.put("query.is_in_water", (animData, parser) -> {
            if (!(animData instanceof AnimationDataEntity animDataEntity)) return 0D;
            return MolangUtils.booleanToDouble(animDataEntity.getHolder().isInWater());
        });
        this.animatableQueryMap.put("query.is_in_water_or_rain", (animData, parser) -> {
            if (!(animData instanceof AnimationDataEntity animDataEntity)) return 0D;
            return MolangUtils.booleanToDouble(animDataEntity.getHolder().isWet());
        });
        this.animatableQueryMap.put("query.health", (animData, parser) -> {
            if (!(animData instanceof AnimationDataEntity animDataEntity)) return 0D;
            return (double) animDataEntity.getHolder().getHealth();
        });
        this.animatableQueryMap.put("query.max_health", (animData, parser) -> {
            if (!(animData instanceof AnimationDataEntity animDataEntity)) return 0D;
            return (double) animDataEntity.getHolder().getMaxHealth();
        });
        this.animatableQueryMap.put("query.is_on_fire", (animData, parser) -> {
            if (!(animData instanceof AnimationDataEntity animDataEntity)) return 0D;
            return MolangUtils.booleanToDouble(animDataEntity.getHolder().isBurning());
        });
        this.animatableQueryMap.put("query.ground_speed", (animData, parser) -> {
            if (!(animData instanceof AnimationDataEntity animDataEntity)) return 0D;
            return MiscUtils.getEntityHorizontalSpeed(animDataEntity.getHolder());
        });
        this.animatableQueryMap.put("query.vertical_speed", (animData, parser) -> {
            if (!(animData instanceof AnimationDataEntity animDataEntity)) return 0D;
            return MiscUtils.getEntityVerticalSpeed(animDataEntity.getHolder());
        });
        this.animatableQueryMap.put("query.yaw_speed", (animData, parser) -> {
            if (!(animData instanceof AnimationDataEntity animDataEntity)) return 0D;
            float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
            float currentEntityYaw = Interpolations.lerpYaw(animDataEntity.getHolder().prevRotationYaw, animDataEntity.getHolder().rotationYaw, partialTick);
            float prevEntityYaw = Interpolations.lerpYaw(animDataEntity.getHolder().prevRotationYaw, animDataEntity.getHolder().rotationYaw, partialTick - 0.1f);
            return (double) (currentEntityYaw - prevEntityYaw);
        });
        this.animatableQueryMap.put("query.is_riding", (animData, parser) -> {
            if (!(animData instanceof AnimationDataEntity animDataEntity)) return 0D;
            return MolangUtils.booleanToDouble(animDataEntity.getHolder().isRiding());
        });
    }

    public void updateQueries(AbstractAnimationData<?> data, MolangParser parser, MolangScope scope) {
        parser.withScope(scope, () -> {
            for (Map.Entry<String, BiFunction<AbstractAnimationData<?>, MolangParser, Double>> entry : this.dataQueryMap.entrySet()) {
                parser.setValue(entry.getKey(), entry.getValue().apply(data, parser));
            }

            for (Map.Entry<String, BiFunction<AbstractAnimationData<?>, MolangParser, Double>> entry : this.animatableQueryMap.entrySet()) {
                parser.setValue(entry.getKey(), entry.getValue().apply(data, parser));
            }
        });
    }
}

package anightdazingzoroark.riftlib.molang;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
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
    private final HashMap<String, BiFunction<AnimationData, MolangParser, Double>> dataQueryMap = new HashMap<>();
    private final HashMap<String, BiFunction<IAnimatable, MolangParser, Double>> animatableQueryMap = new HashMap<>();

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
        this.animatableQueryMap.put("query.modified_distance_moved", (iAnimatable, parser) -> {
            if (!(iAnimatable instanceof Entity entity)) return 0D;
            double speed = MiscUtils.getEntitySpeed(entity);

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

        this.animatableQueryMap.put("query.actor_count", (iAnimatable, parser) -> {
            World world = Minecraft.getMinecraft().world;
            if (world == null) return 0D;
            return (double) world.getLoadedEntityList().size();
        });
        this.animatableQueryMap.put("query.time_of_day", (iAnimatable, parser) -> {
            World world = Minecraft.getMinecraft().world;
            if (world == null) return 0D;
            return world.getTotalWorldTime() / 24000D;
        });
        this.animatableQueryMap.put("query.moon_phase", (iAnimatable, parser) -> {
            World world = Minecraft.getMinecraft().world;
            if (world == null) return 0D;
            return (double) world.getMoonPhase();
        });
        this.animatableQueryMap.put("query.distance_from_camera", (iAnimatable, parser) -> {
            if (!(iAnimatable instanceof Entity entity)) return 0D;

            Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
            float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();

            if (camera == null) return 0D;

            Vec3d entityCamera = new Vec3d(
                    Interpolations.lerp(camera.prevPosX, camera.posX, partialTick),
                    Interpolations.lerp(camera.prevPosY, camera.posY, partialTick),
                    Interpolations.lerp(camera.prevPosZ, camera.posZ, partialTick)
            );
            Vec3d entityPosition = new Vec3d(
                    Interpolations.lerp(entity.prevPosX, entity.posX, partialTick),
                    Interpolations.lerp(entity.prevPosY, entity.posY, partialTick),
                    Interpolations.lerp(entity.prevPosZ, entity.posZ, partialTick)
            );
            return entityCamera.add(ActiveRenderInfo.getCameraPosition()).distanceTo(entityPosition);
        });
        this.animatableQueryMap.put("query.is_on_ground", (iAnimatable, parser) -> {
            if (!(iAnimatable instanceof Entity entity)) return 0D;
            return MolangUtils.booleanToDouble(entity.onGround);
        });
        this.animatableQueryMap.put("query.is_in_water", (iAnimatable, parser) -> {
            if (!(iAnimatable instanceof Entity entity)) return 0D;
            return MolangUtils.booleanToDouble(entity.isInWater());
        });
        this.animatableQueryMap.put("query.is_in_water_or_rain", (iAnimatable, parser) -> {
            if (!(iAnimatable instanceof Entity entity)) return 0D;
            return MolangUtils.booleanToDouble(entity.isWet());
        });
        this.animatableQueryMap.put("query.health", (iAnimatable, parser) -> {
            if (!(iAnimatable instanceof EntityLivingBase entity)) return 0D;
            return (double) entity.getHealth();
        });
        this.animatableQueryMap.put("query.max_health", (iAnimatable, parser) -> {
            if (!(iAnimatable instanceof EntityLivingBase entity)) return 0D;
            return (double) entity.getMaxHealth();
        });
        this.animatableQueryMap.put("query.is_on_fire", (iAnimatable, parser) -> {
            if (!(iAnimatable instanceof Entity entity)) return 0D;
            return MolangUtils.booleanToDouble(entity.isBurning());
        });
        this.animatableQueryMap.put("query.ground_speed", (iAnimatable, parser) -> {
            if (!(iAnimatable instanceof Entity entity)) return 0D;
            return MiscUtils.getEntitySpeed(entity);
        });
        this.animatableQueryMap.put("query.yaw_speed", (iAnimatable, parser) -> {
            if (!(iAnimatable instanceof EntityLivingBase entity)) return 0D;
            float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
            float currentEntityYaw = Interpolations.lerpYaw(entity.prevRotationYaw, entity.rotationYaw, partialTick);
            float prevEntityYaw = Interpolations.lerpYaw(entity.prevRotationYaw, entity.rotationYaw, partialTick - 0.1f);
            return (double) (currentEntityYaw - prevEntityYaw);
        });
    }

    public void updateQueries(AnimationData data, MolangParser parser, MolangScope scope) {
        parser.withScope(scope, () -> {
            for (Map.Entry<String, BiFunction<AnimationData, MolangParser, Double>> entry : this.dataQueryMap.entrySet()) {
                parser.setValue(entry.getKey(), entry.getValue().apply(data, parser));
            }

            for (Map.Entry<String, BiFunction<IAnimatable, MolangParser, Double>> entry : this.animatableQueryMap.entrySet()) {
                parser.setValue(entry.getKey(), entry.getValue().apply(data.iAnimatable, parser));
            }
        });
    }
}

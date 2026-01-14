package anightdazingzoroark.riftlib.molang;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MolangQueryValue {
    private static final HashMap<String, Function<IAnimatable, Double>> molangQueryMap = new HashMap<>();

    public static void updateMolangQueryValues(MolangParser parser, IAnimatable animatable) {
        Set<Map.Entry<String, Function<IAnimatable, Double>>> molangQueryEntrySet = molangQueryMap.entrySet();
        for (Map.Entry<String, Function<IAnimatable, Double>> entry : molangQueryEntrySet) {
            parser.setValue(entry.getKey(), entry.getValue().apply(animatable));
        }
    }

    //one thing to note is that anim time is special because it requires
    //the animation tick to work properly, hence, this function, instead of
    //being part of molangQueryMap
    public static void setAnimTime(MolangParser parser, double value) {
        parser.setValue("query.anim_time", value);
    }

    public static void registerMolangQueries() {
        molangQueryMap.put("query.actor_count", new Function<IAnimatable, Double>() {
            @Override
            public Double apply(IAnimatable animatable) {
                World world = Minecraft.getMinecraft().world;
                if (world == null) return 0D;
                return (double) world.getLoadedEntityList().size();
            }
        });
        molangQueryMap.put("query.time_of_day", new Function<IAnimatable, Double>() {
            @Override
            public Double apply(IAnimatable animatable) {
                World world = Minecraft.getMinecraft().world;
                if (world == null) return 0D;
                return world.getTotalWorldTime() / 24000D;
            }
        });
        molangQueryMap.put("query.moon_phase", new Function<IAnimatable, Double>() {
            @Override
            public Double apply(IAnimatable animatable) {
                World world = Minecraft.getMinecraft().world;
                if (world == null) return 0D;
                return (double) world.getMoonPhase();
            }
        });
        molangQueryMap.put("query.distance_from_camera", new Function<IAnimatable, Double>() {
            @Override
            public Double apply(IAnimatable animatable) {
                if (!(animatable instanceof Entity)) return 0D;

                Entity entity = (Entity) animatable;
                Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
                float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();

                if (camera == null) return 0D;

                Vec3d entityCamera = new Vec3d(camera.prevPosX + (camera.posX - camera.prevPosX) * partialTick,
                        camera.prevPosY + (camera.posY - camera.prevPosY) * partialTick,
                        camera.prevPosZ + (camera.posZ - camera.prevPosZ) * partialTick);
                Vec3d entityPosition = new Vec3d(entity.prevPosX + (entity.posX - entity.prevPosX) * partialTick,
                        entity.prevPosY + (entity.posY - entity.prevPosY) * partialTick,
                        entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTick);
                return entityCamera.add(ActiveRenderInfo.getCameraPosition()).distanceTo(entityPosition);
            }
        });
        molangQueryMap.put("query.is_on_ground", new Function<IAnimatable, Double>() {
            @Override
            public Double apply(IAnimatable animatable) {
                if (!(animatable instanceof Entity)) return 0D;

                Entity entity = (Entity) animatable;
                return entity.onGround ? 1D : 0D;
            }
        });
        molangQueryMap.put("query.is_in_water", new Function<IAnimatable, Double>() {
            @Override
            public Double apply(IAnimatable animatable) {
                if (!(animatable instanceof Entity)) return 0D;

                Entity entity = (Entity) animatable;
                return MolangUtils.booleanToDouble(entity.isInWater());
            }
        });
        molangQueryMap.put("query.is_in_water_or_rain", new Function<IAnimatable, Double>() {
            @Override
            public Double apply(IAnimatable animatable) {
                if (!(animatable instanceof Entity)) return 0D;

                Entity entity = (Entity) animatable;
                return MolangUtils.booleanToDouble(entity.isWet());
            }
        });
        molangQueryMap.put("query.health", new Function<IAnimatable, Double>() {
            @Override
            public Double apply(IAnimatable animatable) {
                if (!(animatable instanceof EntityLivingBase)) return 0D;

                EntityLivingBase entity = (EntityLivingBase) animatable;
                return (double) entity.getHealth();
            }
        });
        molangQueryMap.put("query.max_health", new Function<IAnimatable, Double>() {
            @Override
            public Double apply(IAnimatable animatable) {
                if (!(animatable instanceof EntityLivingBase)) return 0D;

                EntityLivingBase entity = (EntityLivingBase) animatable;
                return (double) entity.getMaxHealth();
            }
        });
        molangQueryMap.put("query.is_on_fire", new Function<IAnimatable, Double>() {
            @Override
            public Double apply(IAnimatable animatable) {
                if (!(animatable instanceof EntityLivingBase)) return 0D;

                EntityLivingBase entity = (EntityLivingBase) animatable;
                return entity.isBurning() ? 1D : 0D;
            }
        });
        molangQueryMap.put("query.ground_speed", new Function<IAnimatable, Double>() {
            @Override
            public Double apply(IAnimatable animatable) {
                if (!(animatable instanceof EntityLivingBase)) return 0D;

                EntityLivingBase entity = (EntityLivingBase) animatable;
                double dx = entity.motionX;
                double dz = entity.motionZ;
                return (double) MathHelper.sqrt((dx * dx) + (dz * dz));
            }
        });
        molangQueryMap.put("query.yaw_speed", new Function<IAnimatable, Double>() {
            @Override
            public Double apply(IAnimatable animatable) {
                if (!(animatable instanceof EntityLivingBase)) return 0D;

                EntityLivingBase entity = (EntityLivingBase) animatable;
                float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();

                float currentEntityYaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTick;
                float prevEntityYaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * (partialTick - 0.1f);
                return (double) (currentEntityYaw - prevEntityYaw);
            }
        });
    }
}

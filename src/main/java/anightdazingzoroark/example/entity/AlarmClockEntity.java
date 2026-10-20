package anightdazingzoroark.example.entity;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityNonLiving;
import anightdazingzoroark.riftlib.util.MathUtils;
import anightdazingzoroark.example.registry.ItemRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class AlarmClockEntity extends Entity implements IAnimatable<AnimationDataEntityNonLiving> {
    @NotNull
    private final AnimationDataEntityNonLiving animationData = new AnimationDataEntityNonLiving(this);

    public AlarmClockEntity(@NotNull World worldIn) {
        super(worldIn);
        this.setSize(2f, 3f);
        this.preventEntitySpawning = true;
    }

    @Override
    protected void entityInit() {}

    // Position, rotation, and motion are already persisted by Entity.
    @Override
    protected void readEntityFromNBT(@NotNull NBTTagCompound compound) {}

    @Override
    protected void writeEntityToNBT(@NotNull NBTTagCompound compound) {}

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.hasNoGravity()) this.motionY -= 0.08D;
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionY *= 0.98D;
        float friction = this.onGround ? 0.546f : 0.91f;
        this.motionX *= friction;
        this.motionZ *= friction;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @Override
    public boolean canBePushed() {
        return !this.isDead;
    }

    @Override
    @NotNull
    public ItemStack getPickedResult(@NotNull RayTraceResult target) {
        return new ItemStack(ItemRegistry.ALARM_CLOCK);
    }

    @Override
    public boolean attackEntityFrom(@NotNull DamageSource source, float amount) {
        if (this.isDead || amount <= 0f || this.isEntityInvulnerable(source)) return false;
        if (this.world.isRemote) return true;

        this.setDead();
        boolean creativeAttacker = source.getTrueSource() instanceof EntityPlayer player && player.capabilities.isCreativeMode;
        if (!creativeAttacker && this.world.getGameRules().getBoolean("doEntityDrops")) {
            this.entityDropItem(new ItemStack(ItemRegistry.ALARM_CLOCK), 0f);
        }
        return true;
    }

    @Override
    public void initializeAnimationData(@NonNull AnimationDataEntityNonLiving animationData) {
        animationData.addAnimationController(new AnimationController<AlarmClockEntity, AnimationDataEntityNonLiving>(
                this, "clock", "default",
                new AnimationControllerState<AnimationDataEntityNonLiving>("default")
                        .addAnimation("animation.alarm_clock.hour_rotation")
        ));

        animationData.addInitAnimationValue("hour_rotation", MathUtils.randomInRange(0D, 360D));
        animationData.addInitAnimationValue("minute_rotation", MathUtils.randomInRange(0D, 360D));

        animationData.addOnUpdateAnimationValue("minute_rotation", () -> animationData.getVariable("minute_rotation") + 1);
    }

    @Override
    @NotNull
    public AnimationDataEntityNonLiving getAnimationData() {
        return this.animationData;
    }
}

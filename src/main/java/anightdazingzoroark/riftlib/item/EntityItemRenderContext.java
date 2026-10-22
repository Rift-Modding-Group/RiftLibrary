package anightdazingzoroark.riftlib.item;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Supplies the owner of an item rendered at an entity attachment, including nonliving entities.
 * */
public class EntityItemRenderContext implements AutoCloseable {
    @NotNull
    private static final ThreadLocal<EntityItemRenderContext> current = new ThreadLocal<>();
    @NotNull
    private final Entity entity;
    @NotNull
    private final String attachment;
    @Nullable
    private final EntityItemRenderContext previous;

    public EntityItemRenderContext(@NotNull Entity entity, @NotNull String attachment) {
        this.entity = entity;
        this.attachment = attachment;
        this.previous = current.get();
        current.set(this);
    }

    @Nullable
    public static EntityItemRenderContext getCurrent() {
        return current.get();
    }

    @NotNull
    public Entity getEntity() {
        return this.entity;
    }

    @NotNull
    public String getAttachment() {
        return this.attachment;
    }

    @Override
    public void close() {
        if (this.previous == null) current.remove();
        else current.set(this.previous);
    }
}

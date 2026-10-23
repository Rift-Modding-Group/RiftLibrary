package anightdazingzoroark.riftlib.model;

import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.keyframe.EventKeyFrame;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.molang.expressions.MolangExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads offense-hitbox lifetimes without running effects on the live creature.
 * TODO: something better should be done in place of this
 * */
public class AnimationHitboxTimeline extends MolangParser {
    @NotNull
    private final List<AnimatedBoundingBox> hitboxes;
    @NotNull
    private final Map<AnimatedBoundingBox, Double> activeSince = new HashMap<>();
    @NotNull
    private final List<HitboxWindow> windows = new ArrayList<>();
    private Animation animation;
    private double tick;

    private AnimationHitboxTimeline(@NotNull List<AnimatedBoundingBox> hitboxes) {
        this.hitboxes = hitboxes;
        // These handlers only change preview state. In particular, phase-completion messages must not fire.
        this.registerFunction("function.send_message", 1, (arguments, data) -> 0D);
        this.registerFunction("function.create_offense_hitbox_by_tag", 1,
                (arguments, data) -> this.setActive(arguments[0].getString(), true, true));
        this.registerFunction("function.destroy_offense_hitbox_by_tag", 1,
                (arguments, data) -> this.setActive(arguments[0].getString(), true, false));
        this.registerFunction("function.create_offense_hitbox_by_name", 1,
                (arguments, data) -> this.setActive(arguments[0].getString(), false, true));
        this.registerFunction("function.destroy_offense_hitbox_by_name", 1,
                (arguments, data) -> this.setActive(arguments[0].getString(), false, false));
    }

    /**
     * Animations are in execution order; active hitboxes carry into the next animation.
     * */
    @NotNull
    public static List<HitboxWindow> getHitboxWindows(
            @NotNull List<Animation> animations, @NotNull List<AnimatedBoundingBox> hitboxes,
            @NotNull AbstractAnimationData<?, ?> data
    ) {
        AnimationHitboxTimeline timeline = new AnimationHitboxTimeline(hitboxes);
        MolangScope scope = new MolangScope();
        scope.replaceValues(data.getDataScope().getValues());
        for (Animation animation : animations) {
            timeline.animation = animation;
            timeline.activeSince.replaceAll((hitbox, start) -> 0D);
            double endTick = Math.max(0D, animation.animationLength);
            for (EventKeyFrame.CustomInstructionKeyFrame event : animation.customInstructionKeyFrames.stream()
                    .sorted(Comparator.comparingDouble(EventKeyFrame::getStartTick)).toList()) {
                if (event.getStartTick() < 0D || event.getStartTick() > endTick) continue;
                timeline.tick = event.getStartTick();
                try {
                    MolangExpression instruction = timeline.parseExpression(event.instruction);
                    timeline.withScope(scope, () -> { instruction.get(); });
                }
                catch (MolangException exception) {
                    // If a script cannot be predicted, do not report a potentially incorrect reach.
                    return List.of();
                }
            }
            for (Map.Entry<AnimatedBoundingBox, Double> active : timeline.activeSince.entrySet()) {
                if (endTick > active.getValue()) {
                    timeline.windows.add(new HitboxWindow(animation, active.getKey(), active.getValue(), endTick));
                }
            }
        }
        return List.copyOf(timeline.windows);
    }

    private double setActive(@NotNull String name, boolean byTag, boolean active) {
        boolean changed = false;
        for (AnimatedBoundingBox hitbox : this.hitboxes) {
            boolean matches;
            if (byTag) matches = Arrays.asList(hitbox.getTags()).contains(name);
            else matches = hitbox.getName().equals(name);
            if (!matches) continue;
            if (active) changed |= this.activeSince.putIfAbsent(hitbox, this.tick) == null;
            else {
                Double start = this.activeSince.remove(hitbox);
                if (start != null) {
                    changed = true;
                    if (this.tick > start) this.windows.add(new HitboxWindow(this.animation, hitbox, start, this.tick));
                }
            }
        }
        return changed ? 1D : 0D;
    }

    public record HitboxWindow(@NotNull Animation animation, @NotNull AnimatedBoundingBox hitbox, double startTick, double endTick) {}
}

package anightdazingzoroark.riftlib.jsonParsing.builder;

import anightdazingzoroark.riftlib.hitboxLogic.HitboxDefinitionList;
import anightdazingzoroark.riftlib.jsonParsing.raw.hitbox.RawHitboxDefinition;
import anightdazingzoroark.riftlib.util.HitboxUtils;

import java.util.ArrayList;
import java.util.List;

public class HitboxBuilder {
    public static HitboxDefinitionList createHitboxDefinitionList(RawHitboxDefinition rawHitboxDefinition) {
        HitboxDefinitionList toReturn = new HitboxDefinitionList();

        for (RawHitboxDefinition.RawHitbox rawHitbox : rawHitboxDefinition.hitboxes) {
            HitboxDefinitionList.HitboxDefinition hitboxDefinition = new HitboxDefinitionList.HitboxDefinition(
                    HitboxUtils.locatorHitboxToHitbox(rawHitbox.locator),
                    rawHitbox.width,
                    rawHitbox.height,
                    rawHitbox.damageMultiplier != null ? rawHitbox.damageMultiplier : 1f,
                    rawHitbox.affectedByAnim != null ? rawHitbox.affectedByAnim : true,
                    createHitboxDamageDefinition(rawHitbox.damageDefinitions)
            );
            toReturn.list.add(hitboxDefinition);
        }

        return toReturn;
    }

    private static List<HitboxDefinitionList.HitboxDamageDefinition> createHitboxDamageDefinition(RawHitboxDefinition.RawHitboxDamageDefinition[] rawHitboxDamageDefinitions) {
        List<HitboxDefinitionList.HitboxDamageDefinition> toReturn = new ArrayList<>();
        if (rawHitboxDamageDefinitions != null) {
            for (RawHitboxDefinition.RawHitboxDamageDefinition rawHitboxDamageDefinition : rawHitboxDamageDefinitions) {
                toReturn.add(new HitboxDefinitionList.HitboxDamageDefinition(
                        rawHitboxDamageDefinition.damageSource,
                        rawHitboxDamageDefinition.damageType,
                        rawHitboxDamageDefinition.damageMultiplier != null ? rawHitboxDamageDefinition.damageMultiplier : 1f
                ));
            }
        }
        return toReturn;
    }
}

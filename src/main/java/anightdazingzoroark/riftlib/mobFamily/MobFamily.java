package anightdazingzoroark.riftlib.mobFamily;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MobFamily {
    private String name = "";
    private final List<String> familyMembers = new ArrayList<>();

    public MobFamily() {}

    public MobFamily(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getFamilyMembers() {
        return new ArrayList<>(this.familyMembers);
    }

    //this is to only accept ids of entities
    public MobFamily addToFamilyMembers(String... entityIDs) {
        Collections.addAll(this.familyMembers, entityIDs);
        return this;
    }

    public MobFamily addToFamilyMembers(String entityID) {
        if (!this.familyMembers.contains(entityID)) this.familyMembers.add(entityID);
        return this;
    }

    public void removeFromFamilyMembers(String entityID) {
        this.familyMembers.remove(entityID);
    }
}

package anightdazingzoroark.riftlib.mobFamily;

import java.util.ArrayList;
import java.util.List;

public class MobFamily {
    private final String name;
    private final List<String> familyMembers = new ArrayList<>();

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
    public void addToFamilyMembers(String entityId) {
        this.familyMembers.add(entityId);
    }
}

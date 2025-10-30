package anightdazingzoroark.riftlib.mobFamily;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MobFamilyCreator {
    private static final List<MobFamilyManager> ALL_MANAGERS = new ArrayList<>();

    public static MobFamilyManager createManager(File configDir, String relativePath) {
        File configFile = new File(configDir, relativePath);
        MobFamilyManager manager = new MobFamilyManager(configFile);
        ALL_MANAGERS.add(manager);
        return manager;
    }

    public static List<MobFamilyManager> getAllManagers() {
        return Collections.unmodifiableList(ALL_MANAGERS);
    }

    //get all registered mob families
    public static List<MobFamily> getAllFamilies() {
        return ALL_MANAGERS.stream()
                .flatMap(m -> m.getFamilies().stream())
                .collect(Collectors.toList());
    }

    public static void addMembersToFamily(String familyId, String... members) {
        for (MobFamilyManager manager : ALL_MANAGERS) {
            boolean changed = manager.addMembersToFamily(familyId, members);
            if (changed) manager.save();
        }
    }

    public static void addMembersToFamily(String familyId, String member) {
        for (MobFamilyManager manager : ALL_MANAGERS) {
            boolean changed = manager.addMembersToFamily(familyId, member);
            if (changed) manager.save();
        }
    }
}

package anightdazingzoroark.riftlib.mobFamily;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MobFamilyManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<MobFamily>>(){}.getType();

    private final File configFile;
    private final List<MobFamily> MOB_FAMILIES_TO_ADD = new ArrayList<>();
    private final List<MobFamily> MOB_FAMILIES = new ArrayList<>();

    public MobFamilyManager(File configFile) {
        this.configFile = configFile;
    }

    //add
    public void addMobFamily(MobFamily family) {
        MOB_FAMILIES_TO_ADD.add(family);
    }

    //load
    public void load() {
        MOB_FAMILIES.clear();

        if (!configFile.exists()) {
            MOB_FAMILIES.addAll(MOB_FAMILIES_TO_ADD);
            this.save();
            return;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            List<MobFamily> loaded = GSON.fromJson(reader, LIST_TYPE);
            if (loaded != null) {
                MOB_FAMILIES.addAll(loaded);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            MOB_FAMILIES.addAll(MOB_FAMILIES_TO_ADD);
        }
    }

    //save families to json
    public void save() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
            GSON.toJson(MOB_FAMILIES, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<MobFamily> getFamilies() {
        return Collections.unmodifiableList(MOB_FAMILIES);
    }
}

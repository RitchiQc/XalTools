package me.serbob.xaltools.hooks;

import me.serbob.xaltools.api.permission.PermissionHook;
import me.serbob.commons.utils.java.JavaVersionUtil;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HooksLoader {

    public enum LoadingStrategy {
        LOAD_ALL_COMPATIBLE,
        LOAD_EXACT_ONLY,
        LOAD_BEST_MATCH
    }

    private static final LoadingStrategy STRATEGY = LoadingStrategy.LOAD_ALL_COMPATIBLE;

    public static List<Map.Entry<String, Class<? extends PermissionHook>>> loadHooks() {
        List<Map.Entry<String, Class<? extends PermissionHook>>> allHooks = new ArrayList<>();

        switch (STRATEGY) {
            case LOAD_ALL_COMPATIBLE:
                allHooks = loadAllCompatibleHooks();
                break;
            case LOAD_EXACT_ONLY:
                allHooks = loadExactMatchOnly();
                break;
            case LOAD_BEST_MATCH:
                allHooks = loadBestMatch();
                break;
        }

        return allHooks;
    }

    private static List<Map.Entry<String, Class<? extends PermissionHook>>> loadAllCompatibleHooks() {
        List<Map.Entry<String, Class<? extends PermissionHook>>> allHooks = new ArrayList<>();
        int javaVersion = JavaVersionUtil.getCurrentJavaVersion();
        int[] availableVersions = {8, 11, 16, 17, 21};

        List<Integer> loadedVersions = new ArrayList<>();

        System.out.println("Loading all hooks compatible with Java " + javaVersion + "...");

        for (int version : availableVersions) {
            if (version <= javaVersion) {
                List<Map.Entry<String, Class<? extends PermissionHook>>> versionHooks = loadHooksForVersion(version);
                if (!versionHooks.isEmpty()) {
                    allHooks.addAll(versionHooks);
                    loadedVersions.add(version);
                }
            }
        }

        if (loadedVersions.isEmpty()) {
            System.err.println("No compatible HooksJava found for Java " + javaVersion);
        } else {
            System.out.println("Loaded " + loadedVersions.size() + " hook versions: " + loadedVersions);
            System.out.println("Total permission hooks collected: " + allHooks.size());
        }

        return allHooks;
    }

    private static List<Map.Entry<String, Class<? extends PermissionHook>>> loadExactMatchOnly() {
        int javaVersion = JavaVersionUtil.getCurrentJavaVersion();
        List<Map.Entry<String, Class<? extends PermissionHook>>> hooks = loadHooksForVersion(javaVersion);

        if (hooks.isEmpty()) {
            System.err.println("No HooksJava" + javaVersion + " found");
        }

        return hooks;
    }

    private static List<Map.Entry<String, Class<? extends PermissionHook>>> loadBestMatch() {
        int javaVersion = JavaVersionUtil.getCurrentJavaVersion();

        List<Map.Entry<String, Class<? extends PermissionHook>>> hooks = loadHooksForVersion(javaVersion);
        if (!hooks.isEmpty()) {
            return hooks;
        }

        int[] availableVersions = {21, 17, 16, 11, 8};
        for (int version : availableVersions) {
            if (version <= javaVersion) {
                hooks = loadHooksForVersion(version);
                if (!hooks.isEmpty()) {
                    System.out.println("Loaded hooks for Java " + version + " (running on Java " + javaVersion + ")");
                    return hooks;
                }
            }
        }

        System.err.println("No compatible HooksJava found for Java " + javaVersion);
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private static List<Map.Entry<String, Class<? extends PermissionHook>>> loadHooksForVersion(
            int version
    ) {
        List<Map.Entry<String, Class<? extends PermissionHook>>> hooks = new ArrayList<>();

        try {
            String className = "me.serbob.xaltools.HooksJava" + version;

            Class<?> hooksClass = Class.forName(className);

            Object hooksInstance = hooksClass.getDeclaredConstructor().newInstance();

            Method getPermissionHooksMethod = hooksClass.getMethod("getPermissionHooks");
            Map<String, Class<? extends PermissionHook>> permissionHooks =
                    (Map<String, Class<? extends PermissionHook>>) getPermissionHooksMethod.invoke(hooksInstance);

            if (permissionHooks != null && !permissionHooks.isEmpty()) {
                hooks.addAll(permissionHooks.entrySet());
                System.out.println("  Added " + permissionHooks.size() + " permission hooks from Java " + version);
            }

            return hooks;

        } catch (ClassNotFoundException e) {
            return hooks;
        } catch (Exception e) {
            System.err.println("Error loading hooks for Java " + version + ": " + e.getMessage());
            e.printStackTrace();
            return hooks;
        }
    }
}

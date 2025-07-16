package com.alexetrey.region.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegionFlags {
    public enum FlagState {
        EVERYONE,
        WHITELIST,
        NONE
    }

    public interface IFlag {
        String getName();
    }

    public enum Flag implements IFlag {
        BLOCK_BREAK("block-break"),
        BLOCK_PLACE("block-place"),
        INTERACT("interact"),
        ENTITY_DAMAGE("entity-damage");

        private final String name;

        Flag(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        public static IFlag fromName(String name) {
            for (Flag flag : values()) {
                if (flag.name.equalsIgnoreCase(name)) {
                    return flag;
                }
            }
            return CustomFlagRegistry.getFlag(name);
        }
    }

    public static class CustomFlag implements IFlag {
        private final String name;

        public CustomFlag(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            CustomFlag that = (CustomFlag) obj;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    public static class CustomFlagRegistry {
        private static final Map<String, IFlag> customFlags = new ConcurrentHashMap<>();

        public static IFlag registerCustomFlag(String name) {
            CustomFlag customFlag = new CustomFlag(name);
            customFlags.put(name.toLowerCase(), customFlag);
            return customFlag;
        }

        public static IFlag getFlag(String name) {
            return customFlags.get(name.toLowerCase());
        }

        public static Map<String, IFlag> getCustomFlags() {
            return new HashMap<>(customFlags);
        }

        public static void unregisterCustomFlag(String name) {
            customFlags.remove(name.toLowerCase());
        }
    }

    private final Map<IFlag, FlagState> flags;

    public RegionFlags() {
        this.flags = new HashMap<>();
        flags.put(Flag.BLOCK_BREAK, FlagState.NONE);
        flags.put(Flag.BLOCK_PLACE, FlagState.NONE);
        flags.put(Flag.INTERACT, FlagState.NONE);
        flags.put(Flag.ENTITY_DAMAGE, FlagState.NONE);
        
        for (IFlag customFlag : CustomFlagRegistry.getCustomFlags().values()) {
            flags.put(customFlag, FlagState.NONE);
        }
    }

    public void setFlag(IFlag flag, FlagState state) {
        flags.put(flag, state);
    }

    public FlagState getFlag(IFlag flag) {
        return flags.getOrDefault(flag, FlagState.NONE);
    }

    public boolean isAllowed(IFlag flag, boolean isWhitelisted) {
        FlagState state = getFlag(flag);
        return switch (state) {
            case EVERYONE -> true;
            case WHITELIST -> isWhitelisted;
            case NONE -> false;
        };
    }

    public Map<IFlag, FlagState> getAllFlags() {
        return new HashMap<>(flags);
    }

    public void setAllFlags(Map<IFlag, FlagState> flags) {
        this.flags.clear();
        this.flags.putAll(flags);
    }

    public static IFlag[] getAllAvailableFlags() {
        Map<String, IFlag> customFlags = CustomFlagRegistry.getCustomFlags();
        IFlag[] allFlags = new IFlag[Flag.values().length + customFlags.size()];
        
        int index = 0;
        for (Flag flag : Flag.values()) {
            allFlags[index++] = flag;
        }
        for (IFlag customFlag : customFlags.values()) {
            allFlags[index++] = customFlag;
        }
        
        return allFlags;
    }
} 
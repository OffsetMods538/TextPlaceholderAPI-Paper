package eu.pb4.placeholders.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class LoaderUtil {
    public static final boolean IS_DEV = FabricLoader.getInstance().isDevelopmentEnvironment();
}

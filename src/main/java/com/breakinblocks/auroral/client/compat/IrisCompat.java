package com.breakinblocks.auroral.client.compat;

import com.breakinblocks.auroral.Auroral;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@OnlyIn(Dist.CLIENT)
public final class IrisCompat {

    private static final boolean IRIS_LOADED = ModList.get().isLoaded("iris");

    private static Object irisApiInstance;
    private static MethodHandle isShaderPackInUseHandle;
    private static MethodHandle isRenderingShadowPassHandle;
    private static boolean reflectionAttempted = false;
    private static boolean reflectionAvailable = false;

    private IrisCompat() {}

    private static synchronized void ensureReflectionInitialized() {
        if (reflectionAttempted) return;
        reflectionAttempted = true;
        if (!IRIS_LOADED) return;

        try {
            Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();

            MethodHandle getInstance = lookup.findStatic(apiClass, "getInstance", MethodType.methodType(apiClass));
            irisApiInstance = getInstance.invoke();

            isShaderPackInUseHandle = lookup.findVirtual(apiClass, "isShaderPackInUse", MethodType.methodType(boolean.class));
            isRenderingShadowPassHandle = lookup.findVirtual(apiClass, "isRenderingShadowPass", MethodType.methodType(boolean.class));

            reflectionAvailable = true;
            Auroral.LOGGER.debug("Iris compat: API bound");
        } catch (Throwable t) {
            Auroral.LOGGER.debug("Iris compat: API unavailable ({})", t.toString());
        }
    }

    public static boolean isShaderPackInUse() {
        ensureReflectionInitialized();
        if (!reflectionAvailable) return false;
        try {
            return (boolean) isShaderPackInUseHandle.invoke(irisApiInstance);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isRenderingShadowPass() {
        ensureReflectionInitialized();
        if (!reflectionAvailable) return false;
        try {
            return (boolean) isRenderingShadowPassHandle.invoke(irisApiInstance);
        } catch (Throwable t) {
            return false;
        }
    }
}

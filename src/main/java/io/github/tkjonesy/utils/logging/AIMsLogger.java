package io.github.tkjonesy.utils.logging;

import io.github.tkjonesy.frontend.models.DebugConsoleManager;
import io.github.tkjonesy.utils.settings.ProgramSettings;
import lombok.Setter;

public class AIMsLogger {

    @Setter
    private static boolean debugModeEnabled = ProgramSettings.getCurrentSettings().isDebugMode();

    public static void trace(String m){
        if(debugModeEnabled) DebugConsoleManager.info("[TRACE] " + m);
    }

    public static void info(String m){
        if(debugModeEnabled) DebugConsoleManager.info("[INFO] " + m);
    }

    public static void warn(String m){
        if(debugModeEnabled) DebugConsoleManager.info("[WARN] " + m);
    }

    public static void error(String m) {
        if(debugModeEnabled) DebugConsoleManager.error("[ERROR] " + m);
    }

    public static void fatal(String m) {
        if(debugModeEnabled) DebugConsoleManager.error("[FATAL] " + m);
    }
}

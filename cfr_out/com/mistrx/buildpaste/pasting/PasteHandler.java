/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.pasting;

import com.mistrx.buildpaste.pasting.PasteModifiers;

public class PasteHandler {
    public static String getPasteModifierFromArgs(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i] == null || !PasteModifiers.ALL_MODIFIERS.contains(args[i])) continue;
            return args[i];
        }
        return "nopastemodifier";
    }

    public static String getBuildIdFromArgs(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i] == null || PasteModifiers.ALL_MODIFIERS.contains(args[i])) continue;
            return args[i];
        }
        return null;
    }
}


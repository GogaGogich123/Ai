/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.rendering;

import javax.annotation.concurrent.Immutable;

@Immutable
public class RenderBlock {
    private final int color;

    public RenderBlock(int color) {
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }
}


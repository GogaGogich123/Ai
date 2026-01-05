/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.gui;

import javax.annotation.concurrent.Immutable;

@Immutable
public class MinecartItem {
    private final String itemName;
    private final String customName;
    private final int amount;
    private final String color;
    private final String customTag;

    public MinecartItem(String itemName, String customName, int amount, String color, String customTag) {
        this.itemName = itemName;
        this.customName = customName;
        this.amount = amount;
        this.color = color;
        this.customTag = customTag;
    }

    public String getItemName() {
        return this.itemName;
    }

    public String getCustomName() {
        return this.customName;
    }

    public int getAmount() {
        return this.amount;
    }

    public String getColor() {
        return this.color;
    }

    public String getCustomTag() {
        return this.customTag;
    }
}


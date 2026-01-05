/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.chat;

import java.net.URI;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public final class SimpleComponent {
    private SimpleComponent() {
    }

    public static TextBuilder text(String content) {
        return new TextBuilder(Component.literal((String)content));
    }

    public static TextBuilder text(String content, ChatFormatting color) {
        return SimpleComponent.text(content).color(color);
    }

    public static TextBuilder error(String errorCode, String content, String details) {
        return SimpleComponent.text(content + " (hover for details)").color(ChatFormatting.RED).hoverText((Component)Component.literal((String)(details + "\n\n")).withStyle(ChatFormatting.WHITE).append((Component)Component.literal((String)("(" + errorCode + ")")).withStyle(ChatFormatting.ITALIC)).append((Component)Component.literal((String)"\n\nClick to view more information or contact us via email or Discord").withStyle(ChatFormatting.RESET).withStyle(ChatFormatting.GRAY))).clickOpenUrl("https://buildpaste.net/wiki/error/" + errorCode);
    }

    public static TextBuilder error(String errorCode, MutableComponent content, String details) {
        return new TextBuilder(content).hoverText((Component)Component.literal((String)("(" + errorCode + ")" + (!details.isEmpty() ? details : ""))).withStyle(ChatFormatting.RED).append((Component)Component.literal((String)"\n\n")).append((Component)Component.translatable((String)errorCode).withStyle(ChatFormatting.WHITE)).append((Component)Component.literal((String)"\n\nClick to view more information or contact us via email or Discord"))).clickOpenUrl("https://buildpaste.net/wiki/error/" + errorCode);
    }

    public static TextBuilder error(String errorCode, String content) {
        return SimpleComponent.error(errorCode, content, "");
    }

    public static TextBuilder translatableError(String errorCode, MutableComponent translateComponent, String details) {
        return SimpleComponent.error(errorCode, translateComponent, details);
    }

    public static TextBuilder translatableError(String errorCode, String translationKey) {
        return SimpleComponent.error(errorCode, Component.translatable((String)translationKey).getString(), "");
    }

    public static TextBuilder translatableError(String errorCode, MutableComponent component) {
        return SimpleComponent.error(errorCode, component, "");
    }

    public static TextBuilder simpleError(String content) {
        return SimpleComponent.text(content).color(ChatFormatting.RED);
    }

    public static class TextBuilder
    implements Component {
        private MutableComponent comp;

        public Style getStyle() {
            return this.comp.getStyle();
        }

        public ComponentContents getContents() {
            return this.comp.getContents();
        }

        public List<Component> getSiblings() {
            return this.comp.getSiblings();
        }

        public FormattedCharSequence getVisualOrderText() {
            return this.comp.getVisualOrderText();
        }

        private TextBuilder(MutableComponent base) {
            this.comp = base;
        }

        public TextBuilder color(ChatFormatting colour) {
            this.comp = this.comp.withStyle(s -> s.withColor(colour));
            return this;
        }

        public TextBuilder clickOpenUrl(String url) {
            this.comp = this.comp.withStyle(s -> s.withClickEvent((ClickEvent)new ClickEvent.OpenUrl(URI.create(url))));
            return this;
        }

        public TextBuilder clickCopyToClipboard(String text) {
            this.comp = this.comp.withStyle(s -> s.withClickEvent((ClickEvent)new ClickEvent.CopyToClipboard(text)));
            return this;
        }

        public TextBuilder clickRunCommand(String cmd) {
            this.comp = this.comp.withStyle(s -> s.withClickEvent((ClickEvent)new ClickEvent.RunCommand(cmd)));
            return this;
        }

        public TextBuilder clickSuggestCommand(String cmd) {
            this.comp = this.comp.withStyle(s -> s.withClickEvent((ClickEvent)new ClickEvent.SuggestCommand(cmd)));
            return this;
        }

        public TextBuilder hoverText(String tooltip) {
            this.comp = this.comp.withStyle(s -> s.withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)tooltip))));
            return this;
        }

        public TextBuilder hoverText(String tooltip, ChatFormatting color) {
            this.comp = this.comp.withStyle(s -> s.withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)tooltip).withStyle(color))));
            return this;
        }

        public TextBuilder hoverText(Component tooltipComponent) {
            this.comp = this.comp.withStyle(s -> s.withHoverEvent((HoverEvent)new HoverEvent.ShowText(tooltipComponent)));
            return this;
        }

        public TextBuilder hoverText(Component tooltipComponent, ChatFormatting color) {
            this.comp = this.comp.withStyle(s -> s.withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)tooltipComponent.copy().withStyle(color))));
            return this;
        }
    }
}


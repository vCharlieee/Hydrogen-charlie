package me.peanut.hydrogen.ui.clickgui;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import me.peanut.hydrogen.Hydrogen;
import me.peanut.hydrogen.file.files.ClickGuiConfig;
import me.peanut.hydrogen.module.Category;
import me.peanut.hydrogen.ui.clickgui.component.Component;
import me.peanut.hydrogen.ui.clickgui.component.Frame;
import me.peanut.hydrogen.utils.BlurUtil;
import me.peanut.hydrogen.utils.ParticleGenerator;
import me.peanut.hydrogen.utils.ReflectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.FontRenderer;

public class ClickGui extends GuiScreen {

    public static ArrayList<Frame> frames;

    // Modern color scheme
    public static final int COLOR_BG = new Color(20, 20, 20, 220).getRGB();
    public static final int COLOR_PRIMARY = new Color(0, 153, 255).getRGB();
    public static final int COLOR_TEXT = new Color(255, 255, 255).getRGB();
    public static final int COLOR_BUTTON_HOVER = new Color(50, 50, 50, 220).getRGB();
    public static final int COLOR_BUTTON = new Color(35, 35, 35, 200).getRGB();

    private final ParticleGenerator particleGenerator;

    public ClickGui() {
        frames = new ArrayList<>();
        int frameX = 10;
        for (Category category : Category.values()) {
            Frame frame = new Frame(category);
            frame.setX(frameX);
            frames.add(frame);
            frameX += frame.getWidth() + 10;
        }

        particleGenerator = new ParticleGenerator(120, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
    }

    @Override
    public void initGui() { }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean blur = Hydrogen.getClient().settingsManager.getSettingByName("Blur").isEnabled();
        boolean particles = Hydrogen.getClient().settingsManager.getSettingByName("Particles").isEnabled();

        // Background
        drawRect(0, 0, width, height, COLOR_BG);

        // Blur effect
        if (blur) BlurUtil.blurAll(0.1f);

        // Top-center client title
        String title = "Paxtan Client";
        int titleWidth = fontRendererObj.getStringWidth(title);
        fontRendererObj.drawStringWithShadow(title, (width / 2) - (titleWidth / 2), 10, COLOR_TEXT);

        // Render all frames
        for (Frame frame : frames) {
            frame.renderFrame(fontRendererObj);                    // ← Fixed: only FontRenderer
            frame.updatePosition(mouseX, mouseY);

            for (Component component : frame.getComponents()) {
                component.updateComponent(mouseX, mouseY);
            }
        }

        // Draw particles
        if (particles) particleGenerator.drawParticles(0, 0, false);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (Frame frame : frames) {
            if (frame.isWithinHeader(mouseX, mouseY) && mouseButton == 0) {
                frame.setDrag(true);
                frame.dragX = mouseX - frame.getX();
                frame.dragY = mouseY - frame.getY();
            }

            if (frame.isWithinHeader(mouseX, mouseY) && mouseButton == 1) {
                frame.setOpen(!frame.isOpen());
            }

            if (frame.isOpen()) {
                for (Component component : frame.getComponents()) {
                    component.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        for (Frame frame : frames) {
            if (frame.isOpen() && keyCode != 1) {
                for (Component component : frame.getComponents()) {
                    component.keyTyped(typedChar, keyCode);
                }
            }
        }
        if (keyCode == 1) mc.displayGuiScreen(null);
    }

    @Override
    public void onGuiClosed() {
        if (mc.entityRenderer.getShaderGroup() != null) {
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
            try {
                ReflectionUtil.theShaderGroup.set(Minecraft.getMinecraft().entityRenderer, null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (!Hydrogen.getClient().panic) {
            ClickGuiConfig clickGuiConfig = new ClickGuiConfig();
            clickGuiConfig.saveConfig();
        }

        super.onGuiClosed();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (Frame frame : frames) frame.setDrag(false);

        for (Frame frame : frames) {
            if (frame.isOpen()) {
                for (Component component : frame.getComponents()) {
                    component.mouseReleased(mouseX, mouseY, state);
                }
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}

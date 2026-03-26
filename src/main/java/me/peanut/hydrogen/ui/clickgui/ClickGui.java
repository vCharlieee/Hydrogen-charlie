package me.peanut.hydrogen.ui.clickgui;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import me.peanut.hydrogen.file.files.ClickGuiConfig;
import me.peanut.hydrogen.Hydrogen;
import me.peanut.hydrogen.module.Category;
import me.peanut.hydrogen.settings.Setting;
import me.peanut.hydrogen.ui.clickgui.component.Component;
import me.peanut.hydrogen.ui.clickgui.component.Frame;
import me.peanut.hydrogen.ui.clickgui.component.components.Button;
import me.peanut.hydrogen.utils.BlurUtil;
import me.peanut.hydrogen.utils.ParticleGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.input.Mouse;

public class ClickGui extends GuiScreen {

    public static ArrayList<Frame> frames;
    public static final int BACKGROUND_COLOR = 0xFF1A1A1A;        // Dark black background
    public static final int HEADER_COLOR = 0xFF0F0F0F;            // Slightly darker header
    public static final int ACCENT_COLOR = 0xFF00BFFF;            // Bright blue accent (modern & clean)
    public static final int TEXT_COLOR = 0xFFFFFFFF;              // White text
    public static final int TEXT_COLOR_DISABLED = 0xFF888888;     // Gray for disabled
    public static final int MODULE_ENABLED = 0xFF00FFAA;          // Green for enabled modules
    public static final int MODULE_DISABLED = 0xFFFF4444;         // Red for disabled

    private final ParticleGenerator particleGenerator = new ParticleGenerator(80, 
            Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);

    public ClickGui() {
        frames = new ArrayList<>();
        int frameX = 20; // More spacing on the left

        for (Category category : Category.values()) {
            Frame frame = new Frame(category);
            frame.setX(frameX);
            frames.add(frame);
            frameX += frame.getWidth() + 8; // Better spacing between frames
        }
    }

    @Override
    public void initGui() {
        // You can add any initialization here if needed later
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean particles = Hydrogen.getClient().settingsManager.getSettingByName("Particles").isEnabled();
        boolean blur = Hydrogen.getClient().settingsManager.getSettingByName("Blur").isEnabled();

        // Dark black-themed background
        drawRect(0, 0, this.width, this.height, BACKGROUND_COLOR);

        // Subtle overlay for depth
        drawRect(0, 0, this.width, this.height, 0x40000000);

        if (blur) {
            BlurUtil.blurAll(0.8f); // Slightly stronger blur for premium feel
        }

        // === Top Bar with "Paxtan Client" Title ===
        int titleY = 8;
        int titleHeight = 28;
        drawRect(0, 0, this.width, titleHeight + 4, 0xFF111111); // Top bar background
        
        // Subtle gradient line under title bar
        drawRect(0, titleHeight + 3, this.width, 1, ACCENT_COLOR);

        // Centered "Paxtan Client" text
        String title = "Paxtan Client";
        int titleWidth = this.fontRendererObj.getStringWidth(title);
        this.fontRendererObj.drawStringWithShadow(title, 
                (this.width - titleWidth) / 2, 
                titleY + 8, 
                ACCENT_COLOR);

        // Render all frames
        for (Frame frame : frames) {
            frame.renderFrame(this.fontRendererObj);
            frame.updatePosition(mouseX, mouseY);

            for (Component comp : frame.getComponents()) {
                comp.updateComponent(mouseX, mouseY);
            }
        }

        if (particles) {
            particleGenerator.drawParticles(0, 0, false);
        }
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        // Check title bar area first (optional future drag for whole GUI, but not implemented now)
        
        for (Frame frame : frames) {
            // Header interaction
            if (frame.isWithinHeader(mouseX, mouseY)) {
                if (mouseButton == 0) {
                    frame.setDrag(true);
                    frame.dragX = mouseX - frame.getX();
                    frame.dragY = mouseY - frame.getY();
                } else if (mouseButton == 1) {
                    frame.setOpen(!frame.isOpen());
                }
            }

            // Component clicks only if frame is open
            if (frame.isOpen() && !frame.getComponents().isEmpty()) {
                for (Component component : frame.getComponents()) {
                    component.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        for (Frame frame : frames) {
            if (frame.isOpen() && keyCode != 1 && !frame.getComponents().isEmpty()) {
                for (Component component : frame.getComponents()) {
                    component.keyTyped(typedChar, keyCode);
                }
            }
        }

        if (keyCode == 1) { // ESC
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void onGuiClosed() {
        if (!Hydrogen.getClient().panic) {
            ClickGuiConfig clickGuiConfig = new ClickGuiConfig();
            clickGuiConfig.saveConfig();
        }
        super.onGuiClosed();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (Frame frame : frames) {
            frame.setDrag(false);
        }

        for (Frame frame : frames) {
            if (frame.isOpen() && !frame.getComponents().isEmpty()) {
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

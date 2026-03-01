package io.github.sst.remake.gui.screen.clickgui.overlay;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.math.RandomIntGenerator;
import io.github.sst.remake.util.math.anim.AnimationManager;

import java.util.ArrayList;
import java.util.List;

public class BrainFreezeOverlay extends Widget implements IMinecraft {
    private final List<ParticleEffect> particles = new ArrayList<>();
    private final AnimationManager animationManager = new AnimationManager();
    public RandomIntGenerator random = new RandomIntGenerator();

    public BrainFreezeOverlay(GuiComponent parentScreen, String name) {
        super(parentScreen, name, 0, 0, client.getWindow().getWidth(), client.getWindow().getHeight(), false);
        this.setFocused(false);
        this.setHovered(false);
        this.setReAddChildren(false);
        this.setBringToFront(true);
        this.setListening(false);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
        int halfScreenWidth = scaledWidth / 2;

        boolean shouldUpdate;
        for (shouldUpdate = false; this.particles.size() < halfScreenWidth; shouldUpdate = true) {
            this.particles.add(new ParticleEffect((float) this.random.nextInt(scaledWidth), (float) this.random.nextInt(scaledHeight)));
        }

        while (this.particles.size() > halfScreenWidth) {
            this.particles.remove(0);
            shouldUpdate = true;
        }

        if (shouldUpdate) {
            for (ParticleEffect particle : this.particles) {
                particle.initialXPosition = (float) this.random.nextInt(scaledWidth);
                particle.yPosition = (float) this.random.nextInt(scaledHeight);
            }
        }

        this.animationManager.update();

        for (ParticleEffect particle : this.particles) {
            particle.updatePosition(this.animationManager);
            if (!(particle.initialXPosition < 0.0F)) {
                if (particle.initialXPosition > (float) scaledWidth) {
                    particle.initialXPosition = 0.0F;
                }
            } else {
                particle.initialXPosition = (float) scaledWidth;
            }

            if (!(particle.yPosition < 0.0F)) {
                if (particle.yPosition > (float) scaledHeight) {
                    particle.yPosition = 0.0F;
                }
            } else {
                particle.yPosition = (float) scaledHeight;
            }

            particle.render(partialTicks);
        }

        super.draw(partialTicks);
    }

}

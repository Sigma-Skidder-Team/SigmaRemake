package io.github.sst.remake.gui.screen.clickgui.math;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.math.vec.Vector2d;
import io.github.sst.remake.util.render.RenderUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class BezierCurve extends InteractiveWidget {
    private AnimationUtils progressAnimation = new AnimationUtils(300, 300);
    private final BezierDot controlPoint1;
    private final BezierDot controlPoint2;
    public int padding;

    public BezierCurve(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, int var7, float var8, float var9, float var10, float var11) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.padding = var7;
        this.addToList(this.controlPoint1 = new BezierDot(this, 10, "pos1"));
        this.addToList(this.controlPoint2 = new BezierDot(this, 10, "pos2"));
        this.addMouseListener((var1x, var2x) -> this.callUIHandlers());
        this.setCurveValues(var8, var9, var10, var11);
    }

    public float[] getCurveValues() {
        int padding = this.padding;
        float usableWidth = (float) (this.width - padding * 2);
        float usableHeight = (float) (this.height - padding * 2);
        float x1 = (float) (this.controlPoint1.getX() - padding + 5) / usableWidth;
        float y1 = 1.0F - (float) (this.controlPoint1.getY() - padding + 5) / usableHeight;
        float x2 = (float) (this.controlPoint2.getX() - padding + 5) / usableWidth;
        float y2 = 1.0F - (float) (this.controlPoint2.getY() - padding + 5) / usableHeight;
        return new float[]{x1, y1, x2, y2};
    }

    public void setCurveValues(float var1, float var2, float var3, float var4) {
        int padding = this.padding;
        float usableWidth = (float) (this.width - padding * 2);
        float usableHeight = (float) (this.height - padding * 2);
        this.controlPoint1.setPosition(padding + usableWidth * var1 - 5, padding + usableHeight * (1.0F - var2) - 5);
        this.controlPoint2.setPosition(padding + usableWidth * var3 - 5, padding + usableHeight * (1.0F - var4) - 5);
    }

    @Override
    public void draw(float partialTicks) {
        this.progressAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);
        if (this.progressAnimation.calcPercent() == 1.0F) {
            this.progressAnimation = new AnimationUtils(1500, 0);
        }

        float[] values = this.getCurveValues();
        int padding = this.padding;
        float usableWidth = (float) (this.width - padding * 2);
        float usableHeight = (float) (this.height - padding * 2);
        float val1 = values[0];
        float val2 = values[1];
        float val3 = values[2];
        float val4 = values[3];
        RenderUtils.drawRoundedRect(
                (float) (this.x + padding),
                (float) (this.y + padding),
                usableWidth,
                usableHeight,
                3.0F,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.05F * partialTicks)
        );
        List<Vector2d> curvePoints = new ArrayList<>();
        curvePoints.add(new Vector2d(0.0, 0.0));
        curvePoints.add(new Vector2d(val1, val2));
        curvePoints.add(new Vector2d(val3, val4));
        curvePoints.add(new Vector2d(1.0, 1.0));
        VecUtils vecUtils = new VecUtils((1.0F / usableWidth * 2.0F) / 4.0F);
        double interpolatedValue = vecUtils.calculateInterpolatedValue(curvePoints, Math.min(0.8F, this.progressAnimation.calcPercent()) * 1.25F);
        RenderUtils.drawCircle(
                (float) (this.x + usableWidth * interpolatedValue + padding),
                (float) (this.y - padding / 2 + this.height),
                14.0F,
                ColorHelper.applyAlpha(ClientColors.DARK_BLUE_GREY.getColor(), partialTicks)
        );
        List<Vector2d> interpolatedPoints = vecUtils.generateInterpolatedPoints(curvePoints);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.x + padding), (float) (this.y + padding), 0.0F);
        GL11.glLineWidth(1.0F);
        GL11.glColor4d(0.0, 0.0, 0.0, 0.6F * partialTicks);
        GL11.glAlphaFunc(519, 0.0F);
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        GL11.glEnable(2848);
        GL11.glBegin(3);
        GL11.glVertex2f(0.0F, usableHeight);

        for (Vector2d point : interpolatedPoints) {
            GL11.glVertex2d(point.getX() * (double) usableWidth, (1.0 - point.getY()) * (double) usableHeight);
        }

        GL11.glVertex2f(usableWidth, 0.0F);
        GL11.glEnd();
        GL11.glLineWidth(3.0F);
        GL11.glColor4d(0.0, 0.2F, 0.4F, 0.2F);
        GL11.glBegin(3);
        GL11.glVertex2f(0.0F, usableHeight);
        GL11.glVertex2f((float) (this.controlPoint1.getX() - padding + 5), (float) (this.controlPoint1.getY() - padding + 5));
        GL11.glEnd();
        GL11.glBegin(3);
        GL11.glVertex2f(usableWidth, 0.0F);
        GL11.glVertex2f((float) (this.controlPoint2.getX() - padding + 5), (float) (this.controlPoint2.getY() - padding + 5));
        GL11.glEnd();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        GL11.glPopMatrix();
        super.draw(partialTicks);
    }
}

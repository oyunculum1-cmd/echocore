package com.echocore.client;

import com.echocore.EchoCoreMod;
import com.echocore.entity.EchoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class EchoRenderer extends MobRenderer<EchoEntity, EchoModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(EchoCoreMod.MODID, "textures/entity/echo.png");
    private static final ResourceLocation TEXTURE_GLITCH =
            new ResourceLocation(EchoCoreMod.MODID, "textures/entity/echo_glitch.png");

    public EchoRenderer(EntityRendererProvider.Context context) {
        super(context, new EchoModel(context.bakeLayer(EchoModel.LAYER_LOCATION)), 0.3F);
        this.addLayer(new EchoGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(EchoEntity entity) {
        return entity.isGlitching() ? TEXTURE_GLITCH : TEXTURE;
    }

    /** Robot oldugunda yana devrilmez; olum animasyonu modelin icinde (donerek duser). */
    @Override
    protected float getFlipDegrees(EchoEntity entity) {
        return 0.0F;
    }
}

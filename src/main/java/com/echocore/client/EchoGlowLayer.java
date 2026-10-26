package com.echocore.client;

import com.echocore.EchoCoreMod;
import com.echocore.entity.EchoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Gozler, anten ucu ve itici karanlikta parlar (tam parlaklik katmani). */
public class EchoGlowLayer extends RenderLayer<EchoEntity, EchoModel> {

    private static final ResourceLocation GLOW =
            new ResourceLocation(EchoCoreMod.MODID, "textures/entity/echo_glow.png");
    private static final ResourceLocation GLOW_GLITCH =
            new ResourceLocation(EchoCoreMod.MODID, "textures/entity/echo_glow_glitch.png");

    public EchoGlowLayer(RenderLayerParent<EchoEntity, EchoModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, EchoEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) {
            return;
        }
        ResourceLocation texture = entity.isGlitching() ? GLOW_GLITCH : GLOW;
        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(texture));
        this.getParentModel().renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}

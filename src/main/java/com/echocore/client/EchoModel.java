package com.echocore.client;

import com.echocore.EchoCoreMod;
import com.echocore.entity.EchoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * ECHO modeli: kup govde (ekran yuz), anten, iki yuzen kanat kapsul, arka panel ve itici.
 * Animasyonlar: suzulme, harekette one yatma, kanat donusu, anten sallanmasi,
 * saldiri atilimi, bekleme modunda kanatlari kapatma, glitch titremesi, olum donusu.
 */
public class EchoModel extends EntityModel<EchoEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(EchoCoreMod.MODID, "echo"), "main");

    private static final float DEG_TO_RAD = (float) Math.PI / 180.0F;
    private static final float BODY_BASE_Y = 16.0F;
    private static final float POD_BASE_X = 7.0F;

    private final ModelPart body;
    private final ModelPart antenna;
    private final ModelPart leftPod;
    private final ModelPart rightPod;
    private final ModelPart thruster;
    private final ModelPart back;

    public EchoModel(ModelPart root) {
        this.body = root.getChild("body");
        this.antenna = this.body.getChild("antenna");
        this.leftPod = this.body.getChild("left_pod");
        this.rightPod = this.body.getChild("right_pod");
        this.thruster = this.body.getChild("thruster");
        this.back = this.body.getChild("back");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, BODY_BASE_Y, 0.0F));

        body.addOrReplaceChild("antenna",
                CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F)
                        .texOffs(36, 0).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        body.addOrReplaceChild("left_pod",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-1.5F, -2.5F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offset(-POD_BASE_X, 0.0F, 0.0F));

        body.addOrReplaceChild("right_pod",
                CubeListBuilder.create().texOffs(12, 16)
                        .addBox(-1.5F, -2.5F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offset(POD_BASE_X, 0.0F, 0.0F));

        body.addOrReplaceChild("thruster",
                CubeListBuilder.create().texOffs(0, 24)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, 4.0F, 0.0F));

        body.addOrReplaceChild("back",
                CubeListBuilder.create().texOffs(24, 24)
                        .addBox(-3.0F, -2.0F, 0.0F, 6.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 4.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(EchoEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        boolean staying = entity.isInSittingPose();
        boolean glitching = entity.isGlitching();
        float attack = this.attackTime;
        float lunge = Mth.sin(attack * (float) Math.PI);
        float move = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);

        // ---- Govde: suzulme + bakis + one yatma ----
        float bobSpeed = staying ? 0.07F : 0.12F;
        float bobAmount = staying ? 0.6F : 1.2F;
        this.body.x = 0.0F;
        this.body.y = BODY_BASE_Y + Mth.sin(ageInTicks * bobSpeed) * bobAmount;
        this.body.z = -lunge * 3.0F;
        this.body.yRot = netHeadYaw * DEG_TO_RAD * 0.6F;
        this.body.xRot = headPitch * DEG_TO_RAD * 0.5F + move * 0.45F + lunge * 0.55F;
        this.body.zRot = Mth.sin(ageInTicks * 0.09F) * 0.04F;

        // ---- Anten: yumusak sallanma, bekleme modunda sarkar ----
        if (staying) {
            this.antenna.zRot = 0.4F + Mth.sin(ageInTicks * 0.05F) * 0.03F;
            this.antenna.xRot = 0.0F;
        } else {
            this.antenna.zRot = Mth.sin(ageInTicks * 0.1F) * 0.14F;
            this.antenna.xRot = Mth.cos(ageInTicks * 0.08F) * 0.08F - move * 0.35F;
        }

        // ---- Yan kapsuller: donus, yukari asagi salinim, saldirida one atilma ----
        float spin = ageInTicks * (staying ? 0.08F : 0.3F + move * 0.5F);
        float podFold = staying ? 2.0F : 0.0F;
        this.leftPod.x = -POD_BASE_X + podFold;
        this.rightPod.x = POD_BASE_X - podFold;
        this.leftPod.y = Mth.sin(ageInTicks * 0.2F) * (staying ? 0.4F : 1.0F);
        this.rightPod.y = Mth.sin(ageInTicks * 0.2F + (float) Math.PI) * (staying ? 0.4F : 1.0F);
        this.leftPod.z = -lunge * 2.5F;
        this.rightPod.z = -lunge * 2.5F;
        this.leftPod.yRot = spin;
        this.rightPod.yRot = -spin;
        this.leftPod.zRot = staying ? 0.0F : -0.12F - move * 0.25F;
        this.rightPod.zRot = staying ? 0.0F : 0.12F + move * 0.25F;
        this.leftPod.xRot = 0.0F;
        this.rightPod.xRot = 0.0F;

        // ---- Itici ve arka panel: hafif titresim ----
        this.thruster.xRot = Mth.sin(ageInTicks * 0.35F) * 0.06F + move * 0.2F;
        this.thruster.zRot = Mth.cos(ageInTicks * 0.31F) * 0.06F;
        this.thruster.y = 4.0F;
        this.back.xRot = move * 0.1F;
        this.back.z = 4.0F + Mth.sin(ageInTicks * 0.3F) * 0.15F;

        // ---- Glitch: sert titreme ----
        if (glitching) {
            this.body.x = Mth.sin(ageInTicks * 7.3F) * 0.9F;
            this.body.zRot += Mth.sin(ageInTicks * 11.1F) * 0.1F;
            this.body.yRot += Mth.sin(ageInTicks * 5.7F) * 0.12F;
            this.antenna.zRot += Mth.sin(ageInTicks * 13.0F) * 0.3F;
            this.leftPod.y += Mth.sin(ageInTicks * 9.1F) * 1.2F;
            this.rightPod.y += Mth.cos(ageInTicks * 8.3F) * 1.2F;
        }

        // ---- Olum: donerek duser, kapsuller dagilir ----
        if (entity.deathTime > 0) {
            float death = entity.deathTime;
            this.body.zRot = death * 0.45F;
            this.body.xRot = death * 0.1F;
            this.leftPod.x = -POD_BASE_X - death * 0.35F;
            this.rightPod.x = POD_BASE_X + death * 0.35F;
            this.antenna.zRot = death * 0.2F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}

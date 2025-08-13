package fr.petitzec.summonstorm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.petitzec.summonstorm.entity.custom.FireSpirit;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FireSpiritModel<T extends FireSpirit> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath( "modid", "fire_spirit"), "main");
    private final ModelPart body;
    private final ModelPart head;

    public FireSpiritModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = body.getChild("head").getChild("crown");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 17.2906F, 0.0F));

        PartDefinition spine = body.addOrReplaceChild("spine", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.3176F, 0.0F, 0.0193F, 0.0F, 0.0F));

        PartDefinition cube_r1 = spine.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(32, 17).addBox(-3.0F, -6.0F, -2.0F, 6.0F, 11.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.739F, 0.0F, -0.0069F, 0.0F, 0.0F));

        PartDefinition crown = head.addOrReplaceChild("crown", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.3153F, 0.0F, 0.056F, 0.0F, 0.0F));

        PartDefinition cube_r2 = crown.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -5.0F, -7.0F, 14.0F, 3.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(FireSpirit entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.applyHeadRotation(netHeadYaw, headPitch);

        this.animate(entity.idleAnimationState, FireSpiritAnimations.ANIM_FIRE_SPIRIT_IDLE, ageInTicks, 1f);
        this.animate(entity.attackAnimationState, FireSpiritAnimations.ANIM_FIRE_SPIRIT_ATTACK, ageInTicks, 1f);
        this.animate(entity.walkAnimationState, FireSpiritAnimations.ANIM_FIRE_SPIRIT_WALKING, ageInTicks, 1f);
        //this.animate(entity.runAnimationState, FireSpiritAnimations.ANIM_FIRE_SPIRIT_RUN, ageInTicks, 1f);
        //this.animate(entity.despawnAnimationState, FireSpiritAnimations.ANIM_FIRE_SPIRIT_DESPAWN, ageInTicks, 1f);
        //this.animate(entity.spawnAnimationState, FireSpiritAnimations.ANIM_FIRE_SPIRIT_SPAWN, ageInTicks, 1f);
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -25.0F, 45.0F);

        this.head.yRot = pNetHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float) Math.PI / 180F);

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }


    public ModelPart root() {
        return body;
    }
}
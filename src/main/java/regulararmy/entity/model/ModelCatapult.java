package regulararmy.entity.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import regulararmy.entity.EntityCatapult;



public class ModelCatapult extends ModelBase
{
	//fields
	public ModelRenderer frameR;
	public ModelRenderer frameL;
	public ModelRenderer winch;
	public ModelRenderer pillarL;
	public ModelRenderer pillarR;
	public ModelRenderer propR;
	public ModelRenderer propL;
	public ModelRenderer beam;
	public ModelRenderer screw;
	public ModelRenderer arms;
	public ModelRenderer backwheel;
	public ModelRenderer frontwheel;

	public ModelCatapult()
	{
		textureWidth = 128;
		textureHeight = 128;
		setTextureOffset("arms.arm", 64, 0);
		setTextureOffset("arms.front", 80, 0);
		setTextureOffset("arms.sideL", 80, 9);
		setTextureOffset("arms.sideR", 104, 9);
		setTextureOffset("arms.back", 64, 36);
		setTextureOffset("arms.bottom", 20, 6);
		setTextureOffset("backwheel.wheelLback", 0, 75);
		setTextureOffset("backwheel.wheelRback", 24, 75);
		setTextureOffset("backwheel.shaftback", 0, 95);
		setTextureOffset("frontwheel.wheelLfront", 48, 75);
		setTextureOffset("frontwheel.wheelRfront", 72, 75);
		setTextureOffset("frontwheel.shaftfront", 0, 99);

		frameR = new ModelRenderer(this, 0, 0);
		frameR.addBox(-2F, -2F, -28F, 4, 4, 56);
		frameR.setRotationPoint(-16F, 15F, 3F);
		frameR.setTextureSize(64, 32);
		frameR.mirror = true;
		setRotation(frameR, 0F, 0F, 0F);
		frameL = new ModelRenderer(this, 0, 0);
		frameL.addBox(-2F, -2F, -28F, 4, 4, 56);
		frameL.setRotationPoint(16F, 15F, 3F);
		frameL.setTextureSize(64, 32);
		frameL.mirror = true;
		setRotation(frameL, 0F, 0F, 0F);
		winch = new ModelRenderer(this, 62, 69);
		winch.addBox(-14F, -1.5F, -1.5F, 28, 3, 3);
		winch.setRotationPoint(0F, 15F, 26F);
		winch.setTextureSize(64, 32);
		winch.mirror = true;
		setRotation(winch, 0F, 0F, 0F);
		pillarL = new ModelRenderer(this, 0, 24);
		pillarL.addBox(-2F, -14F, -2F, 4, 28, 4);
		pillarL.setRotationPoint(16F, -1F, -5F);
		pillarL.setTextureSize(64, 32);
		pillarL.mirror = true;
		setRotation(pillarL, 0F, 0F, 0F);
		pillarR = new ModelRenderer(this, 0, 24);
		pillarR.addBox(-2F, -14F, -2F, 4, 28, 4);
		pillarR.setRotationPoint(-16F, -1F, -5F);
		pillarR.setTextureSize(64, 32);
		pillarR.mirror = true;
		setRotation(pillarR, 0F, 0F, 0F);
		propR = new ModelRenderer(this, 16, 24);
		propR.addBox(-1.5F, -14F, -2F, 3, 28, 4);
		propR.setRotationPoint(-16F, 2F, -13F);
		propR.setTextureSize(64, 32);
		propR.mirror = true;
		setRotation(propR, -0.5235988F, 0F, 0F);
		propL = new ModelRenderer(this, 16, 24);
		propL.addBox(-1.5F, -14F, -2F, 3, 28, 4);
		propL.setRotationPoint(16F, 2F, -13F);
		propL.setTextureSize(64, 32);
		propL.mirror = true;
		setRotation(propL, -0.5235988F, 0F, 0F);
		beam = new ModelRenderer(this, 0, 60);
		beam.addBox(-18.5F, -2.5F, -2F, 37, 5, 4);
		beam.setRotationPoint(0F, -11F, -4F);
		beam.setTextureSize(64, 32);
		beam.mirror = true;
		setRotation(beam, 0F, 0F, 0F);
		screw = new ModelRenderer(this, 0, 69);
		screw.addBox(-14F, -1.5F, -1.5F, 28, 3, 3);
		screw.setRotationPoint(0F, 15F, 0F);
		screw.setTextureSize(64, 32);
		screw.mirror = true;
		setRotation(screw, 0F, 0F, 0F);
		arms = new ModelRenderer(this, "arms");
		arms.setRotationPoint(0F, 15F, 2F);
		setRotation(arms, 0F, 0F, 0F);
		arms.mirror = true;
		arms.addBox("arm", -2F, -32F, -2F, 4, 32, 4);
		arms.addBox("front", -8F, -34F, -5F, 16, 2, 7);
		arms.addBox("sideL", 8F, -48F, -7F, 2, 16, 10);
		arms.addBox("sideR", -10F, -48F, -7F, 2, 16, 10);
		arms.addBox("back", -10F, -50F, -11F, 20, 2, 14);
		arms.addBox("bottom", -8F, -48F, 1F, 16, 16, 2);
		backwheel = new ModelRenderer(this, "backwheel");
		backwheel.setRotationPoint(0F, 18F, 18F);
		setRotation(backwheel, 0F, 0F, 0F);
		backwheel.mirror = true;
		backwheel.addBox("wheelLback", 19F, -5F, -5F, 2, 10, 10);
		backwheel.addBox("wheelRback", -21F, -5F, -5F, 2, 10, 10);
		backwheel.addBox("shaftback", -22F, -1F, -1F, 44, 2, 2);
		frontwheel = new ModelRenderer(this, "frontwheel");
		frontwheel.setRotationPoint(0F, 18F, -14F);
		setRotation(frontwheel, 0F, 0F, 0F);
		frontwheel.mirror = true;
		frontwheel.addBox("wheelLfront", 19F, -5F, -5F, 2, 10, 10);
		frontwheel.addBox("wheelRfront", -21F, -5F, -5F, 2, 10, 10);
		frontwheel.addBox("shaftfront", -22F, -1F, -1F, 44, 2, 2);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5,entity);
		frameR.render(f5);
		frameL.render(f5);
		winch.render(f5);
		pillarL.render(f5);
		pillarR.render(f5);
		propR.render(f5);
		propL.render(f5);
		beam.render(f5);
		screw.render(f5);
		arms.render(f5);
		backwheel.render(f5);
		frontwheel.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
	@Override
	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5,Entity entity)
	{
		this.setRotationAngles(f, f1, f2, f3, f4, f5,(EntityCatapult)entity);
	}
	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5,EntityCatapult entity)
	{
		super.setRotationAngles(f, f1, f2, f3, f4, f5,entity);
		float defaultAngleArm=-0.4f;
		float minAngleArm=-1.2f;
		float maxAngleArm=0.1f;

		float swingStartf=(float)entity.swingToStartLaunch/(float)entity.getArmSwingAnimationEndEx();
		float swingEndf=(float)entity.swingToEndLaunch/(float)entity.getArmSwingAnimationEndEx();

		if(this.swingProgress<0.01f){
			arms.rotateAngleX=defaultAngleArm;
		}else if(this.swingProgress<swingStartf){
			arms.rotateAngleX=defaultAngleArm+(minAngleArm-defaultAngleArm)*MathHelper.sin(this.swingProgress/swingStartf*(float)Math.PI*0.5f);
		}else if(this.swingProgress<swingEndf){

			arms.rotateAngleX=minAngleArm+(maxAngleArm-minAngleArm)*(this.swingProgress-swingStartf)/(swingEndf-swingStartf);
		}else{
			arms.rotateAngleX=maxAngleArm+(defaultAngleArm-maxAngleArm)*(this.swingProgress-swingEndf)/(1-swingEndf);
		}
	}
}

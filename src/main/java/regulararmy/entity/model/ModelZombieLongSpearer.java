package regulararmy.entity.model;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import regulararmy.entity.EntityZombieSpearer;

@SideOnly(Side.CLIENT)
public class ModelZombieLongSpearer extends ModelBiped
{
	public ModelRenderer bar;
	public ModelRenderer bar2;
	public  ModelRenderer spike1;
	public ModelRenderer socket1;
	public  ModelRenderer spike2;
	public ModelRenderer socket2;

	ModelRenderer bamboo_bar;
	ModelRenderer bamboo_bar2;
	ModelRenderer point1A;
	ModelRenderer point1B;
	ModelRenderer point2B;
	ModelRenderer point2A;
	ModelRenderer point3;
	ModelRenderer point4;


	public float spearScale=0.8f;
	public ModelZombieLongSpearer()
	{
		this(0.0F);
	}

	public ModelZombieLongSpearer(float par1)
	{
		super(par1, 0.0F, 128, 64);
		textureWidth = 128;
		textureHeight = 64;

		bar = new ModelRenderer(this, 22, 0);
		bar.addBox(-1.5F, 10F, -31F, 3, 3, 42);
		bar.setRotationPoint(-5.0F, 2.0F, 0.0F);
		bar.setTextureSize(64, 32);
		bar.mirror = true;
		bar2 = new ModelRenderer(this, 22, 0);
		bar2.addBox(-1.5F, 10F, -73F, 3, 3, 42);
		bar2.setRotationPoint(-5.0F, 2.0F, 0.0F);
		bar2.setTextureSize(64, 32);
		bar2.mirror = true;
		socket1 = new ModelRenderer(this, 70, 0);
		socket1.addBox(-2F, 9.5F, -78F, 4, 4, 5);
		socket1.setRotationPoint(-5.0F, 2.0F, 0.0F);
		socket1.setTextureSize(64, 32);
		socket1.mirror = true;
		socket2 = new ModelRenderer(this, 88, 0);
		socket2.addBox(-1F, 10.5F, -80F, 2, 2, 2);
		socket2.setRotationPoint(-5.0F, 2.0F, 0.0F);
		socket2.setTextureSize(64, 32);
		socket2.mirror = true;
		spike1 = new ModelRenderer(this, 70, 9);
		spike1.addBox(-0.5F, 10F, -86F, 1, 3, 6);
		spike1.setRotationPoint(-5.0F, 2.0F, 0.0F);
		spike1.setTextureSize(64, 32);
		spike1.mirror = true;
		spike2 = new ModelRenderer(this, 84, 9);
		spike2.addBox(-0.5F, 10.5F, -88F, 1, 2, 2);
		spike2.setRotationPoint(-5.0F, 2.0F, 0.0F);
		spike2.setTextureSize(64, 32);
		spike2.mirror = true;

		bamboo_bar = new ModelRenderer(this, 22, 0);
		bamboo_bar.addBox(-1.5F, 12F, -33F, 3, 3, 44);
		bamboo_bar.setRotationPoint(-5.0F, 2.0F, 0.0F);
		bamboo_bar.setTextureSize(64, 32);
		bamboo_bar.mirror = true;
		bamboo_bar2 = new ModelRenderer(this, 22, 0);
		bamboo_bar2.addBox(-1.5F, 12F, -77F, 3, 3, 44);
		bamboo_bar2.setRotationPoint(-5.0F, 2.0F, 0.0F);
		bamboo_bar2.setTextureSize(64, 32);
		bamboo_bar2.mirror = true;
		point1A = new ModelRenderer(this, 74, 0);
		point1A.addBox(-1.5F, 12F, -79F, 1, 2, 2);
		point1A.setRotationPoint(-5.0F, 2.0F, 0.0F);
		point1A.setTextureSize(64, 32);
		point1A.mirror = true;
		point1B = new ModelRenderer(this, 80, 0);
		point1B.addBox(0.5F, 12F, -79F, 1, 2, 2);
		point1B.setRotationPoint(-5.0F, 2.0F, 0.0F);
		point1B.setTextureSize(64, 32);
		point1B.mirror = true;
		point2B = new ModelRenderer(this, 74, 4);
		point2B.addBox(0.5F, 13F, -85F, 1, 1, 6);
		point2B.setRotationPoint(-5.0F, 2.0F, 0.0F);
		point2B.setTextureSize(64, 32);
		point2B.mirror = true;
		point2A = new ModelRenderer(this, 88, 4);
		point2A.addBox(-1.5F, 13F, -85F, 1, 1, 6);
		point2A.setRotationPoint(-5.0F, 2.0F, 0.0F);
		point2A.setTextureSize(64, 32);
		point2A.mirror = true;
		point3 = new ModelRenderer(this, 74, 11);
		point3.addBox(-1.5F, 14F, -88F, 3, 1, 11);
		point3.setRotationPoint(-5.0F, 2.0F, 0.0F);
		point3.setTextureSize(64, 32);
		point3.mirror = true;
		point4 = new ModelRenderer(this, 74, 23);
		point4.addBox(-1F, 14F, -90F, 2, 1, 2);
		point4.setRotationPoint(-5.0F, 2.0F, 0.0F);
		point4.setTextureSize(64, 32);
		point4.mirror = true;

	}

	/**
	 * Used for easily adding entity-dependent animations. The second and third float params here are the same second
	 * and third as in the setRotationAngles method.
	 */
	 public void setLivingAnimations(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4)
	{
		 this.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
		 super.setLivingAnimations(par1EntityLivingBase, par2, par3, par4);
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	 public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity)
	 {
		 super.setRotationAngles(par1, par2, par3, par4, par5, par6, par7Entity);
		 float f6 = MathHelper.sin(this.swingProgress * (float)Math.PI);
		 float f7 = MathHelper.sin((1.0F - (1.0F - this.swingProgress) * (1.0F - this.swingProgress)) * (float)Math.PI);
		 this.bipedRightArm.rotateAngleZ = 0.0F;
		 this.bipedLeftArm.rotateAngleZ = 0.0F;
		 this.bipedRightArm.rotateAngleY = -(0.1F - f6 * 0.6F);
		 this.bipedLeftArm.rotateAngleY = 0.1F - f6 * 0.6F;
		 this.bipedRightArm.rotateAngleX = 0.2f;
		 this.bipedLeftArm.rotateAngleX = -((float)Math.PI / 2F);
		 this.bipedRightArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
		 this.bipedLeftArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
		 this.bipedRightArm.rotateAngleZ += MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
		 this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
		 this.bipedRightArm.rotateAngleX += MathHelper.sin(par3 * 0.067F) * 0.05F;
		 this.bipedLeftArm.rotateAngleX -= MathHelper.sin(par3 * 0.067F) * 0.05F;

		 this.bar.rotateAngleX=this.bipedRightArm.rotateAngleX-0.1f;
		 this.bar2.rotateAngleX=this.bipedRightArm.rotateAngleX-0.1f;
		 this.socket1.rotateAngleX=this.bipedRightArm.rotateAngleX-0.1f;
		 this.socket2.rotateAngleX=this.bipedRightArm.rotateAngleX-0.1f;
		 this.spike1.rotateAngleX=this.bipedRightArm.rotateAngleX-0.1f;
		 this.spike2.rotateAngleX=this.bipedRightArm.rotateAngleX-0.1f;

		 this.bamboo_bar.rotateAngleX=this.bipedRightArm.rotateAngleX-0.1f;
		 this.bamboo_bar2.rotateAngleX=this.bipedRightArm.rotateAngleX-0.1f;
		 this.point1A.rotateAngleX=this.bipedRightArm.rotateAngleX-0.1f;
		 this.point1B.rotateAngleX=this.bipedRightArm.rotateAngleX-0.1f;
		 this.point2A.rotateAngleX=this.bipedRightArm.rotateAngleX-0.1f;
		 this.point2B.rotateAngleX=this.bipedRightArm.rotateAngleX-0.1f;
		 this.point3.rotateAngleX=this.bipedRightArm.rotateAngleX-0.1f;
		 this.point4.rotateAngleX=this.bipedRightArm.rotateAngleX-0.1f;
	 }

	 public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	 {
		 super.render(entity, f, f1, f2, f3, f4, f5);
		 GL11.glPushMatrix();
		 GL11.glScalef(this.spearScale, this.spearScale, this.spearScale);
		 //GL11.glTranslatef(5.0F*(1f-this.spearScale)*f5, -2.0f*(1f-this.spearScale)*f5 , 0.0F*(1f-this.spearScale)*f5);
		 if(((EntityZombieSpearer)entity).getSpearType()==0){
			 bamboo_bar.render(f5);
			 bamboo_bar2.render(f5);
			 point1A.render(f5);
			 point1B.render(f5);
			 point2B.render(f5);
			 point2A.render(f5);
			 point3.render(f5);
			 point4.render(f5);

		 }else{
			 bar.render(f5);
			 bar2.render(f5);
			 spike1.render(f5);
			 socket1.render(f5);
			 spike2.render(f5);
			 socket2.render(f5);

		 }
		 GL11.glPopMatrix();
	 }


}

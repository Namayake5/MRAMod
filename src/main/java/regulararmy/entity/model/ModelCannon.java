package regulararmy.entity.model;

import regulararmy.entity.EntityCannon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCannon extends ModelBase {
	//fields
	ModelRenderer cannon;
	ModelRenderer upperCarriage;
	ModelRenderer lowerCarrage;
	ModelRenderer frontWheel;
	ModelRenderer backWheel;

	public ModelCannon()
	{
		textureWidth = 256;
		textureHeight = 128;
		setTextureOffset("cannon.trunnion", 0, 30);
		setTextureOffset("cannon.slide", 0, 0);
		setTextureOffset("cannon.barrelB", 4, 0);
		setTextureOffset("cannon.barrelT", 4, 0);
		setTextureOffset("cannon.barrelL", 86, 2);
		setTextureOffset("cannon.barrelR", 86, 2);
		setTextureOffset("cannon.plug", 0, 0);
		setTextureOffset("cannon.knob", 0, 8);
		setTextureOffset("upperCarriage.right", 86, 0);
		setTextureOffset("upperCarriage.left", 86, 23);
		setTextureOffset("lowerCarrage.right", 150, 0);
		setTextureOffset("lowerCarrage.left", 150, 0);
		setTextureOffset("lowerCarrage.bridge", 150, 69);
		setTextureOffset("frontWheel.wheelL", 0, 62);
		setTextureOffset("frontWheel.wheelR", 0, 62);
		setTextureOffset("frontWheel.axle", 0, 90);
		setTextureOffset("backWheel.wheelL", 32, 62);
		setTextureOffset("backWheel.wheelR", 32, 62);
		setTextureOffset("backWheel.axle", 46, 90);

		cannon = new ModelRenderer(this, "cannon");
		cannon.setRotationPoint(0F, 0F, 6F);
		setRotation(cannon, 0F, 0F, 0F);
		cannon.mirror = true;
		cannon.addBox("trunnion", -9F, -1F, -1F, 18, 2, 2);
		cannon.addBox("slide", -6F, -5F, -7F, 12, 10, 20);
		cannon.addBox("barrelB", -5.5F, -2F, -42F, 11, 2, 60);
		cannon.addBox("barrelT", -5.5F, -11F, -42F, 11, 2, 60);
		cannon.addBox("barrelL", 3.5F, -9F, -42F, 2, 7, 60);
		cannon.addBox("barrelR", -5.5F, -9F, -42F, 2, 7, 60);
		cannon.addBox("plug", -3.5F, -9F, 17F, 7, 7, 1);
		cannon.addBox("knob", -1F, -6.466667F, 18F, 2, 2, 3);
		upperCarriage = new ModelRenderer(this, "upperCarriage");
		upperCarriage.setRotationPoint(0F, 2.5F, 9F);
		setRotation(upperCarriage, 0F, 0F, 0F);
		upperCarriage.mirror = true;
		upperCarriage.addBox("right", -8F, -4F, -13F, 2, 8, 15);
		upperCarriage.addBox("left", 6F, -4F, -13F, 2, 8, 15);
		lowerCarrage = new ModelRenderer(this, "lowerCarrage");
		lowerCarrage.setRotationPoint(0F, 6.5F, 0F);
		setRotation(lowerCarrage, 0F, 0F, 0F);
		lowerCarrage.mirror = true;
		lowerCarrage.addBox("right", -8F, 0F, -24F, 1, 10, 45);
		lowerCarrage.addBox("left", 7F, 0F, -24F, 1, 10, 45);
		lowerCarrage.addBox("bridge", -7F, 8F, -13F, 14, 2, 24);
		frontWheel = new ModelRenderer(this, "frontWheel");
		frontWheel.setRotationPoint(0F, 17F, -17F);
		setRotation(frontWheel, 0F, 0F, 0F);
		frontWheel.mirror = true;
		frontWheel.addBox("wheelL", 8F, -7F, -7F, 2, 14, 14);
		frontWheel.addBox("wheelR", -10F, -7F, -7F, 2, 14, 14);
		frontWheel.addBox("axle", -11F, -0.5F, -0.5F, 22, 1, 1);
		backWheel = new ModelRenderer(this, "backWheel");
		backWheel.setRotationPoint(0F, 17F, 16F);
		setRotation(backWheel, 0F, 0F, 0F);
		backWheel.mirror = true;
		backWheel.addBox("wheelL", 8F, -7F, -7F, 2, 14, 14);
		backWheel.addBox("wheelR", -10F, -7F, -7F, 2, 14, 14);
		backWheel.addBox("axle", -11F, -0.5F, -0.5F, 22, 1, 1);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5,entity);
		cannon.render(f5);
		upperCarriage.render(f5);
		lowerCarrage.render(f5);
		frontWheel.render(f5);
		backWheel.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5,Entity e)
	{
		super.setRotationAngles(f, f1, f2, f3, f4, f5,e);
		//EntityCannon entity=(EntityCannon)e;
		cannon.rotateAngleX=f4/180*(float)Math.PI;
		cannon.rotateAngleY=f3/180*(float)Math.PI;
	}

}

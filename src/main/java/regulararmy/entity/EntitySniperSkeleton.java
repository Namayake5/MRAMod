package regulararmy.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import regulararmy.core.MRACore;
import regulararmy.entity.ai.EntityAIArrowAttack;

public class EntitySniperSkeleton extends EntitySkeletonR {
	public EntityAIArrowAttack aiArrowAttack = new EntityAIArrowAttack(this, 1.0D, 60, 80, 40.0F);

	public EntitySniperSkeleton(World par1World) {
		super(par1World);
		this.doRideHorses=false;
	}

	@Override
	public EntityAIArrowAttack getAIArrow(){
		return this.aiArrowAttack;
	}

	@Override
	 /**
     * Attack the specified entity using a ranged attack.
     */
    public void attackEntityWithRangedAttack(EntityLivingBase par1EntityLivingBase, float par2)
	{
		EntityArrow entityarrow =  this.getArrow(par2);
		if (this.getHeldItemMainhand().getItem() instanceof net.minecraft.item.ItemBow)
			entityarrow = ((net.minecraft.item.ItemBow) this.getHeldItemMainhand().getItem()).customizeArrow(entityarrow);

		if(MRACore.isBowgun)entityarrow.setDamage(entityarrow.getDamage()*2);
		double d1=MathHelper.sqrt((par1EntityLivingBase.posX-entityarrow.posX)*(par1EntityLivingBase.posX-entityarrow.posX)+
				(par1EntityLivingBase.posZ-entityarrow.posZ)*(par1EntityLivingBase.posZ-entityarrow.posZ));
		double d2=par1EntityLivingBase.posY+ (double)par1EntityLivingBase.getEyeHeight()-0.1-entityarrow.posY;
		//double d3=(d2+0.025*(d2*d2+d1*d1)*0.25)/(Math.sqrt(d1*d1+d2*d2)-0.01*(d1*d1+d2*d2)*0.25);
		double d3=Math.atan2(d2, d1);
		entityarrow.shoot(par1EntityLivingBase.posX-this.posX, par1EntityLivingBase.posY-this.posY,par1EntityLivingBase.posZ-this.posZ, 2F, MRACore.isBowgun?4:8);
		if(d2>0){
			entityarrow.rotationPitch=(float) (((d3+0.07*-MathHelper.cos((float) (d3*4))+0.07)*180.0f/(float)Math.PI)+(d2*d2+d1*d1)*0.01f)+this.rand.nextFloat()*6-2;
		}else{
			entityarrow.rotationPitch=(float) (((d3+0.03*-MathHelper.cos((float) (d3*4))+0.03)*180.0f/(float)Math.PI)+(d2*d2+d1*d1)*0.01f)+this.rand.nextFloat()*6-2;
		}
		entityarrow.motionY = (double)2.0*MathHelper.sin((float) (entityarrow.rotationPitch/180.0f*Math.PI));


		this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
		this.world.spawnEntity(entityarrow);
	}

	public static float getCrowdCostPerBlock(){
		return 2;
	}

    public static float getFightRange(){
    	return 30;
    }

}

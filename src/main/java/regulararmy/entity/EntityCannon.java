package regulararmy.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import regulararmy.entity.ai.EntityAIArrowAttack;
import regulararmy.entity.ai.EntityAIForwardBase;
import regulararmy.entity.ai.EntityAILearnedTarget;

public class EntityCannon extends EntityRegularArmy implements IRangedAttackMob {
	public Vec3d lastEnemyPos;

	public static final DataParameter<Boolean> firePar=EntityDataManager.<Boolean>createKey(EntityCannon.class,DataSerializers.BOOLEAN);
	public EntityCannon(World par1World) {
		super(par1World);
		EntityAIArrowAttack arrowAI=new EntityAIArrowAttack(this,1f,200,50);
		arrowAI.angleMovementLimitPerTick=2f;
		arrowAI.headPitchLimit=75f;
		arrowAI.doWanderOnLost=false;
		arrowAI.shootOnLost=true;
		arrowAI.doTurn=true;
		this.tasks.addTask(5, arrowAI);
		this.tasks.addTask(4, new EntityAIForwardBase(this,0.8f));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		//this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
		this.targetTasks.addTask(3, new EntityAILearnedTarget(this,0,true));
		this.getANavigator().setCanSwim(false);
		this.setTurnLimitPerTick(2f);
		this.setSize(1.5f, 2f);
		this.doRideHorses=false;
		this.getDataManager().register(firePar,false);
	}
	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	public boolean isAIEnabled()
	{
		return true;
	}
	@Override
	public void onEntityUpdate(){
		super.onEntityUpdate();
		this.rotationYawHead=this.rotationYaw;
		this.renderYawOffset=this.rotationYawHead;
		if(this.getDataManager().get(firePar)){
			if(this.world.isRemote){
				double xVel=-MathHelper.sin(this.rotationYaw*(float)Math.PI/180f)*0.8;
				double yVel=MathHelper.sin((float) (this.rotationPitch/180.0f*Math.PI))*0.8;
				double zVel=MathHelper.cos(this.rotationYaw*(float)Math.PI/180f)*0.8;
				for(int i=0;i<30;i++){
					this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX,this.posY+0.5,this.posZ,
							xVel+this.rand.nextGaussian()*0.3-0.15,
							yVel+this.rand.nextGaussian()*0.3-0.15,
							zVel+this.rand.nextGaussian()*0.3-0.15);
				}
				for(int i=0;i<60;i++){
					this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX,this.posY+0.5,this.posZ,
							xVel+this.rand.nextGaussian()*0.3-0.15,
							yVel+this.rand.nextGaussian()*0.3-0.15,
							zVel+this.rand.nextGaussian()*0.3-0.15);
				}

			}else{
				this.getDataManager().set(firePar, false);
			}
		}
	}


	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		 this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(80.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.13D);
	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase target,
			float p_82196_2_) {
		if(this.lastEnemyPos==null||this.getEntitySenses().canSee(target)){
			this.lastEnemyPos=new Vec3d(target.posX, target.posY, target.posZ);
		}
		Entity entityarrow=null;
		if(this.rand.nextDouble()<0.3){
			entityarrow=new EntityTNTPrimed(this.world,this.posX, this.posY+this.height+0.5, this.posZ,this);
		}else{
			EntityStone entitystone = new EntityStone(this.world);
			entitystone.setPosition(this.posX, this.posY+this.height+0.5, this.posZ);
			entitystone.timeToDisappearMax=150;
			entitystone.setter=this;
			entityarrow=entitystone;

		}
		double d1=MathHelper.sqrt((this.lastEnemyPos.x-entityarrow.posX)*(this.lastEnemyPos.x-entityarrow.posX)+
				(this.lastEnemyPos.z-entityarrow.posZ)*(this.lastEnemyPos.z-entityarrow.posZ));
		double d2=this.lastEnemyPos.y+ (double)target.getEyeHeight()-0.1-entityarrow.posY;
		double d3=Math.atan2(d2, d1);

		entityarrow.rotationYaw=this.rotationYawHead+(float)this.rand.nextGaussian()*4-2;
		if(d2>0){
			entityarrow.rotationPitch=(float) (((d3+0.07*-MathHelper.cos((float) (d3*4))+0.07)*180.0f/(float)Math.PI)+(d2*d2+d1*d1)*0.01f)+this.rand.nextFloat()*6-2;
		}else{
			entityarrow.rotationPitch=(float) (((d3+0.03*-MathHelper.cos((float) (d3*4))+0.03)*180.0f/(float)Math.PI)+(d2*d2+d1*d1)*0.01f)+this.rand.nextFloat()*6-2;
		}
		entityarrow.posY+=MathHelper.sin((float) (entityarrow.rotationPitch/180.0f*Math.PI))*3.0;
		entityarrow.posX+=-MathHelper.sin(entityarrow.rotationYaw*(float)Math.PI/180f)*3.0;
		entityarrow.posZ+=MathHelper.cos(entityarrow.rotationYaw*(float)Math.PI/180f)*3.0;

		entityarrow.motionY = (double)2.0*MathHelper.sin((float) (entityarrow.rotationPitch/180.0f*Math.PI));
		entityarrow.motionX=-MathHelper.sin(entityarrow.rotationYaw*(float)Math.PI/180f)*2.0;
		entityarrow.motionZ=MathHelper.cos(entityarrow.rotationYaw*(float)Math.PI/180f)*2.0;
		this.world.spawnEntity(entityarrow);
		this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
		this.getDataManager().set(firePar, true);

	}

	protected void dropFewItems(boolean par1, int par2)
    {
        int j;
        int k;

        {
            j = this.rand.nextInt(1+3 + par2);

            for (k = 0; k < j; ++k)
            {
                this.entityDropItem(new ItemStack(Blocks.TNT),0f);
            }
        }
        {
        	j = this.rand.nextInt(3 + par2);

        	for (k = 0; k < j; ++k)
        	{
        		this.dropItem(Items.GUNPOWDER, 1);
        	}
        }
        {
        	j = this.rand.nextInt(3 + par2);

        	for (k = 0; k < j; ++k)
        	{
        		this.dropItem(Items.IRON_INGOT, 1);
        	}
        }
    }
	@Override
	public void setSwingingArms(boolean swingingArms) {

	}
}

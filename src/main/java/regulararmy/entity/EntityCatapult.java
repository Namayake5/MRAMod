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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import regulararmy.entity.ai.EntityAIArrowAttack;
import regulararmy.entity.ai.EntityAIForwardBase;
import regulararmy.entity.ai.EntityAILearnedTarget;

public class EntityCatapult extends EntityRegularArmy implements IRangedAttackMob {
	public Vec3d lastEnemyPos;
	public EntityLivingBase nextTarget;
	public int swingToStartLaunch=75;
	public float swingToEndLaunch=80;
	public EntityCatapult(World par1World) {
		super(par1World);
		EntityAIArrowAttack arrowAI=new EntityAIArrowAttack(this,1f,200,50);
		arrowAI.angleMovementLimitPerTick=1f;
		arrowAI.doWanderOnLost=false;
		arrowAI.shootOnLost=true;
		arrowAI.doTurn=true;
		this.tasks.addTask(5, arrowAI);
		this.tasks.addTask(4, new EntityAIForwardBase(this,0.8f));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		//this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
		this.targetTasks.addTask(3, new EntityAILearnedTarget(this,0,true));
		this.getANavigator().setCanSwim(false);
		this.setTurnLimitPerTick(1f);
		this.setSize(1.5f, 2f);
		this.doRideHorses=false;
	}
	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	public boolean isAIEnabled()
	{
		return true;
	}


	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		 this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.10D);
	}

	@Override
	public void onEntityUpdate(){
		super.onEntityUpdate();
		//System.out.println(this.swingProgressInt);
		if(!this.world.isRemote&&this.swingProgressInt==this.swingToStartLaunch){
			this.onLaunchStone(this.nextTarget);
		}
		this.rotationYawHead=this.rotationYaw;
		this.renderYawOffset=this.rotationYawHead;
	}

	@Override
	public void swingArm(EnumHand hand)
	{
		ItemStack stack = this.getHeldItemMainhand();

		if (stack != null && stack.getItem() != null)
		{
			Item item = stack.getItem();
			if (item.onEntitySwing(this, stack))
			{
				return;
			}
		}

		if (!this.isSwingInProgress || this.swingProgressInt >= this.getArmSwingAnimationEndEx() / 2 || this.swingProgressInt < 0)
		{
			this.swingProgressInt = -1;
			this.isSwingInProgress = true;

			if (this.world instanceof WorldServer)
			{
				((WorldServer)this.world).getEntityTracker().sendToTracking(this, new SPacketAnimation(this, 0));
			}
		}
	}

	@Override
	 /**
     * Updates the arm swing progress counters and animation progress
     */
    protected void updateArmSwingProgress()
    {
        int i = this.getArmSwingAnimationEndEx();

        if (this.isSwingInProgress)
        {
            ++this.swingProgressInt;

            if (this.swingProgressInt >= i)
            {
                this.swingProgressInt = 0;
                this.isSwingInProgress = false;
            }
        }
        else
        {
            this.swingProgressInt = 0;
        }

        this.swingProgress = (float)this.swingProgressInt / (float)i;
    }


	public int getArmSwingAnimationEndEx(){
		return 100;
	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase target,float damageRate) {
		this.swingArm(EnumHand.MAIN_HAND);
		this.nextTarget=target;
	}

	public void onLaunchStone(EntityLivingBase target){
		if(this.lastEnemyPos==null||this.getEntitySenses().canSee(target)){
			this.lastEnemyPos=new Vec3d(target.posX, target.posY, target.posZ);
		}
		Entity entityarrow=null;
		if(this.rand.nextDouble()<0.3){
			entityarrow=new EntityTNTPrimed(this.world,this.posX, this.posY+this.height+0.5, this.posZ,this);
		}else{
			EntityStone entitystone = new EntityStone(this.world);
			entitystone.timeToDisappearMax=150;
			entitystone.setter=this;
			entityarrow=entitystone;
			entityarrow.setPosition(this.posX, this.posY+this.height+0.5, this.posZ);

		}
		double d1=MathHelper.sqrt((this.lastEnemyPos.x-entityarrow.posX)*(this.lastEnemyPos.x-entityarrow.posX)+
				(this.lastEnemyPos.z-entityarrow.posZ)*(this.lastEnemyPos.z-entityarrow.posZ));
		double d2=this.lastEnemyPos.y+ (double)target.getEyeHeight()-0.1-entityarrow.posY;
		if(d1<d2){
			return;
		}
		//double d3=(d2+0.025*(d2*d2+d1*d1)*0.25)/(Math.sqrt(d1*d1+d2*d2)-0.01*(d1*d1+d2*d2)*0.25);
		entityarrow.rotationPitch=45f;
		entityarrow.rotationYaw=this.rotationYawHead;
		double speed=d1*MathHelper.sqrt(0.06/(d1-d2))*1.2;
		if(speed>6)speed=6;
		entityarrow.motionY = speed*0.71+(this.getRNG().nextGaussian()-0.5)/10;
		entityarrow.motionX=-MathHelper.sin(entityarrow.rotationYaw*(float)Math.PI/180f)*0.71*speed+(this.getRNG().nextGaussian()-0.5)/10;
		entityarrow.motionZ=MathHelper.cos(entityarrow.rotationYaw*(float)Math.PI/180f)*0.71*speed+(this.getRNG().nextGaussian()-0.5)/10;

		this.world.spawnEntity(entityarrow);
		this.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));

	}
	protected void dropFewItems(boolean par1, int par2)
    {
		super.dropFewItems(par1, par2);
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
        		this.dropItem(Items.STICK, 1);
        	}
        }
    }
	@Override
	public void setSwingingArms(boolean swingingArms) {

	}
}

package regulararmy.entity.ai;


import regulararmy.entity.EntityRegularArmy;
import regulararmy.pathfinding.AStarPathPoint;
import regulararmy.pathfinding.IPathFindRequester;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.MathHelper;

public class EntityAIBombard extends EntityRegularAIBase implements IPathFindRequester
{
    /** The entity the AI instance has been applied to */
    public final EntityRegularArmy entityHost;

    /**
     * The entity (as a RangedAttackMob) the AI instance has been applied to.
     */
    public final IRangedAttackMob rangedAttackEntityHost;
    public EntityLivingBase attackTarget;

    /**
     * A decrementing tick that spawns a ranged attack once this value reaches 0. It is then set back to the
     * maxRangedAttackTime.
     */
    public int rangedAttackTime;
    public double entityMoveSpeed;
    public int field_75318_f;
    public int minRangedAttackTime;

    /**
     * The maximum time the AI has to wait before peforming another ranged attack.
     */
    public int maxRangedAttackTime;
    public float maxAttackRange;
    public float maxAttackRange2x;
    public int tickAfterLost=0;
    
    public boolean doWanderOnLost=true;
    public float angleMovementLimitPerTick=30f;

    public EntityAIBombard(IRangedAttackMob par1IRangedAttackMob, double par2, int par4, float par5)
    {
        this(par1IRangedAttackMob, par2, par4, par4, par5);
    }

    public EntityAIBombard(IRangedAttackMob par1IRangedAttackMob, double par2, int par4, int par5, float par6)
    {
        this.rangedAttackTime = -1;

        if (!(par1IRangedAttackMob instanceof EntityRegularArmy))
        {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        }
        else
        {
            this.rangedAttackEntityHost = par1IRangedAttackMob;
            this.entityHost = (EntityRegularArmy)par1IRangedAttackMob;
            this.entityMoveSpeed = par2;
            this.minRangedAttackTime = par4;
            this.maxRangedAttackTime = par5;
            this.maxAttackRange = par6;
            this.maxAttackRange2x = par6 * par6;
            this.setMutexBits(3);
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        EntityLivingBase entitylivingbase = this.entityHost.getAttackTarget();

        if (entitylivingbase == null)
        {
            return false;
        }
        else
        {
            this.attackTarget = entitylivingbase;
            return true;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        return this.tickAfterLost<300&&(this.shouldExecute() || !this.entityHost.getNavigator().noPath());
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.attackTarget = null;
        this.field_75318_f = 0;
        this.rangedAttackTime = -1;
        this.tickAfterLost=0;
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        double squareDistance = this.entityHost.getDistanceSq(this.attackTarget.posX, this.attackTarget.getEntityBoundingBox().minY, this.attackTarget.posZ);
        boolean flag = this.entityHost.getEntitySenses().canSee(this.attackTarget);

        if (flag)
        {
            ++this.field_75318_f;
            this.tickAfterLost=0;
        }
        else
        {
            this.field_75318_f = 0;
            this.tickAfterLost++;
        }
        
        if (squareDistance <= (double)this.maxAttackRange2x && this.field_75318_f >= 20)
        {
            this.entityHost.getANavigator().clearAStarPathEntity();
        }
        if(this.doWanderOnLost&&this.entityHost.getANavigator().noPath()){
        	if(squareDistance > (double)this.maxAttackRange2x ){
        		this.entityHost.getANavigator().tryMoveToEntityLiving(this.attackTarget, entityMoveSpeed, 1.4f, this);
        	}else if(!flag){
        		this.entityHost.getANavigator().maxCost=800;
        		this.entityHost.getANavigator().canUseEngineer=0;
        		this.entityHost.getANavigator().tryMoveToXYZ(entityHost.posX+this.getSplitRandom(5, 10), entityHost.posY+entityHost.world.rand.nextInt(5)-2, entityHost.posZ+this.getSplitRandom(5, 10),this.entityMoveSpeed, 1.4f, this);
        		this.entityHost.getANavigator().maxCost=Integer.MAX_VALUE;
        		this.entityHost.getANavigator().canUseEngineer=2;
        	}
        }
       

        this.entityHost.getLookHelper().setLookPositionWithEntity(this.attackTarget, this.angleMovementLimitPerTick,this.angleMovementLimitPerTick);
        float f;

        if (--this.rangedAttackTime == 0)
        {
            if (squareDistance > (double)this.maxAttackRange2x || !flag)
            {
                return;
            }

            f = MathHelper.sqrt(squareDistance) / this.maxAttackRange;
            float f1 = f;

            if (f < 0.1F)
            {
                f1 = 0.1F;
            }

            if (f1 > 1.0F)
            {
                f1 = 1.0F;
            }

            this.rangedAttackEntityHost.attackEntityWithRangedAttack(this.attackTarget, f1);
            this.rangedAttackTime = MathHelper.floor(f * (float)(this.maxRangedAttackTime - this.minRangedAttackTime) + (float)this.minRangedAttackTime);
        }
        else if (this.rangedAttackTime < 0)
        {
            f = MathHelper.sqrt(squareDistance) / this.maxAttackRange;
            this.rangedAttackTime = MathHelper.floor(f * (float)(this.maxRangedAttackTime - this.minRangedAttackTime) + (float)this.minRangedAttackTime);
        }
    }

    public int getSplitRandom(int abs_min,int abs_max){
    	int num=this.entityHost.world.rand.nextInt((abs_max-abs_min)*2)-(abs_max-abs_min);
    	if(num<0){
    		return num-abs_min+1;
    	}else{
    		return num+abs_min;
    	}
    }

	@Override
	public int getTacticsCost(Entity entity, AStarPathPoint start,
			AStarPathPoint current, AStarPathPoint end) {
		return 0;
	}

	@Override
	public boolean isEngineer() {
		return false;
	}

	@Override
	public float getJumpHeight() {
		return this.entityHost.data.jumpHeight;
	}

	@Override
	public float getCrowdCost() {
		return this.entityHost.data.crowdCostPerBlock;
	}

	@Override
	public float getFightRange() {
		return this.entityHost.data.fightRange;
	}
}

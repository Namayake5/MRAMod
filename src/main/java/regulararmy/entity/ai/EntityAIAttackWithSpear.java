package regulararmy.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import regulararmy.entity.EntityRegularArmy;
import regulararmy.pathfinding.AStarPathEntity;
import regulararmy.pathfinding.AStarPathPoint;
import regulararmy.pathfinding.IPathFindRequester;

public class EntityAIAttackWithSpear extends EntityRegularAIBase implements IPathFindRequester
{
	public World world;
	public EntityRegularArmy attacker;

	public float hitboxSize;
	public Vec3d centreOfHitbox;
    /**
     * An amount of decrementing ticks that allows the entity to attack once the tick reaches 0.
     */
    public  int attackTick;

    /** The speed with which the mob will approach the target */
    public double speedTowardsTarget;

    /**
     * When true, the mob will continue chasing its target, even if it can't find a path to them right now.
     */
    public  boolean longMemory;

    /** The PathEntity of our entity. */
    public  AStarPathEntity entityPathEntity;
    private int field_75445_i;

    private int failedPathFindingPenalty;

    public EntityAIAttackWithSpear(EntityRegularArmy attacker,
			float hitboxSize, Vec3d centreOfHitbox,
			double speedTowardsTarget, boolean longMemory) {
		super();
		this.attacker = attacker;
		this.world = attacker.world;
		this.hitboxSize=hitboxSize;
		this.centreOfHitbox=centreOfHitbox;
		this.speedTowardsTarget = speedTowardsTarget;
		this.longMemory = longMemory;
		this.setMutexBits(3);
	}

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (entitylivingbase == null)
        {
            return false;
        }
        else if (!entitylivingbase.isEntityAlive())
        {
            return false;
        }

        else
        {
            if (-- this.field_75445_i <= 0)
            {

                this.entityPathEntity = this.attacker.getANavigator().getPathToEntityLiving(entitylivingbase,MathHelper.sqrt((double)(this.attacker.width *this.attacker.width)-0.2),this);
                this.field_75445_i = 4 + this.attacker.getRNG().nextInt(7);
                return this.entityPathEntity != null;
            }
            else
            {
                return true;
            }
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
        return ((entitylivingbase==null||(entitylivingbase!=null&&!entitylivingbase.isEntityAlive())) ? false : (!this.longMemory ? !this.attacker.getANavigator().noPath() : true));
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.attacker.getANavigator().setPath(this.entityPathEntity, this.speedTowardsTarget);
        this.field_75445_i = 0;
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.attacker.getANavigator().clearAStarPathEntity();
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
        if(entitylivingbase==null){
        	return;
        }
        this.attacker.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);

        if ((this.longMemory || this.attacker.getEntitySenses().canSee(entitylivingbase)) && --this.field_75445_i <= 0)
        {
            this.field_75445_i = failedPathFindingPenalty + 4 + this.attacker.getRNG().nextInt(7);
            this.attacker.getANavigator().tryMoveToEntityLiving(entitylivingbase, this.speedTowardsTarget,this.attacker.width*this.attacker.width,this);
            if (this.attacker.getANavigator().getPath() != null)
            {
                AStarPathPoint finalPathPoint = this.attacker.getANavigator().getPath().getFinalPathPoint();
                if (finalPathPoint != null && entitylivingbase.getDistanceSq(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1)
                {
                    failedPathFindingPenalty = 0;
                }
                else
                {
                    failedPathFindingPenalty += 10;
                }
            }
            else
            {
                failedPathFindingPenalty += 10;
            }
        }

        this.attackTick = Math.max(this.attackTick - 1, 0);

        if (this.attackTick <= 0)
        {
        	//System.out.println("yaw:"+this.attacker.renderYawOffset);
        	Vec3d center=this.centreOfHitbox.rotateYaw(this.attacker.renderYawOffset);
        	if(new AxisAlignedBB(this.attacker.posX, this.attacker.posY, this.attacker.posZ,
        			this.attacker.posX+this.hitboxSize, this.attacker.posY+this.hitboxSize, this.attacker.posZ+this.hitboxSize).offset(center)
        	.intersects(new AxisAlignedBB(entitylivingbase.posX-entitylivingbase.width/2,
        			entitylivingbase.posY-entitylivingbase.height/2,entitylivingbase.posZ-entitylivingbase.width/2,
        			entitylivingbase.posX+entitylivingbase.width/2,entitylivingbase.posY+entitylivingbase.height/2,entitylivingbase.posZ+entitylivingbase.width/2))){

        		this.attackTick = 30;
            	this.attacker.swingArm(EnumHand.MAIN_HAND);
        		this.attacker.attackEntityAsMob(entitylivingbase);
        		//System.out.println("hit");
        	}

        }
    }

	@Override
	public int getTacticsCost(Entity entity, AStarPathPoint start,
			AStarPathPoint current, AStarPathPoint end) {
		return 0;
	}

	@Override
	public boolean isEngineer(){
		return false;
	}

	@Override
	public float getJumpHeight() {
		return this.attacker.data.jumpHeight;
	}

	@Override
	public float getCrowdCost() {
		return this.attacker.data.crowdCostPerBlock;
	}

	@Override
	public float getFightRange() {
		return this.attacker.data.fightRange;
	}
}

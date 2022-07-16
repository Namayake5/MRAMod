package regulararmy.entity.ai;

import java.util.Arrays;
import java.util.Comparator;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import regulararmy.core.MRACore;
import regulararmy.entity.EntityRegularArmy;

public class EntityAILearnedTarget extends EntityRegularAIBase
{
    private final int targetChance;
    public EntityRegularArmy taskOwner;
    public boolean shouldCheckSight;
    public byte timer=0;
    /**
     * This filter is applied to the Entity search.  Only matching entities will be targetted.  (null -> no
     * restrictions)
     */
    private EntityLivingBase targetEntity;

    public Predicate<Entity> selector;
    public Comparator comparator;

    public EntityAILearnedTarget(EntityRegularArmy p_i1663_1_,int p_i1663_3_, boolean p_i1663_4_)
    {
        this(p_i1663_1_,  p_i1663_3_, p_i1663_4_, (Predicate<Entity>)null);
    }

    public EntityAILearnedTarget(EntityRegularArmy hostEntity, int targetChance, boolean shouldCheckSight, final Predicate<Entity> selector)
    {
    	super();
        this.targetChance = targetChance;
        this.taskOwner=hostEntity;
        this.shouldCheckSight=shouldCheckSight;
        this.setMutexBits(1);
        if(selector==null){
        	this.selector = new EnemySelector(shouldCheckSight, hostEntity);
        }
        this.comparator = new Comparator()
          {
            public int compare(Object o1, Object o2) {
              EntityRegularArmy owner = EntityAILearnedTarget.this.taskOwner;
              Vec3d vec3 = new Vec3d(owner.posX, owner.posY, owner.posZ);
              Entity e1 = (Entity)o1;
              Entity e2 = (Entity)o2;
              Integer costIE1 = (Integer)(owner.getSettings()).dangerousSupporter.get(Integer.valueOf(EntityRegularArmy.getCustomEntitySharedID(e1)));
              Integer costIE2 = (Integer)(owner.getSettings()).dangerousSupporter.get(Integer.valueOf(EntityRegularArmy.getCustomEntitySharedID(e2)));

              int costE1 = 0, costE2 = 0;
              if (costIE1 != null) {
                costE1 = costIE1.intValue() / 10;
              }
              if (costIE2 != null) {
                costE2 = costIE2.intValue() / 10;
              }
              if (e1 instanceof EntityPlayer) {
                costE1 += 20 + costE2;
              }
              if (e2 instanceof EntityPlayer) {
                costE2 += 20 + costE1;
              }


              int scoreE1 = (int)-e1.getDistance((Entity)owner) + costE1;
              int scoreE2 = (int)-e2.getDistance((Entity)owner) + costE2;
              if (scoreE2 < scoreE1) {
                return 1;
              }
              return -1;
            }
          };

    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0)
        {
            return false;
        }
        else
        {
        	double d0=taskOwner.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
        	double d1=4;
        	this.timer++;
        	if(this.timer==30){
        		switch(this.taskOwner.world.rand.nextInt(3)){
        		case 0:
        			d0*=2;
        			break;
        		case 1:
        			d1=30;
        			break;
        		case 2:
        			d0*=2;
        			d1=30;
        			break;
        		}
        		this.timer=0;
        	}

        	AxisAlignedBB sight=this.taskOwner.getEntityBoundingBox().expand(d0, d1, d0).offset(d0*0.8*this.taskOwner.getLookVec().x, 0, d0*0.8*this.taskOwner.getLookVec().z);
            Entity[] array = (Entity[]) this.taskOwner.world.getEntitiesInAABBexcluding(this.taskOwner,sight ,this.selector).toArray(new Entity[0]);
            if(!this.taskOwner.world.isRemote){
        		//System.out.println("boundingBox:"+sight.toString());
            }

            if (array.length==0)
            {
            	/*
            	if(!this.taskOwner.world.isRemote){
            	System.out.println("target:null");
            	}
            	*/
                return false;
            }

            try {
                Arrays.sort(array, this.comparator);
              } catch (Exception e) {}


              Integer costIE1 = (Integer)(this.taskOwner.getSettings()).dangerousSupporter.get(Integer.valueOf(EntityRegularArmy.getCustomEntitySharedID(array[0])));
              if (costIE1 == null || costIE1.intValue() > 2) {
                this.targetEntity = (EntityLivingBase)array[0];
                return true;
              }
              return false;
            /*
            if(!this.taskOwner.world.isRemote){
            System.out.println("target:"+array[0].getClass().getSimpleName());
            }
            */

        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.taskOwner.setAttackTarget(this.targetEntity);
        super.startExecuting();
    }
    public static class EnemySelector implements Predicate<Entity> {
        public boolean shouldCheckSight;

        public EnemySelector(boolean shouldCheckSight, EntityRegularArmy taskOwner) {
          this.shouldCheckSight = shouldCheckSight;
          this.taskOwner = taskOwner;
        }
        public EntityRegularArmy taskOwner;
        public boolean apply(Entity e) {
          if (!(e instanceof EntityLivingBase) || e.getIsInvulnerable() || e.isDead || e instanceof EntityRegularArmy || e.getControllingPassenger() instanceof EntityRegularArmy || (this.shouldCheckSight && !this.taskOwner.getEntitySenses().canSee(e)) || (!MRACore.doTargetPlayers && e instanceof EntityPlayer) || (e instanceof net.minecraft.entity.passive.EntityHorse && e.getControllingPassenger() == null))
          {
            return false;
          }
          if(e instanceof EntityPlayer) {
        	  EntityPlayer player=(EntityPlayer) e;
        	  if(player.capabilities.disableDamage||player.isCreative()) return false;
          }

          return ((e.getControllingPassenger()  instanceof EntityPlayer && !((EntityPlayer)e.getControllingPassenger()).capabilities.disableDamage) || (this.taskOwner.getSettings()).dangerousSupporter.containsKey(Integer.valueOf(EntityRegularArmy.getCustomEntitySharedID(e))));
        }
      }
}
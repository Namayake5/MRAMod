package regulararmy.entity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import regulararmy.analysis.DataAnalyzer;
import regulararmy.analysis.DataAnalyzerOneToOne;
import regulararmy.analysis.FinderSettings;
import regulararmy.core.MRACore;
import regulararmy.core.MRAEntityData;
import regulararmy.entity.ai.EntityAIFollowEngineer;
import regulararmy.entity.ai.EntityMoveHelperEx;
import regulararmy.entity.command.MonsterUnit;
import regulararmy.pathfinding.AStarPathEntity;
import regulararmy.pathfinding.AStarPathNavigate;

public abstract class EntityRegularArmy extends EntityMob{

	public MRAEntityData data;
	public AStarPathEntity pathToEntity;
	public AStarPathNavigate navigator;
	public MonsterUnit unit;
	public double lastDistance=0;
    public double lastDistanceDifference=0;
	public double lastDistanceDifferenceAmount=0;
	public short lastDistanceDifferenceAmountNum=0;
	public boolean doRideHorses=true;

	public EntityAIFollowEngineer follow=null;

	public EntityRegularArmy(World par1World) {
		super(par1World);
		this.navigator=new AStarPathNavigate(this,par1World);
		this.unit=null;
		this.data=MRAEntityData.classToData.get(this.getClass());
		this.moveHelper=new EntityMoveHelperEx(this,30f);

	}

	public EntityRegularArmy(World par1World,MonsterUnit unit) {
		super(par1World);
		this.navigator=new AStarPathNavigate(this,par1World);
		this.unit=unit;
		this.moveHelper=new EntityMoveHelperEx(this,30f);

	}

	@Override
	public void updateAITasks(){
		Vec3d vec3=new Vec3d(this.posX, (double)this.navigator.getPathableYPos(), this.posZ);

		if(this.getMoveHelper().isUpdating()&&this.navigator.currentPath!=null){

			this.lastDistanceDifference=this.lastDistance-vec3.distanceTo(this.navigator.currentPath.getVectorFromIndex(this,this.navigator.currentPath.getCurrentPathIndex()));
			this.lastDistanceDifferenceAmount+=this.lastDistanceDifference;
			this.lastDistanceDifferenceAmountNum++;

		}
		super.updateAITasks();

		this.navigator.onUpdateNavigation();

		if(this.navigator.currentPath!=null&&this.navigator.currentPath.getCurrentPathLength()>this.navigator.currentPath.getCurrentPathIndex()){
			this.lastDistance=vec3.distanceTo(this.navigator.currentPath.getVectorFromIndex(this,this.navigator.currentPath.getCurrentPathIndex()));
		}


	}

	public AStarPathNavigate getANavigator(){
		return this.navigator;
	}

	@Override
	public void onDeath(DamageSource par1DamageSource){
		super.onDeath(par1DamageSource);

		if(this.pathToEntity!=null){
			this.pathToEntity.disablePath(this.unit.leader);
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2){
		if(!this.world.isRemote){
			this.addNode(new DataAnalyzer.DataNode
					(par2*8,this.getBlockIDsAround(),new int[]{this.getChunkHash()},this.getEntityIDArrayNear(par1DamageSource.getTrueSource())));
			if(par1DamageSource.getTrueSource()!=null){
				this.addAttackerNode(new DataAnalyzer.DataNode(par2,new int[]{EntityList.getID(par1DamageSource.getTrueSource().getClass())}));
				EntityLivingBase attacker = null;
				if(par1DamageSource.getTrueSource() instanceof EntityLivingBase){
					attacker=(EntityLivingBase) par1DamageSource.getTrueSource();
				}else if(par1DamageSource.getImmediateSource()!=null&&par1DamageSource.getImmediateSource() instanceof EntityLivingBase){
					attacker=(EntityLivingBase) par1DamageSource.getImmediateSource();
				}

				if(attacker!=null){
					double rx=this.posX-attacker.posX;
					double ry=this.posY-attacker.posY;
					double rz=this.posZ-attacker.posZ;
					this.addDistanceNode(new DataAnalyzerOneToOne.DataNode(MathHelper.sqrt(rx*rx+ry*ry+rz*rz), new int[]{(int) par2+1}));
				}
			}
		}

		return super.attackEntityFrom(par1DamageSource, par2);
	}

	@Override
	public void onUpdate(){
		if(this.getAttackTarget()==null||this.getAttackTarget().isDead||!this.getAttackTarget().isEntityAlive()){
			this.setAttackTarget(null);
		}
		if(!this.world.isRemote&&this.ticksExisted%32==0){
			//System.out.println(this.lastDistanceDifferenceAmountNum==0?0:this.lastDistanceDifferenceAmount/this.lastDistanceDifferenceAmountNum);
			this.addNode(new DataAnalyzer.DataNode(-2.0f+(float)(this.lastDistanceDifferenceAmountNum==0?-4.0f:-(this.lastDistanceDifferenceAmount/this.lastDistanceDifferenceAmountNum)*40)
					,this.getBlockIDsAround()
					,new int[]{this.getChunkHash()}
					,this.getEntityIDArrayNear(null)));
			this.lastDistanceDifferenceAmount=0;
			this.lastDistanceDifferenceAmountNum=0;

			double relativeX=this.posX-this.unit.leader.x-0.5;
			double relativeY=this.posY-this.unit.leader.y-0.5;
			double relativeZ=this.posZ-this.unit.leader.z-0.5;
			if(relativeX*relativeX+relativeY*relativeY+relativeZ+relativeZ<1.5){
				this.setAttackTarget(null);
			}
		}
		super.onUpdate();
		 boolean flag = this.isSneaking() && this.isOnLadder();

         if (flag && this.motionY < 0.0D)
         {
             this.motionY = 0.0D;
         }

	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt){
		super.writeEntityToNBT(nbt);
		nbt.setShort("unitId",unit.getID());
		nbt.setByte("leaderId", unit.leader.id);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt){
		super.readEntityFromNBT(nbt);
		this.unit=
				MRACore.leaders
				[nbt.getByte("leaderId")].
				unitList.
				get
				(nbt.getShort("unitId"));
		this.unit.entityList.add(this);
	}

	public FinderSettings getSettings(){
		return this.unit.leader.analyzerManager.getSettings(this);
	}

	public void addNode(DataAnalyzer.DataNode n){
		//System.out.println("Node added(score:"+n.result+")");
		if(!this.world.isRemote)this.unit.leader.getAnalyzer(this).nodes.add(n);
	}

	public void addAttackerNode(DataAnalyzer.DataNode n){
		//System.out.println("Node added(score:"+n.result+")");
		if(!this.world.isRemote)this.unit.leader.getAttackerDanger(this).nodes.add(n);
	}

	public void addDistanceNode(DataAnalyzerOneToOne.DataNode n){
		if(!this.world.isRemote)this.unit.leader.getDistanceAnalyzer(this).nodes.add(n);
	}

	public int[] getBlockIDsAround(){
		int minx=MathHelper.floor(this.posX-this.width/2)-1;
		int miny=MathHelper.floor(this.posY)-1;
		int minz=MathHelper.floor(this.posZ-this.width/2)-1;
		int maxx=MathHelper.ceil(this.posX+this.width/2);
		int maxy=MathHelper.ceil(this.posY+this.height);
		int maxz=MathHelper.ceil(this.posZ+this.width/2);
		int blocksXSide=(maxy-miny-1)*(maxz-minz-1);
		int blocksZSide=(maxy-miny-1)*(maxx-minx-1);
		int[] ids=new int[(blocksXSide+blocksZSide)*2+(maxx-minx-1)*(maxy-miny+1)*(maxz-minz-1)];
		int num=0;
		for(int y=miny+1;y<maxy;y++){
			for(int z=minz+1;z<maxz;z++){

		         int newid1 = Block.getIdFromBlock(this.world.getBlockState(new BlockPos(maxx, y, z)).getBlock());
		         for (int i = 0; i < ids.length &&
		           ids[i] != newid1; i++) {


		           if (ids[i] == 0) {
		             ids[i] = newid1;
		             break;
		           }
		         }
		         int newid2 = Block.getIdFromBlock(this.world.getBlockState(new BlockPos(minx, y, z)).getBlock());
		         for (int j = 0; j < ids.length &&
		           ids[j] != newid2; j++) {


		           if (ids[j] == 0) {
		             ids[j] = newid2;

		             break;
		           }
		         }
		       }

		       for (int x = minx + 1; x < maxx; x++) {
		         int newid1 = Block.getIdFromBlock(this.world.getBlockState(new BlockPos(x, y,maxz)).getBlock());
		         for (int i = 0; i < ids.length &&
		           ids[i] != newid1; i++) {


		           if (ids[i] == 0) {
		             ids[i] = newid1;
		             break;
		           }
		         }
		         int newid2 = Block.getIdFromBlock(this.world.getBlockState(new BlockPos(x, y,minz)).getBlock());
		         for (int j = 0; j < ids.length &&
		           ids[j] != newid2; j++) {


		           if (ids[j] == 0) {
		             ids[j] = newid2;

		             break;
		           }
		         }
			}
		}
		for(int y=miny;y<=maxy;y++){
			for(int z=minz+1;z<maxz;z++){
				for(int x=minx+1;x<maxx;x++){
			           int newid1 = Block.getIdFromBlock(this.world.getBlockState(new BlockPos(x, y, z)).getBlock());
			           for (int i = 0; i < ids.length &&
			             ids[i] != newid1; i++) {


			             if (ids[i] == 0) {
			               ids[i] = newid1;
			               break;
			             }
			           }
				}
			}
		}
		return ids;
	}

	public int getChunkHash(){
		return (((((int)this.posX)/16)&0x7ff+(this.posX<0?0x800:0))<<20)+(((((int)this.posY)/16)&0xff)<<12)+((((int)this.posZ)/16)&0x7ff+(this.posZ<0?0x800:0));
	}

	  public int[] getEntityIDArrayNear(Entity e) {
		  List<Entity> list=this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(this.posX,this.posY,this.posZ,this.posX,this.posY,this.posZ).expand(2d, 2d, 2d));
		     List<Integer> array = new ArrayList<Integer>();
		     if (e != null) {
		       int id = getCustomEntitySharedID(e);
		       array.add(Integer.valueOf(id));
		     }
		     for (int i = 0; i < list.size(); i++) {
		       int id = getCustomEntitySharedID(list.get(i));
		       array.add(Integer.valueOf(id));
		     }
		     if (array.size() == 0) {
		       return new int[0];
		     }
		     Integer[] integerArray = array.<Integer>toArray(new Integer[0]);
		     int[] intArray = new int[integerArray.length];
		     for (int j = 0; j < integerArray.length; j++) {
		       intArray[j] = integerArray[j].intValue();
		     }
		     return intArray;
		   }

		   public static int getCustomEntitySharedID(Entity e) {
		     int id = EntityList.getID(e.getClass());
		     if (id == 0) {
		       String theName;
		       if (e instanceof EntityPlayer) {
		         theName = "Player;" + ((EntityPlayer)e).getDisplayName();
		       } else {
		         theName = e.getClass().getName();
		       }
		       id = getCustomEntitySharedIDFromName(theName);
		     }
		     return id;
		   }

		   public static int getCustomEntitySharedIDFromName(String theName) {
		     for (int i1 = 0; i1 < MRACore.entityIDList.size(); i1++) {
		       if (((String)MRACore.entityIDList.get(i1)).equals(theName)) {
		         return i1 - 1;
		       }
		     }
		     MRACore.entityIDList.add(theName);
		     System.out.println(theName + " idAdd " + -MRACore.entityIDList.size());
		     return -MRACore.entityIDList.size();
		   }


	public float getSpeed(){
		return (float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
	}
	@Override
	public boolean canDespawn(){
		return false;
	}

	public MRAEntityData getDataOfSpecies(){
		return this.data;
	}

	public void setEquipmentsFromTier(int tier){
		this.setEquipmentBasedOnDifficultyFromTier( tier);
		this.enchantEquipmentFromTier( tier);
		this.setHorsesFromTier(tier);
	}

	public void setHorsesFromTier(int tier){
		if(this.unit.ridingentity!=0 || tier<6 ||!this.doRideHorses)return;
		int rand=this.world.rand.nextInt(40+tier);
		if(rand>40){
			this.unit.setRidingEntity(1);
		}
	}

	public void setEquipmentBasedOnDifficultyFromTier(int tier){
		if (this.world.rand.nextInt(32)-8 >tier)return;
		int i = this.world.rand.nextInt(2);
        float f = this.world.getDifficulty() == EnumDifficulty.HARD ? 0.1F : 0.25F;

        if (this.world.rand.nextInt(64) < tier)
        {
            ++i;
        }

        if (this.world.rand.nextInt(64) < tier)
        {
            ++i;
        }

        if (this.world.rand.nextInt(64) < tier)
        {
            ++i;
        }

        for(EntityEquipmentSlot slot:EntityEquipmentSlot.values()) {

        	ItemStack itemstack = this.getItemStackFromSlot(slot);
        	if (itemstack == null || itemstack.isEmpty())
        	{
        		ItemStack weapon=this.getRandomEquip(slot,(byte) i);
        		if(weapon!=null){
        			//System.out.println("equip : "+i+" , "+weapon.getUnlocalizedName());
        			this.setItemStackToSlot(slot, weapon);
        		}
        	}
        }

	}

	public void enchantEquipmentFromTier(int tier){

		for(EntityEquipmentSlot slot:EntityEquipmentSlot.values()) {
			ItemStack itemstack = this.getItemStackFromSlot(slot);

            if (itemstack != null &&(tier>30||this.world.rand.nextInt(5-tier/16)==0))
            {
                EnchantmentHelper.addRandomEnchantment(this.world.rand, itemstack,5+this.world.rand.nextInt(tier/2+1), true);
            }
		}

	}

	public ItemStack getRandomEquip(EntityEquipmentSlot slot,int tier){
		List<ItemStack> canditateItems=new ArrayList();
		Item[] itemArray=null;
		int[] damageArray=null;
		int[] tierArray=null;
		switch(slot){
		case MAINHAND:
			itemArray=MRACore.weapons;
			damageArray=MRACore.weaponsDamage;
			tierArray=MRACore.weaponsTier;
			break;
		case FEET:
			itemArray=MRACore.boots;
			damageArray=MRACore.bootsDamage;
			tierArray=MRACore.bootsTier;
			break;
		case LEGS:
			itemArray=MRACore.legs;
			damageArray=MRACore.legsDamage;
			tierArray=MRACore.legsTier;
			break;
		case CHEST:
			itemArray=MRACore.chests;
			damageArray=MRACore.chestsDamage;
			tierArray=MRACore.chestsTier;
			break;
		case HEAD:
			itemArray=MRACore.helms;
			damageArray=MRACore.helmsDamage;
			tierArray=MRACore.helmsTier;
			break;
		default:
			break;
		}
		if(itemArray==null)return null;
		for(int i=0;i<itemArray.length;i++){
			if(tierArray[i]==tier&&itemArray[i]!=null){
				canditateItems.add(new ItemStack(itemArray[i],1,damageArray[i]));
			}
		}
		if(canditateItems.size()==0){
			return null;
		}else{
			int num=this.world.rand.nextInt(canditateItems.size());
			return canditateItems.get(num);
		}
	}

	@Override
	public double getYOffset()
	{
		return super.getYOffset()-0.4D;
    }


	public Entity getBottomEntity(){
		Entity e=this;
		while(e.getRidingEntity()!=null){
			e=e.getRidingEntity();
		}
		return e;
	}

	public EntityMoveHelperEx getMoveHelperEx(){
		return (EntityMoveHelperEx) this.moveHelper;
	}

	public void setTurnLimitPerTick(float limitAngle){
		this.getMoveHelperEx().angleMovementLimit=limitAngle;
	}

	@Override
	 protected void onDeathUpdate()
    {
        ++this.deathTime;

        if (this.deathTime == 20)
        {
            int i;

            if (!this.world.isRemote && (this.recentlyHit > 0 || this.isPlayer()) && this.canDropLoot() && this.world.getGameRules().getBoolean("doMobLoot"))
            {
                i = this.getExperiencePoints(this.attackingPlayer);

                while (i > 0)
                {
                    int j = EntityXPOrb.getXPSplit(i);
                    i -= j;
                    this.world.spawnEntity(new EntityXPOrb(this.attackingPlayer.world, this.attackingPlayer.posX, this.attackingPlayer.posY, this.attackingPlayer.posZ, j));
                }
            }

            this.setDead();

            for (i = 0; i < 20; ++i)
            {
                double d2 = this.rand.nextGaussian() * 0.02D;
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d2, d0, d1);
            }
        }
    }

	@Override
	 public EntityItem entityDropItem(ItemStack itemStack, float p_70099_2_)
    {
		if(!MRACore.doDropItem)return null;
        if (itemStack.getCount() != 0 && itemStack.getItem() != null)
        {
        	EntityPlayer lastAttacker=this.attackingPlayer;
        	EntityItem entityitem;
        	if(lastAttacker!=null&&!lastAttacker.isDead){
        		entityitem = new EntityItem(lastAttacker.world, lastAttacker.posX, lastAttacker.posY + (double)p_70099_2_, lastAttacker.posZ, itemStack);
        	}else{
        		entityitem = new EntityItem(this.world, this.posX, this.posY + (double)p_70099_2_, this.posZ, itemStack);
        	}
            entityitem.setPickupDelay(1);
            if (captureDrops)
            {
                capturedDrops.add(entityitem);
            }
            else
            {
                this.world.spawnEntity(entityitem);
            }
            return entityitem;
        }
        else
        {
            return null;
        }
    }

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		this.setEquipmentBasedOnDifficulty(difficulty);
		return super.onInitialSpawn(difficulty, livingdata);
	}

}

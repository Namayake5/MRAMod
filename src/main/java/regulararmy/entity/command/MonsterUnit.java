package regulararmy.entity.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import regulararmy.entity.DummyChocolate;
import regulararmy.entity.EntityEngineer;
import regulararmy.entity.EntityRegularArmy;
import regulararmy.entity.ai.EntityAIFollowEngineer;
import regulararmy.entity.ai.IBreakBlocksMob;

public class MonsterUnit {
	public List<EntityRegularArmy> entityList=new ArrayList();
	public int age;
	public RegularArmyLeader leader;
	/**you shouldn't change this value.*/
	public int wave;
	/**0:nothing 1:horse over2:future release :D */
	public int ridingentity;
	public IEntityLivingData ridingentityData;
	public RequestManager requestManager;
	public boolean canUseEngineer=false;
	public boolean isFirstUpdate=true;

	public MonsterUnit(RegularArmyLeader leader,int wave2,EntityRegularArmy... entity){
		this.leader=leader;
		for(int i=0;i<entity.length;i++){
			this.entityList.add(entity[i]);
			if(!this.canUseEngineer && entity[i] instanceof IBreakBlocksMob) this.canUseEngineer=true;
		}
		this.requestManager=new RequestManager();
		this.wave=wave2;
		this.isFirstUpdate=true;
	}

	public MonsterUnit(NBTTagCompound nbt,RegularArmyLeader l){
		this.readFromNBT(nbt);
		this.leader=l;
		this.requestManager=new RequestManager();
		this.isFirstUpdate=true;
	}

	/**0:nothing 1:horse over2:further release :D */
	public void setRidingEntity(int ridingentity){
		this.ridingentity=ridingentity;
		if(ridingentity==0)return;
		if(ridingentity==1){
			for(EntityRegularArmy entity:this.entityList){
				if(!entity.doRideHorses)continue;
				NBTTagCompound nbt=new NBTTagCompound();
				nbt.setString("id", "horse");
				nbt.setTag("SaddleItem", new ItemStack(Items.SADDLE).writeToNBT(new NBTTagCompound()));
				EntityHorse horse=(EntityHorse) EntityList.createEntityFromNBT(nbt, this.leader.theWorld);
				horse.setLocationAndAngles(entity.posX,entity.posY,entity.posZ,0f,0f);

				horse.setTamedBy(new DummyChocolate(this.leader.theWorld));
				float randNum=this.leader.theWorld.rand.nextFloat();
				if(this.ridingentityData==null){
					if(randNum<0.1f){
						this.ridingentityData=
								horse.onInitialSpawn(this.getTheWorld().getDifficultyForLocation(new BlockPos(horse)), (IEntityLivingData)null);
					}else if(randNum<0.5){
						horse.setHorseVariant(3);
						horse.setHorseVariant(256);
						this.ridingentityData=new EntityHorse.GroupData(3);
					}else{
						horse.setHorseVariant(4);
						horse.setHorseVariant(256);
						this.ridingentityData=new EntityHorse.GroupData(4);
					}
				}else{
					horse.onInitialSpawn(this.getTheWorld().getDifficultyForLocation(new BlockPos(horse)), (IEntityLivingData)null);
				}
				this.getTheWorld().spawnEntity(horse);
				entity.startRiding(horse,true);
			}
		}
	}

	public int getRidingEntity() {
		return ridingentity;
	}

	public void setEquipmentsFromTier(int tier){
		for(EntityRegularArmy e:this.entityList){
			e.setEquipmentsFromTier(tier);
		}
	}

	public void onUpdate(){
		if(this.isFirstUpdate&&this.entityList.size()!=0){
			//System.out.println("first update");
			boolean flag=false;
			EntityEngineer engineer=null;
			for(int i=0;i<this.entityList.size();i++){
				if(entityList.get(i) instanceof EntityEngineer){
					flag=true;
					engineer=(EntityEngineer) entityList.get(i);
				}
			}
			if(flag){
				for(int i=0;i<this.entityList.size();i++){
					if(!(entityList.get(i) instanceof EntityEngineer)){
						EntityRegularArmy e=entityList.get(i);
						e.follow=new EntityAIFollowEngineer(e,engineer);
						e.tasks.addTask(3, e.follow);
					}
				}
			}
			this.isFirstUpdate=false;
		}
		boolean flag=true;
		for(int i=0;i<this.entityList.size();i++){
			if(entityList.get(i).isDead){

				entityList.remove(i);
			}else if(entityList.get(i) instanceof IBreakBlocksMob){
				flag=false;

			}
		}

		if(flag) this.canUseEngineer=false;
	}

	public void spawnAll(){
		for(int i=0;i<this.entityList.size();i++){
			this.getTheWorld().spawnEntity(entityList.get(i));

			entityList.get(i).onInitialSpawn(this.getTheWorld().getDifficultyForLocation(new BlockPos(entityList.get(i))), (IEntityLivingData)null);
		}

	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		if(nbt==null)nbt=new NBTTagCompound();
		nbt.setInteger("age", age);
		nbt.setInteger("wave",wave);
		nbt.setBoolean("canUseEngineer", this.canUseEngineer);
		nbt.setInteger("ridingentity", this.ridingentity);
		return nbt;
	}

	public MonsterUnit readFromNBT(NBTTagCompound nbt){
		if(nbt==null)return null;
		this.age=nbt.getInteger("age");
		this.wave=nbt.getInteger("wave");
		this.canUseEngineer=nbt.getBoolean("canUseEngineer");
		this.ridingentity=nbt.getInteger("ridingentity");
		return this;
	}

	public List<EntityRegularArmy> getEntityList() {
		return entityList;
	}
	public World getTheWorld() {
		return leader.getTheWorld();
	}
	public int getAge() {
		return age;
	}
	public RequestManager getRequestManager() {
		return this.requestManager;
	}

	public short getID(){
		for(short i=0;i<this.leader.unitList.size();i++){
			if(this.leader.unitList.get(i)==this){
				return i;
			}
		}
		return -1;
	}

}

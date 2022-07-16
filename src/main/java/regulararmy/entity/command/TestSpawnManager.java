package regulararmy.entity.command;

import net.minecraft.nbt.NBTTagCompound;
import regulararmy.entity.EntityCannon;
import regulararmy.entity.EntityEngineer;
import regulararmy.entity.EntityFastZombie;
import regulararmy.entity.EntityRegularArmy;
import regulararmy.entity.EntityZombieLongSpearer;

public class TestSpawnManager extends SpawnManagerBase {

	public TestSpawnManager(RegularArmyLeader leader) {
		super(leader);
	}

	@Override
	public void onStart() {

	}

	@Override
	public void onUpdate() {
		if(this.leader.age==0&&!this.leader.theWorld.isRemote){

			EntityRegularArmy  e=new EntityEngineer(this.leader.theWorld);
			EntityRegularArmy e1=new EntityZombieLongSpearer(this.leader.theWorld);
			MonsterUnit u=this.leader.addUnit(e1);

			e.setPosition(this.leader.x+30, this.leader.y, this.leader.z+0);
			e1.setPosition(this.leader.x+30, this.leader.y, this.leader.z+0);

			u.spawnAll();
		}
		if(this.leader.age==400){
			this.leader.endPhase();
		}
		if(this.leader.age==500&&!this.leader.theWorld.isRemote){

			EntityRegularArmy  e=new EntityCannon(this.leader.theWorld);
			EntityRegularArmy e1=new EntityZombieLongSpearer(this.leader.theWorld);
			EntityRegularArmy  e2=new EntityFastZombie(this.leader.theWorld);
			MonsterUnit u=this.leader.addUnit(e1);

			e.setPosition(this.leader.x+30, this.leader.y, this.leader.z+20);
			e1.setPosition(this.leader.x+30, this.leader.y, this.leader.z+0);
			e2.setPosition(this.leader.x+30, this.leader.y, this.leader.z+0);
			//e1.setPosition(this.leader.x+10, this.leader.y+5, this.leader.z+10);
			u.spawnAll();


		}
	}

	@Override
	public void onEnd() {

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
return nbt;
	}

}

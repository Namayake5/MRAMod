package regulararmy.entity.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import regulararmy.core.MRACore;
import regulararmy.core.MRAEntityData;
import regulararmy.entity.EntityCreeperR;
import regulararmy.entity.EntityEngineer;
import regulararmy.entity.EntityRegularArmy;
import regulararmy.util.MRAUtil;

public class CreeperRushManager extends SpawnManagerBase {

	List<BlockPos> spawnPosList=new ArrayList<BlockPos>();
	int progress;

	public CreeperRushManager(RegularArmyLeader leader) {
		super(leader);
	}

	@Override
	public void onStart() {
		if(this.leader.theWorld.isRemote)return;
		MRAUtil.sendMessageAll(this.leader.theWorld,"CREEPER RUSH IS COMING!");

		progress=0;
	}

	@Override
	public void onUpdate() {
		if(this.spawnPosList.size()==0) {
			EntityCreeperR creeper=new EntityCreeperR(this.leader.theWorld);

			MonsterUnit unit=this.leader.addUnit(creeper);
			for(int i=0;i<24;i++) {
				this.spawnPosList.addAll(this.leader.getSpawnablePos((i+leader.theWorld.rand.nextFloat())/12.0f*(float)Math.PI, creeper));
			}
			if(spawnPosList.size()==0) {
				MRAUtil.sendMessageAll(this.leader.theWorld,"Couldn't find suitable spawnpoint");
				this.leader.onEnd();
				return;
			}
		}
		if(this.leader.age%MRACore.spawnInterval==0){
			this.progress++;
			for(int i=0;i<5*MRACore.unitMultiplier;i++) {
				BlockPos spawnPos=this.spawnPosList.get(MathHelper.fastFloor(leader.theWorld.rand.nextFloat()*this.spawnPosList.size()));
				makeRandomUnit(spawnPos).spawnAll();
			}
		}
		if(this.progress>4+this.leader.wave/2) {
			this.leader.endPhase();
		}
	}

	@Override
	public void onEnd() {

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		this.progress=nbt.getInteger("progress");
		for(int i=0;nbt.hasKey("spawnpoint_x_"+i);i++){
			this.spawnPosList.add(new BlockPos(nbt.getInteger("spawnpoint_x_"+i),nbt.getInteger("spawnpoint_y_"+i),nbt.getInteger("spawnpoint_z_"+i)));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("progress", this.progress);
		for(int i=0;i<this.spawnPosList.size();i++){
			nbt.setInteger("spawnPoint_x_"+i, this.spawnPosList.get(i).getX());
			nbt.setInteger("spawnPoint_y_"+i, this.spawnPosList.get(i).getY());
			nbt.setInteger("spawnPoint_z_"+i, this.spawnPosList.get(i).getZ());
		}
		return nbt;
	}


	public MonsterUnit makeRandomUnit(BlockPos pos) {
		float subRand=this.leader.getTheWorld().rand.nextFloat();
		List<EntityRegularArmy> list=new ArrayList();
		for(int i=0;i<MRAEntityData.classToData.get(EntityCreeperR.class).numberOfMember;i++){
			EntityCreeperR e=new EntityCreeperR(this.leader.theWorld);
			if(this.leader.theWorld.rand.nextFloat()<0.1f) {
				e.getDataManager().set(EntityCreeperR.POWERED, Boolean.valueOf(true));
			}
			list.add(e);
			e.setPosition(pos.getX(), pos.getY(), pos.getZ());
			e.addPotionEffect(new PotionEffect(MobEffects.SPEED,1000000,1));
		}
		if(MRAEntityData.classToData.get(EntityEngineer.class).basicWeight>subRand) {
			EntityRegularArmy e=new EntityEngineer(this.leader.theWorld);
			list.add(e);
			e.setPosition(pos.getX(), pos.getY(), pos.getZ());
		}
		MonsterUnit unit=this.leader.addUnit(list.toArray(new EntityRegularArmy[0]));
		return unit;
	}
}

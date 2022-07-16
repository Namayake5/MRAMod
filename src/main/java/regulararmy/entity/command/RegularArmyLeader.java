package regulararmy.entity.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import regulararmy.analysis.DataAnalyzer;
import regulararmy.analysis.DataAnalyzerOneToOne;
import regulararmy.core.MRACore;
import regulararmy.entity.EntityRegularArmy;
import regulararmy.util.MRAUtil;

public class RegularArmyLeader {
	//public List<Coord> notToBreak=new ArrayList();
	//public List<Coord> notToSet=new ArrayList();
	public List<MonsterUnit> unitList=new ArrayList();
	public World theWorld;
	public int x,y,z;
	public SpawnManagerBase currentSpawnManager;
	public FaintSpawnManager faintManager;
	public CreeperRushManager creeperManager;
	public TestSpawnManager testManager;
	public TestLearningManager analyzerManager;
	public double creeperRushChance=0;
	public int age;
	public float fightingDistance=1;

	public static boolean useTestManager=false;

	/**you shouldn't change this value.*/
	public byte id;
	public boolean isAnalyzing=false;
	public int wave=1;
	public int hp=10000;
	public int lastHp=10000;
	public int maxWave=40;
	public int maxHP=10000;

	public RegularArmyLeader(World w,BlockPos pos,byte id){
		this.theWorld=w;
		this.x=pos.getX();
		this.y=pos.getY();
		this.z=pos.getZ();
		this.id=id;
		this.maxWave=MRACore.maxWave;
		this.maxHP=MRACore.maxHP;
		this.faintManager=new FaintSpawnManager(this);
		this.testManager=new TestSpawnManager(this);
		this.creeperManager=new CreeperRushManager(this);
		this.analyzerManager=new TestLearningManager(this);

	}

	public RegularArmyLeader(World w,NBTTagCompound nbt,byte id){
		this.theWorld=w;
		this.id=id;
		this.maxWave=MRACore.maxWave;
		this.maxHP=MRACore.maxHP;
		this.faintManager=new FaintSpawnManager(this);
		this.testManager=new TestSpawnManager(this);
		this.creeperManager=new CreeperRushManager(this);
		this.analyzerManager=new TestLearningManager(this,nbt);

		this.readFromNBT(nbt);
	}


	public DataAnalyzer getAnalyzer(EntityRegularArmy e){
		return this.analyzerManager.getAnalyzer(e);
	}

	public DataAnalyzer getAttackerDanger(EntityRegularArmy e){
		return this.analyzerManager.getAttackerDanger(e);
	}

	public DataAnalyzerOneToOne getDistanceAnalyzer(EntityRegularArmy e){
		return this.analyzerManager.getDistanceAnalyzer(e);
	}

	public void onStart(){
		if(this.useTestManager) {
			this.currentSpawnManager=this.testManager;
		}else {
		this.currentSpawnManager=this.faintManager;
		}
		this.currentSpawnManager.onStart();
		this.hp=this.maxHP;
		this.lastHp=this.maxHP;
		this.wave=MRACore.firstWave;
	}

	public void onUpdate() {
		if(theWorld.isRemote)return;
		List<Entity> entityList=this.theWorld.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(x+0.5-2, y+0.5-2, z+0.5-2,x+0.5+2, y+0.5+2, z+0.5+2));
		for(Entity e:entityList){
			if(e instanceof EntityRegularArmy){
				this.hp-=3;
			}
		}
		if(this.hp*10<this.maxHP*3&&this.lastHp*10>=this.maxHP*3){
			MRAUtil.sendMessageAll(this.theWorld,"HP of the Base breaks 30%");

		}
		if(this.hp*10<this.maxHP*7&&this.lastHp*10>=this.maxHP*7){
			MRAUtil.sendMessageAll(this.theWorld,"HP of the Base breaks 70%");

		}
		if(this.age%4096==4095){
				MRAUtil.sendMessageAll(this.theWorld,"HP of the Base is "+this.hp*100/this.maxHP+"%");

		}
		if(this.wave==this.maxWave&&this.getEntityList().isEmpty()){
			MRAUtil.sendMessageAll(this.theWorld,"Oh yeah. You win!");


			this.onEnd();
			return;
		}
		this.lastHp=this.hp;
		if(this.hp<1){
			MRAUtil.sendMessageAll(this.theWorld,"You lose. At wave "+this.wave);

			this.onEnd();
		}
		if(this.age%60==0){
			for(int i=0;i<MRACore.results.length;i++){
				if(((this.age/60)%MRACore.resultsTier[i])==0){
					if(this.theWorld.rand.nextInt(50)<this.wave){
						EntityItem entityItem=new EntityItem(this.theWorld,this.x+0.5,this.y+1.5,this.z+0.5,new ItemStack(MRACore.results[i],1,MRACore.resultsDamage[i]));
						this.theWorld.spawnEntity(entityItem);
					}
				}
			}
		}
		if(this.isAnalyzing){
			this.analyzerManager.onUpdate();
		}else{
			this.currentSpawnManager.onUpdate();
		}
		for(int i=0;i<this.unitList.size();i++){
			this.unitList.get(i).onUpdate();
		}
		age++;
	}

	public void onEnd(){
		if(this.isAnalyzing){
			this.analyzerManager.onEnd();
		}else{
			this.currentSpawnManager.onEnd();
		}
		for(int i=0;i<this.unitList.size();i++){
			MonsterUnit unit=this.unitList.get(i);
			for(int j=0;j<unit.entityList.size();j++){
				EntityRegularArmy entity=unit.entityList.get(j);
				if(unit.getRidingEntity()!=0&&entity.getRidingEntity()!=null){
					entity.getRidingEntity().setDead();
				}
				entity.setDead();
			}

		}
		this.theWorld.setBlockState(new BlockPos(this.x,this.y,this.z),MRACore.blockBase.getDefaultState());
		MRACore.leaders[this.id]=null;
	}

	public MonsterUnit addUnit(EntityRegularArmy... entity){
		MonsterUnit unit=new MonsterUnit(this,this.wave,entity);
		this.unitList.add(unit);
		for(EntityRegularArmy i:entity){
			i.unit=unit;
		}
		return unit;
	}


	public void endPhase(){
		if(this.isAnalyzing){
			this.analyzerManager.onEnd();
			this.isAnalyzing=false;

			this.wave++;

			if(this.wave>this.maxWave){
				MRAUtil.sendMessageAll(this.theWorld,"Oh yeah. You win!");

				this.onEnd();
				return;
			}

			MRAUtil.sendMessageAll(this.theWorld,"Wave "+this.wave);

			for(int i=0;i<this.unitList.size();i++){
				if(this.unitList.get(i).wave<=this.wave-(MRACore.waveMobVanish)){
					MonsterUnit unit=this.unitList.get(i);
					for(int j=0;j<unit.entityList.size();j++){
						EntityRegularArmy entity=unit.entityList.get(j);
						if(unit.getRidingEntity()!=0&&entity.getRidingEntity()!=null){
							entity.getRidingEntity().setDead();
						}
						entity.setDead();
					}
					this.unitList.remove(i);
				}

			}
			if(!this.useTestManager) {
				if(this.creeperRushChance>this.theWorld.rand.nextFloat()) {
					this.currentSpawnManager=this.creeperManager;
					this.creeperRushChance=MRACore.creeperRushChance/2;
				}else {
					this.currentSpawnManager=this.faintManager;
					this.creeperRushChance+=MRACore.creeperRushChance/2;
				}
			}
			//System.out.println("creeperChance:"+this.creeperRushChance);
			this.currentSpawnManager.onStart();
		}else{
			this.currentSpawnManager.onEnd();
			this.isAnalyzing=true;
			this.analyzerManager.onStart();
		}

	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);
		nbt.setInteger("age", age);
		nbt.setInteger("wave", wave);
		nbt.setInteger("hp", this.hp);
		//nbt.setFloat("fightingDistance", this.fightingDistance);
		for(int i=0;i<unitList.size();i++){
			nbt.setTag("unit"+i, unitList.get(i).writeToNBT(new NBTTagCompound()));
		}
		nbt.setTag("spawnmanager1",this.faintManager.writeToNBT(new NBTTagCompound()));
		nbt.setTag("spawnmanager2",this.creeperManager.writeToNBT(new NBTTagCompound()));
		int spawnmanagerid=0;
		if(this.currentSpawnManager==this.faintManager) {
			spawnmanagerid=1;
		}else if(this.currentSpawnManager==this.creeperManager){
			spawnmanagerid=2;
		}
		nbt.setInteger("spawnmanagerid", spawnmanagerid);
		nbt.setTag("analyzer1",this.analyzerManager.writeToNBT(new NBTTagCompound()));

		return nbt;
	}

	public RegularArmyLeader readFromNBT(NBTTagCompound nbt){
		this.x=nbt.getInteger("x");
		this.y=nbt.getInteger("y");
		this.z=nbt.getInteger("z");
		this.age=nbt.getInteger("age");
		this.wave=nbt.getInteger("wave");
		this.hp=nbt.getInteger("hp");
		//this.fightingDistance=nbt.getFloat("fightingDistance");
		for(int i=0;i<25565;i++){
			if(nbt.hasKey("unit"+i)){
				this.unitList.add(new MonsterUnit(nbt.getCompoundTag("unit"+i), this));
			}else{
				break;
			}
		}
		this.faintManager.readFromNBT(nbt.getCompoundTag("spawnmanager1"));
		this.creeperManager.readFromNBT(nbt.getCompoundTag("spawnmanager2"));
		int spawnmanagerid=nbt.getInteger("spawnmanagerid");
		if(spawnmanagerid==1) {
			this.currentSpawnManager=this.faintManager;
		}else if(spawnmanagerid==2) {
			this.currentSpawnManager=this.creeperManager;
		}
		this.analyzerManager.readFromNBT(nbt.getCompoundTag("analyzer1"));
		return this;
	}

	public List<EntityRegularArmy> getEntityList() {
		List entityList=new ArrayList();
		for(int i=0;i<this.unitList.size();i++){
			entityList.addAll(this.unitList.get(i).entityList);
		}
		return entityList;
	}

	public World getTheWorld() {
		return theWorld;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}
	public float getFightingDistance() {
		return fightingDistance;
	}

	public void setFightingDistance(float fightingDistance) {
		this.fightingDistance = fightingDistance;
	}

	public List<BlockPos> getSpawnablePos(float angle,EntityRegularArmy e) {
		int spawnDistance=MRACore.spawnRange;
		int x=(int) (this.x+(spawnDistance)*MathHelper.cos(angle));
		int z=(int) (this.z+(spawnDistance)*MathHelper.sin(angle));
		int y=MRACore.minSpawnHeight;

		List<BlockPos> list=new ArrayList<BlockPos>();

		while(y<Math.min(MRACore.maxSpawnHeight,this.theWorld.getActualHeight())-e.height-1){
			y++;
			if(!MRAUtil.isBlockRidable(e,new BlockPos(x,y-1,z),true,e.width))continue;
			boolean flag=false;
			for(int m=y;m<y+e.height;m++){
				if(!MRAUtil.isBlockPassable(e,new BlockPos(x,m,z))){

					flag=true;
					break;
				}
			}
			if(!flag) {
				list.add(new BlockPos(x,y,z));
			}
		}
		return list;
	}

}

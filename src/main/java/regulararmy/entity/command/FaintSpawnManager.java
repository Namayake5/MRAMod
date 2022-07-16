package regulararmy.entity.command;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import regulararmy.core.MRACore;
import regulararmy.core.MRAEntityData;
import regulararmy.entity.EntityRegularArmy;
import regulararmy.entity.EntityScouter;
import regulararmy.pathfinding.AStarPathEntity;
import regulararmy.pathfinding.AStarPathFinder;
import regulararmy.pathfinding.AStarPathPoint;
import regulararmy.pathfinding.IPathFindRequester;
import regulararmy.util.MRAUtil;

public class FaintSpawnManager extends SpawnManagerBase implements IPathFindRequester {
	public int maxScore=Integer.MIN_VALUE;
	public int secondScore=Integer.MIN_VALUE;
	public int limitCost=Integer.MAX_VALUE;
	public List<SpawnPoint> spawnPointList;
	public Map<SpawnPoint,Float> spawnPointMap=new HashMap();

	public int spawnDistance=40;
	public SpawnPoint searchingSpawnPoint;
	public List<BlockPos> posToEval;
	public int pathFindNum;
	public AStarPathFinder theFinder;
	Map<Class<? extends EntityRegularArmy>,Float> weightMap;
	Map<Class<? extends EntityRegularArmy>,Float> subWeightMap;
	float sumOfWeight=0;
	float sumOfSubWeight=0;

	public int searchProg=0;
	public int searchSpawnTimer=0;

	public FaintSpawnManager(RegularArmyLeader leader) {
		super(leader);
		this.spawnDistance=MRACore.spawnRange;
		this.spawnPointList=new ArrayList();
		this.posToEval=new ArrayList();
	}

	@Override
	public int getTacticsCost(Entity entity, AStarPathPoint start,
			AStarPathPoint current, AStarPathPoint end) {
		return 0;
	}

	@Override
	public boolean isEngineer() {
		return true;
	}

	@Override
	public float getJumpHeight() {
		return 1.2f;
	}

	@Override
	public float getCrowdCost() {
		return 0;
	}

	@Override
	public float getFightRange() {
		return 1;
	}

	@Override
	public void onStart() {

	}

	@Override
	public void onUpdate() {
		if(this.leader.theWorld.isRemote)return;
		this.searchSpawnTimer++;
		if(this.searchProg==0){
			MRAUtil.sendMessageAll(this.leader.theWorld,"Start searching spawn point");
			this.searchProg++;
			this.pathFindNum=0;
			this.maxScore=Integer.MIN_VALUE;
			this.secondScore=Integer.MIN_VALUE;
			this.limitCost=Integer.MAX_VALUE;
			this.searchingSpawnPoint=new SpawnPoint
					(new BlockPos(this.leader.x+(this.spawnDistance),MRACore.minSpawnHeight-1,this.leader.z),0,0);
		}
		else if(this.searchProg<24)searchSpawnPoint:{
			EntityRegularArmy e=new EntityScouter(this.leader.theWorld);
			MonsterUnit unit=this.leader.addUnit(e);
			//System.out.println("prog:"+this.searchProg+" num:"+this.pathFindNum);
			if(this.pathFindNum==0){
				if(this.leader.age%4!=0){
					return;
				}
				boolean flag1=true;

				if(this.posToEval.size()==0) {
					this.posToEval=this.leader.getSpawnablePos((((float)this.searchProg)+leader.theWorld.rand.nextFloat())/12.0f*(float)Math.PI, e);
					this.searchProg++;
					if(this.posToEval.size()==0) {
						break searchSpawnPoint;
					}
				}

				this.searchingSpawnPoint=new SpawnPoint
						(this.posToEval.get(0),0,this.searchingSpawnPoint.direction);
				this.posToEval.remove(0);

				if(this.searchProg%5==0){
						MRAUtil.sendMessageAll(this.leader.theWorld,4*this.searchProg+"% over");


					break searchSpawnPoint;
				}
				this.theFinder=new AStarPathFinder(this.leader.theWorld,true,false,false,true,false,1.2f,this);
				this.theFinder.setSetting(e.getSettings());
			}

			//System.out.println("from:"+this.searchingSpawnPoint.x+","+this.searchingSpawnPoint.y+","+this.searchingSpawnPoint.z);
			e.setPosition(this.searchingSpawnPoint.pos.getX(),this.searchingSpawnPoint.pos.getY()+1,this.searchingSpawnPoint.pos.getZ());
			//int score=this.leader.theWorld.rand.nextInt(500);
			//float score=this.leader.theWorld.rand.nextFloat()*0.1f;
			float score=0;
			int cost=0;
			//finder.maxCost=this.limitCost-cost;
			AStarPathEntity path=this.theFinder.createEntityPathTo(e, this.leader.x, this.leader.y, this.leader.z, 80.0f, 1.0f,1.8f,2.8f);
			if(path==null){
				score=Integer.MIN_VALUE;
				this.pathFindNum=0;
				break searchSpawnPoint;
			}
			cost+=path.getTotalCost();
			score+=getPathScore(path,this.searchingSpawnPoint.pos.getX(), this.searchingSpawnPoint.pos.getY(), this.searchingSpawnPoint.pos.getZ());
			/*System.out.println("length:"+path.getCurrentPathLength()+" cost:"+path.getTotalCost()+
					" score:"+getPathScore(path,this.searchingSpawnPoint.x, this.searchingSpawnPoint.y, this.searchingSpawnPoint.z));
			 */
			//if(score<this.maxScore)break;
			for(int i1=5;i1<path.points.length-5;i1++){
				this.theFinder.unusablePoints.add(path.getPathPointFromIndex(i1).toCoord());
			}
			this.searchingSpawnPoint.score+=score;
			this.pathFindNum++;
			this.leader.unitList.remove(unit);
			if(this.pathFindNum<2){
				break searchSpawnPoint;
			}
			this.spawnPointList.add(this.searchingSpawnPoint);
			this.pathFindNum=0;

		}else if(this.searchProg==24){
			if (this.spawnPointList.size() == 0) {
				MRAUtil.sendMessageAll(this.leader.theWorld,"Couldn't find suitable spawnpoint");
				this.leader.onEnd();
				return;
			}
			float scoreSum=0;
			for(SpawnPoint sp:this.spawnPointList){
				scoreSum+=sp.score;
			}
			for(SpawnPoint sp:this.spawnPointList){
				this.spawnPointMap.put(sp,sp.score/scoreSum);
				System.out.println("("+(int)sp.pos.getX()+","+sp.pos.getY()+","+(int)sp.pos.getZ()+") weight:"+(sp.score/scoreSum));
			}

			List<EntityPlayer> players=this.leader.theWorld.playerEntities;
			for(int i=0;i<players.size();i++){
				MRAUtil.sendMessageAll(this.leader.theWorld,"End searching");
			}
			this.searchProg++;
			/*
		}else if(this.searchProg==25){
			EntityRegularArmy e1,e2,e3;
			e1=new EntityFastZombie(this.leader.theWorld);
			e2=new EntityFastZombie(this.leader.theWorld);
			e3=new EntityEngineer(this.leader.theWorld);
			e1.setPosition(spawnX, spawnY, spawnZ);
			e2.setPosition(spawnX, spawnY, spawnZ);
			e3.setPosition(spawnX, spawnY, spawnZ);
			MonsterUnit u=this.leader.addUnit(e1,e2,e3);
			u.spawnAll();
			this.searchProg++;
		}else if(this.searchProg==26){
			EntityRegularArmy e1,e2,e3;
			e1=new EntitySniperSkeleton(this.leader.theWorld);
			e2=new EntitySniperSkeleton(this.leader.theWorld);
			e3=new EntityEngineer(this.leader.theWorld);
			e1.setPosition(spawnX, spawnY, spawnZ);
			e2.setPosition(spawnX, spawnY, spawnZ);
			e3.setPosition(spawnX, spawnY, spawnZ);
			MonsterUnit u=this.leader.addUnit(e1,e2,e3);
			u.spawnAll();
			this.searchProg++;
			 */
		}else if(this.searchProg==25){
			this.makeWeightMap(this.leader.wave);

			this.searchProg++;
		}else if(this.searchProg>25&&this.searchProg<=(29+this.leader.wave/2)&&this.leader.age%(MRACore.spawnInterval/MRACore.unitMultiplier)==0){
			float weightSumTo=this.leader.getTheWorld().rand.nextFloat();
			float weightSum=0;
			SpawnPoint p=this.spawnPointList.get(0);
			for(int i=0;i<this.spawnPointList.size();i++){
				p=this.spawnPointList.get(i);
				weightSum+=this.spawnPointMap.get(p);
				if(weightSum>weightSumTo){
					break;
				}
			}
			//System.out.println("Spawn at:"+p.x+","+p.y+","+p.z);
			this.makeRandomUnit(p.pos.getX(),p.pos.getY(),p.pos.getZ(),(byte)this.leader.wave).spawnAll();
			this.searchProg++;
		}else if(this.searchProg>(29+this.leader.wave/2)){
			this.searchProg++;
			if(this.searchProg==MRACore.waveInterval+(this.leader.wave==this.leader.maxWave?4000:0)){
				if(this.searchSpawnTimer>6000){
					this.searchSpawnTimer=0;
					this.searchProg=0;
				}else{
					this.searchProg=25;
				}
				this.leader.endPhase();
			}
		}
	}

	@Override
	public void onEnd() {

	}

	public MonsterUnit makeRandomUnit(double x,double y,double z,byte tier){
		if(weightMap==null||subWeightMap==null){
			this.makeWeightMap(tier);
		}
		List<EntityRegularArmy> list=new ArrayList();

		float rand=this.leader.getTheWorld().rand.nextFloat()*this.sumOfWeight;
		float subRand=this.sumOfSubWeight<1?this.leader.getTheWorld().rand.nextFloat():this.leader.getTheWorld().rand.nextFloat()*this.sumOfSubWeight;
		for(Entry<Class<? extends EntityRegularArmy>,Float> entry:weightMap.entrySet()){
			if(rand<entry.getValue()){
				//System.out.println("spawn "+entry.getKey().getSimpleName());
				for(int i=0;i<MRAEntityData.classToData.get(entry.getKey()).numberOfMember;i++){
					try {
						EntityRegularArmy e=entry.getKey().getConstructor(World.class).newInstance(this.leader.theWorld);
						e.setPosition(x, y, z);
						list.add(e);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				}
				for(Entry<Class<? extends EntityRegularArmy>,Float> entryS:subWeightMap.entrySet()){
					if(subRand<entryS.getValue()){
						for(int i=0;i<-MRAEntityData.classToData.get(entryS.getKey()).numberOfMember;i++){
							try {
								EntityRegularArmy e=entryS.getKey().getConstructor(World.class).newInstance(this.leader.theWorld);
								e.setPosition(x, y, z);
								list.add(e);
							} catch (InstantiationException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							} catch (NoSuchMethodException e) {
								e.printStackTrace();
							} catch (SecurityException e) {
								e.printStackTrace();
							}
						}
						break;
					}
					subRand-=entryS.getValue();
				}
				break;
			}
			rand-=entry.getValue();
		}
		/*
		int type=this.leader.theWorld.rand.nextInt(4);
		for(int i=0;i<2+tier/8;i++){
			EntityRegularArmy e=new EntityFastZombie(this.leader.theWorld);
			if(type==3){
				e=new EntityCreeperR(this.leader.theWorld);
				e.setPosition(x, y, z);
				list.add(e);
				break;
			}
			switch(type){
			case 0:
				e=new EntitySkeletonR(this.leader.theWorld);
				break;
			case 1:
				e=new EntityFastZombie(this.leader.theWorld);
				break;
			case 2:
				e=new EntitySniperSkeleton(this.leader.theWorld);
				break;

			}
			this.setEquipmentBasedOnDifficultyFromTier(e, tier);
			this.enchantEquipmentFromTier(e, tier);
			e.setPosition(x, y, z);
			list.add(e);
		}
		if(this.leader.theWorld.rand.nextBoolean()){
			EntityRegularArmy e=new EntityEngineer(this.leader.theWorld);
			this.setEquipmentBasedOnDifficultyFromTier(e, tier);
			this.enchantEquipmentFromTier(e, tier);
			e.setPosition(x, y, z);
			list.add(e);
		}
		 */
		MonsterUnit unit=this.leader.addUnit(list.toArray(new EntityRegularArmy[0]));
		unit.setEquipmentsFromTier(tier);
		return unit;
	}

	public void makeWeightMap(int tier){
		weightMap=new HashMap();
		subWeightMap=new HashMap();
		this.sumOfSubWeight=0;
		this.sumOfWeight=0;
		for(Class<? extends EntityRegularArmy> c:MRACore.entityList){
			float weight=this.getWeight(this.leader.wave, c);
			int num=MRAEntityData.classToData.get(c).numberOfMember;
			if(num<0){
				subWeightMap.put(c, weight);
				sumOfSubWeight+=weight;
				System.out.println(c.getSimpleName()+"(SUB)'s weight: "+weight);
			}else if(num!=0){
				weightMap.put(c, weight);
				sumOfWeight+=weight;
				System.out.println(c.getSimpleName()+"'s weight: "+weight);
			}

		}
	}

	public float getWeight(int tier,Class<? extends EntityRegularArmy> c){
		MRAEntityData data=MRAEntityData.classToData.get(c);
		float distanceModifier = 1.0F;
		return data.centreTier>tier?
				(data.minTier>tier?0:0.1f+data.basicWeight*(tier-data.minTier)/(data.centreTier-data.minTier)*distanceModifier)
				:data.maxTier<tier?0:0.1f+data.basicWeight*(data.maxTier-tier)/(data.maxTier-data.centreTier)*distanceModifier;
	}


	public float getPathScore(AStarPathEntity path,float x,float y,float z){
		float relativex=MathHelper.abs(x-this.leader.x);
		float relativey=MathHelper.abs(y-this.leader.y);
		float relativez=MathHelper.abs(z-this.leader.z);
		float relativex2=MathHelper.abs(this.leader.x-path.getFinalPathPoint().x);
		float relativey2=MathHelper.abs(this.leader.y-path.getFinalPathPoint().y);
		float relativez2=MathHelper.abs(this.leader.z-path.getFinalPathPoint().z);
		return ((float)path.getCurrentPathLength()/(float)path.getTotalCost()/(float)(MathHelper.sqrt(relativex2*relativex2+relativey2*relativey2+relativez2*relativez2)*(this.spawnDistance)/(relativex+relativey+relativez)));
	}


	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		this.searchSpawnTimer=nbt.getInteger("searchTimer");
		for(int i=0;true;i++){
			if(!nbt.hasKey("spawnPoint"+i))break;
			SpawnPoint sp=new SpawnPoint(new BlockPos(0,0,0),0,0).readFromNBT(nbt.getCompoundTag("spawnPoint"+i));
			this.spawnPointList.add(sp);
			this.spawnPointMap.put(sp, nbt.getFloat("spawnWeight"+i));
		}
		this.searchProg=nbt.getInteger("searchProg");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("searchTimer", this.searchSpawnTimer);
		if(this.searchProg<24){
			nbt.setInteger("searchProg", 0);
		}else{
			for(int i=0;i<this.spawnPointList.size();i++){
				NBTTagCompound nbt_=new NBTTagCompound();
				this.spawnPointList.get(i).writeToNBT(nbt_);
				nbt.setTag("spawnPoint"+i, nbt_);
				nbt.setFloat("spawnWeight"+i, this.spawnPointMap.get(this.spawnPointList.get(i)));
			}
			nbt.setInteger("searchProg", this.searchProg);
		}
		return nbt;
	}

	public static class SpawnPoint{
		public BlockPos pos;
		public float score;
		public float direction;
		public SpawnPoint(BlockPos pos,float score,float direction){
			this.pos=pos;
			this.score=score;
			this.direction=direction;
		}
		public SpawnPoint readFromNBT(NBTTagCompound nbt){
			this.pos=new BlockPos(nbt.getInteger("x"),nbt.getInteger("y"),nbt.getInteger("z"));
			this.score=nbt.getFloat("score");
			this.direction=nbt.getFloat("direction");
			return this;
		}
		public void writeToNBT(NBTTagCompound nbt){
			nbt.setInteger("x", this.pos.getX());
			nbt.setInteger("y", this.pos.getY());
			nbt.setInteger("z", this.pos.getZ());
			nbt.setFloat("score", this.score);
			nbt.setFloat("direction", this.direction);
		}
	}
}

package regulararmy.entity.ai;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import regulararmy.core.MRACore;
import regulararmy.entity.EntityRegularArmy;
import regulararmy.entity.ai.EngineerRequest.RequestType;
import regulararmy.pathfinding.AStarPathEntity;
import regulararmy.pathfinding.AStarPathFinder;
import regulararmy.pathfinding.AStarPathPoint;
import regulararmy.pathfinding.IPathFindRequester;
import regulararmy.util.MRAUtil;

public class EntityAIBreakBlock extends EntityRegularAIBase implements IPathFindRequester{
	public int thePoint;
	public EngineerRequest target;
	public int currentMyTarget;
	public EntityRegularArmy entity;
	public World world;
	public AStarPathEntity entityPathEntity;
	public boolean isSlow=false;
	public boolean hasDone=true;

	//public List<EngineerRequest> setRequests;
	//public List<EngineerRequest> breakRequests;

	public EntityAIBreakBlock(EntityRegularArmy b){
		this.entity=b;
		if(!(b instanceof IBreakBlocksMob)){
			throw new IllegalArgumentException("EntityAIBreakBlock requires Mob implements IBreakBlocksMob");
		}
		this.world=b.world;
		this.setMutexBits(3);
	}
	@Override
	public int getTacticsCost(Entity entity, AStarPathPoint start,
			AStarPathPoint current, AStarPathPoint end) {
		return 0;
	}

	@Override
	public boolean shouldExecute() {
		if(Math.abs(this.entity.motionY)>0.3)return false;
		this.target=this.entity.unit.getRequestManager().getNearest(this.entity);
		if(this.target==null){
			if(this.entity.getAttackTarget()==null){
				this.entityPathEntity=this.entity.getANavigator().getPathToXYZ
						(this.entity.unit.leader.x,this.entity.unit.leader.y,this.entity.unit.leader.z,1.2f,this);
			}else{
				EntityLivingBase e=this.entity.getAttackTarget();
				this.entityPathEntity=this.entity.getANavigator().getPathToXYZ
						(e.posX,e.posY,e.posZ,1.2f,this);
			}

			return this.entityPathEntity!=null;
		}else{
			this.world.profiler.startSection("pathfind");
			int l = MathHelper.floor(this.entity.posX);
			int i1 = MathHelper.floor(this.entity.posY);
			int j1 = MathHelper.floor(this.entity.posZ);
			int k1 = (int)(64 + 16.0F);
			int l1 = l - k1;
			int i2 = i1 - k1;
			int j2 = j1 - k1;
			int k2 = l + k1;
			int l2 = i1 + k1;
			int i3 = j1 + k1;
			ChunkCache chunkcache = new ChunkCache(this.world, new BlockPos(l1, i2, j2), new BlockPos(k2, l2, i3), 0);
			AStarPathFinder finder=new AStarPathFinder(chunkcache, true,false,false,true,
					!this.isSlow,1.4f,this);
			finder.setSetting(this.entity.getSettings());
			finder.unusablePoints.add(this.target.waitingPoint);
			this.entityPathEntity= finder.createEntityPathTo(this.entity,target.coord.getX(), MathHelper.floor(target.coord.getY()-this.entity.getEyeHeight()), target.coord.getZ(), this.entity.getANavigator().getPathSearchRange(),2.0f);
			this.world.profiler.endSection();

			/*
			this.entityPathEntity=this.entity.getANavigator().getPathToXYZ
				(target.coord.getX()+0.5, target.coord.getY()-this.entity.getEyeHeight()+0.5, target.coord.getZ()+0.5,1.0f,this);
			 */
			//System.out.println("should:"+(this.entityPathEntity!=null));
			return this.entity.unit.getRequestManager().isThereNotApproved()&&this.entityPathEntity!=null;
		}
	}

	@Override
	public boolean shouldContinueExecuting(){
		//System.out.println("continue:"+!hasDone);
		return !hasDone&&this.entityPathEntity!=null&&!this.entity.getANavigator().noPath();
	}

	@Override
	public void startExecuting(){
		if(this.target!=null)this.target.approve();
		//System.out.println(this.target.coord.getX()+","+this.target.coord.getY()+","+this.target.coord.getZ());
		this.entity.getANavigator().setPath(this.entityPathEntity, 1.2);
		this.hasDone=false;
	}

	@Override
	public void resetTask(){
		this.entityPathEntity=null;
	}

	@Override
	public void updateTask(){

		if(this.target!=null&&!this.target.isEnable){
			this.hasDone=true;
			return;
		}
		/*
		//System.out.println(this.entity.getDistanceSq(target.coord.getX()+0.5, target.coord.getY()-this.entity.getEyeHeight()+0.5, target.coord.getZ()+0.5));
		if(this.target!=null&&this.entity.getDistanceSq(target.coord.getX()+0.5, target.coord.getY()-this.entity.getEyeHeight()+0.5, target.coord.getZ()+0.5)<12){
			if(!this.target.isSet){
				if(this.isSlow){
					this.thePoint++;
				}else{
					this.thePoint+=3;
				}
				if(this.world.getBlockState(new BlockPos(target.coord.getX(), target.coord.getY(), target.coord.getZ())).getBlock()==Blocks.AIR
						||this.thePoint/30>((IBreakBlocksMob)entity).getblockStrength
					(this.world.getBlockState(new BlockPos(target.coord.getX(), target.coord.getY(), target.coord.getZ())).getBlock(),
							this.world, target.coord.getX(), target.coord.getY(), target.coord.getZ())){
					boolean flag=true;
					for(int i=0;i<MonsterRegularArmyCore.blocksDoNotDrop.length;i++){
						Block b=MonsterRegularArmyCore.blocksDoNotDrop[i];
						if(b==this.world.getBlockState(new BlockPos(target.coord.getX(), target.coord.getY(), target.coord.getZ())).getBlock()&&(MonsterRegularArmyCore.blocksDoNotDropMeta[i]==-1||MonsterRegularArmyCore.blocksDoNotDropMeta[i]==this.world.getBlockMetadata(target.coord.getX(), target.coord.getY(), target.coord.getZ()))){
							flag=false;
							break;
						}
					}
					this.world.func_147480_a(target.coord.getX(), target.coord.getY(), target.coord.getZ(), flag);
					this.hasDone=true;
					this.thePoint=0;
					this.target.fulfill();
				}
			}else {
				this.world.setBlock(target.coord.getX(), target.coord.getY(), target.coord.getZ(),MonsterRegularArmyCore.blockMonster);
				this.hasDone=true;
				this.target.fulfill();
			}
		}
		 */
		List<EngineerRequest> targets=this.entityPathEntity.getCurrentPoint().requests;
		List<EngineerRequest> targetsTemp=this.entityPathEntity.getCurrentPoint().requestsTemp;
		if(this.isSlow){
			this.thePoint++;
		}else{
			this.thePoint+=3;
		}
		//System.out.println("point:"+this.thePoint);
		boolean shouldPutBlock=false;
		for(int i=0;i<targets.size();i++){
			EngineerRequest t=targets.get(i);
			if(t.isSet!=RequestType.BREAK){
				shouldPutBlock=true;
				break;
			}
		}
		if(targetsTemp!=null) {
			try {
				Collections.sort(targetsTemp, new RequestComparator(this.entity));
			} catch (Exception e) {}

			for(int i=0;i<targetsTemp.size();i++){
				EngineerRequest t=targetsTemp.get(i);
				if(t.getSquareDistance(this.entity.posX,this.entity.posY,this.entity.posZ)<10){

					if(t.isSet==RequestType.PUT_BLOCK){
						if (this.world.getBlockState(t.coord).isOpaqueCube()) {
							targetsTemp.remove(i);
						}else if(this.thePoint>50){
							this.thePoint=0;
							this.world.setBlockState(t.coord,MRACore.blockMonster.getDefaultState());
							targetsTemp.remove(i);
						}
					}else if(t.isSet==RequestType.PUT_LADDER){
						if(this.thePoint>50){
							this.thePoint=0;

							this.world.setBlockState(t.coord,Blocks.LADDER.getStateForPlacement(this.world, t.coord, t.dir==null?EnumFacing.EAST:t.dir, 0,0,0, 0,this.entity,EnumHand.MAIN_HAND));

							targetsTemp.remove(i);
						}
					}else if(!shouldPutBlock){

						if(MRAUtil.isAir(this.world.getBlockState(t.coord))){

							targetsTemp.remove(i);
						}else{
							//System.out.println("trying breaking id:"+this.world.getBlockId(t.coord.getX(), t.coord.getY(), t.coord.getZ())+" on:"+t.coord.getX()+","+ t.coord.getY()+","+ t.coord.getZ());

							if(this.thePoint>60 &&this.thePoint/10>((IBreakBlocksMob)entity).getblockStrength(entity.getEntityWorld().getBlockState(t.coord),entity.getEntityWorld(),t.coord)){
								boolean flag=true;
								for(Block b:MRACore.blocksDoNotDrop){
									if(b==this.world.getBlockState(new BlockPos(t.coord.getX(), t.coord.getY(), t.coord.getZ())).getBlock()){
										flag=false;
										break;
									}
								}
								this.world.destroyBlock(t.coord, flag);
								this.thePoint=0;
								targetsTemp.remove(i);

							}
						}
					}
				}

			}
		}
		if(targets!=null) {
			Collections.sort(targets, new RequestComparator(this.entity));

			for(int i=0;i<targets.size();i++){
				EngineerRequest t=targets.get(i);
				if(t.isSet==RequestType.PUT_BLOCK){
					if (this.world.getBlockState(t.coord).isOpaqueCube()) {
						targets.remove(i);
					}else if(this.thePoint>50){
						this.thePoint=0;
						this.world.setBlockState(t.coord,MRACore.blockMonster.getDefaultState());
						targets.remove(i);
					}
				}else if(t.isSet==RequestType.PUT_LADDER){
					if(this.thePoint>50){
						this.thePoint=0;

						this.world.setBlockState(t.coord,Blocks.LADDER.getStateForPlacement(this.world, t.coord, t.dir==null?EnumFacing.EAST:t.dir, 0,0,0, 0,this.entity,EnumHand.MAIN_HAND));

						targets.remove(i);
					}
				}else if(!shouldPutBlock){
					if(this.entity.getDistanceSq
							(t.coord.getX()+0.5, t.coord.getY()-this.entity.getEyeHeight()+0.5, t.coord.getZ()+0.5)<16){
						if(MRAUtil.isAir(this.world.getBlockState(new BlockPos(t.coord.getX(), t.coord.getY(), t.coord.getZ())))){

							targets.remove(i);
						}else{
							//System.out.println("trying breaking id:"+this.world.getBlockId(t.coord.getX(), t.coord.getY(), t.coord.getZ())+" on:"+t.coord.getX()+","+ t.coord.getY()+","+ t.coord.getZ());

							if(this.thePoint>60 &&this.thePoint/10>((IBreakBlocksMob)entity).getblockStrength(entity.getEntityWorld().getBlockState(t.coord),entity.getEntityWorld(),t.coord)){
								boolean flag=true;
								for(Block b:MRACore.blocksDoNotDrop){
									if(b==this.world.getBlockState(new BlockPos(t.coord.getX(), t.coord.getY(), t.coord.getZ())).getBlock()){
										flag=false;
										break;
									}
								}
								this.world.destroyBlock(t.coord, flag);
								this.thePoint=0;
								targets.remove(i);
							}
						}
					}
				}
			}
		}


	}

	@Override
	public boolean isEngineer(){
		return true;
	}

	@Override
	public float getJumpHeight() {
		return this.entity.data.jumpHeight;
	}

	@Override
	public float getCrowdCost() {
		return this.entity.data.crowdCostPerBlock;
	}

	@Override
	public float getFightRange() {
		return this.entity.data.fightRange;
	}

	public static class RequestComparator implements Comparator<EngineerRequest> {
		public RequestComparator(EntityRegularArmy entity) {
			this.entity = entity;
		}
		public EntityRegularArmy entity;
		public int compare(EngineerRequest o1, EngineerRequest o2) {
			if (o1.getSquareDistance(this.entity.posX,this.entity.posY,this.entity.posZ) > o2.getSquareDistance(this.entity.posX,this.entity.posY,this.entity.posZ))
				return 1;
			return -1;
		}
	}
}

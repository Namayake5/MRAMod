package regulararmy.pathfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import regulararmy.analysis.FinderSettings;
import regulararmy.entity.EntityRegularArmy;
import regulararmy.entity.ai.EntityAIBreakBlock;
import regulararmy.entity.ai.IBreakBlocksMob;
import regulararmy.util.MRAUtil;

public class AStarPathFinder
{
	/** Used to find obstacles */
	private IBlockAccess worldMap;

	/** The points in the path */
	private IntHashMap pointMap = new IntHashMap();

	/** Selection of path points to add to the path */
	private AStarPathPoint[] pathOptions = new AStarPathPoint[32];

	/** should the PathFinder go through wodden door blocks */
	private boolean isWoddenDoorAllowed;

	/**
	 * should the PathFinder disregard BlockMovement type materials in its path
	 */
	private boolean isMovementBlockAllowed;
	private boolean canSwim;

	/** tells the FathFinder to not stop pathing underwater */
	private boolean canEntityDrown;

	public Map<Integer,Float> usedCrowdCost=new HashMap();

	public float jumpheight;

	public IPathFindRequester ai;

	private boolean lowerEngineerCost=false;

	private List<AStarPathPoint> openedPointList;

	private int firstDistanceFromDestination;

	public boolean canUseEngineer=true;

	public FinderSettings settings;

	public List<BlockPos> unusablePoints=new ArrayList();

	public int maxCost=Integer.MAX_VALUE;

	public int maxLength=Integer.MAX_VALUE;

	public float entityWidth=1.8f;

	public float entityHeight=1.8f;

	public int entitySize;

	public int baseTickToNext;

	public static boolean logPath = false;

	public AStarPathFinder(IBlockAccess par1IBlockAccess, boolean par2, boolean par3, boolean par4, boolean par5,boolean lowerEngineerCost,float par6,IPathFindRequester ai)
	{
		this.worldMap = par1IBlockAccess;
		this.isWoddenDoorAllowed = par2;
		this.isMovementBlockAllowed = par3;
		this.canSwim = par4;
		this.canEntityDrown = par5;
		this.jumpheight=par6;
		this.lowerEngineerCost=lowerEngineerCost;

		this.ai=ai;
	}

	/**
	 * Creates a path from one entity to another within a minimum distance
	 */
	public AStarPathEntity createEntityPathTo(Entity entity, Entity par2Entity, float par3,float par4)
	{

		return this.createEntityPathTo(entity, par2Entity.posX, par2Entity.getEntityBoundingBox().minY, par2Entity.posZ, par3,par4,entity.width,entity.height);
	}

	/**
	 * Creates a path from an entity to a specified location within a minimum distance
	 */
	public AStarPathEntity createEntityPathTo(Entity entity, int par2, int par3, int par4, float par5,float par6)
	{
		return this.createEntityPathTo(entity, (double)((float)par2 + 0.5F), (double)((float)par3 + 0.5F), (double)((float)par4 + 0.5F), par5,par6,entity.width,entity.height);
	}

	/**
	 * Internal implementation of creating a path from an entity to a point
	 */
	public AStarPathEntity createEntityPathTo(Entity par1Entity, double destX, double destY, double destZ, float maxDistance,float minDistance,float width,float height)
	{
		this.entityHeight=height;
		this.entityWidth=width;
		if(par1Entity instanceof EntityLivingBase){
			this.baseTickToNext=(int) (1/( (EntityLivingBase)par1Entity).getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
		}

		//System.out.println("from"+par1Entity.posX+","+par1Entity.posY+","+par1Entity.posZ);
		//System.out.println("for"+par2+","+par4+","+par6);
		/*
    	if(!this.canUseEngineer&&
    			!this.isBlockRidable(par1Entity,  MathHelper.floor(destX ), MathHelper.floor(destY)-1, MathHelper.floor(destZ ))){
    		return null;
    	}
		 */
		//System.out.println("from"+par1Entity.posX+","+par1Entity.posY+","+par1Entity.posZ);
		//System.out.println("for"+par2+","+par4+","+par6);
		this.entitySize=this.entityWidth<1?1:2;
		this.canUseEngineer=this.ai.isEngineer();
		this.openedPointList=new ArrayList((int) (maxDistance*32));
		this.pointMap.clearMap();
		this.usedCrowdCost.clear();
		boolean flag = this.canSwim;
		int i = MathHelper.floor(par1Entity.getEntityBoundingBox().minY + 0.5D);

		if (this.canEntityDrown && par1Entity.isInWater())
		{
			i = (int)par1Entity.getEntityBoundingBox().minY;

			for (IBlockState j = this.worldMap.getBlockState(new BlockPos(MathHelper.floor(par1Entity.posX), i, MathHelper.floor(par1Entity.posZ)));
					MRAUtil.isLiquid(j);
					j = this.worldMap.getBlockState(new BlockPos(MathHelper.floor(par1Entity.posX), i, MathHelper.floor(par1Entity.posZ))))
			{
				++i;
			}

			flag = this.canSwim;
			this.canSwim = false;
		}
		else
		{
			i = MathHelper.floor(par1Entity.getEntityBoundingBox().minY + 0.5D);
		}
		int x1=MathHelper.floor(par1Entity.posX),z1=MathHelper.floor(par1Entity.posZ);
		AxisAlignedBB aabb=par1Entity.getEntityBoundingBox();
		//System.out.println("entityAABB:"+aabb);
		if(this.entitySize==1){
			for(int x=MathHelper.floor(aabb.minX+0.1);x<aabb.maxX-0.1;x++){
				for(int z=MathHelper.floor(aabb.minZ+0.1);z<aabb.maxZ-0.1;z++){
					if(MRAUtil.isBlockRidable(par1Entity, new BlockPos(x, i-1, z),this.canSwim,this.entityWidth)){
						x1=x;
						z1=z;
					}
				}
			}
		}else{
			for(int x=MathHelper.ceil(aabb.minX+0.1);x<aabb.maxX-0.1;x++){
				for(int z=MathHelper.ceil(aabb.minZ+0.1);z<aabb.maxZ-0.1;z++){
					if(MRAUtil.isBlockRidable(par1Entity, new BlockPos(x, i-1, z),this.canSwim,this.entityWidth)){
						x1=x;
						z1=z;
					}
				}
			}
		}
		AStarPathPoint AStarPathPoint = new AStarPathPoint(x1, i, z1);
		AStarPathPoint AStarPathPoint1 = new AStarPathPoint(MathHelper.floor(destX), MathHelper.floor(destY), MathHelper.floor(destZ));
		AStarPathEntity pathentity;
		pathentity = this.addToPath(par1Entity, AStarPathPoint, AStarPathPoint1,  maxDistance,minDistance,this.entitySize);
		if (pathentity != null && pathentity.getCurrentPathLength() < 2) return null;
		this.canSwim = flag;
		/*
        boolean flag1=false;
        for(int j=0;j<pathentity.points.length;j++){
        	if(pathentity.points[j].blocksToBreak.length!=0){
        		flag1=true;
        		break;
        	}
        }
		 */
		/*
		if(pathentity!=null){
			for(int i1=0;i1<pathentity.getCurrentPathLength();i1++){
				System.out.println
				(i1+": "+pathentity.getPathPointFromIndex(i1).x+","+pathentity.getPathPointFromIndex(i1).y+"("+pathentity.getPathPointFromIndex(i1).yOffset+"),"+pathentity.getPathPointFromIndex(i1).z
						+" put:"+pathentity.getPathPointFromIndex(i1).numberOfBlocksToPut+" break:"+pathentity.getPathPointFromIndex(i1).numberOfBlocksToBreak);

			}
			//System.out.println(pathentity.getTotalCost());
		}
		 */
		if (logPath && pathentity != null) {
		       for (int i1 = 0; i1 < pathentity.getCurrentPathLength(); i1++) {
		         AStarPathPoint point = pathentity.getPathPointFromIndex(i1);
		         System.out.println(i1 + ": " + point.x + "," + point.y + "(" + point.yOffset + ")," + point.z + " put:" + point.numberOfBlocksToPut + " break:" + point.numberOfBlocksToBreak);


		         if (point.numberOfBlocksToPut > 0) {
		           System.out.println("Put at");
		           for (int j = 0; j < point.numberOfBlocksToPut; j++) {
		             System.out.println(point.blocksToPut[j]);
		           }
		         }
		         if (point.numberOfBlocksToBreak > 0) {
		           System.out.println("Break at");
		           for (int j = 0; j < point.numberOfBlocksToBreak; j++) {
		             System.out.println(point.blocksToBreak[j]);
		           }
		         }
		       }
		}
		return pathentity;
	}



	private AStarPathEntity addToPath(Entity par1Entity, AStarPathPoint startPathPoint, AStarPathPoint endPathPoint, float maxDistance,float par6,int size)
	{
		startPathPoint.totalCost = 0;
		startPathPoint.distanceToNext = startPathPoint.func_75832_b(endPathPoint);
		startPathPoint.distanceToTarget = startPathPoint.distanceToNext;
		AStarPathPoint headPathPoint=startPathPoint;
		AStarPathPoint nearestPathPoint=startPathPoint;
		this.openedPointList.add(headPathPoint);
		headPathPoint.index=0;
		headPathPoint.isHead=true;

		//Calculate yOffset of the starting point
		double underMaxY=this.getMaxY(par1Entity.world,par1Entity, new BlockPos(startPathPoint.x,startPathPoint.y-1,startPathPoint.z),size);
		if(Double.isNaN(underMaxY)){
			//System.out.println("point"+startPathPoint.toCoord().toString()+"is air");
			startPathPoint.yOffset=-1;

		}else{
			//System.out.println("point"+startPathPoint.toCoord().toString()+"is solid("+(underMaxY-(double)startPathPoint.y)+")");
			startPathPoint.yOffset=(float)underMaxY-(float)startPathPoint.y;
		}
		//System.out.println("underMaxY:"+underMaxY);

		{
			if (startPathPoint.equals(endPathPoint))
			{
				return this.createEntityPath(startPathPoint, startPathPoint,true,this.entitySize,par1Entity);
			}
			{
				float f = (float)(endPathPoint.x - headPathPoint.x);
				float f1 = (float)(endPathPoint.y - headPathPoint.y);
				float f2 = (float)(endPathPoint.z - headPathPoint.z);
				if(f * f + f1 * f1 + f2 * f2<par6*par6){
					return this.createEntityPath(startPathPoint, startPathPoint,true,this.entitySize,par1Entity);
				}

			}
			/**
			 * bit0:EAST(X+)
			 * bit1:WEST(X-)
			 * bit2:SOUTH(Z+)
			 * bit3:NORTH(Z-)*/
			byte binary=0x0f;
			if(size==1){
				for(int y=startPathPoint.y;y<startPathPoint.y+par1Entity.height;y++){
					BlockPos pos=new BlockPos(startPathPoint.x, y,startPathPoint.z);
					IBlockState b=par1Entity.world.getBlockState(pos);
					if(!b.isFullCube()&&par1Entity.getEntityBoundingBox()!=null&&b.getCollisionBoundingBox(par1Entity.world, pos)!=null){
						List<AxisAlignedBB> aabbList=MRAUtil.getCollisionListAt(par1Entity, pos);

						for(int i=0;i<aabbList.size();i++){
							boolean flagEast=(aabbList.get(i).minX>par1Entity.getEntityBoundingBox().maxX);
							boolean flagWest=(aabbList.get(i).maxX<par1Entity.getEntityBoundingBox().minX);
							boolean flagSouth=(aabbList.get(i).minZ>par1Entity.getEntityBoundingBox().maxZ);
							boolean flagNorth=(aabbList.get(i).maxZ<par1Entity.getEntityBoundingBox().minZ);
							if(!flagEast&&!flagWest){
								if(flagSouth){
									binary&=0xfb;
								}else if(flagNorth){
									binary&=0xf7;
								}
							}
							if(!flagSouth&&!flagNorth){
								if(flagEast){
									binary&=0xfe;
								}else if(flagWest){
									binary&=0xfd;
								}
							}
						}
					}
				}
			}else{


				for(int y=startPathPoint.y;y<startPathPoint.y+par1Entity.height;y++){
					for(int x=startPathPoint.x;x>=startPathPoint.x-1;x--){
						for(int z=startPathPoint.z;z>=startPathPoint.z+1;z--){
							BlockPos pos=new BlockPos(startPathPoint.x, y,startPathPoint.z);
							IBlockState b=par1Entity.world.getBlockState(pos);
							if(!b.isOpaqueCube()&&par1Entity.getEntityBoundingBox()!=null&&b.getCollisionBoundingBox(par1Entity.world,pos)!=null){
								List<AxisAlignedBB> aabbList=MRAUtil.getCollisionListAt(par1Entity, pos);
								//ver1.12.2
								for(int i=0;i<aabbList.size();i++){
									boolean flagEast=(aabbList.get(i).minX>par1Entity.getEntityBoundingBox().maxX);
									boolean flagWest=(aabbList.get(i).maxX<par1Entity.getEntityBoundingBox().minX);
									boolean flagSouth=(aabbList.get(i).minZ>par1Entity.getEntityBoundingBox().maxZ);
									boolean flagNorth=(aabbList.get(i).maxZ<par1Entity.getEntityBoundingBox().minZ);
									if(!flagEast&&!flagWest){
										if(flagSouth){
											binary&=0xfb;
										}else if(flagNorth){
											binary&=0xf7;
										}
									}
									if(!flagSouth&&!flagNorth){
										if(flagEast){
											binary&=0xfe;
										}else if(flagWest){
											binary&=0xfd;
										}
									}
								}
							}
						}
					}
				}
			}
			double maxheight=startPathPoint.y+startPathPoint.yOffset+this.jumpheight;
			if((binary&0x01)>0){
				this.openPointSide(par1Entity, startPathPoint, endPathPoint, startPathPoint.x+1, MathHelper.ceil(maxheight), startPathPoint.z, (float) maxheight,size);
			}
			if((binary&0x02)>0){
				this.openPointSide(par1Entity, startPathPoint, endPathPoint, startPathPoint.x-1, MathHelper.ceil(maxheight), startPathPoint.z, (float) maxheight,size);
			}
			if((binary&0x04)>0){
				this.openPointSide(par1Entity, startPathPoint, endPathPoint, startPathPoint.x, MathHelper.ceil(maxheight), startPathPoint.z+1, (float) maxheight,size);
			}
			if((binary&0x08)>0){
				this.openPointSide(par1Entity, startPathPoint, endPathPoint, startPathPoint.x, MathHelper.ceil(maxheight), startPathPoint.z-1, (float) maxheight,size);
			}
			this.openPointSide(par1Entity, startPathPoint, endPathPoint, startPathPoint.x, MathHelper.ceil(maxheight), startPathPoint.z, (float) maxheight,size);
			this.openedPointList.remove(startPathPoint);
			int lowestcost=Integer.MAX_VALUE;
			float nearestDistance=Float.MAX_VALUE;
			if(this.openedPointList.size()==0)return null;
			for(int j=0;j<this.openedPointList.size();j++){
				AStarPathPoint p=this.openedPointList.get(j);
				if(p.totalCost<=lowestcost){
					headPathPoint=p;
					lowestcost=(int) headPathPoint.totalCost;

				}
				if(p.distanceTo(endPathPoint)<nearestDistance){
					nearestPathPoint=p;
					nearestDistance=p.distanceTo(endPathPoint);
				}
			}
			if(!(this.ai instanceof EntityAIBreakBlock)){
				//System.out.println(headPathPoint.x+","+headPathPoint.y+","+headPathPoint.z+"'s cost is"+headPathPoint.totalCost);
			}
		}

		for(int i=0;i<maxDistance*4*(this.canUseEngineer?4:1);i++)
		{
			if (headPathPoint.equals(endPathPoint))
			{
				return this.createEntityPath(startPathPoint, headPathPoint,true,this.entitySize,par1Entity);
			}
			{
				float f = (float)(endPathPoint.x - headPathPoint.x);
				float f1 = (float)(endPathPoint.y - headPathPoint.y);
				float f2 = (float)(endPathPoint.z - headPathPoint.z);
				if(f * f + f1 * f1 + f2 * f2<par6*par6){
					return this.createEntityPath(startPathPoint, headPathPoint,true,this.entitySize,par1Entity);
				}
				if(i>this.maxLength){
					return this.createEntityPath(startPathPoint, nearestPathPoint,false,this.entitySize,par1Entity);
				}
			}
			if(this.canUseEngineer&&headPathPoint.func_75832_b(endPathPoint)>startPathPoint.distanceToTarget+100){
				this.openedPointList.remove(headPathPoint);
			}else{
				this.openPathAround(par1Entity, headPathPoint, endPathPoint, maxDistance,size);
			}
			this.openedPointList.remove(headPathPoint);
			int lowestcost=Integer.MAX_VALUE;
			float nearestDistance=Float.MAX_VALUE;
			if(this.openedPointList.size()==0)return null;
			for(int j=0;j<this.openedPointList.size();j++){
				AStarPathPoint p=this.openedPointList.get(j);
				if(p.totalCost<=lowestcost){
					headPathPoint=p;
					lowestcost=(int) headPathPoint.totalCost;

				}
				if(p.distanceTo(endPathPoint)<nearestDistance){
					nearestPathPoint=p;
					nearestDistance=p.distanceTo(endPathPoint);
				}
			}

			//System.out.println(headPathPoint.x+","+headPathPoint.y+","+headPathPoint.z+
			//" RealCost:"+headPathPoint.totalRealCost+" HCost:"+headPathPoint.getHeuristicCost());
		}

		return this.createEntityPath(startPathPoint, nearestPathPoint,false,this.entitySize,par1Entity);
	}
	//	private AStarPathEntity addToPath(Entity par1Entity, AStarPathPoint startPathPoint, AStarPathPoint endPathPoint, float maxDistance,float par6,int size)
	//	{
	//		startPathPoint.totalCost = 0;
	//		startPathPoint.distanceToNext = startPathPoint.func_75832_b(endPathPoint);
	//		startPathPoint.distanceToTarget = startPathPoint.distanceToNext;
	//		AStarPathPoint headPathPoint=startPathPoint;
	//		AStarPathPoint nearestPathPoint=startPathPoint;
	//		this.openedPointList.add(headPathPoint);
	//		headPathPoint.index=0;
	//		headPathPoint.isHead=true;
	//		double underMaxY=this.getMaxY(par1Entity.world,par1Entity, new BlockPos(startPathPoint.x,startPathPoint.y-1,startPathPoint.z),size);
	//		if(Double.isNaN(underMaxY)){
	//			//System.out.println("point"+newPathPoint.toCoord().toString()+"is air");
	//			startPathPoint.yOffset=-1;
	//
	//		}else{
	//			//System.out.println("point"+newPathPoint.toCoord().toString()+"is solid("+(underMaxY-(double)y)+")");
	//			startPathPoint.yOffset=(float)underMaxY-(float)startPathPoint.y;
	//		}
	//		//System.out.println("underMaxY:"+underMaxY);
	//
	//		{
	//			if (startPathPoint.equals(endPathPoint))
	//			{
	//				return this.createEntityPath(startPathPoint, startPathPoint,true,this.entitySize);
	//			}
	//			{
	//				float f = (float)(endPathPoint.x - headPathPoint.x);
	//				float f1 = (float)(endPathPoint.y - headPathPoint.y);
	//				float f2 = (float)(endPathPoint.z - headPathPoint.z);
	//				if(f * f + f1 * f1 + f2 * f2<par6*par6){
	//					return this.createEntityPath(startPathPoint, startPathPoint,true,this.entitySize);
	//				}
	//
	//			}
	//			/**
	//			 * bit0:EAST(X+)
	//			 * bit1:WEST(X-)
	//			 * bit2:SOUTH(Z+)
	//			 * bit3:NORTH(Z-)*/
	//			byte binary=0x0f;
	//			if(size==1){
	//				for(int y=startPathPoint.y;y<startPathPoint.y+par1Entity.height;y++){
	//					BlockPos pos=new BlockPos(startPathPoint.x, y,startPathPoint.z);
	//					IBlockState b=par1Entity.world.getBlockState(pos);
	//					if(!b.isFullCube()&&par1Entity.getEntityBoundingBox()!=null&&b.getCollisionBoundingBox(par1Entity.world, pos)!=null){
	//						AxisAlignedBB aabb=new AxisAlignedBB(startPathPoint.x, y,startPathPoint.z,startPathPoint.x+1, y+1,startPathPoint.z+1);
	//						List<AxisAlignedBB> aabbList=new ArrayList();
	//						b.addCollisionBoxToList(par1Entity.world, pos, aabb, aabbList, par1Entity,false);
	//						for(int i=0;i<aabbList.size();i++){
	//							boolean flagEast=(aabbList.get(i).minX>par1Entity.getEntityBoundingBox().maxX);
	//							boolean flagWest=(aabbList.get(i).maxX<par1Entity.getEntityBoundingBox().minX);
	//							boolean flagSouth=(aabbList.get(i).minZ>par1Entity.getEntityBoundingBox().maxZ);
	//							boolean flagNorth=(aabbList.get(i).maxZ<par1Entity.getEntityBoundingBox().minZ);
	//							if(!flagEast&&!flagWest){
	//								if(flagSouth){
	//									binary&=0xfb;
	//								}else if(flagNorth){
	//									binary&=0xf7;
	//								}
	//							}
	//							if(!flagSouth&&!flagNorth){
	//								if(flagEast){
	//									binary&=0xfe;
	//								}else if(flagWest){
	//									binary&=0xfd;
	//								}
	//							}
	//						}
	//					}
	//				}
	//			}else{
	//
	//
	//				for(int y=startPathPoint.y;y<startPathPoint.y+par1Entity.height;y++){
	//					for(int x=startPathPoint.x;x>=startPathPoint.x-1;x--){
	//						for(int z=startPathPoint.z;z>=startPathPoint.z+1;z--){
	//							BlockPos pos=new BlockPos(startPathPoint.x, y,startPathPoint.z);
	//							IBlockState b=par1Entity.world.getBlockState(pos);
	//							if(!b.isOpaqueCube()&&par1Entity.getEntityBoundingBox()!=null&&b.getCollisionBoundingBox(par1Entity.world,x, y,z)!=null){
	//								AxisAlignedBB aabb=new AxisAlignedBB(x, y,z,x+1, y+1,z+1);
	//								List<AxisAlignedBB> aabbList=new ArrayList();
	//								//ver1.12.2
	//								b.addCollisionBoxToList(par1Entity.world, startPathPoint.x, y,startPathPoint.z, aabb, aabbList, par1Entity);
	//								for(int i=0;i<aabbList.size();i++){
	//									boolean flagEast=(aabbList.get(i).minX>par1Entity.getEntityBoundingBox().maxX);
	//									boolean flagWest=(aabbList.get(i).maxX<par1Entity.getEntityBoundingBox().minX);
	//									boolean flagSouth=(aabbList.get(i).minZ>par1Entity.getEntityBoundingBox().maxZ);
	//									boolean flagNorth=(aabbList.get(i).maxZ<par1Entity.getEntityBoundingBox().minZ);
	//									if(!flagEast&&!flagWest){
	//										if(flagSouth){
	//											binary&=0xfb;
	//										}else if(flagNorth){
	//											binary&=0xf7;
	//										}
	//									}
	//									if(!flagSouth&&!flagNorth){
	//										if(flagEast){
	//											binary&=0xfe;
	//										}else if(flagWest){
	//											binary&=0xfd;
	//										}
	//									}
	//								}
	//							}
	//						}
	//					}
	//				}
	//			}
	//			double maxheight=startPathPoint.y+startPathPoint.yOffset+this.jumpheight;
	//			if((binary&0x01)>0){
	//				this.openPointSide(par1Entity, startPathPoint, endPathPoint, startPathPoint.x+1, MathHelper.ceil(maxheight), startPathPoint.z, (float) maxheight,size);
	//			}
	//			if((binary&0x02)>0){
	//				this.openPointSide(par1Entity, startPathPoint, endPathPoint, startPathPoint.x-1, MathHelper.ceil(maxheight), startPathPoint.z, (float) maxheight,size);
	//			}
	//			if((binary&0x04)>0){
	//				this.openPointSide(par1Entity, startPathPoint, endPathPoint, startPathPoint.x, MathHelper.ceil(maxheight), startPathPoint.z+1, (float) maxheight,size);
	//			}
	//			if((binary&0x08)>0){
	//				this.openPointSide(par1Entity, startPathPoint, endPathPoint, startPathPoint.x, MathHelper.ceil(maxheight), startPathPoint.z-1, (float) maxheight,size);
	//			}
	//			this.openPointSide(par1Entity, startPathPoint, endPathPoint, startPathPoint.x, MathHelper.ceil(maxheight), startPathPoint.z, (float) maxheight,size);
	//			this.openedPointList.remove(startPathPoint);
	//			int lowestcost=Integer.MAX_VALUE;
	//			float nearestDistance=Float.MAX_VALUE;
	//			if(this.openedPointList.size()==0)return null;
	//			for(int j=0;j<this.openedPointList.size();j++){
	//				AStarPathPoint p=this.openedPointList.get(j);
	//				if(p.totalCost<=lowestcost){
	//					headPathPoint=p;
	//					lowestcost=(int) headPathPoint.totalCost;
	//
	//				}
	//				if(p.distanceTo(endPathPoint)<nearestDistance){
	//					nearestPathPoint=p;
	//					nearestDistance=p.distanceTo(endPathPoint);
	//				}
	//			}
	//			if(!(this.ai instanceof EntityAIBreakBlock)){
	//				//System.out.println(headPathPoint.x+","+headPathPoint.y+","+headPathPoint.z+"'s cost is"+headPathPoint.totalCost);
	//			}
	//		}
	//
	//		for(int i=0;i<maxDistance*4*(this.canUseEngineer?4:1);i++)
	//		{
	//			if (headPathPoint.equals(endPathPoint))
	//			{
	//				return this.createEntityPath(startPathPoint, headPathPoint,true,this.entitySize);
	//			}
	//			{
	//				float f = (float)(endPathPoint.x - headPathPoint.x);
	//				float f1 = (float)(endPathPoint.y - headPathPoint.y);
	//				float f2 = (float)(endPathPoint.z - headPathPoint.z);
	//				if(f * f + f1 * f1 + f2 * f2<par6*par6){
	//					return this.createEntityPath(startPathPoint, headPathPoint,true,this.entitySize);
	//				}
	//				if(i>this.maxLength){
	//					return this.createEntityPath(startPathPoint, nearestPathPoint,false,this.entitySize);
	//				}
	//			}
	//			if(this.canUseEngineer&&headPathPoint.func_75832_b(endPathPoint)>startPathPoint.distanceToTarget+100){
	//				this.openedPointList.remove(headPathPoint);
	//			}else{
	//				this.openPathAround(par1Entity, headPathPoint, endPathPoint, maxDistance,size);
	//			}
	//			this.openedPointList.remove(headPathPoint);
	//			int lowestcost=Integer.MAX_VALUE;
	//			float nearestDistance=Float.MAX_VALUE;
	//			if(this.openedPointList.size()==0)return null;
	//			for(int j=0;j<this.openedPointList.size();j++){
	//				AStarPathPoint p=this.openedPointList.get(j);
	//				if(p.totalCost<=lowestcost){
	//					headPathPoint=p;
	//					lowestcost=(int) headPathPoint.totalCost;
	//
	//				}
	//				if(p.distanceTo(endPathPoint)<nearestDistance){
	//					nearestPathPoint=p;
	//					nearestDistance=p.distanceTo(endPathPoint);
	//				}
	//			}
	//
	//			//System.out.println(headPathPoint.x+","+headPathPoint.y+","+headPathPoint.z+
	//			//" RealCost:"+headPathPoint.totalRealCost+" HCost:"+headPathPoint.getHeuristicCost());
	//		}
	//
	//		return this.createEntityPath(startPathPoint, nearestPathPoint,false,this.entitySize);
	//	}



	/**
	 * populates pathOptions with available points and returns the number of options found (args: unused1, currentPoint,
	 * unused2, targetPoint, maxDistance)
	 */
	private void openPathAround(Entity par1Entity, AStarPathPoint currentPoint, AStarPathPoint endPoint, float par5,int size)
	{
		//AxisAlignedBB aabb=this.getAABB(par1Entity.world, currentPoint.x, currentPoint.y-1, currentPoint.z);
		//double maxheight=(aabb==null?currentPoint.y:aabb.maxY)+this.jumpheight;
		double maxheight=currentPoint.y+currentPoint.yOffset+this.jumpheight;
		/*
    	boolean flag1=true;
    	for(int i=MathHelper.ceil(maxheight-this.jumpheight+par1Entity.height);i<Math.min(maxheight+par1Entity.height, this.worldMap.getHeight());i++){

    		if(!isBlockPassable(par1Entity,currentPoint.x, i, currentPoint.z)){
    			AxisAlignedBB aabb2=this.getAABB(par1Entity.world, currentPoint.x, i+1, currentPoint.z);
    			if(aabb2==null){

    			}else{
    				double d=aabb2.minY-par1Entity.height+1;
    				maxheight=maxheight>d?d:maxheight;
    				flag1=false;
    				break;
    			}
    		}
    	}
		 */
		//System.out.println("maxheight="+maxheight);
		//System.out.println("("+currentPoint.x+","+currentPoint.y+","+currentPoint.z+") offset:"+currentPoint.yOffset+" ->");
		this.openPointSide(par1Entity, currentPoint, endPoint, currentPoint.x, MathHelper.ceil(maxheight), currentPoint.z, (float) maxheight,size);
		this.openPointSide(par1Entity, currentPoint, endPoint, currentPoint.x+1, MathHelper.ceil(maxheight), currentPoint.z, (float) maxheight,size);
		this.openPointSide(par1Entity, currentPoint, endPoint, currentPoint.x, MathHelper.ceil(maxheight), currentPoint.z+1, (float) maxheight,size);
		this.openPointSide(par1Entity, currentPoint, endPoint, currentPoint.x-1, MathHelper.ceil(maxheight), currentPoint.z, (float) maxheight,size);
		this.openPointSide(par1Entity, currentPoint, endPoint, currentPoint.x, MathHelper.ceil(maxheight), currentPoint.z-1, (float) maxheight,size);
		//this.openPointDiagonal(par1Entity, currentPoint, endPoint, currentPoint.x+1,MathHelper.floor(maxheight), currentPoint.z+1, maxheight);
		//this.openPointDiagonal(par1Entity, currentPoint, endPoint, currentPoint.x+1,MathHelper.floor(maxheight), currentPoint.z-1, maxheight);
		//this.openPointDiagonal(par1Entity, currentPoint, endPoint, currentPoint.x-1,MathHelper.floor(maxheight), currentPoint.z-1, maxheight);
		//this.openPointDiagonal(par1Entity, currentPoint, endPoint, currentPoint.x-1,MathHelper.floor(maxheight), currentPoint.z+1, maxheight);
		this.openedPointList.remove(currentPoint);
	}



	private void openPointSide(Entity par1Entity,AStarPathPoint currentPoint,AStarPathPoint endPoint,int x,int y,int z,float maxheight,int size){
		/*while(y>=1){
    		if(isBlockRidable(par1Entity,x,y,z)){
   				boolean flag=true;
   				for(int j=y+1;j<Block.blocksList[this.worldMap.getBlockId(x, y, z)].getCollisionBoundingBoxFromPool(par1Entity.world, x, y, z).maxY
   						+MathHelper.ceil(par1Entity.height);j++){
   					if(isBlockRidable(par1Entity,x,j,z)
   							&&Block.blocksList[this.worldMap.getBlockId(x, j, z)].getCollisionBoundingBoxFromPool(par1Entity.world, x, j, z).minY
   							<y+par1Entity.height){
   						flag=false;
   						break;
   					}
   				}
   				if(flag){
   					openPoint(par1Entity,currentPoint,endPoint,x,y+1,z);
   					if(y>=currentPoint.y-1)break;
   				}
   				y-=MathHelper.ceil(par1Entity.height);
    		}else y-=1;

    	}*/
		for(;y>=1;y--){
			double maxY=this.getMaxY(par1Entity.world, par1Entity, new BlockPos(x, y-1, z),size);
			if((Double.isNaN(maxY)?y:maxY)<=maxheight&&!(x==currentPoint.x&&y==currentPoint.y&&z==currentPoint.z)){
				openPoint(par1Entity,currentPoint,endPoint,x,y,z,size);
			}
			if(y<=currentPoint.y-1&&MRAUtil.isBlockRidable(par1Entity,new BlockPos(x,y,z),this.canSwim,this.entityWidth))break;
		}
	}



	public void openPointDiagonal(EntityRegularArmy par1Entity,AStarPathPoint currentPoint,AStarPathPoint endPoint,int x,int y,int z,float maxheight,int size){
		while(y>=1){
			if(MRAUtil.isBlockRidable(par1Entity, new BlockPos(x,y,z),this.canSwim,this.entityWidth)){
				boolean flag=true;
				AxisAlignedBB aabb=MRAUtil.getAABB(par1Entity, new BlockPos(x, y, z));
				for(int j=y+1;j<aabb.maxY
						+MathHelper.ceil(par1Entity.height);j++){
					if(MRAUtil.isBlockRidable(par1Entity,new BlockPos(x,j,z),this.canSwim,this.entityWidth)
							&&aabb.minY
							<y+par1Entity.height){
						flag=false;
						break;
					}
					if(MRAUtil.isBlockRidable(par1Entity,new BlockPos(currentPoint.x,j,z),this.canSwim,this.entityWidth)
							&&MRAUtil.getAABB(par1Entity, new BlockPos(currentPoint.x, j, z)).minY
							<y+par1Entity.height){
						flag=false;
						break;
					}
					if(MRAUtil.isBlockRidable(par1Entity,new BlockPos(x,j,currentPoint.z),this.canSwim,this.entityWidth)
							&&MRAUtil.getAABB(par1Entity, new BlockPos(x, j, currentPoint.z)).minY
							<y+par1Entity.height){
						flag=false;
						break;
					}
				}
				if(flag){
					openPoint(par1Entity,currentPoint,endPoint,x,y+1,z,size);
					if(y>=currentPoint.y-1)break;
				}
				y-=MathHelper.ceil(par1Entity.height);
			}else y-=1;
		}
	}

	/**
	 * Returns a mapped point or creates and adds one
	 */
	private boolean openPoint(Entity par1Entity,AStarPathPoint previous,AStarPathPoint end,int x, int y, int z,int size)
	{

		AStarPathPoint newPathPoint = null;

		//System.out.println("opened:"+x+","+y+","+z);

		newPathPoint = new AStarPathPoint(x, y, z);

		if(this.unusablePoints.contains(newPathPoint.toCoord()))return false;

		if(newPathPoint.isAssigned())return false;
		newPathPoint.previous=previous;
		newPathPoint.index=previous.index+1;
		newPathPoint.beforeRequested=previous.beforeRequested+1;
		int x__=x-previous.x;
		int z__=z-previous.z;
		boolean isAbove=(x==previous.x)&&z==previous.z;
		if(isAbove){
			//System.out.println("Above");
		}

		/**This is the direction where currently this mob is facing*/
		EnumFacing dir=null;
		if(x__>0)dir=EnumFacing.EAST;
		if(x__<0)dir=EnumFacing.WEST;
		if(z__>0)dir=EnumFacing.SOUTH;
		if(z__<0)dir=EnumFacing.NORTH;
		newPathPoint.dirFromPrev=dir;

		if(size==1){
			boolean putBlockUnder=false;
			if(!MRAUtil.isBlockRidable(par1Entity,new BlockPos(x,y-1,z),this.canSwim,this.entityWidth)){
				//System.out.println("not ridable");

				if(!this.canUseEngineer){
					//System.out.println("Deny ("+x+","+y+","+z+") Unridable");
					return false;
				}

				//Putting blocks shouldn't causes suffocation.
				AStarPathPoint p=previous;
				while(true){
					if(p.x==x&&p.z==z&&p.y<=y&&p.y+this.entityHeight>=y-1){
						return false;
					}
					if(p.previous==null){
						break;
					}else{
						p=p.previous;
					}
				}

				//end
				newPathPoint.addBlocksToPut(new BlockPos(x,y-1,z));
				putBlockUnder=true;
			}
			//These commands decides which blocks should be broken.
			double underMaxY=this.getMaxY(par1Entity.world,par1Entity,  new BlockPos(x, y-1, z),1);
			//AxisAlignedBB aabbUnder=this.getAABB(e.getWorld(),e, x, y-1, z);

			/*
	        double maxY=Math.max(aabbUnder==null?y:aabbUnder.maxY,
	        		this.getAABB(e.getWorld(), previous.x, previous.y-1, previous.z)==null?
	        				previous.y:this.getAABB(e.getWorld(), previous.x, previous.y-1, previous.z).maxY)+e.height;
	        	for(int j=y;j<maxY;j++){
					if(!this.isBlockPassable(e,x, j, z)
							&&this.getAABB(e.getWorld(), x, j, z).minY<maxY
							&&this.getAABB(e.getWorld(), x, j, z).maxY>previous.y+previous.yOffset+this.jumpheight){
						//System.out.println("not passable");
						if(!this.canUseEngineer)return false;
			        		newPathPoint.addBlocksToBreak(new Coord(x,j,z));
					}
				}

	        	for(int j=MathHelper.ceil(previous.y+e.height);j<maxY;j++){
					if(!this.isBlockPassable(e,previous.x, j, previous.z)
							&&this.getAABB(e.getWorld(),previous.x, j, previous.z).minY<maxY
							&&this.getAABB(e.getWorld(),previous.x, j, previous.z).maxY>previous.y+previous.yOffset+this.jumpheight){
						if(!this.canUseEngineer)return false;
			        		newPathPoint.addBlocksToBreak(new Coord(previous.x,j,previous.z));
					}
				}
			 */
			if(putBlockUnder){
				//System.out.println("point"+newPathPoint.toCoord().toString()+"puts a block");
				newPathPoint.yOffset=0;
			}else if(Double.isNaN(underMaxY)){
				//System.out.println("point"+newPathPoint.toCoord().toString()+"is air");
				newPathPoint.yOffset=-1;

			}else{
				//System.out.println("point"+newPathPoint.toCoord().toString()+"is solid("+(aabbUnder.maxY-(double)y)+")");
				newPathPoint.yOffset=(float)underMaxY-(float)y;
			}

			float maxY=Math.max(newPathPoint.yOffset+((float)newPathPoint.y),
					previous.yOffset+((float)previous.y))+this.entityHeight;
			for(int j=y;j<maxY;j++){
				BlockPos pos=new BlockPos(x, j, z);
				if(!MRAUtil.isBlockPassable(par1Entity,pos)
						&&this.getMinY(par1Entity.world,par1Entity, pos,1)<maxY)n:{
					//System.out.println("not passable");
					IBlockState id=this.worldMap.getBlockState(pos);
					if(par1Entity instanceof EntityLivingBase && id.getBlock().isLadder(id,worldMap, pos, (EntityLivingBase)par1Entity)){
						/*
						switch(dir){
						case EAST:
							if(this.worldMap.getBlockMetadata(x, j, z)==4){
								if(j==y){
									newPathPoint.onLadder=true;
								}
								newPathPoint.addCoordsLadder(new Coord(x,j,z));
								break n;
							}
							break;
						case NORTH:
							if(this.worldMap.getBlockMetadata(x, j, z)==3){
								if(j==y){
									newPathPoint.onLadder=true;
								}
								newPathPoint.addCoordsLadder(new Coord(x,j,z));
								break n;
							}
							break;
						case SOUTH:
							if(this.worldMap.getBlockMetadata(x, j, z)==2) {
								if(j==y){
									newPathPoint.onLadder=true;
								}
								newPathPoint.addCoordsLadder(new Coord(x,j,z));
								break n;
							}
							break;
						case WEST:
							if(this.worldMap.getBlockMetadata(x, j, z)==5){
								if(j==y){
									newPathPoint.onLadder=true;
								}
								newPathPoint.addCoordsLadder(new Coord(x,j,z));
								break n;
							}
							break;
						default:
							if(j==y){
								newPathPoint.onLadder=true;
							}
							newPathPoint.addCoordsLadder(new Coord(x,j,z));
							break n;
						}
						 */
						if(j==y){
							newPathPoint.onLadder=true;
						}
						newPathPoint.addCoordsLadder(pos);
						break n;
					}
					if(!this.canUseEngineer){
						//System.out.println("Deny ("+x+","+y+","+z+") Wall");
						return false;
					}
					if(par1Entity.world.getBlockState(pos).getBlockHardness(par1Entity.world, pos)<0)return false;
					newPathPoint.addBlocksToBreak(pos);
				}
			}

			for(int j=MathHelper.ceil(previous.y+this.entityHeight);j<maxY;j++){
				BlockPos posprev=new BlockPos(previous.x, j, previous.z);
				BlockPos pos=new BlockPos(x,j,z);
				if(!MRAUtil.isBlockPassable(par1Entity,posprev)
						&&this.getMinY(par1Entity.world,par1Entity,posprev,1)<maxY
						)n:{
					IBlockState id=this.worldMap.getBlockState(pos);
					if(par1Entity instanceof EntityLivingBase && id.getBlock().isLadder(id,worldMap, pos, (EntityLivingBase)par1Entity)){
						newPathPoint.onLadder=true;
						newPathPoint.addCoordsLadder(pos);
						break n;
					}
					if(!this.canUseEngineer){
						//System.out.println("Deny ("+x+","+y+","+z+") Wall(previous)");
						return false;
					}
					if(par1Entity.world.getBlockState(new BlockPos(posprev)).getBlockHardness(par1Entity.world,posprev)<0)return false;
					newPathPoint.addBlocksToBreak(posprev);

				}
			}
		}else{
			double underMaxY=this.getMaxY(par1Entity.world,par1Entity,  new BlockPos(x, y-1, z),2);
			boolean putBlockUnder=false;
			boolean ridableFlag=false;
			if(this.canUseEngineer){
				ridableFlag=true;
				for(int x_=x;x_>=x-1;x_--){
					for(int z_=z;z_>=z-1;z_--){
						if(!MRAUtil.isBlockRidable(par1Entity,new BlockPos(x_,y-1,z_),this.canSwim,this.entityWidth)){
							ridableFlag=false;

						}
					}
				}
			}else{
				for(int x_=x;x_>=x-1;x_--){
					for(int z_=z;z_>=z-1;z_--){
						if(MRAUtil.isBlockRidable(par1Entity,new BlockPos(x_,y-1,z_),this.canSwim,this.entityWidth)){
							ridableFlag=true;

						}
					}
				}
			}
			if(!ridableFlag)m:{
				//System.out.println("not ridable");
				if(!this.canUseEngineer){
					//System.out.println("Deny ("+x+","+y+","+z+") Unridable");
					return false;
				}

				List<BlockPos> listTmp=new ArrayList();
				int yoff=(underMaxY-y)<0?-1:0;
				int yoffprev=(previous.yOffset)<0?-1:0;
				if(dir==null)break m;
				switch(dir){
				case EAST:
					listTmp.add(new BlockPos(x,y-1+yoff,z));
					listTmp.add(new BlockPos(x,y-1+yoff,z-1));
					listTmp.add(new BlockPos(previous.x,Math.min(previous.y, y)-1+yoffprev,previous.z));
					listTmp.add(new BlockPos(previous.x,Math.min(previous.y, y)-1+yoffprev,previous.z-1));
					break;
				case WEST:
					listTmp.add(new BlockPos(x-1,y-1+yoff,z));
					listTmp.add(new BlockPos(x-1,y-1+yoff,z-1));
					listTmp.add(new BlockPos(previous.x-1,Math.min(previous.y, y)-1+yoffprev,previous.z));
					listTmp.add(new BlockPos(previous.x-1,Math.min(previous.y, y)-1+yoffprev,previous.z-1));
					break;

				case SOUTH:
					listTmp.add(new BlockPos(x,y-1+yoff,z));
					listTmp.add(new BlockPos(x-1,y-1+yoff,z));
					listTmp.add(new BlockPos(previous.x,Math.min(previous.y, y)-1+yoffprev,previous.z));
					listTmp.add(new BlockPos(previous.x-1,Math.min(previous.y, y)-1+yoffprev,previous.z));
					break;
				case NORTH:
					listTmp.add(new BlockPos(x,y-1+yoff,z-1));
					listTmp.add(new BlockPos(x-1,y-1+yoff,z-1));
					listTmp.add(new BlockPos(previous.x,Math.min(previous.y, y)-1+yoffprev,previous.z-1));
					listTmp.add(new BlockPos(previous.x-1,Math.min(previous.y, y)-1+yoffprev,previous.z-1));
					break;
				default:
					//System.out.println("Deny ("+x+","+y+","+z+") Above");
					//return false;
					break m;

				}
				List<BlockPos> coordsToPutBlock=new ArrayList();

				for(BlockPos c:listTmp){
					if(!MRAUtil.isBlockRidable(par1Entity, c,this.canSwim,this.entityWidth)
							||this.getMaxY(par1Entity.world,par1Entity, c, 1)<c.getY()+1)n:{
						if(previous.blocksToPut!=null){
							for(BlockPos c1:previous.blocksToPut){
								if(c1!=null && c1.equals(c))break n;
							}
						}
						coordsToPutBlock.add(c);
						putBlockUnder=true;
					}
				}
				//Putting blocks shouldn't causes suffocation.

				AStarPathPoint p=previous;
				while(true){
					for(BlockPos c:coordsToPutBlock){
						if(p.x-c.getX()<=0&&p.x-c.getX()>=-1
								&&p.z-c.getZ()<=0&&p.z-c.getZ()>=-1
								&&p.y<c.getY()&&p.y+this.entityHeight>=c.getY()){
							//System.out.println("Deny ("+x+","+y+","+z+") Suffocation");
							return false;
						}
					}
					if(p.previous==null){
						break;
					}else{
						p=p.previous;
					}
				}
				/*
			if(y-previous.y>0){
				switch(dir){
				case EAST:
					coordsToPutBlock.add(new Coord(previous.x-1,previous.y-1,previous.z));
					coordsToPutBlock.add(new Coord(previous.x-1,previous.y-1,previous.z-1));
					break;
				case WEST:
					coordsToPutBlock.add(new Coord(previous.x,previous.y-1,previous.z));
					coordsToPutBlock.add(new Coord(previous.x,previous.y-1,previous.z-1));
					break;

				case SOUTH:
					coordsToPutBlock.add(new Coord(previous.x,previous.y-1,previous.z-1));
					coordsToPutBlock.add(new Coord(previous.x-1,previous.y-1,previous.z-1));
					break;
				case NORTH:
					coordsToPutBlock.add(new Coord(previous.x,previous.y-1,previous.z));
					coordsToPutBlock.add(new Coord(previous.x-1,previous.y-1,previous.z));
					break;
				default:
					break;
				}
			}*/

				//end
				for(BlockPos c:coordsToPutBlock){
					//System.out.println("put:"+c);
					newPathPoint.addBlocksToPut(c);
				}


			}
			//These commands decides which blocks should be broken.

			//AxisAlignedBB aabbUnder=this.getAABB(e.getWorld(),e, x, y-1, z);

			/*
        double maxY=Math.max(aabbUnder==null?y:aabbUnder.maxY,
        		this.getAABB(e.getWorld(), previous.x, previous.y-1, previous.z)==null?
        				previous.y:this.getAABB(e.getWorld(), previous.x, previous.y-1, previous.z).maxY)+e.height;
        	for(int j=y;j<maxY;j++){
				if(!this.isBlockPassable(e,x, j, z)
						&&this.getAABB(e.getWorld(), x, j, z).minY<maxY
						&&this.getAABB(e.getWorld(), x, j, z).maxY>previous.y+previous.yOffset+this.jumpheight){
					//System.out.println("not passable");
					if(!this.canUseEngineer)return false;
		        		newPathPoint.addBlocksToBreak(new Coord(x,j,z));
				}
			}

        	for(int j=MathHelper.ceil(previous.y+e.height);j<maxY;j++){
				if(!this.isBlockPassable(e,previous.x, j, previous.z)
						&&this.getAABB(e.getWorld(),previous.x, j, previous.z).minY<maxY
						&&this.getAABB(e.getWorld(),previous.x, j, previous.z).maxY>previous.y+previous.yOffset+this.jumpheight){
					if(!this.canUseEngineer)return false;
		        		newPathPoint.addBlocksToBreak(new Coord(previous.x,j,previous.z));
				}
			}
			 */
			if(putBlockUnder){
				//System.out.println("point"+newPathPoint.toCoord().toString()+"puts a block");
				newPathPoint.yOffset=0;
			}else if(Double.isNaN(underMaxY)){
				//System.out.println("point"+newPathPoint.toCoord().toString()+"is air");
				newPathPoint.yOffset=-1;

			}else{
				//System.out.println("point"+newPathPoint.toCoord().toString()+"is solid("+(aabbUnder.maxY-(double)y)+")");
				newPathPoint.yOffset=(float)underMaxY-(float)y;
			}

			float maxY=Math.max(newPathPoint.yOffset+((float)newPathPoint.y),
					previous.yOffset+((float)previous.y))+this.entityHeight;
			boolean waterFlow=false;
			int maxYToPlaceLadder=0;
			List<BlockPos> laddersCoords=new ArrayList();
			if(this.canUseEngineer){
				AStarPathPoint p=previous;
				for(int i=0;i<5;i++){
					if(p.laddersToPut!=null){
						for(BlockPos c:p.laddersToPut){
							if(c!=null){
								laddersCoords.add(c);
							}
						}
					}
					if(p.previous==null){
						break;
					}else{
						p=p.previous;
					}
				}

			}
			for(int j=y;j<maxY;j++){
				for(int x_=x;x_>=x-1;x_--){
					for(int z_=z;z_>=z-1;z_--){
						BlockPos pos=new BlockPos(x_,j,z_);
						if(laddersCoords.contains(pos)){
							if(j==y){
								newPathPoint.onLadder=true;
							}
							newPathPoint.addCoordsLadder(pos);
						}
						if(!MRAUtil.isBlockPassable(par1Entity,pos)
								&&this.getMinY(par1Entity.world,par1Entity, pos,2)<maxY)n:{
							//System.out.println("not passable");
							//System.out.println("checking "+x_+","+j+","+z_);
							IBlockState id=this.worldMap.getBlockState(pos);
							if(par1Entity instanceof EntityLivingBase && (id.getBlock().isLadder(id,worldMap, pos, (EntityLivingBase)par1Entity))){
								/*
								switch(dir){
								case EAST:
									if(this.worldMap.getBlockMetadata(x_, j, z_)==4){
										if(j==y){
											newPathPoint.onLadder=true;
										}
										newPathPoint.addCoordsLadder(new Coord(x_,j,z_));
										break n;
									}
									break;
								case NORTH:
									if(this.worldMap.getBlockMetadata(x_, j, z_)==3){
										if(j==y){
											newPathPoint.onLadder=true;
										}
										newPathPoint.addCoordsLadder(new Coord(x_,j,z_));
										break n;
									}
									break;
								case SOUTH:
									if(this.worldMap.getBlockMetadata(x_, j, z_)==2) {
										if(j==y){
											newPathPoint.onLadder=true;
										}
										newPathPoint.addCoordsLadder(new Coord(x_,j,z_));
										break n;
									}
									break;
								case WEST:
									if(this.worldMap.getBlockMetadata(x_, j, z_)==5){
										if(j==y){
											newPathPoint.onLadder=true;
										}
										newPathPoint.addCoordsLadder(new Coord(x_,j,z_));
										break n;
									}
									break;
								default:
									if(j==y){
										newPathPoint.onLadder=true;
									}
									newPathPoint.addCoordsLadder(new Coord(x_,j,z_));
									break n;

								}*/

								if(j==y){
									newPathPoint.onLadder=true;
								}
								newPathPoint.addCoordsLadder(pos);
								break n;
							}

							//System.out.println("not passable");
							if(!this.canUseEngineer){
								//System.out.println("Deny ("+x+","+y+","+z+") Wall");
								return false;
							}
							if(id.getBlockHardness(par1Entity.world, pos)<0){
								//System.out.println("Deny ("+x+","+y+","+z+") Unbreakable");
								return false;
							}
							//System.out.println("break:"+new Coord(x_,j,z_));
							newPathPoint.addBlocksToBreak(pos);
							if(!waterFlow && isAbove){
								if(this.isBlockLiquid(par1Entity.world,pos))waterFlow=true;
								if(this.isBlockLiquid(par1Entity.world,pos.add(1, 0, 0)))waterFlow=true;
								if(this.isBlockLiquid(par1Entity.world,pos.add(-1, 0, 0)))waterFlow=true;
								if(this.isBlockLiquid(par1Entity.world,pos.add(0, 0, 1)))waterFlow=true;
								if(this.isBlockLiquid(par1Entity.world,pos.add(0, 0, -1)))waterFlow=true;
								if(this.isBlockLiquid(par1Entity.world,pos.add(0, 1, 0)))waterFlow=true;

							}
							if(waterFlow){
								maxYToPlaceLadder=j;
							}
						}
					}
				}
			}
			for(int x_=previous.x;x_>=x-1;x_--){
				for(int z_=previous.z;z_>=z-1;z_--){
					for(int j=MathHelper.ceil(previous.y+this.entityHeight);j<maxY;j++){
						BlockPos pos=new BlockPos(x_,j,z_);
						if(!MRAUtil.isBlockPassable(par1Entity,pos)
								&&this.getMinY(par1Entity.world,par1Entity,pos,2)<maxY
								)n:{

							IBlockState id=this.worldMap.getBlockState(pos);
							if(par1Entity instanceof EntityLivingBase && this.worldMap.getBlockState(pos).getBlock().isLadder(id,worldMap,pos, (EntityLivingBase)par1Entity)){
								newPathPoint.addCoordsLadder(pos);
								break n;
							}
							if(!this.canUseEngineer){
								//System.out.println("Deny ("+x+","+y+","+z+") Wall(previous)");
								return false;
							}
							if(id.getBlockHardness(par1Entity.world, pos)<0){
								//System.out.println("Deny ("+x+","+y+","+z+") Unbreakable(previous)");
								return false;
							}
							//System.out.println("breakP:"+new Coord(x_,j,z_));
							newPathPoint.addBlocksToBreak(pos);

							if(!waterFlow&& isAbove){
								if(this.isBlockLiquid(par1Entity.world,pos))waterFlow=true;
								if(this.isBlockLiquid(par1Entity.world,pos.add(1, 0, 0)))waterFlow=true;
								if(this.isBlockLiquid(par1Entity.world,pos.add(-1, 0, 0)))waterFlow=true;
								if(this.isBlockLiquid(par1Entity.world,pos.add(0, 0, 1)))waterFlow=true;
								if(this.isBlockLiquid(par1Entity.world,pos.add(0, 0, -1)))waterFlow=true;
								if(this.isBlockLiquid(par1Entity.world,pos.add(0, 1, 0)))waterFlow=true;

							}
							if(waterFlow){
								maxYToPlaceLadder=j;
							}

						}
					}
				}
			}

			//end
			if(waterFlow){
				//System.out.println("water");
				newPathPoint.onLadder=true;
				EnumFacing dir_=null;
				AStarPathPoint p_=previous;
				while(true){
					if(p_.dirFromPrev!=null){
						dir_=p_.dirFromPrev;
					}
					if(p_.previous==null){
						break;
					}else{
						p_=p_.previous;
					}
				}
				if(previous.onLadder){
					{
						List<BlockPos> listTmp=new ArrayList();
						if(dir_==null) {
							listTmp.add(new BlockPos(x-2,maxYToPlaceLadder,z-1));
							listTmp.add(new BlockPos(x-2,maxYToPlaceLadder,z));
							listTmp.add(new BlockPos(x+1,maxYToPlaceLadder,z-1));
							listTmp.add(new BlockPos(x+1,maxYToPlaceLadder,z));
						}else {
							switch(dir_){
							case EAST:
							case WEST:
								listTmp.add(new BlockPos(x-2,maxYToPlaceLadder,z-1));
								listTmp.add(new BlockPos(x-2,maxYToPlaceLadder,z));
								listTmp.add(new BlockPos(x+1,maxYToPlaceLadder,z-1));
								listTmp.add(new BlockPos(x+1,maxYToPlaceLadder,z));
								break;
							case SOUTH:
							case NORTH:
								listTmp.add(new BlockPos(x-1,maxYToPlaceLadder,z-2));
								listTmp.add(new BlockPos(x-1,maxYToPlaceLadder,z+1));
								listTmp.add(new BlockPos(x,maxYToPlaceLadder,z-2));
								listTmp.add(new BlockPos(x,maxYToPlaceLadder,z+1));
								break;
							default:
								listTmp.add(new BlockPos(x-2,maxYToPlaceLadder,z-1));
								listTmp.add(new BlockPos(x-2,maxYToPlaceLadder,z));
								listTmp.add(new BlockPos(x+1,maxYToPlaceLadder,z-1));
								listTmp.add(new BlockPos(x+1,maxYToPlaceLadder,z));
								break;

							}
						}
						List<BlockPos> coordsToPutBlock=new ArrayList();

						for(BlockPos c:listTmp){
							if(!MRAUtil.isBlockRidable(par1Entity, c,this.canSwim,this.entityWidth))n:{
								if(previous.blocksToPut!=null){
									for(BlockPos c1:previous.blocksToPut){
										if(c1!=null && c1.equals(c))break n;
									}
								}
								coordsToPutBlock.add(c);
							}
						}
						//Putting blocks shouldn't causes suffocation.

						AStarPathPoint p=previous;
						while(true){
							for(int i=0;i<coordsToPutBlock.size();i++){
								BlockPos c=coordsToPutBlock.get(i);
								if(p.x-c.getX()<=0&&p.x-c.getX()>=-1
										&&p.z-c.getZ()<=0&&p.z-c.getZ()>=-1
										&&p.y<c.getY()&&p.y+this.entityHeight>=c.getY()){
									coordsToPutBlock.remove(i);
									i--;
								}
							}
							if(p.previous==null){
								break;
							}else{
								p=p.previous;
							}
						}
						for(BlockPos c:coordsToPutBlock){
							//System.out.println("put(water):"+c);
							newPathPoint.addBlocksToPut(c);
						}
					}
					{
						List<BlockPos> listTmp=new ArrayList();

						listTmp.add(new BlockPos(x-1,maxYToPlaceLadder,z-1));
						listTmp.add(new BlockPos(x-1,maxYToPlaceLadder,z));
						listTmp.add(new BlockPos(x,maxYToPlaceLadder,z-1));
						listTmp.add(new BlockPos(x,maxYToPlaceLadder,z));

						List<BlockPos> coordsToPutLadder=new ArrayList();

						for(BlockPos c:listTmp){
							if(!Blocks.LADDER.canPlaceBlockAt(par1Entity.world,c))n:{
								if(previous.blocksToPut!=null){
									for(BlockPos c1:previous.blocksToPut){
										if(c1!=null && c1.equals(c))break n;
									}
								}
								coordsToPutLadder.add(c);
							}
						}
						//Putting blocks shouldn't causes suffocation.

						AStarPathPoint p=previous;
						while(true){
							for(int i=0;i<coordsToPutLadder.size();i++){
								BlockPos c=coordsToPutLadder.get(i);
								if(p.x-c.getX()<=0&&p.x-c.getX()>=-1
										&&p.z-c.getZ()<=0&&p.z-c.getZ()>=-1
										&&p.y<c.getY()&&p.y+this.entityHeight>=c.getY()){
									coordsToPutLadder.remove(i);
									i--;
								}
							}
							if(p.previous==null){
								break;
							}else{
								p=p.previous;
							}
						}

						for(BlockPos c:coordsToPutLadder){
							//System.out.println("put(ladder):"+c);
							newPathPoint.addLaddersToPut(c,dir_);
						}
					}
				}else{
					{
						List<BlockPos> listTmp=new ArrayList();
						if(dir_==null) {
							for(int y_=y;y_<=maxYToPlaceLadder;y_++){
								listTmp.add(new BlockPos(x-2,y_,z-1));
								listTmp.add(new BlockPos(x-2,y_,z));
								listTmp.add(new BlockPos(x+1,y_,z-1));
								listTmp.add(new BlockPos(x+1,y_,z));
							}
						}else {
							switch(dir_){
							case EAST:
								for(int y_=y;y_<=maxYToPlaceLadder;y_++){
									listTmp.add(new BlockPos(x-2,y_,z-1));
									listTmp.add(new BlockPos(x-2,y_,z));
									listTmp.add(new BlockPos(x+1,y_,z-1));
									listTmp.add(new BlockPos(x+1,y_,z));
								}
								break;
							case WEST:
								for(int y_=y;y_<=maxYToPlaceLadder;y_++){
									listTmp.add(new BlockPos(x-2,y_,z-1));
									listTmp.add(new BlockPos(x-2,y_,z));
									listTmp.add(new BlockPos(x+1,y_,z-1));
									listTmp.add(new BlockPos(x+1,y_,z));
								}
								break;
							case SOUTH:
							case NORTH:
								for(int y_=y;y_<=maxYToPlaceLadder;y_++){
									listTmp.add(new BlockPos(x-1,y_,z-2));
									listTmp.add(new BlockPos(x-1,y_,z+1));
									listTmp.add(new BlockPos(x,y_,z-2));
									listTmp.add(new BlockPos(x,y_,z+1));
								}
								break;
							default:
								for(int y_=y;y_<=maxYToPlaceLadder;y_++){
									listTmp.add(new BlockPos(x-2,y_,z-1));
									listTmp.add(new BlockPos(x-2,y_,z));
									listTmp.add(new BlockPos(x+1,y_,z-1));
									listTmp.add(new BlockPos(x+1,y_,z));
								}
								break;

							}
						}
						List<BlockPos> coordsToPutBlock=new ArrayList();

						for(BlockPos c:listTmp){
							if(!Blocks.LADDER.canPlaceBlockAt(par1Entity.world, c))n:{
								if(previous.blocksToPut!=null){
									for(BlockPos c1:previous.blocksToPut){
										if(c1!=null && c1.equals(c))break n;
									}
								}
								coordsToPutBlock.add(c);
							}
						}
						//Putting blocks shouldn't causes suffocation.

						AStarPathPoint p=previous;
						while(true){
							for(int i=0;i<coordsToPutBlock.size();i++){
								BlockPos c=coordsToPutBlock.get(i);
								if(p.x-c.getX()<=0&&p.x-c.getX()>=-1
										&&p.z-c.getZ()<=0&&p.z-c.getZ()>=-1
										&&p.y<c.getY()&&p.y+this.entityHeight>=c.getY()){
									coordsToPutBlock.remove(i);
									i--;
								}
							}
							if(p.previous==null){
								break;
							}else{
								p=p.previous;
							}
						}
						for(BlockPos c:coordsToPutBlock){
							//System.out.println("put(water):"+c);
							newPathPoint.addBlocksToPut(c);
						}
					}
					{
						List<BlockPos> listTmp=new ArrayList();

						for(int y_=y;y_<=maxYToPlaceLadder;y_++){
							listTmp.add(new BlockPos(x-1,y_,z-1));
							listTmp.add(new BlockPos(x-1,y_,z));
							listTmp.add(new BlockPos(x,y_,z-1));
							listTmp.add(new BlockPos(x,y_,z));
						}

						List<BlockPos> coordsToPutLadder=new ArrayList();

						for(BlockPos c:listTmp){
							if(!Blocks.LADDER.canPlaceBlockAt(par1Entity.world, c))n:{
								if(previous.blocksToPut!=null){
									for(BlockPos c1:previous.blocksToPut){
										if(c1!=null && c1.equals(c))break n;
									}
								}
								coordsToPutLadder.add(c);
							}
						}
						//Putting blocks shouldn't causes suffocation.
						/*
						AStarPathPoint p=previous;
						while(true){
							for(int i=0;i<coordsToPutLadder.size();i++){
								Coord c=coordsToPutLadder.get(i);
								if(p.x-c.getX()<=0&&p.x-c.getX()>=-1
										&&p.z-c.getZ()<=0&&p.z-c.getZ()>=-1
										&&p.y<c.getY()&&p.y+this.entityHeight>=c.getY()){
									coordsToPutLadder.remove(i);
									i--;
								}
							}
							if(p.previous==null){
								break;
							}else{
								p=p.previous;
							}
						}
						 */
						for(BlockPos c:coordsToPutLadder){
							//System.out.println("put(ladder):"+c);
							newPathPoint.addLaddersToPut(c,dir_);
						}
					}
				}
			}
			if(newPathPoint.onLadder){
				//System.out.println("Ladder");
			}
			if(isAbove && !newPathPoint.onLadder){
				//System.out.println("Deny ("+x+","+y+","+z+") NoLadder");
				return false;
			}
		}

		newPathPoint.totalRealCost=2+newPathPoint.previous.totalRealCost+this.getBlockCost(par1Entity,newPathPoint,end,size)+this.getEngineerCost(par1Entity,newPathPoint,end);
		if(newPathPoint.totalRealCost>this.maxCost){
			//System.out.println("Deny ("+x+","+y+","+z+") TooMuchCost");
			return false;
		}
		//newPathPoint.totalHeuristicCost=newPathPoint.previous.totalHeuristicCost+getHeuristicCost(e,newPathPoint,end);
		newPathPoint.totalCost=newPathPoint.totalRealCost+getHeuristicCost(par1Entity,newPathPoint,end);
		newPathPoint.tickToNext=this.getPlannedTickToNext(par1Entity, previous, newPathPoint);
		// if(this.ai instanceof EntityAIBreakBlock)
		//System.out.println("opened:"+x+","+y+","+z+" as cost:"+newPathPoint.totalCost+" as time:"+newPathPoint.tickToNext);
		int l = AStarPathPoint.makeHash(x, y, z);
		AStarPathPoint a=(AStarPathPoint)this.pointMap.lookup(l);
		if(a==null){
			this.pointMap.addKey(l, newPathPoint);
			this.openedPointList.add(newPathPoint);
		}else if(a.totalCost>newPathPoint.totalCost){
			a=newPathPoint;
			this.openedPointList.add(newPathPoint);
			this.pointMap.addKey(l, newPathPoint);
		}else{
			//System.out.println("Deny ("+x+","+y+","+z+") Useless cost:"+newPathPoint.totalCost);
			return false;
		}
		//System.out.println("Accept ("+x+","+y+","+z+") cost:"+newPathPoint.totalCost);
		return true;
	}

	public int getEngineerCost(Entity par1Entity,AStarPathPoint p,AStarPathPoint end){
		if(p.numberOfBlocksToBreak==0&&p.numberOfBlocksToPut==0)return 0;
		if(!(par1Entity instanceof EntityRegularArmy))return 0;
		EntityRegularArmy entity=(EntityRegularArmy)par1Entity;
		int cost=20;

		boolean flag=true;
		if(p.blocksToBreak!=null){
			for(int i=0;i<p.numberOfBlocksToBreak;i++){
				BlockPos c=p.blocksToBreak[i];
				IBlockState id=this.worldMap.getBlockState(c);
				if(!MRAUtil.isAir(id)){
					flag=false;
					cost+=5+id.getBlockHardness(par1Entity.world, c)*2;
					if(p.laddersToPut==null){
						if(this.isBlockLiquid(par1Entity.world,c))cost+=60;
						if(this.isBlockLiquid(par1Entity.world,c.add(1, 0, 0)))cost+=60;
						if(this.isBlockLiquid(par1Entity.world,c.add(-1, 0, 0)))cost+=60;
						if(this.isBlockLiquid(par1Entity.world,c.add(0, 0, 1)))cost+=60;
						if(this.isBlockLiquid(par1Entity.world,c.add(0, 0, -1)))cost+=60;
						if(this.isBlockLiquid(par1Entity.world,c.add(0, 1 ,0)))cost+=60;
					}
				}
			}
		}
		if(p.blocksToPut!=null){
			for(int i=0;i<p.numberOfBlocksToPut;i++){
				//BlockPos c=p.blocksToPut[i];
				//IBlockState id=this.worldMap.getBlockState(c);
				flag=false;
				cost+=4;

			}
		}

		if(p.beforeRequested==0){
			if(p.previous.beforeRequested>16&&flag&&!this.lowerEngineerCost){
				cost+=50;
			}
		}
		return cost;
	}


	public int getBlockCost(Entity par1Entity,AStarPathPoint p,AStarPathPoint end,int size){
		int cost=0;
		if(p.previous.y-p.y>=3)cost+=(p.previous.y-p.y-1)*16;
		if (p.previous.y - p.y <= -1) cost += 2;
		if(size==1){
			IBlockState b=this.worldMap.getBlockState(p.toCoord());
			//if(!isBlockRidable(e,p.x,p.y-1,p.z))cost+= Block.blocksList[this.worldMap.getBlockId(p.x,p.y-1,p.z)] instanceof BlockFluid? 4:0;
			//if(isBlockRidable(e,p.x,p.y,p.z))cost+=b.getBlockHardness(e.getWorld(),p.x,p.y,p.z)/2+3;
			if(MRAUtil.isLiquid(b))cost+=30;
			int i=0;
			boolean flag=true;
			switch(p.index%8){
			case 0:
				for(;i<15;i++){
					if(MRAUtil.isBlockRidable(par1Entity, p.toCoord().add(1, 1-i, 0),this.canSwim,this.entityWidth)){
						break;
					}
				}
				break;
			case 2:
				for(;i<15;i++){
					if(MRAUtil.isBlockRidable(par1Entity, p.toCoord().add(0, 1-i, 1),this.canSwim,this.entityWidth)){
						break;
					}
				}
				break;
			case 4:
				for(;i<15;i++){
					if(MRAUtil.isBlockRidable(par1Entity, p.toCoord().add(-1, 1-i, 0),this.canSwim,this.entityWidth)){
						break;
					}
				}
				break;
			case 6:
				for(;i<15;i++){
					if(MRAUtil.isBlockRidable(par1Entity, p.toCoord().add(0, 1-i, -1),this.canSwim,this.entityWidth)){
						break;
					}
				}
				break;
			default:
				flag=false;
			}
			cost+=this.lowerEngineerCost?(i<3?0:i*6):(i<3?0:i*3);
			//if(flag)System.out.println(p.toCoord().toString()+"'s height cost is "+(i<3?0:i*4));
			if(this.settings!=null){
				int[] ids=new int[5*4];
				int num=0;
				for(int y=p.y-1;y<p.y+3;y++){
					BlockPos pos=new BlockPos(p.x, y, p.z);
					ids[num++]=Block.getIdFromBlock(this.worldMap.getBlockState(pos).getBlock());
					ids[num++]=Block.getIdFromBlock(this.worldMap.getBlockState(pos.add(1, 0, 0)).getBlock());
					ids[num++]=Block.getIdFromBlock(this.worldMap.getBlockState(pos.add(0, 0, 1)).getBlock());
					ids[num++]=Block.getIdFromBlock(this.worldMap.getBlockState(pos.add(-1, 0, 0)).getBlock());
					ids[num++]=Block.getIdFromBlock(this.worldMap.getBlockState(pos.add(0, 0, -1)).getBlock());
				}
			}
		}else{
			for(int x=p.x;x>=p.x-1;x--){
				for(int z=p.z;z>=p.z-1;z--){
					BlockPos pos=new BlockPos(x,p.y, z);
					IBlockState b=this.worldMap.getBlockState(pos);
					if(MRAUtil.isLiquid(b))cost+=30;
					if(settings==null){
						if(b.getBlock()==Blocks.LAVA||b.getBlock()==Blocks.FLOWING_LAVA)cost+=500;
						IBlockState id=this.worldMap.getBlockState(pos.add(0, -1, 0));
						if(id.getBlock()==Blocks.CACTUS){
							cost+=40;
						}else if(id.getBlock()==Blocks.LAVA||id.getBlock()==Blocks.FLOWING_LAVA){
							cost+=500;
						}
					}
				}
			}

			//if(!isBlockRidable(e,p.x,p.y-1,p.z))cost+= Block.blocksList[this.worldMap.getBlockId(p.x,p.y-1,p.z)] instanceof BlockFluid? 4:0;
			//if(isBlockRidable(e,p.x,p.y,p.z))cost+=b.getBlockHardness(e.getWorld(),p.x,p.y,p.z)/2+3;
			int i=0;
			boolean flag=true;
			switch(p.index%8){
			case 0:
				for(;i<15;i++){
					if(MRAUtil.isBlockRidable(par1Entity, p.toCoord().add(1, 1-i, 0),this.canSwim,this.entityWidth)){
						break;
					}
				}
				break;
			case 2:
				for(;i<15;i++){
					if(MRAUtil.isBlockRidable(par1Entity,p.toCoord().add(0, 1-i, 1),this.canSwim,this.entityWidth)){
						break;
					}
				}
				break;
			case 4:
				for(;i<15;i++){
					if(MRAUtil.isBlockRidable(par1Entity, p.toCoord().add(-1, 1-i, 0),this.canSwim,this.entityWidth)){
						break;
					}
				}
				break;
			case 6:
				for(;i<15;i++){
					if(MRAUtil.isBlockRidable(par1Entity, p.toCoord().add(0, 1-i, -1),this.canSwim,this.entityWidth)){
						break;
					}
				}
				break;
			default:
				flag=false;
			}
			cost += this.lowerEngineerCost ? ((i < 3) ? 0 : (i * 2)) : ((i < 3) ? 0 : (i * 1));
			//if(flag)System.out.println(p.toCoord().toString()+"'s height cost is "+(i<3?0:i*4));
			if(this.settings!=null){
				int[] ids=new int[12*4];
				int num=0;
				for(int y=p.y-1;y<p.y+3;y++){
					for(int x=p.x-1;x>=p.x+2;x++){
						for(int z=p.z-1;z>=p.z+2;z++){
							ids[num++]=Block.getIdFromBlock(this.worldMap.getBlockState(new BlockPos(x, y, z)).getBlock());
						}
					}
				}

				cost+=this.settings.getTotalBlocksCost(ids);
			}
		}
		if(this.settings!=null){
			if(p.index%4==0){
				Integer num1=(Integer) this.settings.chunkCost.get((((p.x/16)&0x7ff+(p.x<0?0x800:0))<<20)+(((p.y/16)&0xff)<<12)+(p.z/16)&0x7ff+(p.z<0?0x800:0));
				cost+=(num1==null||num1<0?0:num1*2);

			}
			float num2=0;
			int miniChunkHash=p.makeMiniChunkHash();
			if(this.usedCrowdCost.containsKey(miniChunkHash)){
				num2=this.usedCrowdCost.get(miniChunkHash);
			}else{
				if(this.settings.crowdCost.containsKey(miniChunkHash)){
					num2=this.settings.crowdCost.get(miniChunkHash);
					this.usedCrowdCost.put(miniChunkHash, num2);
				}
			}
			cost+=num2*this.ai.getCrowdCost();
		}
		return cost;
	}

	public int getHeuristicCost(Entity par1Entity,AStarPathPoint p,AStarPathPoint end){
		int d1=p.x-end.x;
		int d2=p.z-end.z;
		int d3=p.y-end.y;
		if(this.canUseEngineer){
			int cost=0;
			if((p.totalCost-p.totalRealCost)>(p.previous.totalCost-p.previous.totalRealCost)){
				cost+=p.numberOfBlocksToPut*4;
				cost+=p.numberOfBlocksToBreak*4;
			}
			return (int) (MathHelper.sqrt(d1*d1+d2*d2+d3*d3))*10+cost;
		}else{
			return (int) (MathHelper.sqrt(d1*d1+d2*d2+d3*d3))*5;
		}

	}

	public int getPlannedTickToNext(Entity par1Entity,AStarPathPoint prev,AStarPathPoint current){
		int tickPrev=this.baseTickToNext;
		for(int i=prev.y-1;i<prev.y+MathHelper.floor(this.entityHeight);i++){
			if(MRAUtil.isLiquid(this.worldMap.getBlockState(new BlockPos(prev.x, i, prev.z)))){
				tickPrev*=2;
				break;
			}
		}
		int tickNext=this.baseTickToNext;
		for(int i=current.y-1;i<current.y+MathHelper.floor(this.entityHeight);i++){
			if(MRAUtil.isLiquid(this.worldMap.getBlockState(new BlockPos(current.x, i, current.z)))){
				tickNext*=2;
				break;
			}
		}
		if(prev.y>current.y){
			tickPrev+=(prev.y-current.y)*8;
		}else if(prev.y<current.y){
			tickPrev+=(current.y-prev.y)*4;
		}
		if(par1Entity instanceof IBreakBlocksMob){
			for(int i=0;i<current.numberOfBlocksToBreak;i++){
				BlockPos c=current.blocksToBreak[i];
				tickNext+=10+32*((IBreakBlocksMob)par1Entity).getblockStrength(this.worldMap.getBlockState(c), par1Entity.world, c);

			}
		}
		tickNext+=current.numberOfBlocksToPut*30;


		return tickPrev+tickNext+20;
	}

	public void setSetting(FinderSettings f){
		this.settings=f;
	}

	/**
	 * Returns a new PathEntity for a given start and end point
	 */
	private AStarPathEntity createEntityPath(AStarPathPoint par1AStarPathPoint, AStarPathPoint par2AStarPathPoint,boolean isArrival,int entitySize,Entity entity)
	{
		int i = 1;
		AStarPathPoint AStarPathPoint2;

		for (AStarPathPoint2 = par2AStarPathPoint; AStarPathPoint2.previous != null; AStarPathPoint2 = AStarPathPoint2.previous)
		{
			++i;
		}

		AStarPathPoint[] aAStarPathPoint = new AStarPathPoint[i];
		AStarPathPoint2 = par2AStarPathPoint;
		--i;

		for (aAStarPathPoint[i] = par2AStarPathPoint; AStarPathPoint2.previous != null; aAStarPathPoint[i] = AStarPathPoint2)
		{
			removeUnnecessaryRequests(AStarPathPoint2, entity);
			AStarPathPoint2 = AStarPathPoint2.previous;
			--i;
		}

		return new AStarPathEntity(aAStarPathPoint,this.ai,isArrival,entitySize);
	}

	/*      */   public void removeUnnecessaryRequests(AStarPathPoint newPathPoint, Entity theEntity) {
		/* 1631 */     if (newPathPoint.onLadder)
		/* 1632 */       return;
		boolean canUseEngineer = this.canUseEngineer;
		/* 1633 */     if (!canUseEngineer)
		/* 1634 */       return;
		if (newPathPoint.previous == null || newPathPoint.previous.blocksToPut == null)
		/* 1635 */       return;
		int x = newPathPoint.x;
		/* 1636 */     int y = newPathPoint.y;
		/* 1637 */     int z = newPathPoint.z;
		/* 1638 */     AStarPathPoint previous = newPathPoint.previous;
		/*      */
		/* 1640 */     Entity follower = theEntity;
		/*      */
		/* 1642 */     if (this.entitySize == 1) {
		/* 1643 */       boolean putBlockUnder = false;
		/*      */
		/* 1645 */       float maxY = Math.max(newPathPoint.yOffset + newPathPoint.y, previous.yOffset + previous.y) + follower.height;
		/*      */
		/* 1647 */       for (int j = y; j < maxY; j++) {
		/*      */
		/* 1649 */         for (int k = 0; k < newPathPoint.previous.blocksToPut.length; k++) {
		/* 1650 */           BlockPos c = newPathPoint.previous.blocksToPut[k];
		/* 1651 */           if (c != null && c.equals(new BlockPos(x, j, z))) {
		/* 1652 */             for (int l = k + 1; l < newPathPoint.previous.numberOfBlocksToPut; l++) {
		/* 1653 */               newPathPoint.previous.blocksToPut[l - 1] = newPathPoint.previous.blocksToPut[l];
		/* 1655 */               //if (logRemoveUnne) {
		/* 1656 */                 //System.out.println(c.toString() + "is unnecessary");
		/*      */               //}
		/*      */             }
		newPathPoint.previous.numberOfBlocksToPut--;
		/*      */
		/*      */           }
		/*      */         }
		/*      */       }
		/*      */     } else {
		/*      */
		/* 1665 */       boolean putBlockUnder = false;
		/* 1666 */       boolean ridableFlag = false;
		/* 1667 */       int offY = (newPathPoint.yOffset < 0.0F) ? -1 : 0;
		/*      */
		/*      */
		/* 1670 */       double underMaxY = getMaxY(follower.world, follower, new BlockPos(x, y - 1, z), 2);
		/*      */
		/*      */
		/*      */
		/* 1674 */       float maxY = Math.max(newPathPoint.yOffset + newPathPoint.y, previous.yOffset + previous.y) + follower.height;
		/*      */
		/* 1676 */       float minY = Math.max(newPathPoint.yOffset + newPathPoint.y, previous.yOffset + previous.y) + 0.1F;
		/*      */
		/*      */
		/* 1679 */       boolean waterFlow = false;
		/* 1680 */       int maxYToPlaceLadder = 0;
		/* 1681 */       for (int j = y + offY; j < maxY; j++) {
		/* 1682 */         for (int i = x; i >= x - 1; i--) {
		/* 1683 */           for (int z_ = z; z_ >= z - 1; z_--) {
		/*      */
		/*      */
		/* 1686 */             for (int k = 0; k < newPathPoint.previous.blocksToPut.length; k++) {
		/* 1687 */               BlockPos c = newPathPoint.previous.blocksToPut[k];
		/*      */
		/* 1689 */               if (c != null && c.equals(new BlockPos(i, j, z_))) {
		/* 1690 */                 for (int l = k + 1; l < newPathPoint.previous.numberOfBlocksToPut; l++) {
		/* 1691 */                   newPathPoint.previous.blocksToPut[l - 1] = newPathPoint.previous.blocksToPut[l];
		/*      */                 }

		/* 1692 */                   newPathPoint.previous.numberOfBlocksToPut--;
		/*      */               }
		/*      */             }
		/*      */           }
		/*      */         }
		/*      */       }

		/*      */
		/*      */
		/* 1701 */       for (int x_ = previous.x; x_ >= previous.x - 1; x_--) {
		/* 1702 */         for (int z_ = previous.z; z_ >= previous.z - 1; z_--) {
		/* 1703 */           for (int i = MathHelper.floor(previous.y + follower.height); i < maxY; i++) {
		/*      */
		/* 1705 */             for (int k = 0; k < newPathPoint.previous.blocksToPut.length; k++) {
		/* 1706 */               BlockPos c = newPathPoint.previous.blocksToPut[k];
		/* 1707 */               if (c != null && c.equals(new BlockPos(x_, i, z_))) {
		/* 1708 */                 for (int l = k + 1; l < newPathPoint.previous.numberOfBlocksToPut; l++) {
		/* 1709 */                   newPathPoint.previous.blocksToPut[l - 1] = newPathPoint.previous.blocksToPut[l];
		/* 1710 */
		/*      */                 }
		newPathPoint.previous.numberOfBlocksToPut--;
		/*      */               }
		/*      */             }
		/*      */           }
		/*      */         }
		/*      */       }
		/*      */     }
		/*      */   }


	public static boolean isBlockLiquid(World w,BlockPos pos){
		IBlockState id=w.getBlockState(pos);
		return MRAUtil.isLiquid(id);
	}

	public static double getMaxY(World w,Entity e,BlockPos pos,int size){
		if(size==1){
			IBlockState id=w.getBlockState(pos);
			BlockPos posabove=pos.add(0, 1, 0);
			IBlockState idAbove=w.getBlockState(posabove);
			if(e instanceof EntityLivingBase && idAbove.getBlock().isLadder(idAbove,w, posabove, (EntityLivingBase)e)){
				return posabove.getY()+1;
			}
			if(MRAUtil.isAABBNull(e,pos)){
				return Double.NaN;
			}
			if(id.isOpaqueCube()){
				return posabove.getY();
			}

			if(MRAUtil.isLiquid(id)){
				//System.out.println("point"+newPathPoint.toCoord().toString()+"is fluid");
				return pos.getY()+0.2;
			}
			if((e instanceof EntityLivingBase &&  id.getBlock().isLadder(id,w, pos, (EntityLivingBase)e))){
				return posabove.getY();
			}
			/*
				List<AxisAlignedBB> list=MRAUtil.getCollisionListAt(e, pos);
				double maxY=list.get(0).maxY;
				double maxYNow;
				for(int i=1;i<list.size();i++){
					maxYNow=list.get(i).maxY;
					if(maxYNow>maxY){
						maxY=maxYNow;
					}
				}
				return maxY;
			 */
			//System.out.println("maxY at "+x+","+y+","+z+" is "+maxY);
			return MRAUtil.getAABB(e, pos).maxY;
		}else{
			double maxHeight=Double.NaN;

			for(int i=0;i<4;i++){
				double height;
				//Coord c=null;
				n:{
					IBlockState id=null;
					switch(i){
					case 0:
						id= w.getBlockState(pos);
						//c=new Coord(x, y, z);
						break;
					case 1:
						id= w.getBlockState(pos.add(-1, 0, 0));
						//c=new Coord(x-1, y, z);
						break;
					case 2:
						id= w.getBlockState(pos.add(0, 0, -1));
						//c=new Coord(x, y, z-1);
						break;
					case 3:
						id= w.getBlockState(pos.add(-1, 0, -1));
						//c=new Coord(x-1, y, z-1);
						break;
					}

					if(MRAUtil.isAABBNull(e,pos)){
						height=Double.NaN;
						break n;
					}
					if(id.isOpaqueCube()){
						height=pos.getY()+1;
						break n;
					}

					if(MRAUtil.isLiquid(id)){
						//System.out.println("point"+newPathPoint.toCoord().toString()+"'s under is fluid");
						height= pos.getY()+0.3f;
						break n;
					}
					/*
						List<AxisAlignedBB> list=MRAUtil.getCollisionListAt(e, pos);
						double maxY=list.get(0).maxY;
						double maxYNow;
						for(int j=1;j<list.size();j++){
							maxYNow=list.get(j).maxY;
							if(maxYNow>maxY){
								maxY=maxYNow;
							}
						}
					 */
					height=MRAUtil.getAABB(e, pos).maxY;
					break n;
				}
				//System.out.println("height at "+c+" is "+height);
				if(!Double.isNaN(height) && (Double.isNaN(maxHeight) || maxHeight<height)){
					maxHeight=height;
				}
			}
			//System.out.println("maxY at "+x+","+y+","+z+" is "+maxY);
			return maxHeight;
		}

	}

	public static double getMinY(World w,Entity e,BlockPos pos,int size){
		if(size==1){
			IBlockState id=w.getBlockState(pos);
			if(MRAUtil.isAABBNull(e,pos)){
				return Double.NaN;
			}
			if(id.isOpaqueCube()){
				return pos.getY();
			}
			/*
				List<AxisAlignedBB> list=MRAUtil.getCollisionListAt(e, pos);
				if(list.isEmpty()){
					return pos.getY()-1;
				}
				double minY=list.get(0).minY;
				double minYNow;
				for(int i=1;i<list.size();i++){
					minYNow=list.get(i).minY;
					if(minYNow>minY){
						minY=minYNow;
					}
				}*/
			//System.out.println("maxY at "+x+","+y+","+z+" is "+maxY);
			return MRAUtil.getAABB(e, pos).minY;
		}else{
			double maxHeight=Double.NaN;

			for(int i=0;i<4;i++){
				double height;
				n:{
					IBlockState id=null;
					switch(i){
					case 0:
						id= w.getBlockState(pos);
						break;
					case 1:
						id= w.getBlockState(pos.add(-1, 0, 0));
						break;
					case 2:
						id= w.getBlockState(pos.add(0, 0, -1));
						break;
					case 3:
						id= w.getBlockState(pos.add(-1, 0, -1));
						break;
					}
					if(MRAUtil.isAABBNull(e,pos)){
						height=Double.NaN;
						break n;
					}
					if(id.isOpaqueCube()){
						height= pos.getY();
						break n;
					}
					/*
						List<AxisAlignedBB> list=new ArrayList();
						id.addCollisionBoxesToList(w, x, y, z, new AxisAlignedBB(x, y, z, x+1, y+1, z+1), list, e);
						double minY=list.get(0).minY;
						double minYNow;
						for(int j=1;j<list.size();j++){
							minYNow=list.get(j).minY;
							if(minYNow>minY){
								minY=minYNow;
							}
						}*/
					height=MRAUtil.getAABB(e, pos).minY;
					break n;
				}
				if(Double.isNaN(maxHeight)|| maxHeight<height){
					maxHeight=height;
				}
			}
			//System.out.println("maxY at "+x+","+y+","+z+" is "+maxY);
			return maxHeight;
		}
	}


}

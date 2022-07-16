package regulararmy.pathfinding;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import regulararmy.entity.ai.EngineerRequest;

public class AStarPathPoint
{
    /** The x coordinate of this point */
    public final int x;

    /** The y coordinate of this point */
    public final int y;

    /** The z coordinate of this point */
    public final int z;

    /** A hash of the coordinates used to identify this point */
    private final int hash;

    /** The index of this point in its assigned path */
    public int index = -1;

    /** The cost along the path to this point */
    public int totalCost;

    /** The totalCost,excluded heuristic cost*/
    public int totalRealCost;


    /** Engineers requested before [beforeRequested] blocks.*/
    public int beforeRequested=20;

    /** The linear distance to the next point */
    public float distanceToNext;

    /** The distance to the target */
    public float distanceToTarget;

    public int tickToNext=30;

    /** The point preceding this in its assigned path */
    public AStarPathPoint previous;

    /** Indicates this is on head */
    public boolean isHead;

    /** Indicates whether this is on a ladder*/
    public boolean onLadder;

    public EnumFacing dirFromPrev=null;

    /** They are only for path finding*/
    public BlockPos[] blocksToBreak;
    public BlockPos[] blocksToPut;
    public BlockPos[] laddersToPut;
    public EnumFacing[] ladderDirection;
    public int numberOfBlocksToBreak;
    public int numberOfBlocksToPut;
    public int numberOfLaddersToPut;

    /** Coords where the mob use ladder*/
    public List<BlockPos> coordsLadder;

    public List<EngineerRequest> requestsTemp;

    public List<EngineerRequest> requests;

    /**If a block under this point is opaque, the value is 0.0 . ex)slab:-0.5 dirtBlock:0.0 fence:+0.5*/
    public float yOffset=0f;

    public AStarPathPoint(int par1, int par2, int par3)
    {
        this.x = par1;
        this.y = par2;
        this.z = par3;
        this.hash = makeHash(par1, par2, par3);
    }

    public static int makeHash(int par0, int par1, int par2)
    {
        return par1 & 255 | (par0 & 32767) << 8 | (par2 & 32767) << 24 | (par0 < 0 ? Integer.MIN_VALUE : 0) | (par2 < 0 ? 32768 : 0);
    }

    /**
     * Returns the linear distance to another path point
     */
    public float distanceTo(AStarPathPoint par1PathPoint)
    {
        float f = (float)(par1PathPoint.x - this.x);
        float f1 = (float)(par1PathPoint.y - this.y);
        float f2 = (float)(par1PathPoint.z - this.z);
        return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    public float func_75832_b(AStarPathPoint par1PathPoint)
    {
        float f = (float)(par1PathPoint.x - this.x);
        float f1 = (float)(par1PathPoint.y - this.y);
        float f2 = (float)(par1PathPoint.z - this.z);
        return f * f + f1 * f1 + f2 * f2;
    }

    public boolean equals(Object par1Obj)
    {
        if (!(par1Obj instanceof AStarPathPoint))
        {
            return false;
        }
        else
        {
            AStarPathPoint pathpoint = (AStarPathPoint)par1Obj;
            return this.hash == pathpoint.hash && this.x == pathpoint.x && this.y == pathpoint.y && this.z == pathpoint.z;
        }
    }

    public int hashCode()
    {
        return this.hash;
    }

    /**
     * Returns true if this point has already been assigned to a path
     */
    public boolean isAssigned()
    {
        return this.index >= 0;
    }

    public String toString()
    {
        return this.x + ", " + this.y + ", " + this.z;
    }

    public void addBlocksToBreak(BlockPos c){
    	if(this.blocksToBreak==null){
    		this.blocksToBreak=new BlockPos[16];
    	}else if(this.blocksToBreak[this.blocksToBreak.length-1]!=null){
    		BlockPos[] old=this.blocksToBreak;
    		this.blocksToBreak=new BlockPos[old.length*2];
    		System.arraycopy(old, 0, this.blocksToBreak, 0, old.length);
    	}
    	this.blocksToBreak[this.numberOfBlocksToBreak++]=c;
    	this.beforeRequested=0;
    }

    public void addBlocksToPut(BlockPos c){
    	if(this.blocksToPut==null){
    		this.blocksToPut=new BlockPos[16];
    	}else if(this.blocksToPut[this.blocksToPut.length-1]!=null){
    		BlockPos[] old=this.blocksToPut;
    		this.blocksToPut=new BlockPos[old.length*2];
    		System.arraycopy(old, 0, this.blocksToPut, 0, old.length);
    	}
    	this.blocksToPut[this.numberOfBlocksToPut++]=c;
    	this.beforeRequested=0;
    }

    public void addLaddersToPut(BlockPos c,EnumFacing dir){
    	if(this.laddersToPut==null){
    		this.laddersToPut=new BlockPos[16];
    		this.ladderDirection=new EnumFacing[16];
    	}else if(this.laddersToPut[this.laddersToPut.length-1]!=null){
    		BlockPos[] old=this.laddersToPut;
    		this.laddersToPut=new BlockPos[old.length*2];
    		System.arraycopy(old, 0, this.laddersToPut, 0, old.length);
    		EnumFacing[] old_=this.ladderDirection;
    		this.ladderDirection=new EnumFacing[old_.length*2];
    		System.arraycopy(old_, 0, this.ladderDirection, 0, old_.length);
    	}
    	this.laddersToPut[this.numberOfLaddersToPut]=c;
    	this.ladderDirection[this.numberOfLaddersToPut++]=dir;
    	this.beforeRequested=0;
    }

    public void addCoordsLadder(BlockPos c){
    	if(this.coordsLadder==null){
    		this.coordsLadder=new ArrayList();
    	}
    	this.coordsLadder.add(c);
    }


    public BlockPos toCoord(){
    	return new BlockPos(this.x,this.y,this.z);
    }

    public int getHeuristicCost(){
    	return this.totalCost-this.totalRealCost;
    }

    public int makeMiniChunkHash(){
    	return (((((int)this.x)/4)&0x7ff+(this.x<0?0x800:0))<<20)+(((((int)this.y)/16)&0xff)<<12)+((((int)this.z)/4)&0x7ff+(this.z<0?0x800:0));
    }
}

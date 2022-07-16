package regulararmy.analysis;

import java.util.ArrayList;
import java.util.List;

import regulararmy.analysis.DataAnalyzer.DataNode;
import net.minecraft.nbt.NBTTagCompound;

public class DataAnalyzerOneToOne {
	public static class DataNode{
		public int[] weight;
		public float result;
		
		public DataNode(float result,int...weights){
			this.result=result;
			this.weight=weights;
		}
		
		public DataNode(NBTTagCompound nbt){
			readFromNBT(nbt);
		}
		
		public NBTTagCompound writeToNBT(NBTTagCompound nbt){
			nbt.setIntArray("weight", weight);
			nbt.setFloat("result", this.result);
			return nbt;
		}
		
		public void readFromNBT(NBTTagCompound nbt){
			this.weight=nbt.getIntArray("weight");
			this.result=nbt.getFloat("result");
		}
	}
	public List<DataNode> nodes=new ArrayList(100);
	
	public DataAnalyzerOneToOne(){}
	
	public DataAnalyzerOneToOne(NBTTagCompound nbt){
		this.readFromNBT(nbt);
	}
	
	public float[] analyze_weightedAverage(){
		if(nodes.isEmpty())return new float[0];
		float[] f=new float[nodes.get(0).weight.length];
		for(int i=0;i<nodes.get(0).weight.length;i++){
			float valueSum=0;
			int weightSum=0;
			for(DataNode n:nodes){
				valueSum+=n.result*n.weight[i];
				weightSum+=n.weight[i];
			}
			f[i]=valueSum/weightSum;
			
		}
		return f;
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		DataNode[] nodeArray=this.nodes.toArray(new DataNode[0]);
		for(int i=0;i<nodeArray.length;i++){
			nbt.setTag("node"+i,nodeArray[i].writeToNBT(new NBTTagCompound()));
		}
		return nbt;
	}
	
	public void readFromNBT(NBTTagCompound nbt){
		for(int i=0;true;i++){
			if(!nbt.hasKey("node"+i))break;
			DataNode node=new DataNode(nbt.getCompoundTag("node"+i));
			this.nodes.add(node);
		}
	}
}

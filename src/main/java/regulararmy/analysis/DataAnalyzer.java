package regulararmy.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;

public class DataAnalyzer {

	public static class DataNode {
		public int[][] conditions;

		public float result;
		public DataNode(float result,int[]...conditions){
			this.result=result;
			this.conditions=conditions;
		}

		public DataNode(NBTTagCompound nbt){
			readFromNBT(nbt);
		}

		public NBTTagCompound writeToNBT(NBTTagCompound nbt){
			for(int i=0;i<conditions.length;i++){
				nbt.setIntArray("cond"+i, conditions[i]);
			}
			nbt.setFloat("result", this.result);
			return nbt;
		}

		public void readFromNBT(NBTTagCompound nbt){
			int i=0;
			while(true){
				if(!nbt.hasKey("cond"+i))break;
				i++;
			}
			this.conditions=new int[i][];
			for(int j=0;j<i;j++){
				this.conditions[j]=nbt.getIntArray("cond"+j);
			}
			this.result=nbt.getFloat("result");
		}
	}
	public List<DataNode> nodes=new ArrayList(100);

	public Map<Integer,Integer>[] lastResult;
	public Map<Integer,Integer>[] lastResultsAmount;

	public DataAnalyzer(){

	}

	public DataAnalyzer(NBTTagCompound nbt){
		this.readFromNBT(nbt);
	}

	/**@result Map< Value of condition , Value of result > [Number of condition]*/
	public Map<Integer,Integer>[] analyze_average(){
		if(nodes.isEmpty())return new Map[0];
		Map<Integer,Integer>[] map=new Map[nodes.get(0).conditions.length];
		Map<Integer,Integer>[] resultsAmount=new Map[nodes.get(0).conditions.length];
		//System.out.println(map.length);

		//make a map from nodes
		for(int i=0;i<map.length;i++){

			map[i]=new HashMap(30);
			resultsAmount[i]=new HashMap(30);
			Map<Integer,List<Float>> resultsMap=new HashMap(30);
			for(int j=0;j<nodes.size();j++){
				DataNode node=this.nodes.get(j);
				for(int k=0;k<node.conditions[i].length;k++){
				List<Float> list=resultsMap.get(node.conditions[i][k]);
					if(list==null){
						list=new ArrayList(60);
						resultsMap.put(node.conditions[i][k],list);
					}
					list.add(node.result);
				}

			}
			Integer[] array=resultsMap.keySet().toArray(new Integer[0]);
			for(int j=0;j<resultsMap.size();j++){
				Integer key=array[j];
				List list=resultsMap.get(key);
				float value=0;
				if(this.lastResult!=null&&this.lastResult[i]!=null&&this.lastResult[i].get(key)!=null){
					value=this.lastResult[i].get(key)*this.lastResultsAmount[i].get(key);
					for(int k=0;k<list.size();k++){
						value+=(Float)list.get(k);
					}
					value/=(list.size()+this.lastResultsAmount[i].get(key));

				}else{
					for(int k=0;k<list.size();k++){
						value+=(Float)list.get(k);
					}
					value/=(list.size());
				}
				resultsAmount[i].put(key,list.size());
				map[i].put(key,(int)value);
				//System.out.println("result:"+key+","+value);
			}
		}
		this.lastResult=map;
		this.nodes.clear();
		this.lastResultsAmount=resultsAmount;
		return map;
	}

	/**@result Map< Value of condition , Value of result > [Number of condition]*/
	public Map<Integer,Integer>[] analyze_sum(){
		if(nodes.isEmpty())return new Map[0];
		Map<Integer,Integer>[] map=new Map[nodes.get(0).conditions.length];
		Map<Integer,Integer>[] resultsAmount=new Map[nodes.get(0).conditions.length];
		System.out.println(map.length);
		for(int i=0;i<map.length;i++){
			map[i]=new HashMap(30);
			resultsAmount[i]=new HashMap(30);
			Map<Integer,List<Float>> resultsMap=new HashMap(30);
			for(int j=0;j<nodes.size();j++){
				DataNode node=this.nodes.get(j);
				for(int k=0;k<node.conditions[i].length;k++){
				List<Float> list=resultsMap.get(node.conditions[i][k]);
					if(list==null){
						list=new ArrayList(60);
						resultsMap.put(node.conditions[i][k],list);
					}
					list.add(node.result);
				}

			}
			Integer[] array=resultsMap.keySet().toArray(new Integer[0]);
			for(int j=0;j<resultsMap.size();j++){
				Integer key=array[j];
				List list=resultsMap.get(key);
				float value=0;
				if(this.lastResult!=null&&this.lastResult[i]!=null&&this.lastResult[i].get(key)!=null){
					value=this.lastResult[i].get(key);
					for(int k=0;k<list.size();k++){
						value+=(Float)list.get(k);
					}

				}else{
					for(int k=0;k<list.size();k++){
						value+=(Float)list.get(k);
					}
				}
				resultsAmount[i].put(key,list.size());
				map[i].put(key,(int)value);
				//System.out.println("result:"+key+","+value);
			}
		}
		this.lastResult=map;
		this.lastResultsAmount=resultsAmount;
		this.nodes.clear();
		return map;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		DataNode[] nodeArray=this.nodes.toArray(new DataNode[0]);
		for(int i=0;i<nodeArray.length;i++){
			nbt.setTag("node"+i,nodeArray[i].writeToNBT(new NBTTagCompound()));
		}
		if(this.lastResult!=null){
			for(int i=0;i<this.lastResult.length;i++){
				NBTTagCompound mapNbt=new NBTTagCompound();
				Set<Entry<Integer,Integer>> set=this.lastResult[i].entrySet();
				Iterator<Entry<Integer,Integer>> itr=set.iterator();
				for(int j=0;j<set.size();j++){
					Entry<Integer,Integer> e=itr.next();
					mapNbt.setInteger("key"+j, e.getKey());
					mapNbt.setInteger("value"+j,e.getValue());
				}
				nbt.setTag("lastResult"+i, mapNbt);
			}
			for(int i=0;i<this.lastResultsAmount.length;i++){
				NBTTagCompound mapNbt=new NBTTagCompound();
				Set<Entry<Integer,Integer>> set=this.lastResultsAmount[i].entrySet();
				Iterator<Entry<Integer,Integer>> itr=set.iterator();
				for(int j=0;j<set.size();j++){
					Entry<Integer,Integer> e=itr.next();
					mapNbt.setInteger("key"+j, e.getKey());
					mapNbt.setInteger("value"+j,e.getValue());
				}
				nbt.setTag("lastResultsAmount"+i, mapNbt);
			}
		}
		return nbt;
	}

	public void readFromNBT(NBTTagCompound nbt){
		for(int i=0;true;i++){
			if(!nbt.hasKey("node"+i))break;
			DataNode node=new DataNode(nbt.getCompoundTag("node"+i));
			this.nodes.add(node);
		}
		List<Map> list=new ArrayList();
		for(int i=0;true;i++){
			if(!nbt.hasKey("lastResult"+i))break;
			NBTTagCompound mapNbt=nbt.getCompoundTag("lastResult"+i);
			Map<Integer,Integer> map=new HashMap<Integer,Integer>();
			for(int j=0;true;j++){
				if(!mapNbt.hasKey("key"+j))break;
				map.put(mapNbt.getInteger("key"+j), mapNbt.getInteger("value"+j));
			}
			list.add(map);
		}
		if(list.size()>0){
			this.lastResult=list.toArray(new HashMap[0]);
		}

		List<Map> list1=new ArrayList();
		for(int i=0;true;i++){
			if(!nbt.hasKey("lastResultsAmount"+i))break;
			NBTTagCompound mapNbt=nbt.getCompoundTag("lastResultsAmount"+i);
			Map<Integer,Integer> map=new HashMap<Integer,Integer>();
			for(int j=0;true;j++){
				if(!mapNbt.hasKey("key"+j))break;
				map.put(mapNbt.getInteger("key"+j), mapNbt.getInteger("value"+j));
			}
			list1.add(map);
		}
		if(list1.size()>0){
			this.lastResultsAmount=list1.toArray(new HashMap[0]);
		}
	}
}

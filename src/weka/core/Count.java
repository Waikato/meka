package weka.core;

import weka.core.*;
import java.util.*;

/**
 * For maintaining combination counts (for PS).
 * @see PS
 */
public class Count extends HashMap <String,Integer> {

	public Count() {
	}

	public Count(Instances instances, int L) {
		for (int i = 0; i < instances.numInstances(); i++) {
			this.add(MLUtils.toBitString(instances.instance(i),L));
		}
	}

	public void add(String key) {
		Integer freq = (Integer)get(key);
		put(key,(freq == null) ? 1 : freq + 1);
	}

	public Integer get(Object key) {
		Integer freq = (Integer)super.get(key);
		return (freq == null) ? new Integer(0) : freq;
	}

	public void prune(int min) {
		ArrayList al = new ArrayList();  
		for (Object s : keySet()) {
			if((int)(Integer)get((String)s) <= min) {
				al.add(s);
			}
		}
		for (Object s : al) {
			remove(s);
		}
		al.clear();
		al = null;
	}

}

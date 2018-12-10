package com.nowcoder.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hello on 2018/6/15.
 */
public class ViewObject {
	private Map<String,Object> vo = new HashMap<>();

	public void set(String key,Object value){
		vo.put(key, value);
	}

	public Object get(String key){
		return vo.get(key);
	}
}

package com.nowcoder.async;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 事件生产者,在需要异步处理的地方引入
 * 将发生的事件序列化,放入redis的优先队列
 * Created by Hello on 2018/8/5.
 */
@Service
public class EventProducer {

	@Autowired
	JedisAdapter jedisAdapter;

	public boolean fireEvent(EventModel eventModel){
		try {
			String json = JSONObject.toJSONString(eventModel);
			String key = RedisKeyUtil.getEventQueue();
			jedisAdapter.lpush(key, json);
			return true;
		}catch (Exception e){
			return false;
		}
	}
}

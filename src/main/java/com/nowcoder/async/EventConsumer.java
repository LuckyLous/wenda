package com.nowcoder.async;

import com.alibaba.fastjson.JSON;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事件消费者：加入spring容器，初始化之前进行处理
 * 1、从spring容器中根据EventHandler接口获取所有的实现类，注册到map中，与eventType对应
 * 2、利用多线程异步等待，并从redis的队列中取出所有的入队事件
 * 3、根据事件model中的eventType，从map中寻找具体的Handler，遍历并调用doHandler
 * Created by Hello on 2018/8/5.
 */
@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware{

	private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
	// 根据事件类型，去找一批的事件处理实现类
	private Map<EventType, List<EventHandler>> config = new HashMap<EventType, List<EventHandler>>();
	private ApplicationContext applicationContext;

	@Autowired
	JedisAdapter jedisAdapter;

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);

		// 根据事件类型，将具体的实现类放入map
		if (beans != null) {
			for (Map.Entry<String, EventHandler> entry: beans.entrySet()) {

				// 遍历的EventHandler关注的是什么事件类型，与事件处理一起注册到map
				List<EventType> eventTypes = entry.getValue().getSupportEventTypes();
				for (EventType type: eventTypes) {
					if(!config.containsKey(type)){
						config.put(type, new ArrayList<EventHandler>());
					}
					config.get(type).add(entry.getValue());
				}

			}
		}

		// 多线程实现异步架构，从redis中取出List<EventModal>，在遍历中反序列化，再处理
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true){
					String key = RedisKeyUtil.getEventQueue();
					List<String> events = jedisAdapter.brpop(0, key);// 从队列中取，如果时间为0，就阻塞等待

					for (String message: events){
						// 返回的第一个参数可能是key，把它过滤掉
						if(message.contains(key)){
							continue;
						}

						// 如果map中没有注册对应的处理事件，视为异常
						EventModel eventModel = JSON.parseObject(message, EventModel.class);
						if(!config.containsKey(eventModel.getType())){
							logger.error("不能识别的事件");
							continue;
						}

						// 如果map中有处理的事件链，遍历并处理
						for (EventHandler handler : config.get(eventModel.getType())){
							handler.doHandle(eventModel);
						}
					}
				}
			}
		});
		thread.start();

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}

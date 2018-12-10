package com.nowcoder.async;

import java.util.List;

/**
 * 事件处理接口，定义支持处理的事件类型，根据事件model获取数据进行处理
 *
 * Created by Hello on 2018/8/5.
 */
public interface EventHandler {

	// 处理事件
	void doHandle(EventModel model);

	// 自己关心哪些Event
	List<EventType> getSupportEventTypes();
}

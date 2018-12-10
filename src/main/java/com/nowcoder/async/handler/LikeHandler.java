package com.nowcoder.async.handler;

import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 在点赞的同时，系统给被赞的实体(评论)发出者，发送私信
 * 显示谁在哪个页面点了赞
 * 加入spring容器，为了EventConsumer能从接口获取到
 * Created by Hello on 2018/8/5.
 */
@Component
public class LikeHandler implements EventHandler {

	@Autowired
	MessageService messageService;

	@Autowired
	UserService userService;

	@Override
	public void doHandle(EventModel model) {
		Message message = new Message();
		message.setFromId(WendaUtil.SYSTEM_USERID);
		message.setToId(model.getEntityOwnerId());
		message.setCreatedDate(new Date());
		User user = userService.getUser(model.getActorId());
		message.setContent("用户" + user.getName()
				+ "赞了你的评论,http://127.0.0.1:8080/question/" + model.getExt("questionId"));

		messageService.addMessage(message);
	}

	@Override
	public List<EventType> getSupportEventTypes() {
		return Arrays.asList(EventType.LIKE);
	}
}

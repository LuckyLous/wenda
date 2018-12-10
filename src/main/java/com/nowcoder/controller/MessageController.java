package com.nowcoder.controller;

import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Hello on 2018/6/15.
 */
@Controller
public class MessageController {

	private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

	@Autowired
	HostHolder hostHolder;

	@Autowired
	MessageService messageService;

	@Autowired
	UserService userService;

	@RequestMapping(path = {"/msg/addMessage"}, method = {RequestMethod.POST})
	@ResponseBody
	// 与前端的约定： 0-表示成功 1-表示失败 999-表示未登录
	public String addMessage(@RequestParam(value = "toName") String toName,
							  @RequestParam(value = "content") String content){
		try {
			if(hostHolder.getUser()==null){
				return WendaUtil.getJSONString(999,"未登录");
			}

			User user = userService.selectByName(toName);
			if (user == null) {
				return WendaUtil.getJSONString(1, "用户不存在");
			}

			Message message = new Message();
			message.setFromId(hostHolder.getUser().getId());
			message.setToId(user.getId());
			message.setContent(content);
			message.setCreatedDate(new Date());

			messageService.addMessage(message);
			return WendaUtil.getJSONString(0);

		}catch (Exception e){
			logger.error("发送消息失败",e.getMessage());
			return WendaUtil.getJSONString(1, "发信失败");
		}

	}

	/**
	 * 站内信列表
	 */
	@RequestMapping(path = {"/msg/list"}, method = {RequestMethod.GET})
	public String getConversationList(Model model){
		try {
			if(hostHolder.getUser() == null){
				return "redirect:/reglogin";
			}
			int localUserId = hostHolder.getUser().getId();
			// 查自己的私信列表
			List<Message> conversationList = messageService.getConversationList(localUserId, 0, 10);
			List<ViewObject> conversations = new ArrayList<>();
			for (Message message : conversationList) {
				ViewObject vo = new ViewObject();
				vo.set("message", message);
				// 查对方的信息
				int targetId = localUserId == message.getFromId() ? message.getToId() : message.getFromId();
				vo.set("user", userService.getUser(targetId));
				// 查私信中未读的次数
				vo.set("unread", messageService.getConversationUnreadCount(localUserId, message.getConversationId()));
				conversations.add(vo);
			}

			model.addAttribute("conversations", conversations);
		} catch (Exception e) {
			logger.error("获取站内信列表失败" + e.getMessage());
		}
		return "letter";
	}

	/**
	 * 私信详情
	 */
	@RequestMapping(path = {"/msg/detail"}, method = {RequestMethod.GET})
	public String getConversationDetail(Model model, @RequestParam("conversationId") String conversationId){

		try {
			// 查自己的私信详情
			List<Message> messageList = messageService.getConversationDetail(conversationId, 0, 10);
			List<ViewObject> messages = new ArrayList<>();
			for (Message message : messageList) {
				ViewObject vo = new ViewObject();
				vo.set("message", message);
				vo.set("user", userService.getUser(message.getFromId()));
				messages.add(vo);
			}

			model.addAttribute("messages", messages);
		} catch (Exception e) {
			logger.error("获取详情失败", e.getMessage());
		}

		return "letterDetail";
	}


}

package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hello on 2018/6/15.
 */
@Controller
public class FollowController {

	private static final Logger logger = LoggerFactory.getLogger(FollowController.class);

	@Autowired
	FollowService followService;

	@Autowired
	HostHolder hostHolder;

	@Autowired
	CommentService commentService;

	@Autowired
	QuestionService questionService;

	@Autowired
	UserService userService;

	@Autowired
	EventProducer eventProducer;

	/**
	 * 关注用户
	 * @param userId
	 * @return
	 */
	@RequestMapping(path = {"/followUser"}, method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public String followUser(@RequestParam(value = "userId") int userId){
		if (hostHolder.getUser() == null) {
			return WendaUtil.getJSONString(999);
		}

		boolean ret = followService.follow(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId);

		// 给当前用户推送已关注用户的动态(目前未做)
		eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
								.setActorId(hostHolder.getUser().getId()).setEntityId(userId)
								.setEntityType(EntityType.ENTITY_USER).setEntityOwnerId(userId));

		// 返回关注的人数
		return WendaUtil.getJSONString(ret ? 0 : 1, String.valueOf(followService.getFolloweeCount(hostHolder.getUser().getId(), EntityType.ENTITY_USER)));
	}

	/**
	 * 取消关注用户
	 * @param userId
	 * @return
	 */
	@RequestMapping(path = {"/unfollowUser"}, method = {RequestMethod.POST})
	@ResponseBody
	public String unfollowUser(@RequestParam(value = "userId") int userId){
		if (hostHolder.getUser() == null) {
			return WendaUtil.getJSONString(999);
		}

		// 给当前用户移除已关注用户的动态(目前未做)
		eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW)
				.setActorId(hostHolder.getUser().getId()).setEntityId(userId)
				.setEntityType(EntityType.ENTITY_USER).setEntityOwnerId(userId));

		boolean ret = followService.unfollow(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId);
		// 返回关注的人数
		return WendaUtil.getJSONString(ret ? 0 : 1, String.valueOf(followService.getFolloweeCount(hostHolder.getUser().getId(), EntityType.ENTITY_USER)));
	}

	/**
	 * 关注问题
	 * @param questionId
	 * @return
	 */
	@RequestMapping(path = {"/followQuestion"}, method = {RequestMethod.POST})
	@ResponseBody
	public String followQuestion(@RequestParam(value = "questionId") int questionId){
		if (hostHolder.getUser() == null) {
			return WendaUtil.getJSONString(999);
		}

		Question q = questionService.getById(questionId);
		if(q == null){
			return WendaUtil.getJSONString(1, "问题不存在");
		}

		boolean ret = followService.follow(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, questionId);

		// 给当前用户推送已关注问题的动态
		eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
				.setActorId(hostHolder.getUser().getId()).setEntityId(questionId)
				.setEntityType(EntityType.ENTITY_QUESTION).setEntityOwnerId(q.getUserId()));

		Map<String, Object> info = new HashMap<>();
		info.put("headUrl", hostHolder.getUser().getHeadUrl());
		info.put("name", hostHolder.getUser().getName());
		info.put("id", hostHolder.getUser().getId());
		// 获取问题的收藏人数
		info.put("count", followService.getFollowerCount(EntityType.ENTITY_QUESTION, questionId));

		// 返回关注的人数
		return WendaUtil.getJSONString(ret ? 0 : 1, info);
	}

	/**
	 * 取消关注问题
	 * @param questionId
	 * @return
	 */
	@RequestMapping(path = {"/unfollowQuestion"}, method = {RequestMethod.POST})
	@ResponseBody
	public String unfollowQuestion(@RequestParam(value = "questionId") int questionId){
		if (hostHolder.getUser() == null) {
			return WendaUtil.getJSONString(999);
		}

		Question q = questionService.getById(questionId);
		if(q == null){
			return WendaUtil.getJSONString(1, "问题不存在");
		}

		boolean ret = followService.unfollow(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, questionId);

		// 给当前用户移除已关注问题的动态
		eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW)
				.setActorId(hostHolder.getUser().getId()).setEntityId(questionId)
				.setEntityType(EntityType.ENTITY_QUESTION).setEntityOwnerId(q.getUserId()));

		Map<String, Object> info = new HashMap<>();
		info.put("id", hostHolder.getUser().getId());
		// 获取问题的收藏人数
		info.put("count", followService.getFollowerCount(EntityType.ENTITY_QUESTION, questionId));
		return WendaUtil.getJSONString(ret ? 0 : 1, info);
	}

	/**
	 * 获取某一个用户的粉丝列表，不一定是当前用户
	 * 先查ids，再经ViewObject封装
	 * @param userId
	 */
	@RequestMapping(path = {"/user/{uid}/followers"}, method = {RequestMethod.GET})
	public String followers(Model model, @PathVariable("uid") int userId){
		// 查询对应用户的粉丝id列表
		List<Integer> followerIds = followService.getFollowers(EntityType.ENTITY_USER, userId, 0, 10);
		if (hostHolder.getUser() != null) {
			model.addAttribute("followers", getUserInfo(hostHolder.getUser().getId(), followerIds));
		}else {
			model.addAttribute("followers", getUserInfo(0, followerIds)); // 当前用户未登录时，传入0
		}
		// 查询对应用户的粉丝总数
		model.addAttribute("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
		// 查询对应用户本身
		model.addAttribute("curUser", userService.getUser(userId)); // 获取查询用户
		return "followers";
	}

	/**
	 * 获取某一个用户的关注列表，不一定是当前用户
	 * @param userId
	 */
	@RequestMapping(path = {"/user/{uid}/followees"}, method = {RequestMethod.GET})
	public String followees(Model model, @PathVariable("uid") int userId){
		// 查询对应用户的关注id列表
		List<Integer> followeeIds = followService.getFollowees(userId, EntityType.ENTITY_USER, 0, 10);
		if (hostHolder.getUser() != null) {
			model.addAttribute("followees", getUserInfo(hostHolder.getUser().getId(), followeeIds));
		}else {
			model.addAttribute("followees", getUserInfo(0, followeeIds)); // 当前用户未登录时，传入0
		}
		// 查询对应用户的关注总数
		model.addAttribute("followeeCount", followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
		// 查询对应用户本身
		model.addAttribute("curUser", userService.getUser(userId)); // 获取查询用户
		return "followees";
	}

	/**
	 * 根据id，封装粉丝数据
	 * @param localUserId 为0时，表示当前用户未登录
	 * @param userIds
	 * @return
	 */
	private List<ViewObject> getUserInfo(int localUserId, List<Integer> userIds) {
		List<ViewObject> userInfos = new ArrayList<>();
		for (Integer uid : userIds) {
			ViewObject vo = new ViewObject();

			User user = userService.getUser(uid);
			if(user == null){
				continue;
			}

			vo.set("user", user);
			vo.set("commentCount", commentService.getUserCommentCount(uid));// 该粉丝有多少评论
			vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, uid)); // 该粉丝有多少粉丝
			vo.set("followeeCount", followService.getFolloweeCount(uid, EntityType.ENTITY_USER)); // 该粉丝有多少关注
			if(localUserId != 0){ // 表示当前用户已登录，查看对该粉丝是否关注
				vo.set("followed", followService.isFollower(localUserId, EntityType.ENTITY_USER, uid));
			}else {
				vo.set("followed", false);
			}

			userInfos.add(vo);
		}
		return userInfos;

	}

}

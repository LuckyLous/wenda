package com.nowcoder.async.handler;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.model.EntityType;
import com.nowcoder.model.Feed;
import com.nowcoder.model.Question;
import com.nowcoder.model.User;
import com.nowcoder.service.FeedService;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 在某些行为后，在首页刷新推送(拉拽)信息
 * Created by Hello on 2018/8/7.
 */
@Component
public class FeedHandler implements EventHandler {

	@Autowired
	FeedService feedService;

	@Autowired
	JedisAdapter jedisAdapter;

	@Autowired
	FollowService followService;

	@Autowired
	QuestionService questionService;

	@Autowired
	UserService userService;

	// 往redis的队列中放入对应事件的新鲜事id
	@Override
	public void doHandle(EventModel model) {
		// 为了测试，把model的userId随机一下
		Random random = new Random();
		model.setActorId(1 + random.nextInt(10));

		// 构造一个新鲜事
		Feed feed = new Feed();
		feed.setCreatedDate(new Date());
		feed.setType(model.getType().getValue()); // 根据事件类型确定推送类型
		feed.setUserId(model.getActorId()); // 推送信息的触发者
		feed.setData(buildFeedData(model)); // 根据触发事件确定推送数据
		if(feed.getData() == null){
			// 不支持的feed
			return;
		}
		feedService.addFeed(feed);

		// 获得触发者的所有粉丝
		List<Integer> followers = followService.getFollowers(EntityType.ENTITY_USER, model.getActorId(), Integer.MAX_VALUE);
		// 系统队列
		followers.add(0);
		// 给所有粉丝推事件(即通过TIMELINE+粉丝id确定timelineKey，存储新鲜事id到redis，等待用户取出)
		for (int follower : followers){
			String timelineKey = RedisKeyUtil.getTimelineKey(follower);
			jedisAdapter.lpush(timelineKey, String.valueOf(feed.getId()));
			// 限制最长长度，如果timelineKey的长度过大，就删除后面的新鲜事
		}
	}

	// 构建新鲜事(Feed)的JSON数据
	private String buildFeedData(EventModel model) {
		Map<String, String> map = new HashMap<>();
		// 触发用户是通用的
		User actor = userService.getUser(model.getActorId());
		if(actor == null){
			return null;
		}
		map.put("userId", String.valueOf(actor.getId()));
		map.put("headUrl", actor.getHeadUrl());
		map.put("userName", actor.getName());

		// 如果事件类型是评论或者关注问题，都可以根据entityId查出问题
		// 因为是问答社区，所以动态信息以触发者、以及提出的问题为主
		if(model.getType() == EventType.COMMENT ||
				(model.getType() == EventType.FOLLOW && model.getEntityType() == EntityType.ENTITY_QUESTION)){
			Question question = questionService.getById(model.getEntityId());
			if(question == null){
				return null;
			}
			map.put("questionId", String.valueOf(question.getId()));
			map.put("questionTitle", question.getTitle());
			return JSONObject.toJSONString(map);
		}
		return null;
	}

	// 在评论、关注后，更新推拉信息
	@Override
	public List<EventType> getSupportEventTypes() {
		return Arrays.asList(EventType.COMMENT, EventType.FOLLOW);
	}
}

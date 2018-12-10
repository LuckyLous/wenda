package com.nowcoder.controller;

import com.nowcoder.model.EntityType;
import com.nowcoder.model.Feed;
import com.nowcoder.model.HostHolder;
import com.nowcoder.service.FeedService;
import com.nowcoder.service.FollowService;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * 活跃用户推，其他用户拉，Twitter以推为主
 * Created by Hello on 2018/8/7.
 */
@Controller
public class FeedController {

	private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

	@Autowired
	FeedService feedService;

	@Autowired
	FollowService followService;

	@Autowired
	HostHolder hostHolder;

	@Autowired
	JedisAdapter jedisAdapter;

	/**
	 * 推送模式
	 * @param model
	 * @return
	 */
	@RequestMapping(path = {"/pushfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
	public String getPushFeeds(Model model){
		int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
		// 从redis取10条新鲜事id
		List<String> feedIds = jedisAdapter.lrange(RedisKeyUtil.getTimelineKey(localUserId), 0, 10);
		List<Feed> feeds = new ArrayList<>();
		for (String feedId : feedIds){
			Feed feed = feedService.getById(Integer.parseInt(feedId));
			if(feed != null){
				feeds.add(feed);
			}
		}
		model.addAttribute("feeds", feeds);
		return "feeds";
	}

	/**
	 * 下拉模式
	 * @param model
	 * @return
	 */
	@RequestMapping(path = {"/pullfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
	public String getPullFeeds(Model model){
		int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
		List<Integer> followees = new ArrayList<>();
		if(localUserId != 0){
			// 当前用户所有关注的人
			followees = followService.getFollowees(localUserId, EntityType.ENTITY_USER, Integer.MAX_VALUE);
		}
		List<Feed> feeds = feedService.getUserFeeds(Integer.MAX_VALUE, followees, 10);
		model.addAttribute("feeds", feeds);
		return "feeds";
	}
}

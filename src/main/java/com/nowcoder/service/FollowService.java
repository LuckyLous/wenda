package com.nowcoder.service;

import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 关注服务
 * A关注B：A是B的粉丝（follower），B是A的关注对象（followee）
 * Created by Hello on 2018/8/6.
 */
@Service
public class FollowService {

	@Autowired
	JedisAdapter jedisAdapter;

	/**
	 * 用户关注了某个实体,可以关注问题,关注用户,关注评论等任何实体
	 * A关注B(数据结构)，B的关注列表把A的id加上，A的关注对象里把关注id加上
	 * @param userId
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	public boolean follow(int userId, int entityType, int entityId){
		String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);// 确定关注实体的粉丝列表，将自己加入
		String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);// 确定自己的关注列表，将要关注的人加入
		Date date = new Date();
		// 实体的粉丝增加当前用户
		Jedis jedis = jedisAdapter.getJedis();
		Transaction tx = jedisAdapter.multi(jedis);
		tx.zadd(followerKey, date.getTime(), String.valueOf(userId));
		// 当前用户对这类实体关注+1
		tx.zadd(followeeKey, date.getTime(), String.valueOf(entityId));
		List<Object> ret = jedisAdapter.exec(tx, jedis);
		return ret.size() == 2 && (Long) ret.get(0) > 0 && (Long) ret.get(1) > 0;
	}

	/**
	 * 取消关注
	 * @param userId
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	public boolean unfollow(int userId, int entityType, int entityId){
		String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
		String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
		Date date = new Date();
		// 实体的粉丝减少当前用户
		Jedis jedis = jedisAdapter.getJedis();
		Transaction tx = jedisAdapter.multi(jedis);
		tx.zrem(followerKey, String.valueOf(userId));
		// 当前用户对这类实体关注-1
		tx.zrem(followeeKey,  String.valueOf(entityId));
		List<Object> ret = jedisAdapter.exec(tx, jedis);
		return ret.size() == 2 && (Long) ret.get(0) > 0 && (Long) ret.get(1) > 0;
	}

	// 将Set集合转换为List集合
	private List<Integer> getIdsFromSet(Set<String> idset){
		List<Integer> list = new ArrayList<>();
		for (String str : idset){
			list.add(Integer.parseInt(str));
		}
		return list;
	}

	// 粉丝列表
	public List<Integer> getFollowers(int entityType, int entityId, int count){
		String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
		return getIdsFromSet(jedisAdapter.zrevrange(followerKey, 0, count));
	}

	public List<Integer> getFollowers(int entityType, int entityId, int offset, int count){
		String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
		return getIdsFromSet(jedisAdapter.zrevrange(followerKey, offset, offset + count));
	}

	// 关注列表
	public List<Integer> getFollowees(int userId, int entityType, int count){
		String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
		return getIdsFromSet(jedisAdapter.zrevrange(followeeKey, 0, count));
	}

	public List<Integer> getFollowees(int userId, int entityType, int offset, int count){
		String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
		return getIdsFromSet(jedisAdapter.zrevrange(followeeKey, offset, offset + count));
	}

	// 粉丝(收藏)数
	public long getFollowerCount(int entityType, int entityId) {
		String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
		return jedisAdapter.zcard(followerKey);
	}

	// 关注数
	public long getFolloweeCount(int userId, int entityType) {
		String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
		return jedisAdapter.zcard(followeeKey);
	}

	/**
	 *  判断用户是否关注了某个实体
	 *  看一下要关注的实体的粉丝列表里面有没有自己
	 * @param userId
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	public boolean isFollower(int userId, int entityType, int entityId) {
		String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
		return jedisAdapter.zscore(followerKey, String.valueOf(userId)) != null;
	}
}

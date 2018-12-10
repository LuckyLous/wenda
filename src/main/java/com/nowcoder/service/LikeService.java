package com.nowcoder.service;

import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Hello on 2018/8/4.
 */
@Service
public class LikeService  {

	@Autowired
	JedisAdapter jedisAdapter;

	/**
	 * set集合:将用户id添加到某个实体(如问题)的like的集合，并从dislike的集合中移除
	 */
	public long like(int userId, int entityType, int entityId) {

		String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
		jedisAdapter.sadd(likeKey, String.valueOf(userId));

		String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
		jedisAdapter.srem(disLikeKey, String.valueOf(userId));

		return jedisAdapter.scard(likeKey);
	}

	public long disLike(int userId, int entityType, int entityId) {

		String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
		jedisAdapter.sadd(disLikeKey, String.valueOf(userId));

		String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
		jedisAdapter.srem(likeKey, String.valueOf(userId));

		return jedisAdapter.scard(likeKey);
	}


	/**
	 * 如果用户对某个实体点赞返回1，点踩返回-1，不点返回0
	 */
	public int getLikeStatus(int userId, int entityType, int entityId) {
		String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
		if(jedisAdapter.sismember(likeKey, String.valueOf(userId))){
			return 1;
		}

		String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
		return jedisAdapter.sismember(disLikeKey, String.valueOf(userId)) ? -1 : 0;
	}

	/**
	 * 获取某个实体赞的总数
	 */
	public long getLikeCount(int entityType, int entityId){
		String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
		return jedisAdapter.scard(likeKey);
	}

}

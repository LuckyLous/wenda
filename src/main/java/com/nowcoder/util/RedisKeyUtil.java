package com.nowcoder.util;

/**
 * Created by Hello on 2018/8/4.
 */
public class RedisKeyUtil {

	private static String SPLIT = ":";
	private static String BIZ_LIKE = "LIKE";
	private static String BIZ_DISLIKE = "DISLIKE";
	private static String BIZ_EVENTQUEUE = "EVENT_QUEUE";

	// 获取粉丝
	private static String BIZ_FOLLOWER = "FOLLOWER";
	// 关注对象
	private static String BIZ_FOLLOWEE = "FOLLOWEE";

	private static String BIZ_TIMELINE = "TIMELINE";


	public static String getLikeKey(int entityType, int entityId) {
		return BIZ_LIKE + SPLIT + entityType + SPLIT + entityId;
	}

	public static String getDisLikeKey(int entityType, int entityId) {
		return BIZ_DISLIKE + SPLIT + entityType + SPLIT + entityId;
	}

	public static String getEventQueue(){
		return BIZ_EVENTQUEUE;
	}

	// 某个实体的粉丝key
	public static String getFollowerKey(int entityType, int entityId){
		return BIZ_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
	}

	// 每个用户对某类实体的关注key
	public static String getFolloweeKey(int userId, int entityType){
		return BIZ_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
	}

	// 根据粉丝的id，放入Feed的id
	public static String getTimelineKey(int userId){
		return BIZ_TIMELINE + SPLIT + userId;
	}
}

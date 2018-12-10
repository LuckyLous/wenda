package com.nowcoder.async;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件modal,包括触发事件类型、触发者、目标实体类型、目标实体id，目标实体发出者
 * 额外信息封装到map中
 * Created by Hello on 2018/8/4.
 */
public class EventModel {

	private EventType type;
	private int actorId;
	private int entityType; // 实体类型
	private int entityId; // 实体id
	private int entityOwnerId; // 实体发出者，若为用户，则与entityId无区别，若为问题、评论，则需查表找相关userId

	private Map<String, String> exts = new HashMap<>();

	// 构造方法
	public EventModel() {}

	public EventModel(EventType type) {
		this.type = type;
	}

	// 拓展map方法的连锁
	public EventModel setExt(String key, String value) {
		exts.put(key, value);
		return this;
	}

	public String getExt(String key) {
		return exts.get(key);
	}

	// 改造通常的set方法，形成连锁
	public EventType getType() {
		return type;
	}

	public EventModel setType(EventType type) {
		this.type = type;
		return this;
	}

	public int getActorId() {
		return actorId;
	}

	public EventModel setActorId(int actorId) {
		this.actorId = actorId;
		return this;
	}

	public int getEntityType() {
		return entityType;
	}

	public EventModel setEntityType(int entityType) {
		this.entityType = entityType;
		return this;
	}

	public int getEntityId() {
		return entityId;
	}

	public EventModel setEntityId(int entityId) {
		this.entityId = entityId;
		return this;
	}

	public int getEntityOwnerId() {
		return entityOwnerId;
	}

	public EventModel setEntityOwnerId(int entityOwnerId) {
		this.entityOwnerId = entityOwnerId;
		return this;
	}

	public Map<String, String> getExts() {
		return exts;
	}

	public EventModel setExts(Map<String, String> exts) {
		this.exts = exts;
		return this;
	}
}

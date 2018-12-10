package com.nowcoder.model;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;

/**
 * 新鲜事
 * Created by Hello on 2018/8/7.
 */
public class Feed {

	private int id;
	private int type; // 推送的类型
	private int userId; // 消息的发出者
	private Date createdDate;
	private String data; // 推送的数据
	private JSONObject dataJSON = null;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	// 获取全部数据
	public String getData() {
		return data;
	}

	// 改造方法，不仅作为String存储，也转为JSON存储
	public void setData(String data) {
		this.data = data;
		dataJSON = JSONObject.parseObject(data);
	}

	// 补充方法，可以从JSON状态的Data中根据key获取数据
	public String get(String key){
		return dataJSON == null ? null : dataJSON.getString(key);
	}
}

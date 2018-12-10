package com.nowcoder.model;

import org.springframework.stereotype.Component;

/**
 * 保存不同线程访问的User信息
 * Created by Hello on 2018/7/16.
 */
@Component
public class HostHolder {

	private static ThreadLocal<User> users = new ThreadLocal<User>();

	public User getUser(){
		return users.get();
	}

	public void setUser(User user){
		users.set(user);
	}

	public void clear(){
		users.remove();
	}


}

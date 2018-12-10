package com.nowcoder.interceptor;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 自动登录拦截器，无论ticket过不过期都允许访问
 * Created by Hello on 2018/7/16.
 */
@Component
public class PassportInterceptor implements HandlerInterceptor {

	@Autowired
	LoginTicketDAO loginTicketDAO;

	@Autowired
	UserDAO userDAO;

	@Autowired
	HostHolder hostHolder;

	// 调用controller之前
	@Override
	public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
		// 获取ticket的cookie值
		String ticket = null;
		for (Cookie cookie: httpServletRequest.getCookies()){
			if("ticket".equals(cookie.getName())){
				ticket = cookie.getValue();
				break;
			}
		}

		// cookie的值非空，查询loginTicket
		if(StringUtils.isNotBlank(ticket)){
			LoginTicket loginTicket = loginTicketDAO.selectByTicket(ticket);
			// loginTicket过期，不设置User信息
			if(loginTicket == null || loginTicket.getExpire().before(new Date()) || loginTicket.getStatus() != 0){
				return true;
			}

			// 未过期，给HostHolder设置信息
			User user = userDAO.selectById(loginTicket.getUserId());
			hostHolder.setUser(user);
		}
		return true;
	}

	// 页面渲染之前，放入User信息
	@Override
	public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
		if(modelAndView != null && hostHolder.getUser() != null){
			modelAndView.addObject("user",hostHolder.getUser());
		}
	}

	// 页面渲染完成
	@Override
	public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

	}
}

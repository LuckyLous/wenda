package com.nowcoder.controller;

import com.nowcoder.model.EntityType;
import com.nowcoder.model.Question;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.SearchService;
import com.nowcoder.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hello on 2018/6/15.
 */
@Controller
public class SearchController {
	private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

	@Autowired
	SearchService searchService;

	@Autowired
	UserService userService;

	@Autowired
	QuestionService questionService;

	@Autowired
	FollowService followService;


	@RequestMapping(path = {"/search"}, method = {RequestMethod.GET})
	public String search(Model model, @RequestParam(value = "q") String keyword,
						 @RequestParam(value = "offset", defaultValue = "0") int offset,
						 @RequestParam(value = "count", defaultValue = "0") int count){
		try {
			List<Question> questionList = searchService.searchQuestion(keyword, offset, count, "<em>", "</em>");
			List<ViewObject> vos = new ArrayList<>();

			for(Question question : questionList){
				ViewObject vo = new ViewObject();
				Question q = questionService.getById(question.getId());
				if(question.getTitle() != null){
					q.setTitle(question.getTitle());
				}
				if(question.getContent() != null){
					q.setContent(question.getContent());
				}


				vo.set("question", q); // 问题本身
				vo.set("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId())); // 查询该问题的收藏数(粉丝)
				vo.set("user", userService.getUser(q.getUserId())); // 提出问题的用户信息
				vos.add(vo);
			}

			model.addAttribute("vos", vos);
			model.addAttribute("keyword", keyword); // 关键字回显
		} catch (Exception e){
			logger.error("搜索评论失败" + e.getMessage());
		}
		return "result";
	}
}

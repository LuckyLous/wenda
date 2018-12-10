package com.nowcoder.controller;

import com.nowcoder.model.Question;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hello on 2018/6/15.
 */
@Controller
public class HomeController {
	@Autowired
	QuestionService questionService;

	@Autowired
	UserService userService;

	private List<ViewObject> getQuestions(int userId, int offset, int limit){
		List<Question> questionList = questionService.selectLatestQuestions(userId, offset, limit);
		List<ViewObject> vos = new ArrayList<>();
		for (Question question: questionList) {
			ViewObject vo = new ViewObject();
			vo.set("question",question);
			vo.set("user",userService.getUser(question.getUserId()));

			vos.add(vo);
		}
		return vos;
	}

	@RequestMapping(path = {"/","/index"}, method = {RequestMethod.GET,RequestMethod.POST})
	public String index(Model model,
						@RequestParam(value = "pop",defaultValue = "0") int pop){

		model.addAttribute("vos",getQuestions(0,0,10));
		return "index";
	}

	@RequestMapping(path = {"/user/{userId}"}, method = {RequestMethod.GET,RequestMethod.POST})
	public String userIndex(Model model,
							@PathVariable("userId") int userId){

		model.addAttribute("vos",getQuestions(userId,0,10));
		return "index";
	}
}

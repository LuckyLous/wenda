package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.LikeService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Hello on 2018/6/15.
 */
@Controller
public class QuestionController {

	private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

	@Autowired
	QuestionService questionService;

	@Autowired
	HostHolder hostHolder;

	@Autowired
	UserService userService;

	@Autowired
	CommentService commentService;

	@Autowired
	LikeService likeService;

	@Autowired
	EventProducer eventProducer;

	@RequestMapping(path = {"/question/{qid}"}, method = {RequestMethod.GET})
	public String questionDetail(Model model, @PathVariable("qid") int qid){

		Question question = questionService.getById(qid);
		model.addAttribute("question",question);

		// 连问题的评论、评论对应的用户一起查询出来，放入ViewObject
		List<Comment> commentList = commentService.selectCommentByEntity(qid, EntityType.ENTITY_QUESTION);
		List<ViewObject> comments = new ArrayList<>();
		for (Comment comment: commentList) {
			ViewObject vo = new ViewObject();
			vo.set("comment",comment);
			vo.set("user",userService.getUser(comment.getUserId()));

			//根据用户显示评论的赞踩状态，将总数显示出来
			if(hostHolder.getUser() == null){
				vo.set("liked",0);
			}else{
				vo.set("liked",likeService.getLikeStatus(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, comment.getId()));
			}
			vo.set("likeCount", likeService.getLikeCount(EntityType.ENTITY_COMMENT, comment.getId()));

			comments.add(vo);
		}
		model.addAttribute("comments",comments);

		return "detail";
	}

	@RequestMapping(path = {"/question/add"}, method = {RequestMethod.POST})
	@ResponseBody
	public String addQuestion(@RequestParam(value = "title") String title,
							  @RequestParam(value = "content") String content){
		try {
			Question question = new Question();
			question.setCreatedDate(new Date());
			question.setTitle(title);
			question.setContent(content);

			if(hostHolder.getUser()==null){
				return WendaUtil.getJSONString(WendaUtil.ANONYMOUS_USERID);
			}else{
				question.setUserId(hostHolder.getUser().getId());
			}
			if(questionService.addQuestion(question) > 0){
				// 异步消息：同步到索引库(id、title、content,那边不需要再查一次)
				eventProducer.fireEvent(new EventModel(EventType.ADD_QUESTION)
										.setActorId(question.getUserId()).setEntityId(question.getId())
										.setExt("title", question.getTitle()).setExt("content", question.getContent()));
				// ===============

				return WendaUtil.getJSONString(0);// 0表示成功
			}

		}catch (Exception e){
			logger.error("增加题目失败",e.getMessage());
		}
		return WendaUtil.getJSONString(1,"失败");// 1表示失败

	}


}

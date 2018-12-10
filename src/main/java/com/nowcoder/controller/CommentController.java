package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.Comment;
import com.nowcoder.model.EntityType;
import com.nowcoder.model.HostHolder;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

/**
 * Created by Hello on 2018/6/15.
 */
@Controller
public class CommentController {

	private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

	@Autowired
	CommentService commentService;

	@Autowired
	HostHolder hostHolder;

	@Autowired
	QuestionService questionService;

	@Autowired
	EventProducer eventProducer;

	@RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST})
	public String addComment(@RequestParam(value = "questionId") int questionId,
							  @RequestParam(value = "content") String content){
		try {
			Comment comment = new Comment();
			comment.setContent(content);
			comment.setCreatedDate(new Date());
			if(hostHolder.getUser()!=null){
				comment.setUserId(hostHolder.getUser().getId());
			}else{
				comment.setUserId(WendaUtil.ANONYMOUS_USERID);
			}
			comment.setEntityId(questionId);
			comment.setEntityType(EntityType.ENTITY_QUESTION);
			comment.setStatus(0);

			commentService.addComment(comment);

			int count = commentService.getCommentCount(questionId, EntityType.ENTITY_QUESTION);
			questionService.updateCommentCount(comment.getEntityId(), count);

			// 更新题目里的评论数、给关注问题的人推送信息(均异步化)
			eventProducer.fireEvent(new EventModel(EventType.COMMENT).setActorId(comment.getUserId())
									.setEntityType(EntityType.ENTITY_QUESTION).setEntityId(questionId));

		}catch (Exception e){
			logger.error("增加评论失败",e.getMessage());
		}
		return "redirect:/question/" + String.valueOf(questionId);

	}


}

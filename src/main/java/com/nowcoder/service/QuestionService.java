package com.nowcoder.service;

import com.nowcoder.dao.QuestionDAO;
import com.nowcoder.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * Created by Hello on 2018/6/15.
 */
@Service
public class QuestionService {

	@Autowired
	private QuestionDAO questionDAO;

	@Autowired
	SensitiveService sensitiveService;

	public List<Question> selectLatestQuestions(int userId, int offset, int limit){
		return questionDAO.selectLatestQuestions(userId,offset,limit);
	}

	public Question getById(int id){
		return questionDAO.getById(id);
	}

	public int addQuestion(Question question){
		// 去除HTML标签
		question.setTitle(HtmlUtils.htmlEscape(question.getTitle()));
		question.setContent(HtmlUtils.htmlEscape(question.getContent()));
		// 敏感词过滤
		question.setContent(sensitiveService.filter(question.getContent()));
		question.setTitle(sensitiveService.filter(question.getTitle()));

		// 增加成功就返回相应问题的id,否则返回0
		return questionDAO.addQuestion(question) > 0 ? question.getId() : 0;
	}


	public int updateCommentCount(int id, int count){
		return questionDAO.updateCommentCount(id, count);
	}
}

package com.nowcoder.service;

import com.nowcoder.model.Question;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Hello on 2018/8/7.
 */
@Service
public class SearchService {

	private static final String SOLR_URL = "http://127.0.0.1:8983/solr/wenda";
	private HttpSolrClient client = new HttpSolrClient.Builder(SOLR_URL).build();
	private static final String QUESTION_TITLE_FIELD = "question_title";
	private static final String QUESTION_CONTENT_FIELD = "question_content";

	/**
	 * 根据关键字查询question_title、question_content域，并返回id、title、content的question
	 * @param keyword
	 * @param offset
	 * @param count
	 * @param hlPre
	 * @param hlPos
	 * @return
	 */
	public List<Question> searchQuestion(String keyword, int offset, int count,
										 String hlPre, String hlPos) throws Exception {
		List<Question> questionList = new ArrayList<>();

		SolrQuery query = new SolrQuery(keyword);
		query.setStart(offset);
		query.setRows(count);
		query.setHighlight(true);
		query.setHighlightSimplePre(hlPre);
		query.setHighlightSimplePost(hlPos);
		// 设置查询域
		query.set("hl.fl", QUESTION_TITLE_FIELD + "," + QUESTION_CONTENT_FIELD);
		QueryResponse response = client.query(query);

		for (Map.Entry<String, Map<String, List<String>>> entry : response.getHighlighting().entrySet()){
			Question q = new Question();
			q.setId(Integer.parseInt(entry.getKey()));

			if(entry.getValue().containsKey(QUESTION_TITLE_FIELD)){
				List<String> titleList = entry.getValue().get(QUESTION_TITLE_FIELD);
				if(titleList.size() > 0){
					q.setTitle(titleList.get(0));
				}
			}

			if(entry.getValue().containsKey(QUESTION_CONTENT_FIELD)){
				List<String> contentList = entry.getValue().get(QUESTION_CONTENT_FIELD);
				if(contentList.size() > 0){
					q.setContent(contentList.get(0));
				}
			}

			questionList.add(q);
		}
		return questionList;
	}

	/**
	 * 为问题增加索引
	 * @param qid
	 * @param title
	 * @param content
	 */
	public boolean indexQuestion(int qid, String title, String content) throws Exception {
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", qid);
		doc.addField(QUESTION_TITLE_FIELD, title);
		doc.addField(QUESTION_CONTENT_FIELD, content);
		UpdateResponse response = client.add(doc);
		return response != null && response.getStatus() == 0;
	}

}

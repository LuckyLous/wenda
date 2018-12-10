package com.nowcoder.dao;

import com.nowcoder.model.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by Hello on 2018/6/15.
 */
@Mapper
public interface CommentDAO {
	String TABLE_NAME = " comment ";
	String INSERT_FIELDS = " user_id, content, created_date, entity_id, entity_type, status ";
	String SELECT_FIELDS = " id, " + INSERT_FIELDS;

	@Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
			") values (#{userId},#{content},#{createdDate},#{entityId},#{entityType},#{status})"})
	int addComment(Comment comment);

	@Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
	Comment getCommentById(int id);

	// 查看一个entity下面所有评论
	@Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME,
			" where entity_id=#{entityId} and entity_type=#{entityType} order by created_date desc "})
	List<Comment> selectCommentByEntity(@Param("entityId") int entityId, @Param("entityType") int entityType);

	// 筛选一个问题下面有所有评论，同步到question表
	@Select({"select count(id) from ", TABLE_NAME,
			" where entity_id=#{entityId} and entity_type=#{entityType} "})
	int getCommentCount(@Param("entityId") int entityId, @Param("entityType") int entityType);

	@Update({"update ", TABLE_NAME, " set status=#{status} where id=#{id}"})
	int updateStatus(@Param("id") int id, @Param("status") int status);

	@Select({"select count(id) from ", TABLE_NAME, " where user_id=#{userId}"})
	int getUserCommentCount(int userId);
}

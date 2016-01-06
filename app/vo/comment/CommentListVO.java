package vo.comment;

import java.util.List;

import models.Comment;

/**
 * 评论列表获取result VO
 * @author luobotao
 * @Date 2015年5月4日
 */
public class CommentListVO {

	private String status;
	private Long total;
	private List<CommentItem> comment;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public Long getTotal() {
		return total;
	}
	public void setTotal(Long total) {
		this.total = total;
	}
	public List<CommentItem> getComment() {
		return comment;
	}
	public void setComment(List<CommentItem> comment) {
		this.comment = comment;
	}

	/**
	 * 商品评论Item
	 * @author luobotao
	 * @Date 2015年5月6日
	 */
	public static class CommentItem{
		public String id;
		public String nickname;
		public String content;
		public String headIcon;
		public String editor;
		public String nsort;
		public String date_add;
		
	}
}




package services.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import play.Logger;
import repositories.CommentRepository;
import utils.Constants;

/**
 * 用户相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class CommentService {

    private static final Logger.ALogger logger = Logger.of(CommentService.class);

    @Inject
    private CommentRepository commentRepository;
    
	
	
	/**
	 * 保存评论
	 * @return
	 */
	public Comment saveComment(Comment comment) {
		return commentRepository.save(comment);
	}
	
	public Page<Comment> commentPage(int page,final Long pid) {
		
		Sort sort = new Sort(Sort.Direction.DESC, "editor").and(new Sort(
				Sort.Direction.DESC, "nsort")).and(new Sort(
						Sort.Direction.DESC, "id"));
		
		 return this.commentRepository.findAll(new Specification<Comment>(){

			@Override
			public Predicate toPredicate(Root<Comment> commentParam,
					CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
	            Path<Long> pidPath = commentParam.get("pid");
	            Path<Integer> status = commentParam.get("status");
	            predicates.add(builder.equal(pidPath, pid));
	            predicates.add(builder.equal(status, 2));
	            Predicate[] param = new Predicate[predicates.size()];
	            
	            predicates.toArray(param);
	            return query.where(param).getRestriction();
			}},
	                new PageRequest(page, Constants.PAGESIZE,sort));
	}
	
}

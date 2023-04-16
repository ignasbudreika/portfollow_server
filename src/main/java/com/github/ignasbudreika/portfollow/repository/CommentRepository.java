package com.github.ignasbudreika.portfollow.repository;

import com.github.ignasbudreika.portfollow.model.Comment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface CommentRepository extends CrudRepository<Comment, String> {
    Collection<Comment> findAllByPortfolioId(String id);
}

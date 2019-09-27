package com.alien.service;

import java.util.Iterator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alien.entity.User;
import com.alien.page.Pager;

@Transactional(readOnly = true)
public interface UserService {

	/** 查询全部列表 */
	public Iterator<User> selectAll(int pageNum, int pageSize);

	/**
	 * 
	 * @param size  每页数量
	 * @param start 从第几页开始
	 * @return
	 */
	public Page<User> findPage(int size, int start);

	/**
	 * 
	 * @param page从第几页开始
	 * @param size每页条数
	 * @param user       条件封装实体
	 * @return
	 */
	Page<User> findUserCriteria(Integer page, Integer size, User user);

	/**
	 * 
	 * @param page
	 * @param size
	 * @param user
	 * @return
	 */
	Page<User> findUserPage(Integer page, Integer size, User user);

	@Transactional
	void delete(String id);

	/**
	 * 
	 * @param tableName
	 * @param fields
	 * @param sqlCondition
	 * @param list
	 * @param pager
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	Pager findPage(String tableName, String fields, String sqlCondition, List<String> list, Pager pager);

	/**
	 * 
	 * @param user
	 * @param id
	 * @return
	 */
	@Transactional
	public User getUserById(User user, String id);

	/**
	 * 
	 * @param user
	 * @return
	 */
	@Transactional
	public void saveUser(User user);
	
	/**
	 * 
	 * @return
	 */
	@Transactional
	 public List<User> findAll();
	
	public List<User> getAll();

}
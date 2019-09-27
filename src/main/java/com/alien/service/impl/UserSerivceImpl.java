package com.alien.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alien.entity.User;
import com.alien.page.Pager;
import com.alien.repository.UserRepository;
import com.alien.service.UserService;

/**
 * Created by Fant.J.
 */
@Service
@CacheConfig(cacheNames = "userCache")
public class UserSerivceImpl implements UserService  {

	@Autowired
	private UserRepository userRepository;

	/**
	 * 查询全部列表,并做分页
	 * 
	 * @param pageNum  开始页数
	 * @param pageSize 每页显示的数据条数
	 */

	@SuppressWarnings("deprecation")
	public Iterator<User> selectAll(int pageNum, int pageSize) {
		// 将参数传给这个方法就可以实现物理分页了，非常简单。
		Sort sort = new Sort(Sort.Direction.DESC, "id");
		Pageable pageable = new PageRequest(pageNum, pageSize, sort);
		Page<User> users = userRepository.findAll(pageable);
		Iterator<User> userIterator = users.iterator();
		return userIterator;
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public Page<User> findPage(int size, int start) {
		start = start < 0 ? 0 : start;
		Sort sort = new Sort(Sort.Direction.DESC, "createDate"); // 设置根据id倒序排列
		Pageable pageable = new PageRequest(start, size, sort); // 根据start、size、sort创建分页对象
		@SuppressWarnings("rawtypes")
		Page<User> page = (Page) userRepository.findAll(pageable);
		return page;
	}

	@SuppressWarnings({ "serial", "deprecation" })
	@Override
	public Page<User> findUserCriteria(Integer page, Integer size, User user) {
		Pageable pageable = new PageRequest(page, size, Sort.Direction.ASC, "createDate");
		Page<User> users = userRepository.findAll(new Specification<User>() {
			public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> list = new ArrayList<Predicate>();
				if (null != user.getUsername() && !"".equals(user.getUsername())) {
					list.add(criteriaBuilder.equal(root.get("username").as(String.class), user.getUsername()));
				}
//				if (null != user.getSex() && !"".equals(user.getSex())) {
//					list.add(criteriaBuilder.equal(root.get("sex").as(String.class), user.getSex()));
//				}
//				if (null != user.getAddress() && !"".equals(user.getAddress())) {
//					list.add(criteriaBuilder.like(root.get("address").as(String.class), "%" + user.getAddress() + "%"));
//				}
				Predicate[] p = new Predicate[list.size()];
				return criteriaBuilder.and(list.toArray(p));
			}
		}, pageable);
		return users;
	}

	@SuppressWarnings({ "serial", "deprecation" })
	@Override
	public Page<User> findUserPage(Integer page, Integer size, User user) {
		Pageable pageable = new PageRequest(page, size, Sort.Direction.ASC, "createDate");
		Page<User> userpage = userRepository.findAll(new Specification<User>() {
			@Override
			public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				Predicate p1 = criteriaBuilder.equal(root.get("username").as(String.class), user.getUsername());
//				Predicate p2 = criteriaBuilder.equal(root.get("sex").as(String.class), user.getSex());
//				Predicate p3 = criteriaBuilder.like(root.get("address").as(String.class),
//						"%" + user.getAddress() + "%");
				query.where(criteriaBuilder.and(p1));
				return query.getRestriction();
			}
		}, pageable);
		return userpage;
	}
    @CacheEvict(key = "#id")
	public void delete(String id) {
		userRepository.delete(id);
	}

	 @Cacheable(key="targetClass + methodName +#pager.pageNumber")
	public Pager findPage(String tableName, String fields, String sqlCondition, List<String> list, Pager pager) {
		return userRepository.findsqlpage(tableName, fields, sqlCondition, list, pager);
	}
	@Cacheable(key="#id")
	public User getUserById(User user,String id) {
		User use=userRepository.findById(user, id);
		return use;
	}
	 @Transactional
	 @CachePut(key="#user.id")
	 public void saveUser(User user) {
		userRepository.saveEntity(user);
	 }
	 
	 @Cacheable(key="targetClass + methodName")
	 public List<User> findAll(){
		List<User> users= userRepository.findAll();
		return users;
	 }

	@Override
	public List<User> getAll() {
		// TODO Auto-generated method stub
		return userRepository.getAllList();
	}
}

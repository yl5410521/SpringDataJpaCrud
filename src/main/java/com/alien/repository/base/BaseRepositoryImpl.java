package com.alien.repository.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.CriteriaImpl.OrderEntry;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.transform.ResultTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.alien.entity.BaseEntity;
import com.alien.page.Pager;
import com.alien.utils.upload.ReflectionUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * Created by
 */
public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
		implements BaseRepository<T, ID> {

	private static final String ORDER_LIST_PROPERTY_NAME = "orderList";// "排序"属性名称
	private static final String CREATE_DATE_PROPERTY_NAME = "createDate";// "创建日期"属性名称


	@SuppressWarnings("unused")
	private JPAQueryFactory queryFactory;

	@Autowired
	private EntityManager entityManager;

	private Class<T> clazz;

	@SuppressWarnings({ "rawtypes", "unused" })
	private final JpaEntityInformation entityInformation;

	// 父类没有不带参数的构造方法，这里手动构造父类
	
	public BaseRepositoryImpl(JpaEntityInformation<T, ?> ef, EntityManager em) {

		super(ef, em);

		this.entityManager = em;
		this.clazz = ef.getJavaType();
		this.entityInformation = ef;
		this.queryFactory = new JPAQueryFactory(entityManager);
	}

	private Session getSession() {
		Session session = entityManager.unwrap(Session.class);
		return session;

	}
	
	private SessionFactory getSessionFactory() {
		Session session = (Session) entityManager.getDelegate();
		SessionFactoryImpl sessionFactory = (SessionFactoryImpl) session.getSessionFactory();
		return sessionFactory;
	}

	// 通过EntityManager来完成查询
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listBySQL(String sql) {
		return entityManager.createNativeQuery(sql).getResultList();
	}

	@Override
	public void updateBySql(String sql, Object... args) {
		Query query = entityManager.createNativeQuery(sql);
		int i = 0;
		for (Object arg : args) {
			query.setParameter(++i, arg);
		}
		query.executeUpdate();
	}

	@Override
	public void updateByHql(String hql, Object... args) {
		Query query = entityManager.createQuery(hql);
		int i = 0;
		for (Object arg : args) {
			System.out.println(arg);
			query.setParameter(++i, arg);
		}
		query.executeUpdate();
	}

	public void saveEntity(T t) {
		entityManager.persist(t);
	}

	@SuppressWarnings("unchecked")
	public T findById(T t, Object id) {
		return (T) entityManager.find(t.getClass(), id);
	}

	public void deleteEntity(Object id) {
		Query query = entityManager.createQuery("delete from " + clazz.getSimpleName() + " p where p.id = ?1");
		query.setParameter(1, id);
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll() {
		String hql = "select t from " + clazz.getSimpleName() + " t";
		Query query = entityManager.createQuery(hql);
		List<T> beans = query.getResultList();
		return beans;
	}

	@SuppressWarnings({ "unchecked" })
	public Pager findsqlpage(String tableName, String fields, String sqlCondition, List<String> list, Pager pager) {
		List<T> param = new ArrayList<T>();
		String sqls = null;
		if (tableName != null && fields != null) {
			sqls = "select " + fields + " from " + tableName;

			try {
				if (sqlCondition != null) {
					sqls = sqls + " " + sqlCondition;
				}

				Query query = entityManager
						.createNativeQuery(pager.getPageMySQL(sqls, pager.getPageNumber(), pager.getPageSize()), clazz);

				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						query.setParameter(i + 1, list.get(i));
					}
					param = query.getResultList();
					pager.setTotalCount(findsqltotalcount(tableName, fields, sqlCondition, list));
					pager.initPageBean(pager.getTotalCount(), pager.getPageSize());
					pager.setResult(param);
					pager.setCurrentCount(param.size());
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return pager;
	}

	@SuppressWarnings({ "unchecked" })
	public Pager findsqlpage(String tableName, String fields, List<String> list, Pager pager) {
		List<T> param = new ArrayList<T>();
		String sqls = null;
		if (tableName != null && fields != null) {
			sqls = "select " + fields + " from " + tableName;

			try {

				Query query = entityManager
						.createNativeQuery(pager.getPageMySQL(sqls, pager.getPageNumber(), pager.getPageSize()), clazz);

				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						query.setParameter(i + 1, list.get(i));
					}
					param = query.getResultList();
					pager.setTotalCount(findsqltotalcount(tableName, fields, list));
					pager.initPageBean(pager.getTotalCount(), pager.getPageSize());
					pager.setResult(param);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return pager;
	}

	@SuppressWarnings("rawtypes")
	public int findsqltotalcount(String tableName, String fields, List<String> list) {
		int totalcount = 0;
		String sqls = "select " + fields + " from " + tableName;
		try {
			Query query = entityManager.createNativeQuery(sqls, clazz);
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i + 1, list.get(i));
				}
				List lists = query.getResultList();
				totalcount = lists.size();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return totalcount;
	}

	@SuppressWarnings("rawtypes")
	public int findsqltotalcount(String tableName, String fields, String sqlCondition, List<String> list) {
		int totalcount = 0;
		String sqls = "select " + fields + " from " + tableName;
		try {

			if (sqlCondition != null) {
				sqls = sqls + " " + sqlCondition;
			}
			Query query = entityManager.createNativeQuery(sqls, clazz);
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					query.setParameter(i + 1, list.get(i));
				}
				List lists = query.getResultList();
				totalcount = lists.size();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return totalcount;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public int deleteEqualField(String field, Object value) {
		Query query = entityManager
				.createQuery("delete from " + clazz.getSimpleName() + " p where p." + field + " = ?1");
		query.setParameter(1, value);
		return query.executeUpdate();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateEntity(T bean) {
		entityManager.merge(bean);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int batchDelete(List<Object> ids) {
		StringBuffer hql = new StringBuffer("delete from " + clazz.getSimpleName() + " where id  in(:ids)");
		Query query = entityManager.createQuery(hql.toString());
		query.setParameter("ids", ids);
		return query.executeUpdate();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int deleteLikeField(String field, String value) {
		Query query = entityManager
				.createQuery("delete from " + clazz.getSimpleName() + " p where p." + field + " like ?1");
		query.setParameter(1, value);
		return query.executeUpdate();
	}

	/**
	 * 根据SQL获取实体对象
	 * 
	 * @param sql
	 * 
	 * @return 实体对象
	 */
	@SuppressWarnings("unchecked")
	public T getBySql(String sql) {
		return (T) getSession().createSQLQuery(sql).addEntity(clazz).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<T> getAllList() {
		ClassMetadata classMetadata = getSessionFactory().getClassMetadata(clazz);
		String hql;
		if (ArrayUtils.contains(classMetadata.getPropertyNames(), ORDER_LIST_PROPERTY_NAME)) {
			hql = "from " + clazz.getName() + " as entity order by entity." + ORDER_LIST_PROPERTY_NAME + " desc";
		} else {
			hql = "from " + clazz.getName();
		}
		return getSession().createQuery(hql).list();
	}

	public Long getTotalCount() {
		String hql = "select count(1) from " + clazz.getName();
		return (Long) getSession().createQuery(hql).setCacheable(true).uniqueResult();
	}

	public void update(T entity) {
		Assert.notNull(entity, "entity is required");
		if (entity instanceof BaseEntity) {
			try {
				Method method = entity.getClass().getMethod(BaseEntity.ON_UPDATE_METHOD_NAME);
				method.invoke(entity);
				getSession().update(entity);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			getSession().update(entity);
		}
	}

	public void updateOther(T entity) {
		Assert.notNull(entity, "entity is required");
		if (entity instanceof BaseEntity) {
			try {
				Method method = entity.getClass().getMethod(BaseEntity.ON_UPDATE_METHOD_NAME);
				method.invoke(entity);
				getSession().merge(entity);
				getSession().flush();
				getSession().clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			getSession().merge(entity);
		}
	}

	@SuppressWarnings("unchecked")
	public void delete(Object id) {
		Assert.notNull(id, "id is required");
		T entity = (T) getSession().load(clazz, (Serializable) id);
		getSession().delete(entity);
	}

	@SuppressWarnings("unchecked")
	public void delete(Object[] ids) {
		Assert.notEmpty(ids, "ids must not be empty");
		for (Object id : ids) {
			T entity = (T) getSession().load(clazz, (Serializable) id);
			getSession().delete(entity);
		}
	}

	public void flush() {
		getSession().flush();
	}

	public void evict(Object object) {
		Assert.notNull(object, "object is required");
		getSession().evict(object);
	}

	public void clear() {
		getSession().clear();
	}

	public List<T> getBySQL(String sql) {
		return (List<T>) getSession().createSQLQuery(sql).addEntity(clazz).list();
	}

	public Pager findPager(Pager pager) {
		Criteria criteria = getSession().createCriteria(clazz);
		criteria.setCacheable(true);
		return findPager(pager, criteria);
	}

	public Pager findPager(Pager pager, Criterion... criterions) {
		Criteria criteria = getSession().createCriteria(clazz);
		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}
		criteria.setCacheable(true);
		return findPager(pager, criteria);
	}

	public Pager findPager(Pager pager, Order... orders) {
		Criteria criteria = getSession().createCriteria(clazz);
		for (Order order : orders) {
			criteria.addOrder(order);
		}
		criteria.setCacheable(true);
		return findPager(pager, criteria);
	}

	public Pager findPager(Pager pager, Criteria criteria) {
		Assert.notNull(pager, "pager is required");
		Assert.notNull(criteria, "criteria is required");

		Integer pageNumber = pager.getPageNumber();
		Integer pageSize = pager.getPageSize();
		String searchBy = pager.getSearchBy();
		String keyword = pager.getKeyword();
		String orderBy = pager.getOrderBy();
		Pager.Order order = pager.getOrder();

		if (StringUtils.isNotEmpty(searchBy) && StringUtils.isNotEmpty(keyword)) {
			if (searchBy.contains(".")) {
				String alias = StringUtils.substringBefore(searchBy, ".");
				criteria.createAlias(alias, alias);
			}
			criteria.add(Restrictions.like(searchBy, "%" + keyword + "%"));
		}

		pager.setTotalCount(criteriaResultTotalCount(criteria));

		if (StringUtils.isNotEmpty(orderBy) && order != null) {
			if (order == Pager.Order.asc) {
				criteria.addOrder(Order.asc(orderBy));
			} else {
				criteria.addOrder(Order.desc(orderBy));
			}
		}

		ClassMetadata classMetadata = getSessionFactory().getClassMetadata(clazz);
		if (!StringUtils.equals(orderBy, ORDER_LIST_PROPERTY_NAME)
				&& ArrayUtils.contains(classMetadata.getPropertyNames(), ORDER_LIST_PROPERTY_NAME)) {
			criteria.addOrder(Order.asc(ORDER_LIST_PROPERTY_NAME));
			criteria.addOrder(Order.desc(CREATE_DATE_PROPERTY_NAME));
			if (StringUtils.isEmpty(orderBy) || order == null) {
				pager.setOrderBy(ORDER_LIST_PROPERTY_NAME);
				pager.setOrder(Pager.Order.asc);
			}
		} else if (!StringUtils.equals(orderBy, CREATE_DATE_PROPERTY_NAME)
				&& ArrayUtils.contains(classMetadata.getPropertyNames(), CREATE_DATE_PROPERTY_NAME)) {
			criteria.addOrder(Order.desc(CREATE_DATE_PROPERTY_NAME));
			if (StringUtils.isEmpty(orderBy) || order == null) {
				pager.setOrderBy(CREATE_DATE_PROPERTY_NAME);
				pager.setOrder(Pager.Order.desc);
			}
		}
		criteria.setFirstResult((pageNumber - 1) * pageSize);
		criteria.setMaxResults(pageSize);
		criteria.setCacheable(true);
		pager.setResult(criteria.list());
		return pager;
	}

	public Pager findPagers(Pager pager, Criteria criteria) {
		Assert.notNull(pager, "pager is required");
		Assert.notNull(criteria, "criteria is required");

		Integer pageNumber = pager.getPageNumber();
		Integer pageSize = pager.getPageSize();
		String searchBy = pager.getSearchBy();
		String keyword = pager.getKeyword();
		String orderBy = pager.getOrderBy();
		Pager.Order order = pager.getOrder();

		if (StringUtils.isNotEmpty(searchBy) && StringUtils.isNotEmpty(keyword)) {
			if (searchBy.contains(".")) {
				String alias = StringUtils.substringBefore(searchBy, ".");
				criteria.createAlias(alias, alias);
			}
			criteria.add(Restrictions.like(searchBy, "%" + keyword + "%"));
		}

		pager.setTotalCount(criteriaResultTotalCount(criteria));

		if (StringUtils.isNotEmpty(orderBy) && order != null) {
			if (order == Pager.Order.asc) {
				criteria.addOrder(Order.asc(orderBy));
			} else {
				criteria.addOrder(Order.desc(orderBy));
			}
		}

		ClassMetadata classMetadata = getSessionFactory().getClassMetadata(clazz);
		if (!StringUtils.equals(orderBy, ORDER_LIST_PROPERTY_NAME)
				&& ArrayUtils.contains(classMetadata.getPropertyNames(), ORDER_LIST_PROPERTY_NAME)) {
			criteria.addOrder(Order.asc(ORDER_LIST_PROPERTY_NAME));
			criteria.addOrder(Order.desc(CREATE_DATE_PROPERTY_NAME));
			if (StringUtils.isEmpty(orderBy) || order == null) {
				pager.setOrderBy(ORDER_LIST_PROPERTY_NAME);
				pager.setOrder(Pager.Order.asc);
			}
		} else if (!StringUtils.equals(orderBy, CREATE_DATE_PROPERTY_NAME)
				&& ArrayUtils.contains(classMetadata.getPropertyNames(), CREATE_DATE_PROPERTY_NAME)) {
			criteria.addOrder(Order.desc(CREATE_DATE_PROPERTY_NAME));
			if (StringUtils.isEmpty(orderBy) || order == null) {
				pager.setOrderBy(CREATE_DATE_PROPERTY_NAME);
				pager.setOrder(Pager.Order.desc);
			}
		}
		criteria.setFirstResult((pageNumber - 1) * pageSize);
		criteria.setMaxResults(pageSize);
		criteria.setCacheable(true);

		pager.setResult(criteria.list());
		return pager;
	}

	public Pager findPager(Class clazz, Pager pager, List<String> criteriaList, Criterion... criterions) {
		Criteria criteria = getSession().createCriteria(clazz);
		if (criteriaList != null) {
			for (String str : criteriaList) {
				criteria.createCriteria(str, str);
			}
		}

		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}
		criteria.setCacheable(true);
		return findPager(clazz, pager, criteria);
	}

	/**
	 * 新增指定对象类型查询接口
	 * 
	 * @author hpzxyj
	 * @createTime 2012-8-23
	 * @param clazz
	 * @param pager
	 * @param criteria
	 * @return
	 */
	public Pager findPager(Class clazz, Pager pager, Criteria criteria) {
		Assert.notNull(pager, "pager is required");
		Assert.notNull(criteria, "criteria is required");

		Integer pageNumber = pager.getPageNumber();
		Integer pageSize = pager.getPageSize();
		String searchBy = pager.getSearchBy();
		String keyword = pager.getKeyword();
		String orderBy = pager.getOrderBy();
		Pager.Order order = pager.getOrder();

		if (StringUtils.isNotEmpty(searchBy) && StringUtils.isNotEmpty(keyword)) {
			if (searchBy.contains(".")) {
				String alias = StringUtils.substringBefore(searchBy, ".");
				criteria.createAlias(alias, alias);
			}
			criteria.add(Restrictions.like(searchBy, "%" + keyword + "%"));
		}

		pager.setTotalCount(criteriaResultTotalCount(criteria));

		if (StringUtils.isNotEmpty(orderBy) && order != null) {
			if (order == Pager.Order.asc) {
				criteria.addOrder(Order.asc(orderBy));
			} else {
				criteria.addOrder(Order.desc(orderBy));
			}
		}

		ClassMetadata classMetadata = getSessionFactory().getClassMetadata(clazz);
		if (!StringUtils.equals(orderBy, ORDER_LIST_PROPERTY_NAME)
				&& ArrayUtils.contains(classMetadata.getPropertyNames(), ORDER_LIST_PROPERTY_NAME)) {
			criteria.addOrder(Order.asc(ORDER_LIST_PROPERTY_NAME));
			criteria.addOrder(Order.desc(CREATE_DATE_PROPERTY_NAME));
			if (StringUtils.isEmpty(orderBy) || order == null) {
				pager.setOrderBy(ORDER_LIST_PROPERTY_NAME);
				pager.setOrder(Pager.Order.asc);
			}
		} else if (!StringUtils.equals(orderBy, CREATE_DATE_PROPERTY_NAME)
				&& ArrayUtils.contains(classMetadata.getPropertyNames(), CREATE_DATE_PROPERTY_NAME)) {
			criteria.addOrder(Order.desc(CREATE_DATE_PROPERTY_NAME));
			if (StringUtils.isEmpty(orderBy) || order == null) {
				pager.setOrderBy(CREATE_DATE_PROPERTY_NAME);
				pager.setOrder(Pager.Order.desc);
			}
		}
		criteria.setFirstResult((pageNumber - 1) * pageSize);
		criteria.setMaxResults(pageSize);
		criteria.setCacheable(true);
		pager.setTotalPages(pager.getTotalCount() / pager.getPageSize());
		pager.setResult(criteria.list());
		return pager;
	}

	// 获取Criteria查询数量
	@SuppressWarnings("unchecked")
	private int criteriaResultTotalCount(Criteria criteria) {
		Assert.notNull(criteria, "criteria is required");

		int criteriaResultTotalCount = 0;
		try {
			CriteriaImpl criteriaImpl = (CriteriaImpl) criteria;
			criteria.setCacheable(true);
			Projection projection = criteriaImpl.getProjection();
			ResultTransformer resultTransformer = criteriaImpl.getResultTransformer();
			List<OrderEntry> orderEntries = (List) ReflectionUtil.getFieldValue(criteriaImpl, "orderEntries");
			ReflectionUtil.setFieldValue(criteriaImpl, "orderEntries", new ArrayList());

			Integer totalCount = ((Long) criteriaImpl.setProjection(Projections.rowCount()).uniqueResult()).intValue();
			if (totalCount != null) {
				criteriaResultTotalCount = totalCount;

			}

			criteriaImpl.setProjection(projection);
			if (projection == null) {
				criteriaImpl.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
			}
			if (resultTransformer != null) {
				criteriaImpl.setResultTransformer(resultTransformer);
			}
			ReflectionUtil.setFieldValue(criteriaImpl, "orderEntries", orderEntries);
		} catch (Exception e) {

		}
		return criteriaResultTotalCount;
	}

	/**
	 * 执行sql语句
	 * 
	 * @param sql
	 * @param param
	 * @return
	 */

	public boolean runsql(String sql, List<String> list) {
		boolean flag = false;
		try {
			SQLQuery query = getSession().createSQLQuery(sql);
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					query.setString(i, list.get(i));
				}
			}
			query.executeUpdate();
			getSession().flush();
			getSession().clear();
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 数组中添加元素
	 * 
	 * @param ori原数组
	 * @param val添加的元素值
	 * @param index下标
	 * @return
	 */
	public static String[] insert(String[] ori, String val, int index) {
		for (int i = ori.length - 1; i > index; i--)
			ori[i] = ori[i - 1];
		ori[index] = val;
		return ori;
	}

	/** -------------sql分页---------- */
	/**
	 * compcode传入的参数 pageNumber页码 pageSize每页记录数
	 */
	@SuppressWarnings("unchecked")

	public Pager runsqlpage(String tableName, String sqlCondition, List<String> list, Pager pager) {
		List<T> param = new ArrayList<T>();
		String sqls = "select * from " + tableName;
		try {
			if (sqlCondition != null) {
				sqls = sqls + " " + sqlCondition;
			}

			SQLQuery query = getSession()
					.createSQLQuery(pager.getOrclPageSql(sqls, pager.getPageNumber(), pager.getPageSize()))
					.addEntity(clazz);
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					query.setString(i, list.get(i));
				}
				query.setCacheable(true);
				param = query.list();
				pager.setTotalCount(findtotalcount(tableName, sqlCondition, list));
				pager.initPageBean(pager.getTotalCount(), pager.getPageSize());
				pager.setResult(param);
				getSession().flush();
				getSession().clear();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pager;
	}

	public int findtotalcount(String tableName, String sqlCondition, List<String> list) {
		int totalcount = 0;
		String sqls = "select count(1) as count from " + tableName;
		try {

			if (sqlCondition != null) {
				sqls = sqls + " " + sqlCondition;
			}
			Query query = (SQLQuery) getSession().createSQLQuery(sqls);
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					((org.hibernate.query.Query) query).setString(i, list.get(i));
				}
				List lists = ((org.hibernate.query.Query) query).list();
				totalcount = ((BigDecimal) lists.get(0)).intValue();
			}
			getSession().flush();
			getSession().clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return totalcount;
	}

	@Override
	public List<T> runsqlList(String sql, List<String> list) {
		List<T> lists = new ArrayList<T>();
		try {
			SQLQuery query = getSession().createSQLQuery(sql).addEntity(clazz);
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					query.setString(i, list.get(i));
				}
				lists = query.list();
			}
			return lists;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void importTxt(File myTxt, String myTxtFileName, String sql) {

		try {
			BufferedReader br = new BufferedReader(new FileReader(myTxt));// 构造一个BufferedReader类来读取文件
			String s = null;
			int lineNum = 0;
			while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
				lineNum++;
				if (lineNum == 1)
					continue;
				String line = s.replaceAll("\\|\\|", ",");
				System.out.println(line);
				String[] custArray = line.split("\\,");
				System.out.println(JSON.toJSON(custArray));
				insertTxt(custArray, sql);
				Thread.sleep(10);
			}
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void insertTxt(String[] strArray, String sql) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < strArray.length; i++) {
			list.add(strArray[i]);
		}
		runsql(sql, list);

	}

}
package com.alien.repository.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * Created by yaolin
 */
@SuppressWarnings({ "rawtypes" })
public class BaseRepositoryFactoryBean<R extends JpaRepository<T, I>, T, I extends Serializable>
		extends JpaRepositoryFactoryBean<R, T, I> {

	public BaseRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
		super(repositoryInterface);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
		return new BaseRepositoryFactory(em);
	}

	// 创建一个内部类，该类不用在外部访问
	private static class BaseRepositoryFactory<T, I extends Serializable> extends JpaRepositoryFactory {

		@SuppressWarnings("unused")
		private final EntityManager em;

		public BaseRepositoryFactory(EntityManager em) {
			super(em);
			this.em = em;
		}

		// 设置具体的实现类是BaseRepositoryImple
		// 设置=实现类是BaseRepositoryImpl

		protected JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation information,
				EntityManager entityManager) {
			JpaEntityInformation<?, Object> entityInformation = this
					.getEntityInformation(information.getDomainType());
			Object repository = this.getTargetRepositoryViaReflection(information,
					new Object[] { entityInformation, entityManager });
			Assert.isInstanceOf(BaseRepositoryImpl.class, repository);
			return (JpaRepositoryImplementation) repository;
		}

		// 设置具体的实现类的class
		@Override
		protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
			return BaseRepositoryImpl.class;
		}
	}
	
	
}
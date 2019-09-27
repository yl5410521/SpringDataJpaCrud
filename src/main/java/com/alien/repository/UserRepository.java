package com.alien.repository;


import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alien.entity.User;
import com.alien.repository.base.BaseRepository;

@Repository
public interface UserRepository extends BaseRepository<User, String>,JpaSpecificationExecutor<User> {

	
}

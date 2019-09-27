package com.alien.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="role")
public class Role extends BaseEntity{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4408292356023054896L;
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
}

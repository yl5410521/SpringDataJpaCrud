package com.alien.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.deser.Deserializers.Base;

/**
 * 权限
 * @author Administrator
 *
 */
@Entity
@Table(name="permission")
public class Permission extends BaseEntity {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2953449385070067201L;

	//权限名称
    private String name;

    //权限描述
    private String descritpion;

    //授权链接
    private String url;

  //父节点id
    private String pid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescritpion() {
        return descritpion;
    }

    public void setDescritpion(String descritpion) {
        this.descritpion = descritpion;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

   
}

package com.easy.cloud.user.base.pojo.vo;

import com.dq.easy.cloud.module.basic.pojo.vo.DqBaseVO;

public class UserVo extends DqBaseVO{
	private String userName;
	private String password;
	private Integer status;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return "UserEntity [getUserName()=" + getUserName() + ", getPassword()=" + getPassword() + ", getStatus()="
				+ getStatus() + ", getId()=" + getId() + ", getCreateDate()=" + getCreateDate() + ", getUpdateDate()="
				+ getUpdateDate() + ", getVersion()=" + getVersion() + ", getCreateBy()=" + getCreateBy()
				+ ", getUpdateBy()=" + getUpdateBy() + "]";
	}
}

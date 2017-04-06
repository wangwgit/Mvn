package com.cn.hnust.service;

import org.springframework.transaction.annotation.Transactional;

import com.cn.hnust.domain.User;

public interface IUserService {
	@Transactional(readOnly = true)
	public User getUserById(int userId);
	@Transactional
	public void add(User u);
	@Transactional
	public void addSelect(User u);
}
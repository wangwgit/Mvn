package com.cn.hnust.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cn.hnust.IDao.UserMapper;
import com.cn.hnust.domain.User;
import com.cn.hnust.service.IUserService;

@Service("userService")
public class UserServiceImpl implements IUserService {
	@Resource
	private UserMapper userDao;
	@Override
	public User getUserById(int userId) {
		this.userDao.selectByPrimaryKey(userId);
		this.userDao.selectByPrimaryKey(userId);
		return this.userDao.selectByPrimaryKey(userId);
	}
	@Override
	public void add(User u) {
	
		this.userDao.insertSelective(u);
		
	//	int i= 1/0;
	//	System.out.println(i);
	}
	@Override
	public void addSelect(User u) {
		// TODO Auto-generated method stub
		this.userDao.insertSelective(u);
		
		User user= this.userDao.selectByPrimaryKey(u.getId());
	//	System.out.println(u.getUserName());
	}

}

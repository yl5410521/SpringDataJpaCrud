package com.alien.controller.bm;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.alien.controller.base.BaseController;
import com.alien.entity.User;
import com.alien.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;



    @RequestMapping(method = RequestMethod.GET,value = "/selectAll/{pageNum}/{pageSize}")
    public List<User> selectAll(@PathVariable("pageNum") int pageNum, @PathVariable("pageSize") int pageSize){
        Iterator<User> userIterator = userService.selectAll(pageNum, pageSize);
        List<User> list = new ArrayList<>();
        while(userIterator.hasNext()){
            list.add(userIterator.next());
        }
        return list;
    }
    @GetMapping("/page")
    public String list() {
    	
    	Page<User> page=userService.findPage(2, 1);
    	System.out.println(page.getNumber()); //当前页start
    	System.out.println(page.getNumberOfElements());//当前页start
    	System.out.println(page.getSize());//每页数量size
    	System.out.println(page.getTotalElements());  //总数量
        System.out.println(page.getTotalPages());    //总页数
        
        List<User> users=page.getContent();
        for(User user:users) {
//        	System.out.println(user.getSex());
        }
    	return null;
    }
    @GetMapping("/pagelist")
    public String conditionPage() {
    	User user=new User();
//    	user.setSex("女");
    	user.setUsername("12121");
//    	user.setAddress("x");
    	Page<User> page=userService.findUserCriteria(0, 2, user);
    	List<User> users=page.getContent();
    	  for(User usr:users) {
//          	System.out.println(usr.getAddress()+"\r");
          }
    	  System.out.println(page.getNumber()); //当前页start
      	System.out.println(page.getNumberOfElements());//当前页start
      	System.out.println(page.getSize());//每页数量size
      	System.out.println(page.getTotalElements());  //总数量
          System.out.println(page.getTotalPages());    //总页数
    return null;	
    }
    
    @GetMapping("/pageuser")
    public String PageUser() {
    	User user=new User();
//    	user.setSex("女");
    	user.setUsername("12121");
//    	user.setAddress("x");
    	Page<User> page=userService.findUserPage(0, 2, user);
    	List<User> users=page.getContent();
    	  for(User usr:users) {
//          	System.out.println(usr.getAddress()+"\r");
          }
    	  System.out.println(page.getNumber()); //当前页start
      	System.out.println(page.getNumberOfElements());//当前页start
      	System.out.println(page.getSize());//每页数量size
      	System.out.println(page.getTotalElements());  //总数量
          System.out.println(page.getTotalPages());    //总页数
    	  
    	  return null;
    }
    @SuppressWarnings("unchecked")
	@ResponseBody
    @GetMapping("/userlist")
    public String page() {
    	String tableName="user";
    	String fields="id,create_by,create_date,lastmodified_by,lastmodify_date,address,birthday,sex,username,loginname";
    	String sqlCondition="where loginname=?";
    	list.clear();
    	list.add("张三");
    	pager.setPageNumber(5);
    	pager=userService.findPage(tableName, fields, sqlCondition, list, pager);
    	System.out.println("第"+pager.getPageNumber()+"页");
    	System.out.println("一页多少条"+pager.getPageSize());
    	System.out.println("共多少条"+pager.getTotalCount());
    	System.out.println("多少页"+pager.getTotalPages());
    	System.out.println("当前记录数"+pager.getCurrentCount());
    	List<User> users=(List<User>) pager.getResult();
    	for(User us:users) {
    		System.out.println(us.getCreateDate());
    	}
    	return null;
    }
    
    @GetMapping("/query")
    public ModelAndView  query(ModelAndView modelAndView) {
    	  modelAndView.setViewName("index");
    	User user = new User();
    	String id="297ed5e069a8e07f0169a909b3c4002f";
    	User use=userService.getUserById(user, id);
    	//System.out.println("**********"+use.getAddress());
    	modelAndView.addObject("user", use);
    return modelAndView;
    }
    @GetMapping("/del")
    public String del(String id) {
    	System.out.println(id);
    	userService.delete(id);
    	return null;
    }
    
    @GetMapping("/save")
    public String save() {
    	System.out.println("//////////////////");
    	User users=new User();
    	users.setUsername("孙悟空");
//    	users.setBirthday(new Date());
//    	users.setAddress("sdffsdfdsfsd");
    	users.setCreateDate(new Date());
    	users.setLastmodifyDate(new Date());
//    	users.setSex("男");
    	userService.saveUser(users);
    	return null;
    }
    @GetMapping("/list")
    public String lists() {
    	userService.findAll();
    return null;	
    }
    
    @GetMapping("/getquery")
    public String getquery() {
    	List<User> users=userService.getAll();
    	for(User ur:users) {
    		System.out.println(ur.getUsername());
    	}
    	return null;
    }
}

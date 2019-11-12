package net.wendal.nutzbook;

import net.wendal.nutzbook.bean.User;
import org.nutz.dao.Dao;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.Ioc;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;

import java.util.Date;

/**
 * @author Administrator
 * @date 2019/11/11 15:20:48
 * @description
 */
//在这个类里做初始化操作
public class MainSetup implements Setup {
    public void init(NutConfig nc) {//可以从这个参数中拿到ioc容器
        Ioc ioc = nc.getIoc();//拿到Ioc容器
        Dao dao = ioc.get(Dao.class);//拿到nutz.dao对象
        Daos.createTablesInPackage(dao,"net.wendal.nutzbook",false);//放入dao,自动循环建表

        //初始化数据
        if(dao.count(User.class)==0){//先计算表中的记录数是否为0
                User user= new User();
                user.setName("admin");
                user.setPassword("123456");
                user.setCreateTime(new Date());
                user.setUpdateTime(new Date());
                dao.insert(user);

        }


    }

    public void destroy(NutConfig nc) {

    }
}

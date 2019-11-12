package net.wendal.nutzbook.module;

import net.wendal.nutzbook.bean.User;
import net.wendal.nutzbook.bean.UserProfile;
import net.wendal.nutzbook.util.Toolkit;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.QueryResult;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.Scope;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.filter.CheckSession;

import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * @author Administrator
 * @date 2019/11/11 16:26:51
 * @description
 */

@IocBean//标志子模块
@At("/user")//模块入口
@Ok("json:{locked:'password|salt',ignoreNull:true}")//成功返回视图：json格式，过滤掉密码和salt的显示
@Fail("http:500")//失败返回视图
@Filters(@By(type= CheckSession.class, args={"me", "/"}))//添加一个session检查过滤器，session如果不存在me,则重定向/,该注解会拦截该模块所有方法
public class UserModule extends  BaseModule {



    @At
    public int count(){
        //返回当前表的记录数
        return dao.count(User.class);//直接return会通过@Ok注解转换到json视图
    }

    @At
    @Filters()//给个空过滤器相当于放行
    public Object login(@Param("username") String name,
                        @Param("password") String password,
                        @Param("captcha")String captcha,
                        @Attr(scope= Scope.SESSION, value="nutz_captcha")String _captcha,
                        HttpSession session){//字段适配器


        NutMap re = new NutMap();
        if (!Toolkit.checkCaptcha(_captcha, captcha)) {//校验验证码
            return re.setv("ok", false).setv("msg", "验证码错误");
        }
        User user = dao.fetch(User.class, Cnd.where("name", "=", name).and("password", "=", password));
        if (user == null) {
            return re.setv("ok", false).setv("msg", "用户名或密码错误");
        } else {
            session.setAttribute("me", user.getId());
            return re.setv("ok", true);
        }

    }

    @At
    @Ok(">>:/")//一旦销毁了Session中的对象，就重定向
    //登出方法
    public void logOut(HttpSession session){
        session.invalidate();
    }

    //校验方法
    protected String checkUser(User user, boolean create) {
        if (user == null) {
            return "空对象";
        }
        if (create) {
            if (Strings.isBlank(user.getName()) || Strings.isBlank(user.getPassword()))
                return "用户名/密码不能为空";
        } else {
            if (Strings.isBlank(user.getPassword()))
                return "密码不能为空";
        }
        String passwd = user.getPassword().trim();
        if (6 > passwd.length() || passwd.length() > 12) {
            return "密码长度错误";
        }
        user.setPassword(passwd);
        if (create) {
            int count = dao.count(User.class, Cnd.where("name", "=", user.getName()));
            if (count != 0) {
                return "用户名已经存在";
            }
        } else {
            if (user.getId() < 1) {
                return "用户Id非法";
            }
        }
        if (user.getName() != null)
            user.setName(user.getName().trim());
        return null;
    }

    @At
    public Object add(@Param("..")User user) {//..用于接收一个表单对象，首先表单字段要和pojo一致
        NutMap re = new NutMap();//一个nutz的工具类
        String msg = checkUser(user, true);
        if (msg != null){
            return re.setv("ok", false).setv("msg", msg);//暂时不知作用
        }
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user = dao.insert(user);
        return re.setv("ok", true).setv("data", user);//反正会将内容转为json
    }

    @At
    public Object update(@Param("..")User user) {//id=2&salt=123456&password=123456
        NutMap re = new NutMap();
        String msg = checkUser(user, false);
        if (msg != null){
            return re.setv("ok", false).setv("msg", msg);
        }
        user.setName(null);// 不允许更新用户名
        user.setCreateTime(null);//也不允许更新创建时间
        user.setUpdateTime(new Date());// 设置正确的更新时间
        dao.updateIgnoreNull(user);// 更新忽略空
        return re.setv("ok", true);
    }

    @At
    @Aop(TransAop.READ_COMMITTED)
    public Object delete(@Param("id")int id, @Attr("me")int me) {//@Attr,会去request(先)，session(后)找key：me,这里没有返回0
        System.out.println("我是me:"+me);
        if (me == id) {
            return new NutMap().setv("ok", false).setv("msg", "不能删除当前用户!!");
        }
        //因为有多步数据库操作，添加AOP注解
        dao.delete(User.class, id); // 再严谨一些的话,需要判断是否为>0
        dao.clear(UserProfile.class,Cnd.where("userId","=",me));
        return new NutMap().setv("ok", true);
    }

    @At
    //查询方法
    public Object query(@Param("name")String name, @Param("..") Pager pager) {
        Cnd cnd = Strings.isBlank(name)? null : Cnd.where("name", "like", "%"+name+"%");
        QueryResult qr = new QueryResult();//这个类作用不明
        qr.setList(dao.query(User.class, cnd, pager));
        pager.setRecordCount(dao.count(User.class, cnd));//获得总页码
        qr.setPager(pager);
        return qr; //默认分页是第1页,每页20条
    }

    @At("/")//访问路径：user/
    @Ok("jsp:jsp.user.list") // 真实路径是 /WEB-INF/jsp/user/list.jsp
    public void index() {
    }
}

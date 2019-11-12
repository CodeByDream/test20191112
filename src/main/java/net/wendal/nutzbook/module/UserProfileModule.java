package net.wendal.nutzbook.module;

import net.wendal.nutzbook.bean.UserProfile;
import org.nutz.dao.DaoException;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.util.Daos;
import org.nutz.img.Images;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.Scope;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.filter.CheckSession;
import org.nutz.mvc.impl.AdaptorErrorContext;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadAdaptor;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import static org.nutz.dao.util.Pojos.log;

/**
 * @author Administrator
 * @date 2019/11/12 09:34:06
 * @description
 */
@IocBean
@At("/user/profile")//声明模块入口
@Filters(@By(type= CheckSession.class, args={"me", "/"}))//添加session检查过滤器
public class UserProfileModule extends  BaseModule {

    @At
    public UserProfile get(@Attr(scope= Scope.SESSION, value="me")int userId) {//session里面没有这个me返回0
        UserProfile profile = Daos.ext(dao, FieldFilter.locked(UserProfile.class, "avatar")).fetch(UserProfile.class, userId);
        if (profile == null) {//没有记录
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setCreateTime(new Date());
            profile.setUpdateTime(new Date());
            dao.insert(profile);
        }
        return profile;
    }

    //更新方法
    @At
    @AdaptBy(type= JsonAdaptor.class)
    @Ok("void")
    public void update(@Param("..")UserProfile profile, @Attr(scope=Scope.SESSION, value="me")int userId) {//更新表单提交的user详细信息
        if (profile == null)
        return;
        profile.setUserId(userId);//修正userId,防止恶意修改其他用户的信息
        profile.setUpdateTime(new Date());
        profile.setAvatar(null); // 不准通过这个方法更新
        UserProfile old = get(userId);
        // 检查email相关的更新
        if (old.getEmail() == null) {
            // 老的邮箱为null,所以新的肯定是未check的状态
            profile.setEmailChecked(false);
        } else {
            if (profile.getEmail() == null) {
                profile.setEmail(old.getEmail());
                profile.setEmailChecked(old.isEmailChecked());
            } else if (!profile.getEmail().equals(old.getEmail())) {
                // 设置新邮箱,果断设置为未检查状态
                profile.setEmailChecked(false);
            } else {
                profile.setEmailChecked(old.isEmailChecked());
            }
        }
        Daos.ext(dao, FieldFilter.create(UserProfile.class, null, "avatar", true)).update(profile);
    }

    //头像上传的入口方法Avatar（头像）
    @AdaptBy(type= UploadAdaptor.class, args={"${app.root}/WEB-INF/tmp/user_avatar", "8192", "utf-8", "20000", "102400"})
    @POST//限定restful风格为post
    @Ok(">>:/user/profile")//更新成功从定向
    @At("/avatar")
    public void uploadAvatar(@Param("file") TempFile tf,
                             @Attr(scope=Scope.SESSION, value="me")int userId,
                             AdaptorErrorContext err) {//TempFile：临时文件类
        String msg = null;
        if (err != null && err.getAdaptorErr() != null) {//！=null,说明有错误
            msg = "文件大小不符合规定";
        } else if (tf == null) {//临时文件为null，说明没上传文件
            msg = "空文件";
        } else {//没有错误信息，开始上传文件
            UserProfile profile = get(userId);
            try {
                BufferedImage image = Images.read(tf.getFile());//从临时文件中读图片信息
                image = Images.zoomScale(image, 128, 128, Color.WHITE);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Images.writeJpeg(image, out, 0.8f);
                profile.setAvatar(out.toByteArray());//从流里面将图片信息转化为byte数组
                dao.update(profile, "^avatar$");//这里表示只更新profile中的avatar字段
            } catch(DaoException e) {//异常处理
                log.info("System Error", e);
                msg = "系统错误";
            } catch (Throwable e) {
                msg = "图片格式错误";
            }
        }

        if (msg != null)//即有错误，返回前端去提示
            Mvcs.getHttpSession().setAttribute("upload-error-msg", msg);
    }

    //头像读取方法
    @Ok("raw:jpg")//成功返回一个图片
    @At("/avatar")
    @GET//restful限定为get风格
    public Object readAvatar(@Attr(scope=Scope.SESSION, value="me")int userId, HttpServletRequest req) throws SQLException {
        UserProfile profile = Daos.ext(dao, FieldFilter.create(UserProfile.class, "^avatar$")).fetch(UserProfile.class, userId);
        if (profile == null || profile.getAvatar() == null) {//如果数据库里没有当前用户相关头像信息，返回一个预设头像
            return new File(req.getServletContext().getRealPath("/rs/user_avatar/none.jpg"));
        }
        return profile.getAvatar();//否则返回从数据库中查到的头像，会自动转成jpg
    }

    @At("/")//真实访问路径：/user/profile/
    @GET
    @Ok("jsp:jsp.user.profile")//跳转到这个页面
    public UserProfile index(@Attr(scope=Scope.SESSION, value="me")int userId) {
        return get(userId);
    }
}

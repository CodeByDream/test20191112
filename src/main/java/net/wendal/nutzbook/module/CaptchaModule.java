package net.wendal.nutzbook.module;

import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.gimpy.FishEyeGimpyRenderer;
import net.wendal.nutzbook.util.Toolkit;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;

/**
 * @author Administrator
 * @date 2019/11/12 11:04:07
 * @description
 */
@IocBean
@At("/captcha")//模块入口
public class CaptchaModule {//登录验证码模块

    @At
    @Ok("raw:png")//成功返回png格式
    public BufferedImage next(HttpSession session, @Param("w") int w, @Param("h") int h) {
        if (w * h < 1) { //长或宽为0?重置为默认长宽.
            w = 200;
            h = 60;
        }
        Captcha captcha = new Captcha.Builder(w, h)
                .addText().addBackground(new GradiatedBackgroundProducer())
                .gimp(new FishEyeGimpyRenderer())
                .build();
        String text = captcha.getAnswer();
        session.setAttribute(Toolkit.captcha_attr, text);
        return captcha.getImage();
    }
}

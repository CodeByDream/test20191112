package net.wendal.nutzbook;

import org.nutz.mvc.annotation.*;
import org.nutz.mvc.ioc.provider.ComboIocProvider;

/**
 * @author Administrator
 * @date 2019/11/11 15:03:36
 * @description
 */
@ChainBy(args="mvc/nutzbook-mvc-chain.js")//与动作链有关
@Localization(value="msg/", defaultLocalizationKey="zh-CN")//本地化，国际化
@Fail("jsp:jsp.500")
@Ok("json:full")//full -- 不忽略空值,换行,key带双引号, 新版jquery兼容
@SetupBy(value = MainSetup.class)//加载主模块时先初始化MainSetup的内容
@IocBy(type = ComboIocProvider.class,args = {
        "*js", "/conf/ioc/dao.js",
        "*anno","net.wendal.nutzbook",
        "*tx",
        "*async"
})
@Modules(scanPackage = true)//这个注解会自动扫描所有的子模块
public class MainModule {
}

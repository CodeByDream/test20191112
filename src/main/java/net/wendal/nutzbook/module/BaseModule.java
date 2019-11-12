package net.wendal.nutzbook.module;

import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;

/**
 * @author Administrator
 * @date 2019/11/12 09:20:30
 * @description
 */
public abstract class BaseModule {

    /** 注入同名的一个ioc对象 */
    @Inject
    protected Dao dao;
}

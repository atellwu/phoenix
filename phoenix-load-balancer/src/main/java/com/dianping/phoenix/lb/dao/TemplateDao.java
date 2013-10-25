/**
 * Project: phoenix-load-balancer
 * 
 * File Created at 2013-10-16
 * 
 */
package com.dianping.phoenix.lb.dao;

import java.io.IOException;
import java.util.List;

import com.dianping.phoenix.lb.model.configure.entity.Template;

/**
 * @author Leo Liang
 * 
 */
public interface TemplateDao {

    Template find(String templateName);

    void add(Template template) throws IOException;

    void update(Template template) throws IOException;

    List<Template> list();

    void delete(String templateName) throws IOException;

}
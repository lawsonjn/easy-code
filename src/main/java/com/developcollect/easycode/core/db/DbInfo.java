package com.developcollect.easycode.core.db;

import com.developcollect.easycode.utils.EasyCodeUtil;
import com.mysql.cj.jdbc.MysqlDataSource;
import lombok.Data;

import javax.sql.DataSource;
import java.io.Serializable;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/8 15:59
 */
@Data
public class DbInfo implements Serializable {

    private String url;
    private String username;
    private String password;


    public DataSource getDataSource() {
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setURL(EasyCodeUtil.dressDbUrl(url));
        mysqlDataSource.setUser(username);
        mysqlDataSource.setPassword(password);
        return mysqlDataSource;
    }

    @Override
    public String toString() {
        return url;
    }
}

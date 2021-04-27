package com.developcollect.easycode.core.fakedata;

import com.baomidou.mybatisplus.generator.config.po.TableField;
import lombok.Data;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/12 11:36
 */
@Data
public class FakeDataFieldInfo {

    private TableField tableField;
    private IDbDataFaker dbDataFaker;

}

package com.developcollect.easycode.core.fakedata;

import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import lombok.Data;

import java.util.List;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/12 11:36
 */
@Data
public class FakeDataTableInfo {

    private TableInfo tableInfo;

    private List<FakeDataFieldInfo> fieldInfos;

}

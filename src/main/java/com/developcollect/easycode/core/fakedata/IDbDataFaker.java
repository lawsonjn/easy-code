package com.developcollect.easycode.core.fakedata;

import java.io.Serializable;

public interface IDbDataFaker extends Serializable {

    default void init(FakeDataContext context) {};

    Object getFakerData(FakeDataContext context);


}

package com.hcp.dao;

import android.content.Context;

import com.hcp.db.BaseDBHelper;
import com.hcp.stocktaking.entity.Locator;

import java.util.List;

public class LocatorDao extends BaseDao {
    private static LocatorDao mInstance;

    private static Object mLocker = new Object();

    private BaseDBHelper mBaseDBHelper;

    private LocatorDao(Context context){
        super(context);
        mBaseDBHelper = BaseDBHelper.getInstance(context);
    }

    public static LocatorDao getInstance(Context context){
        if(mInstance == null){
            synchronized (mLocker) {
                mInstance = new LocatorDao(context);
            }
        }
        return mInstance;
    }

    public void insertLocators(List<Locator> list) throws Exception {
        mBaseDBHelper.insert(list);
    }

    public boolean existLocator(String locatorName) throws Exception {
       return mBaseDBHelper.exists(Locator.class, "Name=?", new String[]{locatorName});
    }

    public void clear() throws Exception {
        mBaseDBHelper.delete(Locator.class, null, null);
    }

}

package com.hcp.dao;

import android.content.Context;

import com.hcp.db.BaseDBHelper;
import com.hcp.stocktaking.entity.SubInventory;

import java.util.List;

public class SubInventoryDao extends BaseDao {
    private static SubInventoryDao mInstance;

    private BaseDBHelper mBaseDBHelper;

    private SubInventoryDao(Context context){
        super(context);
        mBaseDBHelper = BaseDBHelper.getInstance(context);
    }

    public static SubInventoryDao getInstance(Context context){
        if(mInstance == null){
            synchronized (SubInventoryDao.class) {
                mInstance = new SubInventoryDao(context);
            }
        }
        return mInstance;
    }

    public void insertSubInventories(List<SubInventory> list) throws Exception {
        mBaseDBHelper.insert(list);
    }

    public boolean existSubInventories(String subName) throws Exception {
       return mBaseDBHelper.exists(SubInventory.class, "Name=?", new String[]{subName});
    }

    public void clear() throws Exception {
        mBaseDBHelper.delete(SubInventory.class, null, null);
    }

}

package com.hcp.intraware.entity;

import java.math.BigDecimal;

/**
 * Created by tmzhiPC on 2016-10-11.
 */
public class ISPBill {
    public String Bill;
    public String Organization;
    public String Component;
    public String ComponentDescription;
    public String Uom;
    public String LotNo;
    public String ProjectNo;
    public BigDecimal TransactionQuantity;
    public BigDecimal ReceivedQuantity;
    public BigDecimal RemainingQuantity;
}

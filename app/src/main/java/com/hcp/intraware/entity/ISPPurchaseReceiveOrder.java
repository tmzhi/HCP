package com.hcp.intraware.entity;

import java.math.BigDecimal;

/**
 * Created by tmzhiPC on 2016-10-11.
 */
public class ISPPurchaseReceiveOrder {
    public String Order;
    public String Line;
    public String Shipment;
    public String ShipmentLine;
    public String Component;
    public String ComponentDescription;
    public String Uom;
    public BigDecimal ReceivedQuantity;
    public BigDecimal UnReceivedQuantity;
    public BigDecimal PurchaseQuantity;
    public String LotNo;
    public String ProjectNo;
    public String Wip;
}

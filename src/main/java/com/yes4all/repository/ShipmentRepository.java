package com.yes4all.repository;

import com.yes4all.common.utils.DateUtils;
import com.yes4all.domain.Shipment;


import com.yes4all.service.IShipmentProformaInvoiceDTO;


import com.yes4all.service.IShipmentPurchaseOrdersDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;


import java.util.Optional;



@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {

    @Query(value = "SELECT s.* FROM shipment s where " +
        "  ( length(:shipmentId) =0  or upper(s.shipment_id) like '%'||:shipmentId||'%' )"+
        " and ( s.status =cast(:status as integer) or  cast(:status as integer) =-1) " +
        " and  ( length(:poNumber) =0  or  s.id in (select shipment_id from shipment_purchase_orders spo where spo.purchase_order_no like '%'||:poNumber||'%'))"+
        " and ( length(:etdFrom) =0 or s.etd >=TO_DATE(:etdFrom, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:etdTo) =0 or s.etd <=TO_DATE(:etdTo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:etaFrom) =0 or s.eta >=TO_DATE(:etaFrom, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:etaTo) =0 or s.eta <=TO_DATE(:etaTo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:atdFrom) =0 or s.atd >=TO_DATE(:atdFrom, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:atdTo) =0 or s.atd <=TO_DATE(:atdTo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:ataFrom) =0 or s.ata >=TO_DATE(:ataFrom, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:ataTo) =0 or s.ata <=TO_DATE(:ataTo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( s.created_by in (select id from jhi_user where upper(unaccent(concat(last_name,' ',first_name)))  like '%'||:createdBy||'%') or length(:createdBy) =0) " +
        " and ( s.updated_by in (select id from jhi_user where upper(unaccent(concat(last_name,' ',first_name)))  like '%'||:updatedBy||'%') or length(:updatedBy) =0) " +
        " and ( length(:createdDateFrom) =0 or to_date(to_char(s.created_date AT TIME ZONE 'UTC', '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"'), '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"') >=TO_DATE(:createdDateFrom, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:createdDateTo) =0 or to_date(to_char(s.created_date AT TIME ZONE 'UTC', '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"'), '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"') <=TO_DATE(:createdDateTo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')   )  " +
        " and ( length(:updatedDateFrom) =0 or to_date(to_char(s.updated_date AT TIME ZONE 'UTC', '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"'), '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"') >=TO_DATE(:updatedDateFrom, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')  ) " +
        " and ( length(:updatedDateTo) =0 or to_date(to_char(s.updated_date AT TIME ZONE 'UTC', '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"'), '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"') <=TO_DATE(:updatedDateTo, '"+ DateUtils.SIMPLE_DATE_FORMAT_SEARCH +"')   )  " ,
        nativeQuery = true)
    Page<Shipment> findByCondition(@Param("shipmentId") String shipmentId, @Param("status") String status, @Param("poNumber") String poNumber,
                                   @Param("etdFrom") String etdFrom, @Param("etdTo") String etdTo, @Param("etaFrom") String etaFrom, @Param("etaTo") String etaTo,
                                   @Param("atdFrom") String atdFrom, @Param("atdTo") String atdTo, @Param("ataFrom") String ataFrom, @Param("ataTo") String ataTo,
                                   @Param("createdBy") String createdBy, @Param("updatedBy") String updatedBy, @Param("createdDateFrom") String createdDateFrom,
                                   @Param("createdDateTo") String createdDateTo, @Param("updatedDateFrom") String updatedDateFrom, @Param("updatedDateTo") String updatedDateTo, Pageable
                                       pageable);


    @Query(value = "SELECT null id,a.po_number purchaseOrderNo,b.order_no proformaInvoiceNo,b.supplier,a.port_of_loading portOfLoading " +
        ",a.port_of_departure portOfDeparture ,a.pl_order demand ,cont.containers,a.etd,a.eta,a.id purchaseOrderId,b.id proformaInvoiceId  FROM purchase_orders_wh a inner join proforma_invoice_wh b on a.proforma_invoice_wh_id=b.id" +
        " left join (select  string_agg(distinct pod.container_no ,',') as containers, pod.purchase_order_wh_id" +
        "   from purchase_orders_wh_detail pod group by pod.purchase_order_wh_id) as cont on cont.purchase_order_wh_id=a.id " +
        " where b.status=5 and b.is_confirmed=true " +
        " and (length(:invoiceNo)=0   or (upper(:invoiceNo)=a.po_number) or  (upper(:invoiceNo)=b.order_no)) " +
        " and (length(:demand)=0   or ((:demand)=a.pl_order)  ) " +
        " and (length(:container)=0   or ( exists (select 1 from purchase_orders_wh_detail detail where detail.container_no LIKE N'%'||:container||'%' and detail.purchase_order_wh_id=a.id ) ) ) " +
        " and (length(:supplierSearch)=0  or (upper(:supplierSearch)=a.vendor_id)  ) " +
        " and (length(:portOfLoading)=0   or ((:portOfLoading)=a.port_of_loading)  ) " +
        " and (length(:portOfDischarge)=0  or ((:portOfDischarge)=a.port_of_departure)  ) " +
        " and ( length(:etdShipment) =0 or a.etd =to_date(:etdShipment, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "')  ) " +
        " and ( length(:etaShipment) =0 or a.eta =to_date(:etaShipment, '" + DateUtils.SIMPLE_DATE_FORMAT_SEARCH + "')  ) " +
        " and ( (:id =0 and a.id not in(select purchase_order_id from shipment_purchase_orders spo) ) " +
        " or ( :id<>0 and ( a.id not in(select purchase_order_id from shipment_purchase_orders spo where spo.shipment_id<>:id ) or a.id  in(select purchase_order_id from shipment_purchase_orders spo where spo.shipment_id=:id ))))",
        nativeQuery = true)
    List<IShipmentPurchaseOrdersDTO> findAllPurchaseOrderByCondition(@Param("invoiceNo") String invoiceNo
        , @Param("demand") String demand, @Param("container") String container, @Param("supplierSearch") String supplierSearch
        , @Param("portOfLoading") String portOfLoading, @Param("portOfDischarge") String portOfDischarge
        , @Param("etdShipment") String etd, @Param("etaShipment") String eta,@Param("id") Integer id);

    @Query(value = " select max(cast(  substring(shipment_id,4,5)  as int)) as shipmentId from shipment s", nativeQuery = true)
    Optional<Integer> findMaxShipmentId();

    @Query(value = "select  null id,b.order_no proformaInvoiceNo,b.id proformaInvoiceId,b.supplier,a.etd,a.eta,a.port_of_loading portOfLoading,a.port_of_departure portOfDeparture " +
        " from  purchase_orders_wh a inner join proforma_invoice_wh b on a.proforma_invoice_wh_id=b.id" +
        " where  b.status=5 and b.is_confirmed=true and a.id in(select purchase_order_id from shipment_purchase_orders spo where spo.shipment_id=:id )"+
        " and b.order_no not in(select proforma_invoice_no from shipment_proforma_invoice_pkl spi   )"+
        " and (length(:supplier)=0   or ((:supplier)=b.supplier)  ) "
        , nativeQuery = true)
    List<IShipmentProformaInvoiceDTO> findAllProformaInvoiceByCondition(@Param("supplier") String supplier,@Param("id") Integer id);



    @Query(value = "select s.* from shipment s inner join shipment_proforma_invoice_pkl sd on s.id=sd.shipment_id " +
        "where sd.proforma_invoice_id=:proformaInvoiceId  ", nativeQuery = true)
    List<Shipment> findAllByProformaInvoiceId(@Param("proformaInvoiceId") Integer proformaInvoiceId);


    List<Shipment> findAllByToWarehouseAndStatus( String wareHouseCode,Integer status);

    @Query(value = "select s.* from shipment s where exists (select 1 from shipment_containers sc where sc.id=:containerId and sc.shipment_id=s.id and sc.status=4)", nativeQuery = true)
    Optional<Shipment> findShipmentWithContainerId(@Param("containerId") Integer containerId);
}

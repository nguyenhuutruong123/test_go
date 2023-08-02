package com.yes4all.service.kafka;

import com.google.gson.Gson;
import com.yes4all.common.enums.*;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.domain.Product;
import com.yes4all.domain.SupplierCountry;
import com.yes4all.domain.Vendor;
import com.yes4all.domain.model.Message;
import com.yes4all.domain.model.ProductSyncDTO;
import com.yes4all.repository.ProductRepository;
import com.yes4all.repository.SupplierCountryRepository;
import com.yes4all.repository.VendorRepository;
import com.yes4all.service.impl.LogServiceImpl;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class KafkaConsumer {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private SupplierCountryRepository supplierCountryRepository;

    @Autowired
    private LogServiceImpl logServiceImpl;

    @KafkaListener(topicPartitions = @TopicPartition(topic = "product_create",partitions ={"0"}), groupId = "myGroup")
    public void consumeProductCreate(ConsumerRecord<String, Object> consumerRecord) {
        Gson g = new Gson();
        try {
            Object data = consumerRecord.value();
            Message result = (Message) data;
            if (CommonDataUtil.isNotNull(result)) {
                if (result.getObject().equals("product")) {
                    ProductSyncDTO productSync = g.fromJson(result.getPayload(), ProductSyncDTO.class);
                    Optional<Product> oProduct = productRepository.findBySku(productSync.getProduct_sku());
                    if (oProduct.isPresent()) {
                        productRepository.delete(oProduct.get());
                    }
                    Product product =new Product();
                    product.setSku(productSync.getProduct_sku());
                    product.setTitleInternal(productSync.getTitle());
                    product.setCompany(productSync.getCompany());
                    product.setId(productSync.getId());
                    logServiceImpl.writeLog(EnumLogFunctionDescription.PIMS_PRODUCT_TO_POMS.getCode(), EnumLogFunctionCode.PRODUCT.getCode(),
                        product.getId()+"", productSync, EnumLogProject.PIMS.getCode(), EnumLogFunctionAction.SYNC.getCode());
                    productRepository.saveAndFlush(product);
                }
            }
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage());
        }

    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "product_update",partitions ={"0"}), groupId = "myGroup")
    public void consumeProductUpdate(ConsumerRecord<String, Object> consumerRecord) {
        Gson g = new Gson();
        try {
            Object data = consumerRecord.value();
            Message result = (Message) data;
            if (CommonDataUtil.isNotNull(result)) {
                if (result.getObject().equals("product")) {
                    ProductSyncDTO productSync = g.fromJson(result.getPayload(), ProductSyncDTO.class);
                    Optional<Product> oProduct = productRepository.findBySku(productSync.getProduct_sku());
                    if (oProduct.isPresent()) {
                        productRepository.delete(oProduct.get());
                    }
                    Product product =new Product();
                    product.setSku(productSync.getProduct_sku());
                    product.setTitleInternal(productSync.getTitle());
                    product.setCompany(productSync.getCompany());
                    product.setId(productSync.getId());
                    logServiceImpl.writeLog(EnumLogFunctionDescription.PIMS_PRODUCT_TO_POMS.getCode(), EnumLogFunctionCode.PRODUCT.getCode(),
                        product.getId()+"", productSync, EnumLogProject.PIMS.getCode(), EnumLogFunctionAction.SYNC.getCode());
                    productRepository.saveAndFlush(product);
                }
            }
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage());
        }

    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "vendor_create",partitions ={"0"}), groupId = "myGroup")
    public void consumeVendorCreate(ConsumerRecord<String, Object> consumerRecord) {
        Gson g = new Gson();
        try {
            Object data = consumerRecord.value();
            Message result = (Message) data;
            if (CommonDataUtil.isNotNull(result)) {
                if (result.getObject().equals("vendor")) {
                    Vendor vendor = g.fromJson(result.getPayload(), Vendor.class);
                    Optional<Vendor> oVendor = vendorRepository.findByVendorCode(vendor.getVendorCode());
                    if (oVendor.isPresent()) {
                        vendorRepository.delete(oVendor.get());
                    } else {
                        List<SupplierCountry> supplierCountries = new ArrayList<>();
                        Arrays.stream(CountryEnum.values()).forEach(
                            k -> {
                                SupplierCountry supplierCountry = new SupplierCountry();
                                supplierCountry.setCountry(k.getCode());
                                supplierCountry.setOrderNumber(1);
                                supplierCountry.setSupplier(vendor.getVendorCode());
                                supplierCountries.add(supplierCountry);
                            }
                        );
                        supplierCountryRepository.saveAll(supplierCountries);
                    }
                    logServiceImpl.writeLog(EnumLogFunctionDescription.PIMS_VENDOR_TO_POMS.getCode(), EnumLogFunctionCode.VENDOR.getCode(),
                        vendor.getId()+"", vendor, EnumLogProject.PIMS.getCode(), EnumLogFunctionAction.SYNC.getCode());
                    vendorRepository.saveAndFlush(vendor);
                }
            }
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage());
        }

    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "vendor_update",partitions ={"0"}
    ), groupId = "myGroup")
    public void consumeVendorUpdate(ConsumerRecord<String, Object> consumerRecord) {
        Gson g = new Gson();
        try {
            Object data = consumerRecord.value();
            Message result = (Message) data;
            if (CommonDataUtil.isNotNull(result)) {
                if (result.getObject().equals("vendor")) {
                    Vendor vendor = g.fromJson(result.getPayload(), Vendor.class);
                    Optional<Vendor> oVendor = vendorRepository.findByVendorCode(vendor.getVendorCode());
                    if (oVendor.isPresent()) {
                        vendorRepository.delete(oVendor.get());
                    } else {
                        List<SupplierCountry> supplierCountries = new ArrayList<>();
                        Arrays.stream(CountryEnum.values()).forEach(
                            k -> {
                                SupplierCountry supplierCountry = new SupplierCountry();
                                supplierCountry.setCountry(k.getCode());
                                supplierCountry.setOrderNumber(1);
                                supplierCountry.setSupplier(vendor.getVendorCode());
                                supplierCountries.add(supplierCountry);
                            }
                        );
                        supplierCountryRepository.saveAll(supplierCountries);
                    }
                    logServiceImpl.writeLog(EnumLogFunctionDescription.PIMS_VENDOR_TO_POMS.getCode(), EnumLogFunctionCode.VENDOR.getCode(),
                        vendor.getId()+"", vendor, EnumLogProject.PIMS.getCode(), EnumLogFunctionAction.SYNC.getCode());
                    vendorRepository.saveAndFlush(vendor);
                }
            }
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage());
        }

    }
}

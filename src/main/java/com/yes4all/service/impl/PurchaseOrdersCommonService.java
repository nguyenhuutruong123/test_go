package com.yes4all.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.domain.SupplierCountry;
import com.yes4all.repository.SupplierCountryRepository;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@Transactional
public class PurchaseOrdersCommonService {
    @Autowired
    SupplierCountryRepository supplierCountryRepository;

    @Value("${attribute.host.url_api_soms}")
    public String URL_GET_SHIP_WINDOW_SOMS;

    public String getLastShipWindow(List<String> listFromSo) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject object = new JSONObject();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        //mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        object.put("saleOrders", listFromSo);
        HttpEntity<String> request = new HttpEntity<>(object.toString(), headers);
        ResponseEntity<String> response =
            restTemplate.exchange(URL_GET_SHIP_WINDOW_SOMS, HttpMethod.POST, request, String.class);
        try {
            Map<String, String> body = mapper.readValue(response.getBody(), new TypeReference<>() {
            });
            return body.get("lastestShipWindow");
        } catch (JsonProcessingException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get next numberOrderNo by vendorCode
     *
     * @param supplier
     * @return numberOrderNo
     */
    public Integer getNextNumberOrderNo(String supplier, String country, boolean isWH) {
        try {
            Optional<SupplierCountry> oSupplierCountry = supplierCountryRepository.findBySupplierAndCountry(supplier, country);
            if (oSupplierCountry.isEmpty()) {
                throw new BusinessException("Supplier not found.");
            }
            SupplierCountry supplierCountry = oSupplierCountry.get();
            Integer numberOrderNo = oSupplierCountry.get().getOrderNumber();
            if (isWH) {
                numberOrderNo = oSupplierCountry.get().getOrderNumberWh();
            }
            if (CommonDataUtil.isNull(numberOrderNo)) {
                numberOrderNo = 1;
            } else {
                numberOrderNo++;
            }
            if (isWH) {
                supplierCountry.setOrderNumberWh(numberOrderNo);
            }else{
                supplierCountry.setOrderNumber(numberOrderNo);
            }
            supplierCountryRepository.saveAndFlush(supplierCountry);
            return numberOrderNo;
        } catch (Exception e) {
            throw new BusinessException("Error when get number order no:" + e.getMessage());
        }
    }

    /**
     * create new orderNo formula: COUNTRY+"DI"+VENDOR+YEAR+4 number increase
     *
     * @param country
     * @param supplier
     * @return orderNo
     */
    public String generateOrderNo(String country, String supplier, boolean isWH) {
        String year = String.valueOf(new Date().getYear());
        year = year.substring(1, 3);
        Integer nextNumberOrderNo = getNextNumberOrderNo(supplier, country, isWH);
        String channel="DI";
        if(isWH){
            channel="WH";
        }
        return country + channel + supplier + year + String.format("%04d", nextNumberOrderNo);
    }

}

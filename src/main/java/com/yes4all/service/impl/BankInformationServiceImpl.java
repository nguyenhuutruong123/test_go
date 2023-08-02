package com.yes4all.service.impl;


import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.domain.BankInformation;
import com.yes4all.domain.LogService;
import com.yes4all.domain.User;
import com.yes4all.domain.Vendor;
import com.yes4all.domain.model.BankInformationDTO;
import com.yes4all.repository.BankInformationRepository;
import com.yes4all.repository.LogServiceRepository;
import com.yes4all.repository.UserRepository;
import com.yes4all.repository.VendorRepository;
import com.yes4all.service.BankInformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Service class for managing users.
 */
@Service
@Transactional
public class BankInformationServiceImpl implements BankInformationService {

    private static final Logger log = LoggerFactory.getLogger(BankInformationServiceImpl.class);

    @Autowired
    private BankInformationRepository bankInformationRepository;

    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<BankInformationDTO> findAllBankInformationByVendor(String userId) {
        try {
            Optional<User> oUser = userRepository.findOneByLogin(userId);
            if (oUser.isEmpty()) {
                throw new BusinessException("Can not found user.");
            }
            User user = oUser.get();
            String supplier = user.getVendor();
            if (CommonDataUtil.isEmpty(supplier)) {
                throw new BusinessException("User is not a supplier");
            }
            Optional<Vendor> oVendor = vendorRepository.findByVendorCode(supplier);
            if (oVendor.isEmpty()) {
                throw new BusinessException("Can not found vendor.");
            } else {
                Vendor vendor = oVendor.get();
                List<BankInformation> bankInformations = bankInformationRepository.findAllByVendor(vendor);
                if (bankInformations.isEmpty()) {
                    return Collections.emptyList();
                }
                return bankInformations.parallelStream().map(i -> CommonDataUtil.getModelMapper().map(i, BankInformationDTO.class)).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public BankInformationDTO saveBank(BankInformationDTO request) {
        try {
            BankInformation bankInformation = new BankInformation();
            BeanUtils.copyProperties(request, bankInformation);
            Optional<Vendor> oVendor = vendorRepository.findByVendorCode(request.getVendorCode());
            if (oVendor.isEmpty()) {
                throw new BusinessException("Can not found vendor.");
            }
            Vendor vendor = oVendor.get();
            bankInformation.setVendor(vendor);
            bankInformationRepository.saveAndFlush(bankInformation);
            return CommonDataUtil.getModelMapper().map(bankInformation, BankInformationDTO.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }
}

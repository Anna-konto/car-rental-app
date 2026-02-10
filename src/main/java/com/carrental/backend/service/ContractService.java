package com.carrental.backend.service;

import com.carrental.backend.model.Rental;
import com.carrental.backend.model.RentalType;
import org.springframework.stereotype.Service;

@Service
public class ContractService {

    private final PdfGenerator pdfGenerator;

    public ContractService(PdfGenerator pdfGenerator) {
        this.pdfGenerator = pdfGenerator;
    }

    public byte[] generateContractPdf(Rental rental) {
        if (rental.getRentalType() == RentalType.LONG_TERM) {
            return pdfGenerator.generateLongTermContract(rental);
        } else {
            return pdfGenerator.generateShortTermContract(rental);
        }
    }
}
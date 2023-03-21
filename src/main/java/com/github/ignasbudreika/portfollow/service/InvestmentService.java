package com.github.ignasbudreika.portfollow.service;

import com.github.ignasbudreika.portfollow.api.dto.response.InvestmentDTO;
import com.github.ignasbudreika.portfollow.enums.InvestmentType;
import com.github.ignasbudreika.portfollow.external.client.AlphaVantageClient;
import com.github.ignasbudreika.portfollow.model.Investment;
import com.github.ignasbudreika.portfollow.model.User;
import com.github.ignasbudreika.portfollow.repository.InvestmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

@Slf4j
@Service
public class InvestmentService {
    @Autowired
    private AlphaVantageClient alphaVantageClient;

    @Autowired private AssetService assetService;

    @Autowired
    private InvestmentRepository investmentRepository;

    public Collection<InvestmentDTO> getInvestmentsByUserId(String userId) {
        Collection<Investment> investments = investmentRepository.findAllByUserId(userId);

        Collection<InvestmentDTO> result = investments.stream().map(investment -> {
            BigDecimal price = assetService.getRecentPrice(investment.getSymbol(), investment.getType());

            return InvestmentDTO.builder()
                    .id(investment.getId())
                    .symbol(investment.getSymbol())
                    .value(investment.getQuantity().multiply(price).setScale(8, RoundingMode.HALF_UP))
                    .type(investment.getType()).build();
        }).toList();

        return result;
    }

    public Collection<Investment> getInvestmentsByUserIdAndType(String userId, InvestmentType type) {
        return investmentRepository.findAllByUserIdAndType(userId, type);
    }

    public Investment getInvestmentById(String id) {
        return investmentRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public Investment createInvestment(Investment investment, User user) {
        investment.setUser(user);

        return investmentRepository.save(investment);
    }

    public Investment saveInvestmentFetchedFromConnection(Investment investment, String connectionId) {
        Investment existing = investmentRepository.findBySymbolAndConnectionId(investment.getSymbol(), connectionId);
        if (existing != null) {
            log.info("investment: {} for connection: {} exists, updating quantity", investment.getSymbol(), connectionId);

            existing.setQuantity(investment.getQuantity());

            return investmentRepository.save(existing);
        }

        investment.setConnectionId(connectionId);

        return investmentRepository.save(investment);
    }
}

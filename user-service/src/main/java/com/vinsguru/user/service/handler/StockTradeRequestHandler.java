package com.vinsguru.user.service.handler;

import com.vinsguru.common.Ticker;
import com.vinsguru.user.StockTradeRequest;
import com.vinsguru.user.StockTradeResponse;
import com.vinsguru.user.entity.PortfolioItem;
import com.vinsguru.user.entity.User;
import com.vinsguru.user.exceptions.InsufficientBalanceException;
import com.vinsguru.user.exceptions.InsufficientSharedException;
import com.vinsguru.user.exceptions.UnknownTickerException;
import com.vinsguru.user.exceptions.UnknownUserException;
import com.vinsguru.user.repository.PortfolioItemRepository;
import com.vinsguru.user.repository.UserRepository;
import com.vinsguru.user.util.EntityMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockTradeRequestHandler {

    private final UserRepository userRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    public StockTradeRequestHandler(UserRepository userRepository, PortfolioItemRepository portfolioItemRepository) {
        this.userRepository = userRepository;
        this.portfolioItemRepository = portfolioItemRepository;
    }

    @Transactional
    public StockTradeResponse buyStock(StockTradeRequest request) {

        // validate
        validateTicker(request.getTicker());

        User user = this.userRepository
                .findById(request.getUserId())
                .orElseThrow(() -> new UnknownUserException(request.getUserId()));

        var totalPrice = request.getQuantity() * request.getPrice();

        validateUserBalance(request.getUserId(), user.getBalance(), totalPrice);

        //valid request

        user.setBalance(user.getBalance() - totalPrice);

        this.portfolioItemRepository.findByUserIdAndTicker(user.getId(), request.getTicker())
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + request.getQuantity()),
                        () -> this.portfolioItemRepository.save(EntityMessageMapper.toPortfolioItem(request))
                );

        return EntityMessageMapper.toStockTradeResponse(request, user.getBalance());
    }

    @Transactional
    public StockTradeResponse sellStock(StockTradeRequest request) {

        // validate
        validateTicker(request.getTicker());

        User user = this.userRepository
                .findById(request.getUserId())
                .orElseThrow(() -> new UnknownUserException(request.getUserId()));

        PortfolioItem portfolioItem = portfolioItemRepository.findByUserIdAndTicker(user.getId(), request.getTicker())
                .filter(pi -> pi.getQuantity() >= request.getQuantity())
                .orElseThrow(() -> new InsufficientSharedException(request.getUserId()));

        // valid request

        var totalPrice = request.getQuantity() * request.getPrice();

        user.setBalance(user.getBalance() + totalPrice);

        portfolioItem.setQuantity(portfolioItem.getQuantity() - request.getQuantity());

        return EntityMessageMapper.toStockTradeResponse(request, user.getBalance());
    }

    private void validateTicker(Ticker ticker) {
        if (Ticker.UNKNOWN.equals(ticker)) {
            throw new UnknownTickerException();
        }
    }

    private void validateUserBalance(Integer userId, Integer userBalance, Integer totalPrice) {

        if (totalPrice > userBalance) {
            throw new InsufficientBalanceException(userId);
        }
    }

}

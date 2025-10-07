package com.vinsguru.user.service.handler;

import com.vinsguru.user.UserInformation;
import com.vinsguru.user.UserInformationRequest;
import com.vinsguru.user.entity.PortfolioItem;
import com.vinsguru.user.entity.User;
import com.vinsguru.user.exceptions.UnknownUserException;
import com.vinsguru.user.repository.PortfolioItemRepository;
import com.vinsguru.user.repository.UserRepository;
import com.vinsguru.user.util.EntityMessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInformationRequestHandler {

    private final UserRepository userRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    public UserInformationRequestHandler(
            UserRepository userRepository,
            PortfolioItemRepository portfolioItemRepository
    ) {
        this.userRepository = userRepository;
        this.portfolioItemRepository = portfolioItemRepository;
    }

    public UserInformation getUserInformation(UserInformationRequest request) {
        User user = this.userRepository
                .findById(request.getUserId())
                .orElseThrow(() -> new UnknownUserException(request.getUserId()));

        List<PortfolioItem> portfolioItems = this.portfolioItemRepository.findAllByUserId(request.getUserId());

        return EntityMessageMapper.toUserInformation(user, portfolioItems);
    }


}

package com.bank.service;

import com.bank.model.*;
import com.bank.model.dto.TransactionViewDto;
import com.bank.model.dto.WalletDto;
import com.bank.model.enums.CurrencyType;
import com.bank.model.request.WalletRequest;
import com.bank.repository.*;
import com.bank.utils.DtoMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final TransactionRepository transactionRepository;
    private final GoalRepository goalRepository;
    private final DtoMapper dtoMapper;

    public WalletDto addWallet(WalletRequest request) {
        User user = userRepository.getUserById(request.getUserId());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer with id " + request.getUserId() + " not found");
        }

        Currency currency = currencyRepository.getCurrencyByName(CurrencyType.valueOf(request.getCurrency()));
        if (currency == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Currency with name " + request.getCurrency() + " not found");
        }

        Wallet walletToSave = new Wallet();
        walletToSave.setName(request.getName());
        walletToSave.setInitAmount(request.getInitialAmount());
        walletToSave.setCurrentAmount(request.getInitialAmount());
        walletToSave.setCurrency(currency);
        walletToSave.setUser(user);
        walletToSave.setSavings(request.getSavings());
        walletToSave.setArchived(false);
        walletToSave.setModified(Instant.now());

        walletRepository.save(walletToSave);

        return dtoMapper.toDto(walletToSave);
    }

    @Transactional
    public WalletDto updateWallet(Long id, WalletRequest request, HttpServletRequest session) {
        Long userId = (Long) session.getAttribute("USER_ID");

        Wallet wallet = walletRepository.getWalletById(id);
        if(wallet == null || !Objects.equals(wallet.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet with id " + id + " not found");
        }

        Currency currency = currencyRepository.getCurrencyByName(CurrencyType.valueOf(request.getCurrency()));
        if (currency == null) {
            throw new RuntimeException("Currency with name " + request.getCurrency() + " not found");
        }

        if (request.getName() != null)
            wallet.setName(request.getName());

        if (request.getCurrentAmount() != null)
            wallet.setCurrentAmount(request.getCurrentAmount());

        if (request.getSavings() != null)
            wallet.setSavings(request.getSavings());

        if(request.getCurrency() != null)
            wallet.setCurrency(currency);

        if(request.getArchived() != null) {
            wallet.setArchived(request.getArchived());
        }

        wallet.setModified(Instant.now());

        walletRepository.save(wallet);

        return dtoMapper.toDto(wallet);
    }

    public List<WalletDto> getAllWallets(Long userId) {
        List<Wallet> wallets = walletRepository.getWalletsByUserId(userId) ;

        if (wallets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No wallets found");
        }

        return wallets.stream().map(dtoMapper::toDto).toList();
    }

    public WalletDto getWallet(Long id, HttpServletRequest session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Wallet wallet = walletRepository.getWalletById(id);

        if (wallet == null || !Objects.equals(wallet.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet with id " + id + " not found");
        }

        return dtoMapper.toDto(wallet);
    }

    public WalletDto archiveWallet(Long id, HttpServletRequest session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Wallet wallet = walletRepository.getWalletById(id);
        if (wallet == null || !Objects.equals(wallet.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet with id " + id + " not found");
        }

        wallet.setArchived(true);
        walletRepository.save(wallet);

        return dtoMapper.toDto(wallet);
    }

    public WalletDto unarchiveWallet(Long id, HttpServletRequest session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Wallet wallet = walletRepository.getWalletById(id);
        if (wallet == null || !Objects.equals(wallet.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet with id " + id + " not found");
        }

        wallet.setArchived(false);
        walletRepository.save(wallet);

        return dtoMapper.toDto(wallet);
    }

    public void deleteWallet(Long id, HttpServletRequest session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Wallet wallet = walletRepository.getWalletById(id);
        if (wallet == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet with id " + id + " not found");
        }
        if (!Objects.equals(wallet.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User doesn't have wallet with id: " + id);
        }
        walletRepository.deleteById(id);
    }

    public List<TransactionViewDto> getWalletTransactions(Long walletId, HttpServletRequest session) {
        Long userId = (Long) session.getAttribute("USER_ID");

        Wallet wallet = walletRepository.getWalletById(walletId);
        if (wallet == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found");
        }

        if (!userId.equals(wallet.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Wallet doesn't belong to user");
        }

        return transactionRepository.findByWallet_Id(walletId).stream()
                .map(t -> TransactionViewDto.builder()
                        .name(t.getName())
                        .date(t.getTransactionDate())
                        .amount(t.getAmount())
                        .type(t.getTransactionType())
                        .currency(t.getCurrency())
                        .build()
                )
                .toList();
    }
}

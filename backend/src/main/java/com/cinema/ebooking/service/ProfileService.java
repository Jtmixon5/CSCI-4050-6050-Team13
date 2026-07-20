package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.AddressDto;
import com.cinema.ebooking.dto.PaymentCardDto;
import com.cinema.ebooking.dto.UpdateProfileRequest;
import com.cinema.ebooking.dto.UserProfileResponse;
import com.cinema.ebooking.entity.PaymentCard;
import com.cinema.ebooking.entity.User;
import com.cinema.ebooking.entity.UserAddress;
import com.cinema.ebooking.repository.PaymentCardRepository;
import com.cinema.ebooking.repository.UserAddressRepository;
import com.cinema.ebooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.time.YearMonth;

@Service
public class ProfileService {

    private static final int MAX_PAYMENT_CARDS = 3;

    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final PaymentInformationEncryptionService encryptionService;
    private final RegistrationEmailService emailService;

    public ProfileService(
        UserRepository userRepository,
        UserAddressRepository addressRepository,
        PaymentCardRepository paymentCardRepository,
        RegistrationEmailService emailService,
        @Value("${PAYMENT_ENCRYPTION_KEY:change-this-local-development-key}")
        String paymentEncryptionKey
    ) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.paymentCardRepository = paymentCardRepository;
        this.emailService = emailService;
        this.encryptionService =
            new PaymentInformationEncryptionService(
                paymentEncryptionKey
            );
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = requireUser(userId);

        AddressDto address = addressRepository
            .findByUserId(userId)
            .map(this::toAddressDto)
            .orElse(null);

        List<PaymentCardDto> cards =
            paymentCardRepository
                .findAllByUserIdOrderByCardSlotAsc(userId)
                .stream()
                .map(this::toPaymentCardDto)
                .toList();

        return toProfileResponse(user, address, cards);
    }

    @Transactional
    public UserProfileResponse updateProfile(
        Long userId,
        UpdateProfileRequest request
    ) {
        User user = requireUser(userId);

        validateRequest(request);

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhoneNumber(request.phoneNumber().trim());
        user.setPromotionOptIn(request.promotionOptIn());

        userRepository.save(user);

        updateAddress(user, request.address());
        updateCards(user, request.paymentCards());

        emailService.sendProfileChanged(user.getEmail());

        return getProfile(userId);
    }

    private void updateAddress(
        User user,
        AddressDto addressDto
    ) {
        if (isAddressEmpty(addressDto)) {
            addressRepository.deleteByUserId(user.getId());
            return;
        }

        validateAddress(addressDto);

        UserAddress address = addressRepository
            .findByUserId(user.getId())
            .orElseGet(() ->
                new UserAddress(
                    user,
                    addressDto.street().trim(),
                    addressDto.city().trim(),
                    addressDto.state().trim(),
                    addressDto.zipCode().trim()
                )
            );

        address.update(
            addressDto.street().trim(),
            addressDto.city().trim(),
            addressDto.state().trim(),
            addressDto.zipCode().trim()
        );

        addressRepository.save(address);
    }

    private void updateCards(
        User user,
        List<PaymentCardDto> cardDtos
    ) {
        List<PaymentCardDto> safeCards =
            cardDtos == null
                ? List.of()
                : cardDtos;

        if (safeCards.size() > MAX_PAYMENT_CARDS) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A user may store no more than three payment cards."
            );
        }

        List<PaymentCard> cardsToSave = new ArrayList<>();
        List<Long> submittedExistingIds = safeCards.stream()
            .map(PaymentCardDto::id)
            .filter(java.util.Objects::nonNull)
            .toList();
        if (submittedExistingIds.stream().distinct().count()
            != submittedExistingIds.size()) {
            throw badRequest("A saved payment card may only appear once.");
        }

        paymentCardRepository
            .findAllByUserIdOrderByCardSlotAsc(user.getId())
            .stream()
            .filter(card -> !submittedExistingIds.contains(card.getId()))
            .forEach(paymentCardRepository::delete);

        for (int index = 0; index < safeCards.size(); index++) {
            PaymentCardDto cardDto = safeCards.get(index);

            if (cardDto.id() != null) {
                PaymentCard existing = paymentCardRepository
                    .findByIdAndUserId(cardDto.id(), user.getId())
                    .orElseThrow(() -> badRequest("Saved payment card was not found."));
                existing.setCardSlot(index + 1);
                cardsToSave.add(existing);
            } else {
                validateCard(cardDto);
                String number = cardDto.cardNumber().replaceAll("\\s+", "");
                cardsToSave.add(new PaymentCard(
                    user,
                    index + 1,
                    cardDto.cardholderName().trim(),
                    cardDto.cardType().trim(),
                    number.substring(number.length() - 4),
                    encryptionService.encrypt(number),
                    encryptionService.encrypt(cardDto.expirationMonth().trim()),
                    encryptionService.encrypt(cardDto.expirationYear().trim()),
                    cardDto.billingZipCode().trim()
                ));
            }
        }

        paymentCardRepository.saveAll(cardsToSave);
    }

    private void validateRequest(
        UpdateProfileRequest request
    ) {
        if (request.firstName() == null
            || request.firstName().isBlank()) {
            throw badRequest("First name is required.");
        }

        if (request.lastName() == null
            || request.lastName().isBlank()) {
            throw badRequest("Last name is required.");
        }

        if (request.phoneNumber() == null
            || request.phoneNumber().isBlank()) {
            throw badRequest("Phone number is required.");
        }
    }

    private void validateAddress(AddressDto address) {
        if (address.street() == null
            || address.street().isBlank()
            || address.city() == null
            || address.city().isBlank()
            || address.state() == null
            || address.state().isBlank()
            || address.zipCode() == null
            || address.zipCode().isBlank()) {
            throw badRequest(
                "All address fields must be completed."
            );
        }
    }

    private boolean isAddressEmpty(AddressDto address) {
        if (address == null) {
            return true;
        }

        return isBlank(address.street())
            && isBlank(address.city())
            && isBlank(address.state())
            && isBlank(address.zipCode());
    }

    private void validateCard(PaymentCardDto card) {
        if (card.cardholderName() == null
            || card.cardholderName().isBlank()) {
            throw badRequest(
                "Cardholder name is required."
            );
        }

        if (card.cardType() == null || card.cardType().isBlank()) {
            throw badRequest("Card type is required.");
        }
        String cardNumber = card.cardNumber() == null
            ? ""
            : card.cardNumber().replaceAll("\\s+", "");
        if (!cardNumber.matches("\\d{13,19}")) {
            throw badRequest(
                "Payment card number must contain 13 to 19 digits."
            );
        }
        if (card.expirationMonth() == null
            || !card.expirationMonth().matches("0[1-9]|1[0-2]")) {
            throw badRequest("Expiration month must be between 01 and 12.");
        }
        if (card.expirationYear() == null
            || !card.expirationYear().matches("\\d{4}")) {
            throw badRequest("Expiration year must contain four digits.");
        }
        YearMonth expiration = YearMonth.of(
            Integer.parseInt(card.expirationYear()),
            Integer.parseInt(card.expirationMonth())
        );
        if (expiration.isBefore(YearMonth.now())) {
            throw badRequest("Payment card has expired.");
        }

        if (card.billingZipCode() == null
            || card.billingZipCode().isBlank()) {
            throw badRequest(
                "Billing ZIP code is required."
            );
        }
    }

    private User requireUser(Long userId) {
        return userRepository
            .findById(userId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found."
                )
            );
    }

    private UserProfileResponse toProfileResponse(
        User user,
        AddressDto address,
        List<PaymentCardDto> cards
    ) {
        return new UserProfileResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.isPromotionOptIn(),
            address,
            cards
        );
    }

    private AddressDto toAddressDto(
        UserAddress address
    ) {
        return new AddressDto(
            address.getStreet(),
            address.getCity(),
            address.getState(),
            address.getZipCode()
        );
    }

    private PaymentCardDto toPaymentCardDto(
        PaymentCard card
    ) {
        return new PaymentCardDto(
            card.getId(),
            card.getCardholderName(),
            card.getCardType(),
            null,
            null,
            null,
            card.getLastFour(),
            card.getBillingZipCode()
        );
    }

    private ResponseStatusException badRequest(
        String message
    ) {
        return new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            message
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

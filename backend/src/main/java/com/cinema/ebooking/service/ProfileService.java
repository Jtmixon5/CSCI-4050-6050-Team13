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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileService {

    private static final int MAX_PAYMENT_CARDS = 3;

    /*
     * Temporary development encryption key.
     * Replace with an environment variable/configured secret.
     */
    private static final String DEVELOPMENT_ENCRYPTION_KEY =
        "change-this-development-payment-key";

    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final PaymentInformationEncryptionService encryptionService;

    public ProfileService(
        UserRepository userRepository,
        UserAddressRepository addressRepository,
        PaymentCardRepository paymentCardRepository
    ) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.paymentCardRepository = paymentCardRepository;
        this.encryptionService =
            new PaymentInformationEncryptionService(
                DEVELOPMENT_ENCRYPTION_KEY
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

        /*
         * TODO:
         * Send profile-change notification email here.
         */

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

        /*
         * Simplest Sprint 2 synchronization:
         * replace the user's saved card list with the submitted list.
         */
        paymentCardRepository.deleteAllByUserId(user.getId());
        paymentCardRepository.flush();

        List<PaymentCard> cardsToSave = new ArrayList<>();

        for (int index = 0; index < safeCards.size(); index++) {
            PaymentCardDto cardDto = safeCards.get(index);

            validateCard(cardDto);

            String lastFour = cardDto.lastFour().trim();

            /*
             * The current frontend only supplies the final four digits.
             * These encrypted placeholders satisfy the non-null database
             * columns until the frontend collects full card data.
             */
            String encryptedNumber =
                encryptionService.encrypt(lastFour);

            String encryptedMonth =
                encryptionService.encrypt("00");

            String encryptedYear =
                encryptionService.encrypt("0000");

            PaymentCard card = new PaymentCard(
                user,
                index + 1,
                cardDto.cardholderName().trim(),
                "UNKNOWN",
                lastFour,
                encryptedNumber,
                encryptedMonth,
                encryptedYear,
                cardDto.billingZipCode().trim()
            );

            cardsToSave.add(card);
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

        if (card.lastFour() == null
            || !card.lastFour().matches("\\d{4}")) {
            throw badRequest(
                "Card last four must contain exactly four digits."
            );
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

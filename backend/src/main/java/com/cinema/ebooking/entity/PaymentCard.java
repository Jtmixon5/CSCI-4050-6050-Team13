package com.cinema.ebooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_cards")
public class PaymentCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "card_slot", nullable = false)
    private int cardSlot;

    @Column(
        name = "cardholder_name",
        nullable = false,
        length = 150
    )
    private String cardholderName;

    @Column(name = "card_type", nullable = false, length = 50)
    private String cardType;

    @Column(name = "last_four", nullable = false, length = 4)
    private String lastFour;

    @Column(
        name = "encrypted_card_number",
        nullable = false,
        length = 1000
    )
    private String encryptedCardNumber;

    @Column(
        name = "encrypted_expiration_month",
        nullable = false,
        length = 1000
    )
    private String encryptedExpirationMonth;

    @Column(
        name = "encrypted_expiration_year",
        nullable = false,
        length = 1000
    )
    private String encryptedExpirationYear;

    @Column(
        name = "billing_zip_code",
        nullable = false,
        length = 20
    )
    private String billingZipCode;

    protected PaymentCard() {
    }

    public PaymentCard(
        User user,
        int cardSlot,
        String cardholderName,
        String cardType,
        String lastFour,
        String encryptedCardNumber,
        String encryptedExpirationMonth,
        String encryptedExpirationYear,
        String billingZipCode
    ) {
        this.user = user;
        this.cardSlot = cardSlot;
        this.cardholderName = cardholderName;
        this.cardType = cardType;
        this.lastFour = lastFour;
        this.encryptedCardNumber = encryptedCardNumber;
        this.encryptedExpirationMonth = encryptedExpirationMonth;
        this.encryptedExpirationYear = encryptedExpirationYear;
        this.billingZipCode = billingZipCode;
    }

    public Long getId() {
        return id;
    }

    public int getCardSlot() {
        return cardSlot;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public String getCardType() {
        return cardType;
    }

    public String getLastFour() {
        return lastFour;
    }

    public String getBillingZipCode() {
        return billingZipCode;
    }
}

import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import {
  getProfile,
  updateProfile,
} from "../api/profile";
import type {
  Address,
  PaymentCard,
  UserProfile,
} from "../api/profile";
import { getFavorites } from "../api/favorites";
import type { Movie } from "../types/Movie";
import FavoritesList from "./FavoritesList";

const emptyAddress: Address = {
  street: "",
  city: "",
  state: "",
  zipCode: "",
};

const emptyCard: PaymentCard = {
  cardholderName: "",
  lastFour: "",
  billingZipCode: "",
};

export default function ProfilePage() {
  const [profile, setProfile] =
    useState<UserProfile | null>(null);

  const [favorites, setFavorites] =
    useState<Movie[]>([]);

  const [newCard, setNewCard] =
    useState<PaymentCard>(emptyCard);

  const [loading, setLoading] =
    useState(true);

  const [saving, setSaving] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const [success, setSuccess] =
    useState<string | null>(null);

  useEffect(() => {
    async function loadPage() {
      try {
        setLoading(true);
        setError(null);

        const [loadedProfile, loadedFavorites] =
          await Promise.all([
            getProfile(),
            getFavorites(),
          ]);

        setProfile({
          ...loadedProfile,
          address:
            loadedProfile.address ?? emptyAddress,
          paymentCards:
            loadedProfile.paymentCards ?? [],
        });

        setFavorites(loadedFavorites);
      } catch {
        setError(
          "Unable to load the profile. Make sure the backend is running."
        );
      } finally {
        setLoading(false);
      }
    }

    void loadPage();
  }, []);

  const updateAddress = (
    field: keyof Address,
    value: string
  ) => {
    if (!profile) {
      return;
    }

    setProfile({
      ...profile,
      address: {
        ...(profile.address ?? emptyAddress),
        [field]: value,
      },
    });
  };

  const addCard = () => {
    if (!profile) {
      return;
    }

    if (profile.paymentCards.length >= 3) {
      setError(
        "You may store no more than three payment cards."
      );
      return;
    }

    if (!newCard.cardholderName.trim()) {
      setError("Cardholder name is required.");
      return;
    }

    if (!/^\d{4}$/.test(newCard.lastFour)) {
      setError(
        "Enter exactly four digits for the card's last four digits."
      );
      return;
    }

    if (!newCard.billingZipCode.trim()) {
      setError("Billing ZIP code is required.");
      return;
    }

    setProfile({
      ...profile,
      paymentCards: [
        ...profile.paymentCards,
        newCard,
      ],
    });

    setNewCard(emptyCard);
    setError(null);
  };

  const removeCard = (index: number) => {
    if (!profile) {
      return;
    }

    setProfile({
      ...profile,
      paymentCards:
        profile.paymentCards.filter(
          (_, cardIndex) => cardIndex !== index
        ),
    });
  };

  const saveProfile = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (!profile) {
      return;
    }

    if (
      !profile.firstName.trim() ||
      !profile.lastName.trim() ||
      !profile.phoneNumber.trim()
    ) {
      setError(
        "First name, last name, and phone number are required."
      );
      return;
    }

    try {
      setSaving(true);
      setError(null);
      setSuccess(null);

      const savedProfile = await updateProfile({
        firstName: profile.firstName.trim(),
        lastName: profile.lastName.trim(),
        phoneNumber: profile.phoneNumber.trim(),
        promotionOptIn:
          profile.promotionOptIn,
        address: profile.address,
        paymentCards: profile.paymentCards,
      });

      setProfile({
        ...savedProfile,
        address:
          savedProfile.address ?? emptyAddress,
        paymentCards:
          savedProfile.paymentCards ?? [],
      });

      setSuccess(
        "Profile updated successfully. A notification email should be sent."
      );
    } catch {
      setError(
        "Unable to save the profile. Check the backend response."
      );
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <main>
        <h1>My Profile</h1>
        <p>Loading profile...</p>
      </main>
    );
  }

  if (!profile) {
    return (
      <main>
        <h1>My Profile</h1>
        <p role="alert">
          {error ?? "Profile was not found."}
        </p>
      </main>
    );
  }

  return (
    <main>
      <h1>Edit Profile</h1>

      {error && (
        <p
          role="alert"
          style={{ color: "#b00020" }}
        >
          {error}
        </p>
      )}

      {success && (
        <p
          role="status"
          style={{ color: "#087f23" }}
        >
          {success}
        </p>
      )}

      <form
        onSubmit={saveProfile}
        style={{
          display: "grid",
          gap: "16px",
          maxWidth: "700px",
        }}
      >
        <label>
          First name *
          <input
            required
            value={profile.firstName}
            onChange={(event) =>
              setProfile({
                ...profile,
                firstName: event.target.value,
              })
            }
          />
        </label>

        <label>
          Last name *
          <input
            required
            value={profile.lastName}
            onChange={(event) =>
              setProfile({
                ...profile,
                lastName: event.target.value,
              })
            }
          />
        </label>

        <label>
          Email address
          <input
            value={profile.email}
            disabled
            readOnly
            title="Email addresses cannot be changed."
          />
        </label>

        <label>
          Phone number *
          <input
            required
            value={profile.phoneNumber}
            onChange={(event) =>
              setProfile({
                ...profile,
                phoneNumber: event.target.value,
              })
            }
          />
        </label>

        <label
          style={{
            display: "flex",
            alignItems: "center",
            gap: "8px",
          }}
        >
          <input
            type="checkbox"
            checked={profile.promotionOptIn}
            onChange={(event) =>
              setProfile({
                ...profile,
                promotionOptIn:
                  event.target.checked,
              })
            }
            style={{ width: "auto" }}
          />

          Receive promotional emails
        </label>

        <fieldset>
          <legend>
            Address — maximum one
          </legend>

          <label>
            Street
            <input
              value={
                profile.address?.street ?? ""
              }
              onChange={(event) =>
                updateAddress(
                  "street",
                  event.target.value
                )
              }
            />
          </label>

          <label>
            City
            <input
              value={
                profile.address?.city ?? ""
              }
              onChange={(event) =>
                updateAddress(
                  "city",
                  event.target.value
                )
              }
            />
          </label>

          <label>
            State
            <input
              value={
                profile.address?.state ?? ""
              }
              onChange={(event) =>
                updateAddress(
                  "state",
                  event.target.value
                )
              }
            />
          </label>

          <label>
            ZIP code
            <input
              value={
                profile.address?.zipCode ?? ""
              }
              onChange={(event) =>
                updateAddress(
                  "zipCode",
                  event.target.value
                )
              }
            />
          </label>
        </fieldset>

        <fieldset>
          <legend>
            Payment Cards (
            {profile.paymentCards.length}/3)
          </legend>

          {profile.paymentCards.length === 0 && (
            <p>No payment cards saved.</p>
          )}

          {profile.paymentCards.map(
            (card, index) => (
              <div
                key={`${card.id ?? "new"}-${index}`}
                style={{
                  display: "flex",
                  justifyContent:
                    "space-between",
                  alignItems: "center",
                  gap: "12px",
                  marginBottom: "8px",
                }}
              >
                <span>
                  {card.cardholderName} — ••••{" "}
                  {card.lastFour}
                </span>

                <button
                  type="button"
                  onClick={() =>
                    removeCard(index)
                  }
                >
                  Remove
                </button>
              </div>
            )
          )}

          <label>
            Cardholder name
            <input
              value={newCard.cardholderName}
              onChange={(event) =>
                setNewCard({
                  ...newCard,
                  cardholderName:
                    event.target.value,
                })
              }
            />
          </label>

          <label>
            Last four digits
            <input
              inputMode="numeric"
              maxLength={4}
              value={newCard.lastFour}
              onChange={(event) =>
                setNewCard({
                  ...newCard,
                  lastFour:
                    event.target.value.replace(
                      /\D/g,
                      ""
                    ),
                })
              }
            />
          </label>

          <label>
            Billing ZIP code
            <input
              value={newCard.billingZipCode}
              onChange={(event) =>
                setNewCard({
                  ...newCard,
                  billingZipCode:
                    event.target.value,
                })
              }
            />
          </label>

          <button
            type="button"
            onClick={addCard}
            disabled={
              profile.paymentCards.length >= 3
            }
          >
            Add Payment Card
          </button>
        </fieldset>

        <button
          type="submit"
          disabled={saving}
        >
          {saving
            ? "Saving..."
            : "Save Profile"}
        </button>
      </form>

      <hr style={{ margin: "32px 0" }} />

      <FavoritesList favorites={favorites} />
    </main>
  );
}

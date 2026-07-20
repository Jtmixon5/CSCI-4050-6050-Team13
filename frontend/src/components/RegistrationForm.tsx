import { useState } from "react";
import type { FormEvent } from "react";
import {
  getApiErrorMessage,
  register,
} from "../api/auth";

interface RegistrationFormProps {
  onCancel: () => void;
}

const initialForm = {
  firstName: "",
  lastName: "",
  email: "",
  phoneNumber: "",
  password: "",
  confirmPassword: "",
  promotionOptIn: false,
};

export default function RegistrationForm({
  onCancel,
}: RegistrationFormProps) {
  const [form, setForm] = useState(initialForm);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const updateField = (
    field: keyof typeof initialForm,
    value: string | boolean
  ) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setSuccess(null);

    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    const strongPassword =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,72}$/;

    if (!strongPassword.test(form.password)) {
      setError(
        "Password must be 8–72 characters and include uppercase, lowercase, number, and special characters."
      );
      return;
    }

    try {
      setSubmitting(true);
      const message = await register({
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        email: form.email.trim(),
        phoneNumber: form.phoneNumber.trim(),
        password: form.password,
        promotionOptIn: form.promotionOptIn,
      });
      setSuccess(message);
      setForm(initialForm);
    } catch (requestError) {
      setError(
        getApiErrorMessage(
          requestError,
          "Unable to register. Please check your information and try again."
        )
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="registration-page">
      <button type="button" onClick={onCancel}>
        Back to Movies
      </button>

      <section className="registration-card">
        <h1>Create an Account</h1>
        <p className="form-guidance">
          Fields marked with <span aria-hidden="true">*</span> are required.
        </p>

        {error && (
          <p className="form-message error-message" role="alert">
            {error}
          </p>
        )}

        {success && (
          <p className="form-message success-message" role="status">
            {success}
          </p>
        )}

        <form className="registration-form" onSubmit={submit}>
          <label htmlFor="registration-first-name">
            First name <span aria-hidden="true">*</span>
          </label>
          <input
            id="registration-first-name"
            autoComplete="given-name"
            maxLength={100}
            required
            value={form.firstName}
            onChange={(event) =>
              updateField("firstName", event.target.value)
            }
          />

          <label htmlFor="registration-last-name">
            Last name <span aria-hidden="true">*</span>
          </label>
          <input
            id="registration-last-name"
            autoComplete="family-name"
            maxLength={100}
            required
            value={form.lastName}
            onChange={(event) =>
              updateField("lastName", event.target.value)
            }
          />

          <label htmlFor="registration-email">
            Email address <span aria-hidden="true">*</span>
          </label>
          <input
            id="registration-email"
            type="email"
            autoComplete="email"
            maxLength={255}
            required
            value={form.email}
            onChange={(event) =>
              updateField("email", event.target.value)
            }
          />

          <label htmlFor="registration-phone">
            Phone number <span aria-hidden="true">*</span>
          </label>
          <input
            id="registration-phone"
            type="tel"
            autoComplete="tel"
            minLength={7}
            maxLength={30}
            pattern="[0-9+() .-]{7,30}"
            title="Use 7–30 digits and common phone punctuation."
            required
            value={form.phoneNumber}
            onChange={(event) =>
              updateField("phoneNumber", event.target.value)
            }
          />

          <label htmlFor="registration-password">
            Password <span aria-hidden="true">*</span>
          </label>
          <input
            id="registration-password"
            type="password"
            autoComplete="new-password"
            minLength={8}
            maxLength={72}
            aria-describedby="password-help"
            required
            value={form.password}
            onChange={(event) =>
              updateField("password", event.target.value)
            }
          />
          <small id="password-help">
            Use 8–72 characters with uppercase, lowercase, a number, and a
            special character.
          </small>

          <label htmlFor="registration-confirm-password">
            Confirm password <span aria-hidden="true">*</span>
          </label>
          <input
            id="registration-confirm-password"
            type="password"
            autoComplete="new-password"
            minLength={8}
            maxLength={72}
            required
            value={form.confirmPassword}
            onChange={(event) =>
              updateField("confirmPassword", event.target.value)
            }
          />

          <label className="checkbox-label">
            <input
              type="checkbox"
              checked={form.promotionOptIn}
              onChange={(event) =>
                updateField("promotionOptIn", event.target.checked)
              }
            />
            Send me promotional emails
          </label>

          <button type="submit" disabled={submitting || !!success}>
            {submitting ? "Creating account..." : "Create Account"}
          </button>
        </form>
      </section>
    </main>
  );
}

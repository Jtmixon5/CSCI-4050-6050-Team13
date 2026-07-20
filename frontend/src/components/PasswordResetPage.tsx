import { useState } from "react";
import type { FormEvent } from "react";
import {
  getApiErrorMessage,
  requestPasswordReset,
  resetPassword,
} from "../api/auth";

interface PasswordResetPageProps {
  token?: string;
  onContinue: () => void;
}

export default function PasswordResetPage({
  token,
  onContinue,
}: PasswordResetPageProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    if (token && password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    try {
      setSubmitting(true);
      setMessage(
        token
          ? await resetPassword(token, password)
          : await requestPasswordReset(email.trim())
      );
    } catch (requestError) {
      setError(
        getApiErrorMessage(requestError, "Unable to complete this request.")
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="registration-page">
      <button type="button" onClick={onContinue}>
        Back to Sign In
      </button>
      <section className="registration-card">
        <h1>{token ? "Choose a New Password" : "Reset Password"}</h1>
        {!token && (
          <p className="form-guidance">
            Enter your email and we will send a reset link if an account exists.
          </p>
        )}
        {error && (
          <p className="form-message error-message" role="alert">{error}</p>
        )}
        {message && (
          <p className="form-message success-message" role="status">{message}</p>
        )}
        <form className="registration-form" onSubmit={submit}>
          {token ? (
            <>
              <label htmlFor="reset-password">New password *</label>
              <input
                id="reset-password"
                type="password"
                minLength={8}
                maxLength={72}
                required
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
              <label htmlFor="reset-confirm">Confirm password *</label>
              <input
                id="reset-confirm"
                type="password"
                minLength={8}
                maxLength={72}
                required
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
              />
            </>
          ) : (
            <>
              <label htmlFor="forgot-email">Email address *</label>
              <input
                id="forgot-email"
                type="email"
                required
                value={email}
                onChange={(event) => setEmail(event.target.value)}
              />
            </>
          )}
          <button type="submit" disabled={submitting || !!message}>
            {submitting
              ? "Submitting..."
              : token
                ? "Reset Password"
                : "Send Reset Link"}
          </button>
        </form>
      </section>
    </main>
  );
}

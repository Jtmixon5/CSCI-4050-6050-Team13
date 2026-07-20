import { useEffect, useState } from "react";
import {
  getApiErrorMessage,
  verifyEmail,
} from "../api/auth";

interface EmailVerificationPageProps {
  token: string;
  onContinue: () => void;
}

export default function EmailVerificationPage({
  token,
  onContinue,
}: EmailVerificationPageProps) {
  const [message, setMessage] = useState("Verifying your email...");
  const [error, setError] = useState(false);

  useEffect(() => {
    let active = true;

    async function verify() {
      try {
        const response = await verifyEmail(token);
        if (active) {
          setMessage(response);
        }
      } catch (requestError) {
        if (active) {
          setError(true);
          setMessage(
            getApiErrorMessage(
              requestError,
              "Unable to verify this email address."
            )
          );
        }
      }
    }

    void verify();
    return () => {
      active = false;
    };
  }, [token]);

  return (
    <main className="registration-page">
      <section className="registration-card">
        <h1>Email Verification</h1>
        <p
          className={`form-message ${
            error ? "error-message" : "success-message"
          }`}
          role={error ? "alert" : "status"}
        >
          {message}
        </p>
        <button type="button" onClick={onContinue}>
          Continue to Movies
        </button>
      </section>
    </main>
  );
}

import { useState } from "react";
import type { FormEvent } from "react";
import {
  getApiErrorMessage,
  login,
} from "../api/auth";
import type { AuthUser } from "../api/auth";

interface LoginPageProps {
  onLogin: (user: AuthUser) => void;
  onCancel: () => void;
  onForgotPassword: () => void;
  onRegister: () => void;
}

export default function LoginPage({
  onLogin,
  onCancel,
  onForgotPassword,
  onRegister,
}: LoginPageProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    try {
      setSubmitting(true);
      setError(null);
      onLogin(await login(email.trim(), password));
    } catch (requestError) {
      setError(
        getApiErrorMessage(
          requestError,
          "Unable to sign in. Please try again."
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
        <h1>Sign In</h1>
        {error && (
          <p className="form-message error-message" role="alert">
            {error}
          </p>
        )}
        <form className="registration-form" onSubmit={submit}>
          <label htmlFor="login-email">Email address *</label>
          <input
            id="login-email"
            type="email"
            autoComplete="email"
            required
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
          <label htmlFor="login-password">Password *</label>
          <input
            id="login-password"
            type="password"
            autoComplete="current-password"
            required
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
          <button type="submit" disabled={submitting}>
            {submitting ? "Signing in..." : "Sign In"}
          </button>
          <button type="button" onClick={onForgotPassword}>
            Forgot my password
          </button>
          <button type="button" onClick={onRegister}>
            Create an account
          </button>
        </form>
      </section>
    </main>
  );
}

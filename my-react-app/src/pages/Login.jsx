import { useState } from "react";
import { login } from "../api/fileApi";

export default function Login({ onLogin, onSwitch }) {
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handle = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const res = await login(form.username, form.password);
      onLogin(res.data.token);
    } catch (err) {
      setError(err.response?.data || "Invalid credentials.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">DFS<span>/</span>VAULT</div>
        <h1 className="auth-title">Sign in</h1>
        <p className="auth-subtitle">Access your distributed file storage.</p>

        {error && <div className="error-msg">{error}</div>}

        <form onSubmit={handle}>
          <div className="form-group">
            <label className="form-label">Username</label>
            <input
              className="form-input"
              placeholder="your_username"
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              required
              autoFocus
            />
          </div>
          <div className="form-group">
            <label className="form-label">Password</label>
            <input
              className="form-input"
              type="password"
              placeholder="••••••••"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              required
            />
          </div>
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? <span className="loading-spinner" /> : null}
            {loading ? "Authenticating…" : "Sign In →"}
          </button>
        </form>

        <div className="auth-switch">
          No account?{" "}
          <button className="link-btn" onClick={onSwitch}>Register here</button>
        </div>
      </div>
    </div>
  );
}
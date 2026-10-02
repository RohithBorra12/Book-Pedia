import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api, { saveSession, errorMessage } from "../api.js";

export default function Login({ onLogin }) {
  const [mode, setMode] = useState("login");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");
  const navigate = useNavigate();

  async function submit(e) {
    e.preventDefault();
    setError("");
    setInfo("");
    try {
      if (mode === "register") {
        await api.post("/auth/register", { username, password });
        setInfo("Account created. Log in with your new username and password.");
        setMode("login");
        return;
      }
      const { data } = await api.post("/auth/login", { username, password });
      saveSession(data);
      onLogin(data);
      navigate("/books");
    } catch (err) {
      setError(errorMessage(err));
    }
  }

  const isLogin = mode === "login";

  return (
    <div className="login-page">
      <form className="login-card" onSubmit={submit}>
        <h1>Book Store</h1>
        <p className="muted">{isLogin ? "Log in to browse and order books." : "Create an account to start ordering."}</p>

        <label>
          Username
          <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" required />
        </label>
        <label>
          Password
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)}
                 autoComplete={isLogin ? "current-password" : "new-password"} required />
        </label>

        {error && <p className="note error">{error}</p>}
        {info && <p className="note success">{info}</p>}

        <button type="submit" className="primary">{isLogin ? "Log in" : "Create account"}</button>
        <button type="button" className="quiet" onClick={() => { setMode(isLogin ? "register" : "login"); setError(""); }}>
          {isLogin ? "New here? Create an account" : "Already have an account? Log in"}
        </button>

        <p className="hint">Demo admin: <strong>admin</strong> / <strong>admin123</strong></p>
      </form>
    </div>
  );
}

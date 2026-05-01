import { useState, useEffect } from "react";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import "./App.css";

export default function App() {
  const [page, setPage] = useState("login");
  const [token, setToken] = useState(localStorage.getItem("jwt") || null);

  useEffect(() => {
    if (token) setPage("dashboard");
    else setPage("login");
  }, [token]);

  const handleLogin = (jwt) => {
    localStorage.setItem("jwt", jwt);
    setToken(jwt);
  };

  const handleLogout = () => {
    localStorage.removeItem("jwt");
    setToken(null);
    setPage("login");
  };

  return (
    <div className="app">
      {page === "dashboard" && token ? (
        <Dashboard onLogout={handleLogout} />
      ) : page === "register" ? (
        <Register onSwitch={() => setPage("login")} />
      ) : (
        <Login onLogin={handleLogin} onSwitch={() => setPage("register")} />
      )}
    </div>
  );
}
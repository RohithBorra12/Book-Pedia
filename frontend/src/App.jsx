import { useState } from "react";
import { Routes, Route, Navigate, NavLink, useNavigate } from "react-router-dom";
import Login from "./pages/Login.jsx";
import Books from "./pages/Books.jsx";
import Orders from "./pages/Orders.jsx";
import { getSession, clearSession } from "./api.js";

export default function App() {
  const [session, setSession] = useState(getSession());
  const navigate = useNavigate();

  function logout() {
    clearSession();
    setSession(null);
    navigate("/login");
  }

  if (!session) {
    return (
      <Routes>
        <Route path="/login" element={<Login onLogin={setSession} />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  const isAdmin = session.role === "ADMIN";

  return (
    <div className="shell">
      <header className="topbar">
        <span className="brand">Book Store</span>
        <nav>
          <NavLink to="/books">Books</NavLink>
          <NavLink to="/orders">Orders</NavLink>
        </nav>
        <div className="who">
          <span>{session.username}</span>
          <span className={isAdmin ? "role admin" : "role"}>{isAdmin ? "Admin" : "User"}</span>
          <button className="quiet" onClick={logout}>Log out</button>
        </div>
      </header>
      <main>
        <Routes>
          <Route path="/books" element={<Books isAdmin={isAdmin} />} />
          <Route path="/orders" element={<Orders isAdmin={isAdmin} />} />
          <Route path="*" element={<Navigate to="/books" replace />} />
        </Routes>
      </main>
    </div>
  );
}

import { useEffect, useState } from "react";
import api, { errorMessage } from "../api.js";

export default function Orders({ isAdmin }) {
  const [view, setView] = useState("mine");
  const [orders, setOrders] = useState([]);
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    setError("");
    const url = view === "all" ? "/orders" : "/orders/my";
    api.get(url).then((r) => setOrders(r.data)).catch((err) => setError(errorMessage(err)));
    if (view === "mine") {
      api.get("/orders/my/summary").then((r) => setSummary(r.data)).catch(() => setSummary(null));
    }
  }, [view]);

  return (
    <section>
      <div className="page-head">
        <h2>{view === "all" ? "All orders" : "My orders"}</h2>
        {isAdmin && (
          <div className="toggle" role="group" aria-label="Which orders">
            <button className={view === "mine" ? "on" : ""} onClick={() => setView("mine")}>Mine</button>
            <button className={view === "all" ? "on" : ""} onClick={() => setView("all")}>Everyone's</button>
          </div>
        )}
      </div>

      {view === "mine" && summary && summary.orderCount > 0 && (
        <p className="summary">
          {summary.orderCount} {summary.orderCount === 1 ? "order" : "orders"}, ₹{summary.totalSpent.toFixed(0)} spent on confirmed orders.
        </p>
      )}

      {error && <p className="note error">{error}</p>}

      {orders.length === 0 && !error ? (
        <p className="muted">No orders yet. Pick a book on the Books page to place one.</p>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>#</th>
                {view === "all" && <th>Customer</th>}
                <th>Book</th>
                <th className="num">Qty</th>
                <th className="num">Total</th>
                <th>Status</th>
                <th>Placed</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id}>
                  <td>{o.id}</td>
                  {view === "all" && <td>{o.username}</td>}
                  <td>{o.bookTitle}</td>
                  <td className="num">{o.quantity}</td>
                  <td className="num">₹{o.totalPrice.toFixed(0)}</td>
                  <td><span className={`status ${o.status.toLowerCase()}`} title={o.statusDescription}>{o.status.toLowerCase()}</span></td>
                  <td>{o.createdAt ? new Date(o.createdAt).toLocaleString() : ""}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

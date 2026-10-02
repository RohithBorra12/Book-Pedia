import { useEffect, useState } from "react";
import api, { errorMessage } from "../api.js";

const emptyForm = { title: "", author: "", price: "", stock: "" };

export default function Books({ isAdmin }) {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [quantities, setQuantities] = useState({});
  const [note, setNote] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [showForm, setShowForm] = useState(false);

  async function load() {
    try {
      const { data } = await api.get("/books");
      setBooks(data);
    } catch (err) {
      setNote({ type: "error", text: errorMessage(err) });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function order(book) {
    const quantity = Number(quantities[book.id] || 1);
    try {
      const { data } = await api.post("/orders", { bookId: book.id, quantity });
      setNote({
        type: data.status === "PLACED" ? "success" : "warn",
        text: `Order #${data.id} for "${book.title}": ${data.statusDescription}.`,
      });
      // Stock is reduced asynchronously by Kafka, so refresh shortly after
      setTimeout(load, 1500);
    } catch (err) {
      setNote({ type: "error", text: errorMessage(err) });
    }
  }

  async function addBook(e) {
    e.preventDefault();
    try {
      await api.post("/books", { ...form, price: Number(form.price), stock: Number(form.stock) });
      setNote({ type: "success", text: `Added "${form.title}".` });
      setForm(emptyForm);
      setShowForm(false);
      load();
    } catch (err) {
      setNote({ type: "error", text: errorMessage(err) });
    }
  }

  async function remove(book) {
    if (!window.confirm(`Delete "${book.title}"?`)) return;
    try {
      await api.delete(`/books/${book.id}`);
      setNote({ type: "success", text: `Deleted "${book.title}".` });
      load();
    } catch (err) {
      setNote({ type: "error", text: errorMessage(err) });
    }
  }

  return (
    <section>
      <div className="page-head">
        <h2>Books</h2>
        {isAdmin && (
          <button className={showForm ? "quiet" : "primary"} onClick={() => setShowForm(!showForm)}>
            {showForm ? "Cancel" : "Add a book"}
          </button>
        )}
      </div>

      {note && <p className={`note ${note.type}`}>{note.text}</p>}

      {isAdmin && showForm && (
        <form className="add-form" onSubmit={addBook}>
          <label>Title<input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required /></label>
          <label>Author<input value={form.author} onChange={(e) => setForm({ ...form, author: e.target.value })} required /></label>
          <label>Price (₹)<input type="number" min="1" step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} required /></label>
          <label>Stock<input type="number" min="0" value={form.stock} onChange={(e) => setForm({ ...form, stock: e.target.value })} required /></label>
          <button type="submit" className="primary">Save book</button>
        </form>
      )}

      {loading ? (
        <p className="muted">Loading books…</p>
      ) : books.length === 0 ? (
        <p className="muted">No books yet.{isAdmin ? " Use “Add a book” to create the first one." : ""}</p>
      ) : (
        <ul className="shelf">
          {books.map((book) => (
            <li key={book.id} className="book">
              <div className="spine" aria-hidden="true" />
              <div className="book-info">
                <h3>{book.title}</h3>
                <p className="muted">{book.author}</p>
              </div>
              <div className="book-meta">
                <span className="price">₹{book.price.toFixed(0)}</span>
                <span className={book.stock === 0 ? "stock out" : "stock"}>
                  {book.stock === 0 ? "Out of stock" : `${book.stock} in stock`}
                </span>
              </div>
              <div className="book-actions">
                <input type="number" min="1" max={book.stock || 1} aria-label={`Quantity of ${book.title}`}
                       value={quantities[book.id] || 1}
                       onChange={(e) => setQuantities({ ...quantities, [book.id]: e.target.value })} />
                <button className="primary" disabled={book.stock === 0} onClick={() => order(book)}>Order</button>
                {isAdmin && <button className="danger" onClick={() => remove(book)}>Delete</button>}
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

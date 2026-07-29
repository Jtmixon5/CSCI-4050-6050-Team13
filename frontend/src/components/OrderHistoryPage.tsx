import { useEffect, useState } from "react";
import { getOrderHistory, type OrderHistoryItem } from "../api/bookings";

const formatDateTime = (value: string) =>
  new Intl.DateTimeFormat("en-US", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));

export default function OrderHistoryPage({ onBack }: { onBack: () => void }) {
  const [orders, setOrders] = useState<OrderHistoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getOrderHistory()
      .then(setOrders)
      .catch(() => setError("Unable to load your order history."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <main className="booking-page">
      <button type="button" onClick={onBack}>Back to Movies</button>
      <h1>My Orders</h1>
      {loading && <p>Loading orders...</p>}
      {error && <p className="form-error" role="alert">{error}</p>}
      {!loading && !error && orders.length === 0 && <p>You have no confirmed orders yet.</p>}
      <div className="order-history-list">
        {orders.map((order) => (
          <article className="booking-card order-history-card" key={order.id}>
            <h2>{order.movieTitle}</h2>
            <p><strong>Confirmation:</strong> {order.confirmationNumber}</p>
            <p><strong>Showtime:</strong> {formatDateTime(order.showtime)}</p>
            <p><strong>Showroom:</strong> {order.showroom}</p>
            <p><strong>Seats:</strong> {order.seats.join(", ")}</p>
            <p>
              <strong>Tickets:</strong> {order.adultTickets} adult,{" "}
              {order.childTickets} child, {order.seniorTickets} senior
            </p>
            <p><strong>Total:</strong> ${Number(order.totalAmount).toFixed(2)}</p>
            <p>Paid with card ending in {order.cardLastFour}</p>
          </article>
        ))}
      </div>
    </main>
  );
}

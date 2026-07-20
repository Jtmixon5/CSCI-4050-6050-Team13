interface AdminHomeProps {
  name: string;
  onLogout: () => void;
}

export default function AdminHome({
  name,
  onLogout,
}: AdminHomeProps) {
  return (
    <main>
      <h1>Admin Portal</h1>
      <p>Welcome, {name}.</p>
      <nav className="admin-menu" aria-label="Admin menu">
        <button type="button">Manage Movies</button>
        <button type="button">Promotions</button>
        <button type="button">Users</button>
        <button type="button">Showtimes</button>
      </nav>
      <button type="button" onClick={onLogout}>Logout</button>
    </main>
  );
}

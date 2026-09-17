import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function getShortRole(role: string | null): string {
  switch (role) {
    case 'SUPER_ADMIN': return 'SUPER';
    case 'ADMIN': return 'ADMIN';
    case 'USER': return 'USER';
    case 'DRIVER': return 'DRIVER';
    default: return role ?? '';
  }
}

export default function Navbar() {
  const { role, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const currentRoute = location.pathname;

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      {/* LEFT SECTION */}
      <div className="left-section">
        {role && (
          <div className={`role-badge ${role}`}>
            {getShortRole(role)}
          </div>
        )}
        <div className="logo">Car Rental</div>
      </div>

      {/* RIGHT ACTIONS */}
      <div className="actions">
        {!role ? (
          <>
            {currentRoute === '/login' && (
              <button onClick={() => navigate('/register')}>Register</button>
            )}
            {currentRoute === '/register' && (
              <button onClick={() => navigate('/login')}>Login</button>
            )}
            {currentRoute !== '/login' && currentRoute !== '/register' && (
              <>
                <button onClick={() => navigate('/login')}>Login</button>
                <button onClick={() => navigate('/register')}>Register</button>
              </>
            )}
          </>
        ) : (
          <button onClick={handleLogout}>Logout</button>
        )}
      </div>
    </nav>
  );
}

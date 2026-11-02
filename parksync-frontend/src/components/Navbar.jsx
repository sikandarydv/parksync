import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Car, Shield, User, LogOut, Calendar, Compass } from 'lucide-react';

export function Navbar({ activeTab, setActiveTab, onOpenAuth }) {
  const { user, logout, isAdmin } = useAuth();

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <div className="brand" onClick={() => setActiveTab('browse')}>
          <div className="brand-icon">
            <Car size={22} />
          </div>
          <span>ParkSync</span>
        </div>

        <nav className="nav-links">
          <button
            className={`nav-link ${activeTab === 'browse' ? 'active' : ''}`}
            onClick={() => setActiveTab('browse')}
          >
            <Compass size={17} style={{ marginRight: '4px', verticalAlign: 'text-bottom' }} />
            Browse Parking
          </button>

          {user && (
            <>
              <button
                className={`nav-link ${activeTab === 'reservations' ? 'active' : ''}`}
                onClick={() => setActiveTab('reservations')}
              >
                <Calendar size={17} style={{ marginRight: '4px', verticalAlign: 'text-bottom' }} />
                My Bookings
              </button>

              <button
                className={`nav-link ${activeTab === 'vehicles' ? 'active' : ''}`}
                onClick={() => setActiveTab('vehicles')}
              >
                <Car size={17} style={{ marginRight: '4px', verticalAlign: 'text-bottom' }} />
                My Vehicles
              </button>
            </>
          )}

          {isAdmin && (
            <button
              className={`nav-link ${activeTab === 'admin' ? 'active' : ''}`}
              onClick={() => setActiveTab('admin')}
              style={{ color: '#064e3b' }}
            >
              <Shield size={17} style={{ marginRight: '4px', verticalAlign: 'text-bottom' }} />
              Admin Operations
            </button>
          )}
        </nav>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
          {user ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <div style={{ textAlign: 'right', display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontWeight: 700, fontSize: '0.9rem' }}>{user.fullName}</span>
                <span className={`badge-role ${isAdmin ? 'admin' : 'user'}`}>
                  {isAdmin ? 'Administrator' : 'Account Member'}
                </span>
              </div>
              <button
                className="btn btn-secondary btn-sm"
                onClick={logout}
                title="Sign Out"
              >
                <LogOut size={16} />
              </button>
            </div>
          ) : (
            <button className="btn btn-primary btn-sm" onClick={onOpenAuth}>
              <User size={16} />
              Sign In / Register
            </button>
          )}
        </div>
      </div>
    </header>
  );
}

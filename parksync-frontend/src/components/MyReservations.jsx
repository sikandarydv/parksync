import React, { useState, useEffect } from 'react';
import { api } from '../api';
import { useAuth } from '../context/AuthContext';
import confetti from 'canvas-confetti';
import { Calendar, Clock, MapPin, Car, AlertCircle, CheckCircle, CreditCard, XCircle, FileText, ArrowUpRight, X } from 'lucide-react';

export function MyReservations({ onOpenPayment }) {
  const { user } = useAuth();
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');
  const [error, setError] = useState(null);
  const [cancellingId, setCancellingId] = useState(null);

  // Extend Booking Modal State
  const [extendingReservation, setExtendingReservation] = useState(null);
  const [extendHours, setExtendHours] = useState(1);
  const [extendLoading, setExtendLoading] = useState(false);
  const [extendError, setExtendError] = useState(null);
  const [extendSuccess, setExtendSuccess] = useState(null);

  useEffect(() => {
    if (user) {
      loadReservations();
    }
  }, [user]);

  const loadReservations = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.getUserReservations(user.userId);
      setReservations(data);
    } catch (err) {
      setError(err.message || 'Failed to load reservations');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (reservationId) => {
    if (!window.confirm('Are you sure you want to cancel this reservation? The slot will immediately become available for others.')) {
      return;
    }

    try {
      setCancellingId(reservationId);
      await api.cancelReservation(reservationId);
      await loadReservations();
    } catch (err) {
      alert('Failed to cancel reservation: ' + err.message);
    } finally {
      setCancellingId(null);
    }
  };

  const handleOpenExtend = (res) => {
    setExtendingReservation(res);
    setExtendHours(1);
    setExtendError(null);
    setExtendSuccess(null);
  };

  const handleConfirmExtend = async () => {
    if (!extendingReservation) return;
    try {
      setExtendLoading(true);
      setExtendError(null);
      await api.extendReservation(extendingReservation.reservationId, extendHours);
      confetti({ particleCount: 60, spread: 50, origin: { y: 0.6 } });
      setExtendSuccess(`Successfully extended Bay ${extendingReservation.slotNumber} by +${extendHours} hour(s)!`);
      setTimeout(async () => {
        setExtendingReservation(null);
        await loadReservations();
      }, 1200);
    } catch (err) {
      setExtendError(err.message || 'Failed to extend reservation. Slot may be reserved by another driver.');
    } finally {
      setExtendLoading(false);
    }
  };

  const filtered = reservations.filter((r) => {
    if (filter === 'ALL') return true;
    return r.status === filter;
  });

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', fontWeight: 800 }}>My Parking Bookings</h1>
          <p style={{ color: '#64748b' }}>Manage your active reservations, cancellations, and receipts.</p>
        </div>

        <button className="btn btn-secondary btn-sm" onClick={loadReservations}>
          Refresh List
        </button>
      </div>

      {error && (
        <div className="alert alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* Tabs */}
      <div className="tabs-header">
        {['ALL', 'CONFIRMED', 'PENDING', 'CANCELLED', 'COMPLETED'].map((tab) => (
          <button
            key={tab}
            className={`tab-btn ${filter === tab ? 'active' : ''}`}
            onClick={() => setFilter(tab)}
          >
            {tab}
          </button>
        ))}
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem', color: '#64748b' }}>
          Loading your reservations...
        </div>
      ) : filtered.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3.5rem' }}>
          <Calendar size={48} color="#94a3b8" style={{ margin: '0 auto 1rem auto' }} />
          <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: '#1e293b' }}>No reservations found</h3>
          <p style={{ color: '#64748b', marginTop: '0.4rem' }}>
            {filter === 'ALL'
              ? 'You have not booked any parking slots yet. Go to Browse Parking to book one!'
              : `You have no ${filter.toLowerCase()} reservations.`}
          </p>
        </div>
      ) : (
        <div className="grid-2">
          {filtered.map((r) => {
            const statusClass =
              r.status === 'CONFIRMED'
                ? 'badge-confirmed'
                : r.status === 'PENDING'
                ? 'badge-pending'
                : r.status === 'CANCELLED'
                ? 'badge-cancelled'
                : 'badge-completed';

            const canCancel = r.status === 'CONFIRMED' || r.status === 'PENDING';
            const canPay = r.status === 'PENDING';

            return (
              <div key={r.reservationId} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
                  <div>
                    <h3 style={{ fontSize: '1.15rem', fontWeight: 800 }}>{r.lotName}</h3>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: '#64748b', fontSize: '0.82rem' }}>
                      <MapPin size={14} />
                      <span>{r.lotAddress}</span>
                    </div>
                  </div>
                  <span className={`badge ${statusClass}`}>{r.status}</span>
                </div>

                <div style={{ background: '#f8fafc', padding: '0.85rem', borderRadius: '8px', margin: '0.75rem 0', display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '0.5rem', fontSize: '0.85rem' }}>
                  <div>
                    <span style={{ color: '#64748b', fontSize: '0.75rem', fontWeight: 700 }}>PARKING SPOT</span>
                    <div style={{ fontWeight: 800, color: '#4f46e5', fontSize: '1.05rem' }}>Slot {r.slotNumber}</div>
                  </div>
                  <div>
                    <span style={{ color: '#64748b', fontSize: '0.75rem', fontWeight: 700 }}>VEHICLE</span>
                    <div style={{ fontWeight: 700 }}>{r.vehicleNumber} ({r.vehicleType})</div>
                  </div>
                  <div style={{ gridColumn: 'span 2', borderTop: '1px solid #e2e8f0', paddingTop: '0.4rem' }}>
                    <span style={{ color: '#64748b', fontSize: '0.75rem', fontWeight: 700 }}>SCHEDULE</span>
                    <div style={{ fontWeight: 600 }}>
                      {new Date(r.startTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })} →{' '}
                      {new Date(r.endTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto', paddingTop: '0.75rem', borderTop: '1px solid #f1f5f9' }}>
                  <div style={{ fontSize: '1.15rem', fontWeight: 800, color: '#10b981' }}>
                    ₹{r.totalAmount}
                  </div>

                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    {canPay && (
                      <button
                        className="btn btn-primary btn-sm"
                        onClick={() => onOpenPayment(r)}
                      >
                        <CreditCard size={15} />
                        Pay Now
                      </button>
                    )}

                    {canCancel && (
                      <button
                        className="btn btn-secondary btn-sm"
                        style={{ color: '#ef4444', borderColor: '#fca5a5' }}
                        disabled={cancellingId === r.reservationId}
                        onClick={() => handleCancel(r.reservationId)}
                      >
                        <XCircle size={15} />
                        {cancellingId === r.reservationId ? 'Cancelling...' : 'Cancel'}
                      </button>
                    )}

                    {r.status === 'CONFIRMED' && (
                      <>
                        <button
                          className="btn btn-primary btn-sm"
                          style={{ background: 'var(--primary)', borderColor: 'var(--primary)' }}
                          onClick={() => handleOpenExtend(r)}
                        >
                          <Clock size={15} />
                          Extend +1 Hr
                        </button>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => onOpenPayment(r)}
                        >
                          <FileText size={15} />
                          Receipt
                        </button>
                      </>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Extend Booking Modal */}
      {extendingReservation && (
        <div className="modal-overlay" onClick={() => !extendLoading && setExtendingReservation(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Extend Parking Session</h2>
                <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                  {extendingReservation.lotName} • Bay {extendingReservation.slotNumber}
                </div>
              </div>
              <button className="modal-close" onClick={() => !extendLoading && setExtendingReservation(null)}>
                <X size={20} />
              </button>
            </div>

            {extendError && (
              <div className="alert alert-danger" style={{ marginBottom: '1rem' }}>
                <AlertCircle size={18} />
                <span>{extendError}</span>
              </div>
            )}

            {extendSuccess && (
              <div className="alert alert-success" style={{ marginBottom: '1rem', background: '#ecfdf5', color: '#065f46', border: '1px solid #a7f3d0' }}>
                <CheckCircle size={18} />
                <span>{extendSuccess}</span>
              </div>
            )}

            {!extendSuccess && (
              <div>
                <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '8px', border: '1px solid #e2e8f0', marginBottom: '1.25rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.4rem', fontSize: '0.88rem' }}>
                    <span style={{ color: '#64748b' }}>Current Expiry:</span>
                    <span style={{ fontWeight: 700 }}>
                      {new Date(extendingReservation.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.4rem', fontSize: '0.88rem' }}>
                    <span style={{ color: '#64748b' }}>New Expiry (+{extendHours} hr):</span>
                    <span style={{ fontWeight: 800, color: 'var(--primary)' }}>
                      {new Date(new Date(extendingReservation.endTime).getTime() + extendHours * 3600000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid #e2e8f0', paddingTop: '0.5rem', marginTop: '0.5rem', fontSize: '0.9rem' }}>
                    <span style={{ fontWeight: 600 }}>Additional Fee:</span>
                    <span style={{ fontWeight: 800, color: '#10b981', fontSize: '1.1rem' }}>₹{30 * extendHours}</span>
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Select Extra Duration</label>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.5rem' }}>
                    {[1, 2, 3].map((hr) => (
                      <button
                        key={hr}
                        type="button"
                        className={`btn ${extendHours === hr ? 'btn-primary' : 'btn-secondary'}`}
                        onClick={() => setExtendHours(hr)}
                      >
                        +{hr} Hour{hr > 1 ? 's' : ''}
                      </button>
                    ))}
                  </div>
                </div>

                <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: '8px', padding: '0.75rem', marginBottom: '1.25rem', fontSize: '0.82rem', color: '#166534' }}>
                  🛡️ <strong>ParkSync Buffer Protection:</strong> An additional +1 hour grace buffer will automatically shield your bay from incoming bookings.
                </div>

                <button
                  className="btn btn-primary"
                  style={{ width: '100%', padding: '0.75rem' }}
                  disabled={extendLoading}
                  onClick={handleConfirmExtend}
                >
                  {extendLoading ? 'Verifying & Extending...' : `Confirm Extension (+₹${30 * extendHours})`}
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

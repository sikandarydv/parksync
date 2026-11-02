import React, { useState, useEffect } from 'react';
import { api } from '../api';
import { useAuth } from '../context/AuthContext';
import { X, Car, AlertCircle, Info, Zap } from 'lucide-react';

export function SlotPickerModal({ lot, isOpen, onClose, onBookingSuccess, onOpenAuth }) {
  const { user } = useAuth();

  const now = new Date();
  const defaultStart = new Date(now.getTime() + 10 * 60000);
  const defaultEnd = new Date(defaultStart.getTime() + 2 * 3600000);

  const formatLocalIso = (d) => {
    const pad = (n) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  };

  const [startTime, setStartTime] = useState(formatLocalIso(defaultStart));
  const [endTime, setEndTime] = useState(formatLocalIso(defaultEnd));

  const [slots, setSlots] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [selectedSlot, setSelectedSlot] = useState(null);

  // User vehicles
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState('');
  const [showAddVehicleInline, setShowAddVehicleInline] = useState(false);
  const [newVehNum, setNewVehNum] = useState('');
  const [newVehType, setNewVehType] = useState('CAR');
  const [newVehModel, setNewVehModel] = useState('');

  const [bookingLoading, setBookingLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen && lot) {
      loadAvailability();
      if (user) {
        loadUserVehicles();
      }
    }
  }, [isOpen, lot, startTime, endTime, user]);

  const loadAvailability = async () => {
    if (!lot) return;
    try {
      setLoadingSlots(true);
      setError(null);
      const data = await api.checkAvailability(lot.lotId, startTime + ':00', endTime + ':00');
      setSlots(data);
      if (selectedSlot && !data.find((s) => s.slotId === selectedSlot.slotId && s.available)) {
        setSelectedSlot(null);
      }
    } catch (err) {
      setError(err.message || 'Failed to check slot availability');
    } finally {
      setLoadingSlots(false);
    }
  };

  const loadUserVehicles = async () => {
    try {
      const data = await api.getVehiclesByUser(user.userId);
      setVehicles(data);
      if (data.length > 0 && !selectedVehicleId) {
        setSelectedVehicleId(data[0].vehicleId);
      }
    } catch (err) {
      console.error('Failed to load vehicles:', err);
    }
  };

  const handleRegisterVehicleQuick = async (e) => {
    e.preventDefault();
    if (!newVehNum.trim()) return;
    try {
      const created = await api.registerVehicle({
        userId: user.userId,
        vehicleNumber: newVehNum.trim().toUpperCase(),
        vehicleType: newVehType,
        vehicleModel: newVehModel.trim(),
      });
      setVehicles([...vehicles, created]);
      setSelectedVehicleId(created.vehicleId);
      setShowAddVehicleInline(false);
      setNewVehNum('');
      setNewVehModel('');
    } catch (err) {
      alert('Failed to register vehicle: ' + err.message);
    }
  };

  const startD = new Date(startTime);
  const endD = new Date(endTime);
  const durationMs = endD.getTime() - startD.getTime();
  const durationHours = Math.max(1, Math.ceil(durationMs / 3600000));
  const pricePreview = lot ? (durationHours * lot.pricePerHour).toFixed(2) : '0.00';

  const handleConfirmBooking = async () => {
    if (!user) {
      onOpenAuth();
      return;
    }

    if (!selectedSlot) {
      setError('Please click on an available bay to reserve it.');
      return;
    }

    if (!selectedVehicleId) {
      setError('Please select or register a vehicle for this reservation.');
      return;
    }

    if (durationMs <= 0) {
      setError('End time must be later than start time.');
      return;
    }

    try {
      setBookingLoading(true);
      setError(null);

      const reservation = await api.createReservation({
        userId: user.userId,
        vehicleId: selectedVehicleId,
        slotId: selectedSlot.slotId,
        startTime: startTime + ':00',
        endTime: endTime + ':00',
      });

      onBookingSuccess(reservation);
    } catch (err) {
      setError(err.message || 'Booking failed. Bay may have been reserved by another user.');
      loadAvailability();
    } finally {
      setBookingLoading(false);
    }
  };

  if (!isOpen || !lot) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" style={{ maxWidth: '780px' }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <h2 style={{ fontSize: '1.3rem', fontWeight: 800 }}>Reserve bay: {lot.lotName}</h2>
            <p style={{ color: '#64748b', fontSize: '0.85rem' }}>{lot.address}, {lot.city}</p>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        {error && (
          <div className="alert alert-danger">
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        {/* Time Selector: Single containment layer, no nested card (Hallmark Floor 4) */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', border: '1px solid var(--border-subtle)', borderRadius: '8px', padding: '1rem', marginBottom: '1.25rem' }}>
          <div>
            <label className="form-label">Reservation start</label>
            <input
              type="datetime-local"
              className="form-control"
              value={startTime}
              onChange={(e) => setStartTime(e.target.value)}
            />
          </div>
          <div>
            <label className="form-label">Reservation end</label>
            <input
              type="datetime-local"
              className="form-control"
              value={endTime}
              onChange={(e) => setEndTime(e.target.value)}
            />
          </div>
        </div>

        {/* Legend: Clean sentence case */}
        <div style={{ display: 'flex', gap: '1.25rem', fontSize: '0.82rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <span style={{ width: 12, height: 12, background: 'var(--bay-vacant)', borderRadius: 2 }} />
            <span>Available</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <span style={{ width: 12, height: 12, background: 'var(--bay-reserved)', borderRadius: 2 }} />
            <span>Reserved for this window</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <span style={{ width: 12, height: 12, background: 'var(--bay-maintenance)', borderRadius: 2 }} />
            <span>Maintenance</span>
          </div>
        </div>

        {/* Slots Visual Bay Grid */}
        <div style={{ marginBottom: '1.25rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontWeight: 700, fontSize: '0.88rem' }}>Parking bay layout:</span>
            {loadingSlots && <span style={{ fontSize: '0.78rem', color: '#64748b' }}>Refreshing status...</span>}
          </div>

          {slots.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '2rem', color: '#64748b' }}>
              No bays configured for this facility.
            </div>
          ) : (
            <div className="slots-grid">
              {slots.map((s) => {
                const isSelected = selectedSlot && selectedSlot.slotId === s.slotId;
                const slotClass = !s.available
                  ? s.status === 'MAINTENANCE'
                    ? 'maintenance'
                    : 'reserved'
                  : isSelected
                  ? 'available selected'
                  : 'available';

                return (
                  <div
                    key={s.slotId}
                    className={`slot-card ${slotClass}`}
                    onClick={() => {
                      if (s.available) {
                        setSelectedSlot(s);
                      }
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '2px' }}>
                      {s.slotType === 'EV' && <Zap size={11} color="#0284c7" />}
                      <span className="slot-type-pill">{s.slotType}</span>
                    </div>
                    <Car size={20} color={!s.available ? '#94a3b8' : isSelected ? 'var(--primary)' : 'var(--bay-vacant)'} />
                    <span className="slot-num">{s.slotNumber}</span>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Vehicle Selection: Single-layer border */}
        {user ? (
          <div style={{ marginBottom: '1.25rem', border: '1px solid var(--border-subtle)', padding: '0.85rem 1rem', borderRadius: '8px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem' }}>
              <label className="form-label" style={{ marginBottom: 0 }}>Select vehicle for reservation</label>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => setShowAddVehicleInline(!showAddVehicleInline)}
              >
                {showAddVehicleInline ? 'Cancel' : '+ Add vehicle'}
              </button>
            </div>

            {showAddVehicleInline ? (
              <form onSubmit={handleRegisterVehicleQuick} style={{ marginTop: '0.65rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                <input
                  type="text"
                  required
                  placeholder="Number: KA-01-AB-1234"
                  className="form-control"
                  style={{ flex: 1, minWidth: '150px' }}
                  value={newVehNum}
                  onChange={(e) => setNewVehNum(e.target.value)}
                />
                <select
                  className="form-control"
                  style={{ width: '100px' }}
                  value={newVehType}
                  onChange={(e) => setNewVehType(e.target.value)}
                >
                  <option value="CAR">Car</option>
                  <option value="BIKE">Bike</option>
                  <option value="SUV">SUV</option>
                  <option value="TRUCK">Truck</option>
                </select>
                <input
                  type="text"
                  placeholder="Model (optional)"
                  className="form-control"
                  style={{ flex: 1, minWidth: '120px' }}
                  value={newVehModel}
                  onChange={(e) => setNewVehModel(e.target.value)}
                />
                <button type="submit" className="btn btn-primary btn-sm">Save</button>
              </form>
            ) : vehicles.length === 0 ? (
              <div style={{ color: 'var(--bay-reserved)', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <Info size={16} />
                <span>No registered vehicles found. Click "+ Add vehicle" above.</span>
              </div>
            ) : (
              <select
                className="form-control"
                value={selectedVehicleId}
                onChange={(e) => setSelectedVehicleId(e.target.value)}
              >
                {vehicles.map((v) => (
                  <option key={v.vehicleId} value={v.vehicleId}>
                    {v.vehicleNumber} ({v.vehicleType}{v.vehicleModel ? ` - ${v.vehicleModel}` : ''})
                  </option>
                ))}
              </select>
            )}
          </div>
        ) : (
          <div className="alert alert-info" style={{ marginBottom: '1.25rem' }}>
            <Info size={18} />
            <span>Please sign in or register to complete this parking reservation.</span>
          </div>
        )}

        {/* Booking Summary Footer */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid var(--border-subtle)', paddingTop: '1rem' }}>
          <div>
            <div style={{ fontSize: '0.78rem', color: '#64748b' }}>Estimated total ({durationHours} hrs @ ₹{lot.pricePerHour}/hr)</div>
            <div className="tabular-nums" style={{ fontSize: '1.35rem', fontWeight: 800, color: 'var(--bay-vacant)' }}>
              ₹{pricePreview}
            </div>
          </div>

          <div style={{ display: 'flex', gap: '0.65rem' }}>
            <button className="btn btn-secondary" onClick={onClose}>
              Cancel
            </button>
            <button
              className="btn btn-primary"
              disabled={bookingLoading || !selectedSlot || (user && vehicles.length === 0 && !showAddVehicleInline)}
              onClick={handleConfirmBooking}
            >
              {bookingLoading ? 'Reserving...' : user ? 'Confirm & proceed to payment' : 'Sign in to book'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

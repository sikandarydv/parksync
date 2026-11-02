import React, { useState, useEffect } from 'react';
import { api } from '../api';
import { Shield, Plus, Edit, AlertTriangle, DollarSign, Calendar, MapPin, Grid } from 'lucide-react';

export function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [activeSubTab, setActiveSubTab] = useState('lots');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Lots state
  const [lots, setLots] = useState([]);
  const [showLotModal, setShowLotModal] = useState(false);
  const [editingLot, setEditingLot] = useState(null);
  const [lotForm, setLotForm] = useState({ lotName: '', address: '', city: '', totalSlots: 50, pricePerHour: 30.0 });

  // Slots state
  const [selectedLotId, setSelectedLotId] = useState('');
  const [slots, setSlots] = useState([]);
  const [showSlotModal, setShowSlotModal] = useState(false);
  const [slotForm, setSlotForm] = useState({ slotNumber: '', slotType: 'REGULAR', status: 'AVAILABLE' });

  // Reservations & Payments state
  const [reservations, setReservations] = useState([]);
  const [payments, setPayments] = useState([]);
  const [users, setUsers] = useState([]);

  useEffect(() => {
    loadAllAdminData();
  }, []);

  useEffect(() => {
    if (selectedLotId) {
      loadSlotsForLot(selectedLotId);
    }
  }, [selectedLotId]);

  const loadAllAdminData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [statsData, lotsData, resData, payData, usersData] = await Promise.all([
        api.getAdminStats(),
        api.getParkingLots(false),
        api.getAllReservations(),
        api.getAllPayments(),
        api.getAdminUsers(),
      ]);

      setStats(statsData);
      setLots(lotsData);
      setReservations(resData);
      setPayments(payData);
      setUsers(usersData);

      if (lotsData.length > 0 && !selectedLotId) {
        setSelectedLotId(lotsData[0].lotId);
      }
    } catch (err) {
      setError(err.message || 'Failed to load admin data');
    } finally {
      setLoading(false);
    }
  };

  const loadSlotsForLot = async (lotId) => {
    try {
      const data = await api.getSlotsByLot(lotId, false);
      setSlots(data);
    } catch (err) {
      console.error('Failed to load slots for lot', err);
    }
  };

  const handleSaveLot = async (e) => {
    e.preventDefault();
    try {
      if (editingLot) {
        await api.updateParkingLot(editingLot.lotId, lotForm);
      } else {
        await api.createParkingLot(lotForm);
      }
      setShowLotModal(false);
      setEditingLot(null);
      setLotForm({ lotName: '', address: '', city: '', totalSlots: 50, pricePerHour: 30.0 });
      await loadAllAdminData();
    } catch (err) {
      alert('Error saving lot: ' + err.message);
    }
  };

  const handleDeactivateLot = async (id) => {
    if (!window.confirm('Deactivate this parking lot?')) return;
    try {
      await api.deactivateParkingLot(id);
      await loadAllAdminData();
    } catch (err) {
      alert('Error: ' + err.message);
    }
  };

  const handleSaveSlot = async (e) => {
    e.preventDefault();
    try {
      await api.addSlot({
        lotId: selectedLotId,
        slotNumber: slotForm.slotNumber.trim().toUpperCase(),
        slotType: slotForm.slotType,
        status: slotForm.status,
      });
      setShowSlotModal(false);
      setSlotForm({ slotNumber: '', slotType: 'REGULAR', status: 'AVAILABLE' });
      await loadSlotsForLot(selectedLotId);
      await loadAllAdminData();
    } catch (err) {
      alert('Error creating slot: ' + err.message);
    }
  };

  const handleToggleSlotStatus = async (slotId, currentStatus) => {
    const nextStatus = currentStatus === 'MAINTENANCE' ? 'AVAILABLE' : 'MAINTENANCE';
    try {
      await api.updateSlotStatus(slotId, nextStatus);
      await loadSlotsForLot(selectedLotId);
    } catch (err) {
      alert('Error updating slot: ' + err.message);
    }
  };

  const handleToggleUserActive = async (userId, currentActive) => {
    try {
      await api.toggleUserActive(userId, !currentActive);
      await loadAllAdminData();
    } catch (err) {
      alert('Error updating user: ' + err.message);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <Shield color="#db2777" size={24} />
            Facility Operations Center
          </h1>
          <p style={{ color: '#64748b', fontSize: '0.9rem' }}>Real-time inventory, bay occupancy, transactions, and user access control.</p>
        </div>

        <button className="btn btn-secondary btn-sm" onClick={loadAllAdminData}>
          Refresh metrics
        </button>
      </div>

      {error && (
        <div className="alert alert-danger">
          <AlertTriangle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* KPI Stats Cards (Hallmark: Sentence case, tabular numerals) */}
      {stats && (
        <div className="grid-4" style={{ marginBottom: '1.75rem' }}>
          <div className="card" style={{ padding: '1.15rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: '#64748b', fontSize: '0.8rem', fontWeight: 600 }}>
              <span>Total facilities</span>
              <MapPin size={16} color="var(--primary)" />
            </div>
            <div className="tabular-nums" style={{ fontSize: '1.65rem', fontWeight: 800, marginTop: '0.35rem' }}>
              {stats.totalLots}
            </div>
          </div>

          <div className="card" style={{ padding: '1.15rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: '#64748b', fontSize: '0.8rem', fontWeight: 600 }}>
              <span>Configured bays</span>
              <Grid size={16} color="#0284c7" />
            </div>
            <div className="tabular-nums" style={{ fontSize: '1.65rem', fontWeight: 800, marginTop: '0.35rem' }}>
              {stats.totalSlots}
            </div>
          </div>

          <div className="card" style={{ padding: '1.15rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: '#64748b', fontSize: '0.8rem', fontWeight: 600 }}>
              <span>Active bookings</span>
              <Calendar size={16} color="var(--bay-vacant)" />
            </div>
            <div className="tabular-nums" style={{ fontSize: '1.65rem', fontWeight: 800, color: 'var(--bay-vacant)', marginTop: '0.35rem' }}>
              {stats.activeReservations}
            </div>
          </div>

          <div className="card" style={{ padding: '1.15rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: '#64748b', fontSize: '0.8rem', fontWeight: 600 }}>
              <span>Total revenue</span>
              <DollarSign size={16} color="var(--bay-reserved)" />
            </div>
            <div className="tabular-nums" style={{ fontSize: '1.65rem', fontWeight: 800, marginTop: '0.35rem' }}>
              ₹{Number(stats.totalRevenue || 0).toFixed(2)}
            </div>
          </div>
        </div>
      )}

      {/* Tabs */}
      <div className="tabs-header">
        {[
          { key: 'lots', label: 'Facilities' },
          { key: 'slots', label: 'Bay inventory' },
          { key: 'reservations', label: 'Reservations' },
          { key: 'payments', label: 'Transactions' },
          { key: 'users', label: 'Users' },
        ].map((t) => (
          <button
            key={t.key}
            className={`tab-btn ${activeSubTab === t.key ? 'active' : ''}`}
            onClick={() => setActiveSubTab(t.key)}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* SUB-TAB: LOTS */}
      {activeSubTab === 'lots' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '1rem' }}>
            <button
              className="btn btn-primary btn-sm"
              onClick={() => {
                setEditingLot(null);
                setLotForm({ lotName: '', address: '', city: '', totalSlots: 50, pricePerHour: 30.0 });
                setShowLotModal(true);
              }}
            >
              <Plus size={15} />
              Add parking facility
            </button>
          </div>

          <div className="data-table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Facility name</th>
                  <th>Address & city</th>
                  <th>Capacity</th>
                  <th>Hourly rate</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {lots.map((l) => (
                  <tr key={l.lotId}>
                    <td style={{ fontWeight: 600 }}>#{l.lotId}</td>
                    <td style={{ fontWeight: 700 }}>{l.lotName}</td>
                    <td>{l.address}, {l.city}</td>
                    <td className="tabular-nums">{l.totalSlots} bays</td>
                    <td className="tabular-nums" style={{ fontWeight: 700, color: 'var(--bay-vacant)' }}>₹{l.pricePerHour}</td>
                    <td>
                      <span className={`badge ${l.isActive ? 'badge-available' : 'badge-maintenance'}`}>
                        {l.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.4rem' }}>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => {
                            setEditingLot(l);
                            setLotForm({
                              lotName: l.lotName,
                              address: l.address,
                              city: l.city,
                              totalSlots: l.totalSlots,
                              pricePerHour: l.pricePerHour,
                            });
                            setShowLotModal(true);
                          }}
                        >
                          <Edit size={13} />
                          Edit
                        </button>
                        {l.isActive && (
                          <button
                            className="btn btn-secondary btn-sm"
                            style={{ color: '#dc2626' }}
                            onClick={() => handleDeactivateLot(l.lotId)}
                          >
                            Deactivate
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* SUB-TAB: SLOTS */}
      {activeSubTab === 'slots' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.15rem', flexWrap: 'wrap', gap: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
              <span style={{ fontWeight: 600, fontSize: '0.88rem' }}>Facility:</span>
              <select
                className="form-control"
                style={{ minWidth: '220px' }}
                value={selectedLotId}
                onChange={(e) => setSelectedLotId(e.target.value)}
              >
                {lots.map((l) => (
                  <option key={l.lotId} value={l.lotId}>
                    {l.lotName} ({l.city})
                  </option>
                ))}
              </select>
            </div>

            <button
              className="btn btn-primary btn-sm"
              onClick={() => setShowSlotModal(true)}
              disabled={!selectedLotId}
            >
              <Plus size={15} />
              Add bay to selected facility
            </button>
          </div>

          <div className="data-table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Bay ID</th>
                  <th>Bay identifier</th>
                  <th>Category</th>
                  <th>Current status</th>
                  <th>Active</th>
                  <th>Toggle state</th>
                </tr>
              </thead>
              <tbody>
                {slots.length === 0 ? (
                  <tr>
                    <td colSpan={6} style={{ textAlign: 'center', padding: '2rem', color: '#64748b' }}>
                      No bays configured yet for this parking lot.
                    </td>
                  </tr>
                ) : (
                  slots.map((s) => (
                    <tr key={s.slotId}>
                      <td style={{ fontWeight: 600 }}>#{s.slotId}</td>
                      <td style={{ fontWeight: 800, color: 'var(--primary)' }}>{s.slotNumber}</td>
                      <td>
                        <span className="slot-type-pill">{s.slotType}</span>
                      </td>
                      <td>
                        <span className={`badge ${s.status === 'AVAILABLE' ? 'badge-available' : s.status === 'MAINTENANCE' ? 'badge-maintenance' : 'badge-reserved'}`}>
                          {s.status}
                        </span>
                      </td>
                      <td>{s.isActive ? 'Yes' : 'No'}</td>
                      <td>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => handleToggleSlotStatus(s.slotId, s.status)}
                        >
                          {s.status === 'MAINTENANCE' ? 'Mark available' : 'Mark maintenance'}
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* SUB-TAB: RESERVATIONS */}
      {activeSubTab === 'reservations' && (
        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Booking ID</th>
                <th>Client</th>
                <th>Facility & bay</th>
                <th>Vehicle plate</th>
                <th>Time schedule</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Alert Action</th>
              </tr>
            </thead>
            <tbody>
              {reservations.map((r) => (
                <tr key={r.reservationId}>
                  <td style={{ fontWeight: 600 }}>#{r.reservationId}</td>
                  <td>
                    <div style={{ fontWeight: 700 }}>{r.userName}</div>
                    <div style={{ fontSize: '0.78rem', color: '#64748b' }}>{r.userEmail}</div>
                  </td>
                  <td>
                    <div style={{ fontWeight: 700 }}>{r.lotName}</div>
                    <div style={{ color: 'var(--primary)', fontWeight: 600 }}>Bay {r.slotNumber}</div>
                  </td>
                  <td>{r.vehicleNumber} ({r.vehicleType})</td>
                  <td style={{ fontSize: '0.82rem' }}>
                    {new Date(r.startTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })} →<br />
                    {new Date(r.endTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}
                  </td>
                  <td className="tabular-nums" style={{ fontWeight: 800, color: 'var(--bay-vacant)' }}>₹{r.totalAmount}</td>
                  <td>
                    <span className={`badge ${r.status === 'CONFIRMED' ? 'badge-confirmed' : r.status === 'PENDING' ? 'badge-pending' : 'badge-cancelled'}`}>
                      {r.status}
                    </span>
                  </td>
                  <td>
                    <button
                      className="btn btn-secondary btn-sm"
                      style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem' }}
                      title="Send expiry alert email immediately"
                      onClick={async () => {
                        try {
                          await api.sendTestAlert(r.reservationId);
                          alert(`Expiry alert email sent successfully to ${r.userEmail}!`);
                        } catch (err) {
                          alert('Error sending email: ' + err.message);
                        }
                      }}
                    >
                      Email Alert
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* SUB-TAB: PAYMENTS */}
      {activeSubTab === 'payments' && (
        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Transaction reference</th>
                <th>Booking ref</th>
                <th>Channel</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Recorded time</th>
              </tr>
            </thead>
            <tbody>
              {payments.map((p) => (
                <tr key={p.paymentId}>
                  <td style={{ fontFamily: 'monospace', fontWeight: 600 }}>{p.transactionReference}</td>
                  <td>Booking #{p.reservationId}</td>
                  <td><span className="badge-role user">{p.paymentMethod}</span></td>
                  <td className="tabular-nums" style={{ fontWeight: 800, color: 'var(--bay-vacant)' }}>₹{p.amount}</td>
                  <td>
                    <span className={`badge ${p.paymentStatus === 'SUCCESS' ? 'badge-confirmed' : 'badge-cancelled'}`}>
                      {p.paymentStatus}
                    </span>
                  </td>
                  <td style={{ fontSize: '0.82rem' }}>{new Date(p.paymentTime).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* SUB-TAB: USERS */}
      {activeSubTab === 'users' && (
        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>User ID</th>
                <th>Full name</th>
                <th>Email address</th>
                <th>Phone</th>
                <th>Role</th>
                <th>Account status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.userId}>
                  <td>#{u.userId}</td>
                  <td style={{ fontWeight: 700 }}>{u.fullName}</td>
                  <td>{u.email}</td>
                  <td>{u.phone || '—'}</td>
                  <td>
                    <span className={`badge-role ${u.role === 'ROLE_ADMIN' ? 'admin' : 'user'}`}>
                      {u.role}
                    </span>
                  </td>
                  <td>
                    <span className={`badge ${u.isActive ? 'badge-available' : 'badge-maintenance'}`}>
                      {u.isActive ? 'Active' : 'Suspended'}
                    </span>
                  </td>
                  <td>
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() => handleToggleUserActive(u.userId, u.isActive)}
                    >
                      {u.isActive ? 'Suspend' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Add / Edit Lot Modal */}
      {showLotModal && (
        <div className="modal-overlay" onClick={() => setShowLotModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>
                {editingLot ? 'Edit parking facility' : 'Add new parking facility'}
              </h2>
              <button className="modal-close" onClick={() => setShowLotModal(false)}>×</button>
            </div>

            <form onSubmit={handleSaveLot}>
              <div className="form-group">
                <label className="form-label">Facility name *</label>
                <input
                  type="text"
                  required
                  className="form-control"
                  placeholder="e.g. West Campus Multi-Level Parking"
                  value={lotForm.lotName}
                  onChange={(e) => setLotForm({ ...lotForm, lotName: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Address *</label>
                <input
                  type="text"
                  required
                  className="form-control"
                  placeholder="Street / Landmark"
                  value={lotForm.address}
                  onChange={(e) => setLotForm({ ...lotForm, address: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label className="form-label">City *</label>
                <input
                  type="text"
                  required
                  className="form-control"
                  placeholder="e.g. Vellore"
                  value={lotForm.city}
                  onChange={(e) => setLotForm({ ...lotForm, city: e.target.value })}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">Capacity (bays) *</label>
                  <input
                    type="number"
                    min="1"
                    required
                    className="form-control"
                    value={lotForm.totalSlots}
                    onChange={(e) => setLotForm({ ...lotForm, totalSlots: parseInt(e.target.value, 10) })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Hourly rate (₹) *</label>
                  <input
                    type="number"
                    step="0.5"
                    min="0"
                    required
                    className="form-control"
                    value={lotForm.pricePerHour}
                    onChange={(e) => setLotForm({ ...lotForm, pricePerHour: parseFloat(e.target.value) })}
                  />
                </div>
              </div>

              <div style={{ display: 'flex', gap: '0.65rem', marginTop: '1.25rem' }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  style={{ flex: 1 }}
                  onClick={() => setShowLotModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>
                  Save facility
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add Slot Modal */}
      {showSlotModal && (
        <div className="modal-overlay" onClick={() => setShowSlotModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Add bay to facility</h2>
              <button className="modal-close" onClick={() => setShowSlotModal(false)}>×</button>
            </div>

            <form onSubmit={handleSaveSlot}>
              <div className="form-group">
                <label className="form-label">Bay identifier *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. C-12, VIP-04, EV-01"
                  className="form-control"
                  value={slotForm.slotNumber}
                  onChange={(e) => setSlotForm({ ...slotForm, slotNumber: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Category</label>
                <select
                  className="form-control"
                  value={slotForm.slotType}
                  onChange={(e) => setSlotForm({ ...slotForm, slotType: e.target.value })}
                >
                  <option value="REGULAR">Regular bay</option>
                  <option value="VIP">VIP reserve</option>
                  <option value="EV">EV charging</option>
                  <option value="HANDICAPPED">Accessible</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Initial status</label>
                <select
                  className="form-control"
                  value={slotForm.status}
                  onChange={(e) => setSlotForm({ ...slotForm, status: e.target.value })}
                >
                  <option value="AVAILABLE">Available</option>
                  <option value="MAINTENANCE">Maintenance</option>
                </select>
              </div>

              <div style={{ display: 'flex', gap: '0.65rem', marginTop: '1.25rem' }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  style={{ flex: 1 }}
                  onClick={() => setShowSlotModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>
                  Create bay
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

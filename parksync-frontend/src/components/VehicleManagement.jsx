import React, { useState, useEffect } from 'react';
import { api } from '../api';
import { useAuth } from '../context/AuthContext';
import { Car, Plus, Trash2, AlertCircle, CheckCircle, Shield } from 'lucide-react';

export function VehicleManagement() {
  const { user } = useAuth();
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Form states
  const [showAddModal, setShowAddModal] = useState(false);
  const [vehicleNumber, setVehicleNumber] = useState('');
  const [vehicleType, setVehicleType] = useState('CAR');
  const [vehicleModel, setVehicleModel] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (user) {
      loadVehicles();
    }
  }, [user]);

  const loadVehicles = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.getVehiclesByUser(user.userId, false);
      setVehicles(data);
    } catch (err) {
      setError(err.message || 'Failed to load vehicles');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!vehicleNumber.trim()) return;

    try {
      setSubmitting(true);
      setError(null);

      await api.registerVehicle({
        userId: user.userId,
        vehicleNumber: vehicleNumber.trim().toUpperCase(),
        vehicleType,
        vehicleModel: vehicleModel.trim(),
      });

      setShowAddModal(false);
      setVehicleNumber('');
      setVehicleModel('');
      await loadVehicles();
    } catch (err) {
      setError(err.message || 'Failed to register vehicle');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeactivate = async (vehicleId) => {
    if (!window.confirm('Are you sure you want to remove this vehicle?')) return;
    try {
      await api.deactivateVehicle(vehicleId);
      await loadVehicles();
    } catch (err) {
      alert('Failed to remove vehicle: ' + err.message);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', fontWeight: 800 }}>Registered Vehicles</h1>
          <p style={{ color: '#64748b' }}>Manage your vehicles for quick, one-click parking reservations.</p>
        </div>

        <button className="btn btn-primary" onClick={() => setShowAddModal(true)}>
          <Plus size={18} />
          Register New Vehicle
        </button>
      </div>

      {error && (
        <div className="alert alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem', color: '#64748b' }}>
          Loading your vehicles...
        </div>
      ) : vehicles.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3.5rem' }}>
          <Car size={48} color="#94a3b8" style={{ margin: '0 auto 1rem auto' }} />
          <h3 style={{ fontSize: '1.2rem', fontWeight: 700 }}>No vehicles registered yet</h3>
          <p style={{ color: '#64748b', marginTop: '0.4rem', marginBottom: '1.5rem' }}>
            Add your car, bike, or SUV license plate to start reserving slots.
          </p>
          <button className="btn btn-primary" onClick={() => setShowAddModal(true)}>
            <Plus size={16} />
            Register Vehicle
          </button>
        </div>
      ) : (
        <div className="grid-3">
          {vehicles.map((v) => (
            <div key={v.vehicleId} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <span className="badge-role user" style={{ fontSize: '0.8rem' }}>
                  {v.vehicleType}
                </span>
                <span className={`badge ${v.isActive ? 'badge-available' : 'badge-maintenance'}`}>
                  {v.isActive ? 'Active' : 'Archived'}
                </span>
              </div>

              <div style={{ fontSize: '1.4rem', fontWeight: 800, color: '#0f172a', letterSpacing: '0.5px' }}>
                {v.vehicleNumber}
              </div>

              <div style={{ color: '#64748b', fontSize: '0.9rem', marginTop: '0.25rem', marginBottom: '1.5rem' }}>
                {v.vehicleModel || 'Standard Model'}
              </div>

              <div style={{ marginTop: 'auto', display: 'flex', justifyContent: 'flex-end' }}>
                {v.isActive && (
                  <button
                    className="btn btn-secondary btn-sm"
                    style={{ color: '#ef4444', borderColor: '#fecaca' }}
                    onClick={() => handleDeactivate(v.vehicleId)}
                  >
                    <Trash2 size={15} />
                    Remove
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add Vehicle Modal */}
      {showAddModal && (
        <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Register Vehicle</h2>
              <button className="modal-close" onClick={() => setShowAddModal(false)}>×</button>
            </div>

            <form onSubmit={handleRegister}>
              <div className="form-group">
                <label className="form-label">License Plate / Vehicle Number *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. TN-01-AB-1234"
                  className="form-control"
                  value={vehicleNumber}
                  onChange={(e) => setVehicleNumber(e.target.value.toUpperCase())}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Vehicle Category *</label>
                <select
                  className="form-control"
                  value={vehicleType}
                  onChange={(e) => setVehicleType(e.target.value)}
                >
                  <option value="CAR">Car</option>
                  <option value="BIKE">Bike / Motorcycle</option>
                  <option value="SUV">SUV</option>
                  <option value="TRUCK">Truck</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Vehicle Make / Model (Optional)</label>
                <input
                  type="text"
                  placeholder="e.g. Hyundai Creta, Honda City"
                  className="form-control"
                  value={vehicleModel}
                  onChange={(e) => setVehicleModel(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  style={{ flex: 1 }}
                  onClick={() => setShowAddModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  style={{ flex: 1 }}
                  disabled={submitting}
                >
                  {submitting ? 'Registering...' : 'Save Vehicle'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

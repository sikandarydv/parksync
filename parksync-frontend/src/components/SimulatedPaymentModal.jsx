import React, { useState } from 'react';
import { api } from '../api';
import confetti from 'canvas-confetti';
import { X, CreditCard, Smartphone, CheckCircle, AlertTriangle, Building2, Banknote } from 'lucide-react';

export function SimulatedPaymentModal({ reservation, isOpen, onClose, onPaymentSuccess }) {
  const [method, setMethod] = useState('UPI');
  const [simulateFailure, setSimulateFailure] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [paymentResult, setPaymentResult] = useState(null);

  if (!isOpen || !reservation) return null;

  const handlePay = async () => {
    try {
      setLoading(true);
      setError(null);

      const res = await api.processPayment({
        reservationId: reservation.reservationId,
        paymentMethod: method,
        simulateFailure: simulateFailure,
      });

      setPaymentResult(res);

      if (res.paymentStatus === 'SUCCESS') {
        confetti({
          particleCount: 80,
          spread: 60,
          origin: { y: 0.6 },
        });
        if (onPaymentSuccess) {
          onPaymentSuccess(res);
        }
      } else {
        setError('Payment simulation resulted in failed state as requested.');
      }
    } catch (err) {
      setError(err.message || 'Payment processing failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>
              {paymentResult?.paymentStatus === 'SUCCESS' ? 'Booking Confirmed' : 'Payment Checkout'}
            </h2>
            <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginTop: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
              <span className="badge badge-available" style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem' }}>Instant Confirmation</span>
              <span>Encrypted & secure payment</span>
            </div>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        {error && (
          <div className="alert alert-danger">
            <AlertTriangle size={18} />
            <span>{error}</span>
          </div>
        )}

        {paymentResult?.paymentStatus === 'SUCCESS' ? (
          <div>
            <div style={{ textAlign: 'center', margin: '1.5rem 0' }}>
              <CheckCircle size={52} color="var(--bay-vacant)" style={{ margin: '0 auto 0.5rem auto' }} />
              <h3 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Payment recorded</h3>
              <p style={{ color: '#64748b', fontSize: '0.88rem' }}>
                Your parking bay is confirmed. Please display your registration plate at the gate.
              </p>
            </div>

            <div className="receipt-card">
              <div className="receipt-row">
                <span style={{ color: '#64748b' }}>Transaction ID</span>
                <span style={{ fontWeight: 700, fontFamily: 'monospace' }}>{paymentResult.transactionReference}</span>
              </div>
              <div className="receipt-row">
                <span style={{ color: '#64748b' }}>Facility</span>
                <span style={{ fontWeight: 700 }}>{reservation.lotName}</span>
              </div>
              <div className="receipt-row">
                <span style={{ color: '#64748b' }}>Assigned bay</span>
                <span style={{ fontWeight: 700 }}>Bay {reservation.slotNumber}</span>
              </div>
              <div className="receipt-row">
                <span style={{ color: '#64748b' }}>Vehicle</span>
                <span style={{ fontWeight: 700 }}>{reservation.vehicleNumber}</span>
              </div>
              <div className="receipt-row">
                <span style={{ color: '#64748b' }}>Time window</span>
                <span style={{ fontWeight: 600 }}>
                  {new Date(reservation.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} –{' '}
                  {new Date(reservation.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </span>
              </div>
              <div className="receipt-divider" />
              <div className="receipt-row" style={{ fontSize: '1.05rem' }}>
                <span style={{ fontWeight: 800 }}>Total amount</span>
                <span className="tabular-nums" style={{ fontWeight: 800, color: 'var(--bay-vacant)' }}>₹{reservation.totalAmount}</span>
              </div>
            </div>

            <button
              className="btn btn-primary"
              style={{ width: '100%', marginTop: '0.75rem' }}
              onClick={onClose}
            >
              Done & view my bookings
            </button>
          </div>
        ) : (
          <div>
            {/* Single-layer reservation summary (No card inside card) */}
            <div style={{ border: '1px solid var(--border-subtle)', borderRadius: '8px', padding: '0.85rem 1rem', marginBottom: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.35rem', fontSize: '0.88rem' }}>
                <span style={{ color: '#64748b' }}>Facility:</span>
                <span style={{ fontWeight: 600 }}>{reservation.lotName}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.35rem', fontSize: '0.88rem' }}>
                <span style={{ color: '#64748b' }}>Assigned bay:</span>
                <span style={{ fontWeight: 700, color: 'var(--primary)' }}>Bay {reservation.slotNumber}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.35rem', fontSize: '0.88rem' }}>
                <span style={{ color: '#64748b' }}>Vehicle plate:</span>
                <span style={{ fontWeight: 600 }}>{reservation.vehicleNumber}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid var(--border-subtle)', paddingTop: '0.5rem', marginTop: '0.5rem' }}>
                <span style={{ fontWeight: 700 }}>Total due:</span>
                <span className="tabular-nums" style={{ fontWeight: 800, fontSize: '1.15rem', color: 'var(--bay-vacant)' }}>₹{reservation.totalAmount}</span>
              </div>
            </div>

            {/* Payment Method Selector (Hallmark: Crisp vector icons, no emojis) */}
            <div className="form-group">
              <label className="form-label">Payment method</label>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '0.65rem' }}>
                <button
                  type="button"
                  className={`btn ${method === 'UPI' ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setMethod('UPI')}
                  style={{ justifyContent: 'flex-start' }}
                >
                  <Smartphone size={17} />
                  <span>UPI / QR</span>
                </button>

                <button
                  type="button"
                  className={`btn ${method === 'CREDIT_CARD' ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setMethod('CREDIT_CARD')}
                  style={{ justifyContent: 'flex-start' }}
                >
                  <CreditCard size={17} />
                  <span>Card</span>
                </button>

                <button
                  type="button"
                  className={`btn ${method === 'NET_BANKING' ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setMethod('NET_BANKING')}
                  style={{ justifyContent: 'flex-start' }}
                >
                  <Building2 size={17} />
                  <span>Net banking</span>
                </button>

                <button
                  type="button"
                  className={`btn ${method === 'CASH' ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setMethod('CASH')}
                  style={{ justifyContent: 'flex-start' }}
                >
                  <Banknote size={17} />
                  <span>Pay at counter</span>
                </button>
              </div>
            </div>


            <button
              className="btn btn-primary"
              style={{ width: '100%', padding: '0.75rem' }}
              disabled={loading}
              onClick={handlePay}
            >
              {loading ? 'Processing transaction...' : `Pay ₹${reservation.totalAmount} & confirm reservation`}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

import React, { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { AuthModal } from './components/AuthModal';
import { BrowseLots } from './components/BrowseLots';
import { SlotPickerModal } from './components/SlotPickerModal';
import { SimulatedPaymentModal } from './components/SimulatedPaymentModal';
import { MyReservations } from './components/MyReservations';
import { VehicleManagement } from './components/VehicleManagement';
import { AdminDashboard } from './components/AdminDashboard';

function MainApp() {
  const { user, isAdmin } = useAuth();
  const [activeTab, setActiveTab] = useState('browse');
  const [authModalOpen, setAuthModalOpen] = useState(false);

  // Slot booking modal
  const [selectedLotForBooking, setSelectedLotForBooking] = useState(null);

  // Payment modal
  const [pendingPaymentReservation, setPendingPaymentReservation] = useState(null);

  const handleBookingSuccess = (reservation) => {
    setSelectedLotForBooking(null);
    setPendingPaymentReservation(reservation);
  };

  const handlePaymentSuccess = () => {
    // Navigate to reservations tab on confirmed payment
    setActiveTab('reservations');
  };

  return (
    <div className="app-container">
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onOpenAuth={() => setAuthModalOpen(true)}
      />

      <main className="main-content">
        <div className="content-wrapper">
          {activeTab === 'browse' && (
            <BrowseLots
              onSelectLot={(lot) => setSelectedLotForBooking(lot)}
            />
          )}

          {activeTab === 'reservations' && (
            <MyReservations
              onOpenPayment={(res) => setPendingPaymentReservation(res)}
            />
          )}

          {activeTab === 'vehicles' && <VehicleManagement />}

          {activeTab === 'admin' && isAdmin && <AdminDashboard />}
        </div>
      </main>

      <footer style={{ background: '#ffffff', borderTop: '1px solid #e2e8f0', padding: '1.75rem 0', textAlign: 'center', color: '#64748b', fontSize: '0.88rem' }}>
        <div className="content-wrapper" style={{ padding: 0 }}>
          <div style={{ fontWeight: 700, color: '#1e293b', marginBottom: '0.25rem' }}>
            ParkSync – Smart Parking Management and Reservation System
          </div>
          <div>Enterprise Parking & Lot Allocation Platform • Seamless Digital Reservations</div>
        </div>
      </footer>

      {/* Modals */}
      <AuthModal
        isOpen={authModalOpen}
        onClose={() => setAuthModalOpen(false)}
      />

      <SlotPickerModal
        lot={selectedLotForBooking}
        isOpen={!!selectedLotForBooking}
        onClose={() => setSelectedLotForBooking(null)}
        onBookingSuccess={handleBookingSuccess}
        onOpenAuth={() => {
          setSelectedLotForBooking(null);
          setAuthModalOpen(true);
        }}
      />

      <SimulatedPaymentModal
        reservation={pendingPaymentReservation}
        isOpen={!!pendingPaymentReservation}
        onClose={() => setPendingPaymentReservation(null)}
        onPaymentSuccess={handlePaymentSuccess}
      />
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <MainApp />
    </AuthProvider>
  );
}

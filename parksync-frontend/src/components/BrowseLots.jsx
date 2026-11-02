import React, { useState, useEffect } from 'react';
import { api } from '../api';
import { MapPin, Search, ArrowRight, ShieldCheck, Check, Car, Zap, Navigation, Clock, CreditCard, ChevronRight } from 'lucide-react';

export function BrowseLots({ onSelectLot }) {
  const [lots, setLots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [cityFilter, setCityFilter] = useState('');

  useEffect(() => {
    loadLots();
  }, []);

  const loadLots = async () => {
    try {
      setLoading(true);
      const data = await api.getParkingLots(false);
      setLots(data);
    } catch (err) {
      console.error('Failed to load parking lots:', err);
    } finally {
      setLoading(false);
    }
  };

  const scrollToLocator = () => {
    const el = document.getElementById('parking-locator-section');
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' });
    }
  };

  const filteredLots = lots.filter((lot) => {
    const matchesSearch =
      lot.lotName.toLowerCase().includes(search.toLowerCase()) ||
      lot.address.toLowerCase().includes(search.toLowerCase());
    const matchesCity = cityFilter === '' || lot.city.toLowerCase() === cityFilter.toLowerCase();
    return matchesSearch && matchesCity;
  });

  const cities = [...new Set(lots.map((l) => l.city))];

  return (
    <div>
      {/* =========================================================================
          1. HERO SECTION (Dark Green / Black with Road & Telemetry Visual)
          ========================================================================= */}
      <section className="hero-stage">
        <h1 className="hero-headline">Parking just got a<br />lot simpler</h1>
        <p className="hero-subhead">
          The smart parking app integrated with real-time bay sensors to make finding parking spots quick and easy with competitive pricing.
        </p>

        <button className="btn btn-primary" onClick={scrollToLocator} style={{ padding: '0.85rem 2rem', fontSize: '1rem' }}>
          <span>Find Parking Now</span>
          <ArrowRight size={18} />
        </button>

        {/* Telemetry Visual Strip mimicking the reference design */}
        <div className="hero-visual-board">
          <div className="radar-beacon">
            <span className="radar-dot" />
            <span>VIT Main Facility • Active</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <Car size={26} color="#10b981" />
            <div style={{ width: 60, height: 2, background: 'rgba(16, 185, 129, 0.4)' }} />
            <Navigation size={22} color="#ffffff" />
          </div>
          <div className="radar-beacon">
            <span className="radar-dot" />
            <span>Zero Double-Booking Guarantee</span>
          </div>
        </div>
      </section>

      {/* =========================================================================
          2. METRIC & HIGHLIGHT BAR (Deep Forest Green Banner + Crisp White Cards)
          ========================================================================= */}
      <section className="metric-row">
        {/* Left Dark Green Highlight Banner */}
        <div className="metric-banner-darkgreen">
          <div className="metric-banner-text">
            There are systems that offer nearby listings — ParkSync guarantees zero double-bookings with transactional locks.
          </div>
        </div>

        {/* Middle White Metric Card */}
        <div className="metric-card-white">
          <div className="metric-card-num tabular-nums">100%</div>
          <div className="metric-card-desc">
            Concurrency safe guarantee based on database isolation & pessimistic write locks.
          </div>
        </div>

        {/* Right White Metric Card */}
        <div className="metric-card-white">
          <div className="metric-card-num tabular-nums">12+</div>
          <div className="metric-card-desc">
            Configured live parking bays at VIT Main Parking & municipal campus facilities.
          </div>
        </div>
      </section>

      {/* =========================================================================
          3. FEATURE SHOWCASE 1: "The solution to your parking problems"
          ========================================================================= */}
      <section className="feature-showcase-section">
        <div>
          <span className="feature-tag">Real-Time Platform</span>
          <h2 className="feature-title">The solution to your parking problems</h2>
          <p className="feature-lead">
            We understand that finding a parking space can be a challenge for every driver. Our app addresses the problem with these three essential services:
          </p>

          <ul className="feature-checklist">
            <li className="checklist-item">
              <span className="check-icon-circle"><Check size={15} /></span>
              <span>Well-organized facility & bay information</span>
            </li>
            <li className="checklist-item">
              <span className="check-icon-circle"><Check size={15} /></span>
              <span>Live slot availability checks by date and time interval</span>
            </li>
            <li className="checklist-item">
              <span className="check-icon-circle"><Check size={15} /></span>
              <span>Direct vehicle license plate integration</span>
            </li>
          </ul>

          <button className="btn btn-dark" onClick={scrollToLocator}>
            Explore Facilities
          </button>
        </div>

        {/* Smartphone Device Mockup with Parking Map / Bays UI */}
        <div className="device-container">
          <div className="device-backing-aura" />
          <div className="phone-mockup">
            <div className="phone-screen">
              <div className="phone-island" />
              <div className="phone-content">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                  <div>
                    <div style={{ fontWeight: 800, fontSize: '0.85rem' }}>ParkSync Live</div>
                    <div style={{ color: '#64748b', fontSize: '0.65rem' }}>Vellore Campus Hub</div>
                  </div>
                  <span className="badge badge-available" style={{ fontSize: '0.65rem' }}>Open</span>
                </div>

                {/* Simulated Mini Map Grid */}
                <div style={{ background: '#090e0b', borderRadius: '12px', padding: '0.75rem', color: '#ffffff', marginBottom: '0.75rem' }}>
                  <div style={{ fontSize: '0.7rem', color: '#10b981', fontWeight: 700, marginBottom: '0.4rem' }}>
                    VIT Main Parking
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '4px' }}>
                    {['A-1', 'A-2', 'A-3', 'A-4', 'VIP-1', 'VIP-2', 'EV-1', 'EV-2'].map((slot, i) => (
                      <div
                        key={slot}
                        style={{
                          background: i === 1 ? '#d97706' : '#064e3b',
                          border: '1px solid rgba(16, 185, 129, 0.4)',
                          borderRadius: '4px',
                          padding: '4px 2px',
                          textAlign: 'center',
                          fontSize: '0.6rem',
                          fontWeight: 700,
                          color: '#ffffff',
                        }}
                      >
                        {slot}
                      </div>
                    ))}
                  </div>
                  <div style={{ fontSize: '0.65rem', color: '#a3b8ad', marginTop: '0.5rem', textAlign: 'center' }}>
                    8 Bays Free • Rate: ₹30/hr
                  </div>
                </div>

                <div style={{ background: '#f8faf9', border: '1px solid #e2e8e5', borderRadius: '10px', padding: '0.6rem' }}>
                  <div style={{ fontWeight: 700, fontSize: '0.7rem', marginBottom: '0.2rem' }}>Selected Slot: A-1</div>
                  <div style={{ color: '#526059', fontSize: '0.65rem' }}>Duration: 2 hrs • Amount: ₹60.00</div>
                  <div style={{ color: '#059669', fontSize: '0.65rem', fontWeight: 700, marginTop: '0.25rem' }}>
                    ✓ Ready to Reserve
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* =========================================================================
          4. FEATURE SHOWCASE 2: "The solution to your parking challenges" (Inverted)
          ========================================================================= */}
      <section className="feature-showcase-section">
        {/* Smartphone Device Mockup with Vehicle and Receipt UI */}
        <div className="device-container">
          <div className="device-backing-aura" style={{ background: '#f0fdf4' }} />
          <div className="phone-mockup">
            <div className="phone-screen">
              <div className="phone-island" />
              <div className="phone-content">
                <div style={{ fontWeight: 800, fontSize: '0.85rem', marginBottom: '0.5rem' }}>Booking Checkout</div>
                
                <div style={{ background: '#f8faf9', border: '1px solid #e2e8e5', borderRadius: '10px', padding: '0.75rem', marginBottom: '0.6rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.65rem', color: '#526059' }}>
                    <span>Plate:</span>
                    <span style={{ fontWeight: 700, color: '#060b08' }}>TN-01-AB-1234</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.65rem', color: '#526059', marginTop: '0.2rem' }}>
                    <span>Bay:</span>
                    <span style={{ fontWeight: 700, color: '#064e3b' }}>Slot A-1</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', fontWeight: 800, marginTop: '0.4rem', borderTop: '1px solid #e2e8e5', paddingTop: '0.4rem' }}>
                    <span>Total:</span>
                    <span style={{ color: '#059669' }}>₹60.00</span>
                  </div>
                </div>

                <div style={{ background: '#064e3b', color: '#ffffff', borderRadius: '8px', padding: '0.6rem', textAlign: 'center', fontSize: '0.7rem', fontWeight: 700, marginBottom: '0.5rem' }}>
                  Instant Simulated UPI Payment
                </div>

                <div style={{ background: '#ecfdf5', border: '1px solid #a7f3d0', borderRadius: '8px', padding: '0.6rem', fontSize: '0.65rem', color: '#065f46' }}>
                  <div style={{ fontWeight: 700 }}>TXN-94B82F19</div>
                  <div>Status: Confirmed & Paid</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div>
          <span className="feature-tag">Smart Management</span>
          <h2 className="feature-title">The solution to your parking challenges</h2>
          <p className="feature-lead">
            We believe that finding a parking spot can be tough for a lot of people. Our application resolves this issue with these three key features:
          </p>

          <ul className="feature-checklist">
            <li className="checklist-item">
              <span className="check-icon-circle"><Check size={15} /></span>
              <span>Structured and clear bay status indicators</span>
            </li>
            <li className="checklist-item">
              <span className="check-icon-circle"><Check size={15} /></span>
              <span>Instant simulated checkout with digital receipt references</span>
            </li>
            <li className="checklist-item">
              <span className="check-icon-circle"><Check size={15} /></span>
              <span>Full cancellation support immediately restoring bay availability</span>
            </li>
          </ul>

          <button className="btn btn-dark" onClick={scrollToLocator}>
            Learn More & Reserve
          </button>
        </div>
      </section>

      {/* =========================================================================
          5. INTERACTIVE LOCATOR & PARKING LOTS SECTION ("Easy way to find parking...")
          ========================================================================= */}
      <section id="parking-locator-section" style={{ paddingTop: '2rem' }}>
        <div className="locator-section-header">
          <h2 className="locator-title">Easy way to find parking location here</h2>
          <p className="locator-subtitle">
            Browse available facilities, check hourly rates, and reserve your specific bay.
          </p>
        </div>

        {/* Filter / Search Bar */}
        <div className="search-bar-box">
          <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
            <div style={{ flex: 1, minWidth: '240px', position: 'relative' }}>
              <Search
                size={18}
                style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#7e8e86' }}
              />
              <input
                type="text"
                className="form-control"
                style={{ paddingLeft: '2.5rem' }}
                placeholder="Search by facility name or address..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>

            <div style={{ minWidth: '180px' }}>
              <select
                className="form-control"
                value={cityFilter}
                onChange={(e) => setCityFilter(e.target.value)}
              >
                <option value="">All cities</option>
                {cities.map((city) => (
                  <option key={city} value={city}>{city}</option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {/* Facilities Grid */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem', color: '#526059' }}>
            Checking parking locations...
          </div>
        ) : filteredLots.length === 0 ? (
          <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
            <p style={{ color: '#526059', fontSize: '1rem' }}>No parking locations found matching your criteria.</p>
          </div>
        ) : (
          <div className="grid-3">
            {filteredLots.map((lot) => (
              <div key={lot.lotId} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
                  <div>
                    <h3 style={{ fontSize: '1.2rem', fontWeight: 800, color: 'var(--black)' }}>{lot.lotName}</h3>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: '#526059', fontSize: '0.85rem', marginTop: '0.25rem' }}>
                      <MapPin size={15} color="var(--primary)" />
                      <span>{lot.address}, {lot.city}</span>
                    </div>
                  </div>
                  <span className={`badge ${lot.isActive ? 'badge-available' : 'badge-maintenance'}`}>
                    {lot.isActive ? 'Open Now' : 'Closed'}
                  </span>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid var(--border-subtle)', paddingTop: '0.85rem', marginTop: '1rem', marginBottom: '1.25rem' }}>
                  <div>
                    <span style={{ fontSize: '0.78rem', color: '#526059', fontWeight: 600 }}>Capacity</span>
                    <div className="tabular-nums" style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--black)' }}>
                      {lot.totalSlots} bays
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <span style={{ fontSize: '0.78rem', color: '#526059', fontWeight: 600 }}>Hourly rate</span>
                    <div className="tabular-nums" style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--primary)' }}>
                      ₹{lot.pricePerHour}/hr
                    </div>
                  </div>
                </div>

                <div style={{ marginTop: 'auto' }}>
                  <button
                    className="btn btn-dark"
                    style={{ width: '100%' }}
                    onClick={() => onSelectLot(lot)}
                    disabled={!lot.isActive}
                  >
                    <span>Select bay & reserve</span>
                    <ArrowRight size={16} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

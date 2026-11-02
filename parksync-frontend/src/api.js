const API_BASE_URL = 'http://localhost:8080/api';

async function request(endpoint, options = {}) {
  const token = localStorage.getItem('parksync_token');
  
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const config = {
    ...options,
    headers,
  };

  const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
  
  // Handle empty responses like 204 or void
  const contentType = response.headers.get('content-type');
  const isJson = contentType && contentType.includes('application/json');
  const data = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    const errorMessage = (typeof data === 'object' && data.message) 
      ? data.message 
      : (typeof data === 'string' && data ? data : `Request failed with status ${response.status}`);
    
    const error = new Error(errorMessage);
    error.status = response.status;
    error.data = data;
    throw error;
  }

  return data;
}

export const api = {
  // Auth
  login: (credentials) => request('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }),
  register: (userData) => request('/auth/register', { method: 'POST', body: JSON.stringify(userData) }),
  getCurrentUser: () => request('/auth/me'),
  getProfile: (userId) => request(`/auth/profile/${userId}`),
  updateProfile: (userId, data) => request(`/auth/profile/${userId}`, { method: 'PUT', body: JSON.stringify(data) }),

  // Parking Lots
  getParkingLots: (activeOnly = false) => request(`/parking-lots${activeOnly ? '?activeOnly=true' : ''}`),
  getParkingLotById: (id) => request(`/parking-lots/${id}`),
  createParkingLot: (lot) => request('/parking-lots', { method: 'POST', body: JSON.stringify(lot) }),
  updateParkingLot: (id, lot) => request(`/parking-lots/${id}`, { method: 'PUT', body: JSON.stringify(lot) }),
  deactivateParkingLot: (id) => request(`/parking-lots/${id}/deactivate`, { method: 'PATCH' }),

  // Parking Slots
  getSlotsByLot: (lotId, activeOnly = false) => request(`/slots/lot/${lotId}${activeOnly ? '?activeOnly=true' : ''}`),
  getSlotById: (slotId) => request(`/slots/${slotId}`),
  addSlot: (slotData) => request('/slots', { method: 'POST', body: JSON.stringify(slotData) }),
  updateSlot: (slotId, slotData) => request(`/slots/${slotId}`, { method: 'PUT', body: JSON.stringify(slotData) }),
  updateSlotStatus: (slotId, status) => request(`/slots/${slotId}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),
  deactivateSlot: (slotId) => request(`/slots/${slotId}/deactivate`, { method: 'PATCH' }),

  // Availability
  checkAvailability: (lotId, startTime, endTime) => {
    let url = `/reservations/availability?lotId=${lotId}`;
    if (startTime && endTime) {
      url += `&startTime=${encodeURIComponent(startTime)}&endTime=${encodeURIComponent(endTime)}`;
    }
    return request(url);
  },

  // Vehicles
  getVehiclesByUser: (userId, activeOnly = true) => request(`/vehicles/user/${userId}${activeOnly ? '?activeOnly=true' : ''}`),
  registerVehicle: (vehicleData) => request('/vehicles', { method: 'POST', body: JSON.stringify(vehicleData) }),
  updateVehicle: (vehicleId, vehicleData) => request(`/vehicles/${vehicleId}`, { method: 'PUT', body: JSON.stringify(vehicleData) }),
  deactivateVehicle: (vehicleId) => request(`/vehicles/${vehicleId}/deactivate`, { method: 'PATCH' }),

  // Reservations
  createReservation: (reservationData) => request('/reservations', { method: 'POST', body: JSON.stringify(reservationData) }),
  getReservationById: (id) => request(`/reservations/${id}`),
  getUserReservations: (userId) => request(`/reservations/user/${userId}`),
  cancelReservation: (id) => request(`/reservations/${id}/cancel`, { method: 'PATCH' }),
  completeReservation: (id) => request(`/reservations/${id}/complete`, { method: 'PATCH' }),
  extendReservation: (id, extraHours = 1) => request(`/reservations/${id}/extend`, { method: 'POST', body: JSON.stringify({ extraHours }) }),
  checkExtension: (id, extraHours = 1) => request(`/reservations/${id}/check-extension?extraHours=${extraHours}`),
  getAllReservations: () => request('/reservations'),

  // Payments
  processPayment: (paymentData) => request('/payments', { method: 'POST', body: JSON.stringify(paymentData) }),
  getPaymentByReservation: (reservationId) => request(`/payments/reservation/${reservationId}`),
  getAllPayments: () => request('/payments'),

  // Admin
  getAdminStats: () => request('/admin/stats'),
  getAdminUsers: () => request('/admin/users'),
  toggleUserActive: (userId, isActive) => request(`/admin/users/${userId}/toggle-active?isActive=${isActive}`, { method: 'PATCH' }),
  sendTestAlert: (reservationId) => request(`/admin/send-test-alert?reservationId=${reservationId}`, { method: 'POST' }),
};

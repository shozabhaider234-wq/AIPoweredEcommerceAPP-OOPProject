import axios from 'axios';

const api = axios.create({
  baseURL: '/api'
});

// Attach JWT to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Global error normalisation
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const msg =
      err.response?.data?.message ||
      err.response?.data?.error ||
      err.message ||
      'Something went wrong';
    err.userMessage = msg;
    return Promise.reject(err);
  }
);

/* ─── Auth ─────────────────────────────────────────────────── */
export const authAPI = {
  login:    (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  profile:  ()     => api.get('/users/profile'),
  updateProfile: (data) => api.patch('/users/profile', data),
};

/* ─── Products ─────────────────────────────────────────────── */
export const productAPI = {
  list:   (page = 0, size = 12, sortBy = 'name') =>
    api.get(`/products?page=${page}&size=${size}&sortBy=${sortBy}`),

  search: (params) => {
    const q = new URLSearchParams();
    //This creates an object to build URL query strings safely.
    if (params.name)       q.set('name', params.name);
    if (params.categoryId) q.set('categoryId', params.categoryId);
    if (params.minPrice)   q.set('minPrice', params.minPrice);
    if (params.maxPrice)   q.set('maxPrice', params.maxPrice);
    q.set('page',   params.page   ?? 0);
    //?? = nullish coalescing operator, it assigns the value on the right if the value on the left is null or undefined.
    q.set('size',   params.size   ?? 12);
    q.set('sortBy', params.sortBy ?? 'price');
    return api.get(`/products/search?${q}`);
  },

  getById:  (id, userId) => api.get(`/products/${id}${userId ? `?userId=${userId}` : ''}`),
  create:   (data)       => api.post('/products', data),
  update:   (id, data)   => api.put(`/products/${id}`, data),
  delete:   (id)         => api.delete(`/products/${id}`),
  uploadImage: (id, file) => {
    const form = new FormData();
    form.append('file', file);
    return api.post(`/products/${id}/images`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  sellerProducts: (page=0,size=12) => api.get(`/sellers/myproducts?page=${page}&?size=${size}`),
};

/* ─── Categories ────────────────────────────────────────────── */
export const categoryAPI = {
  list:   ()           => api.get('/categories'),
  create: (data)       => api.post('/categories', data),
  update: (id, data)   => api.put(`/categories/${id}`, data),
  delete: (id)         => api.delete(`/categories/${id}`),
};

/* ─── Cart ──────────────────────────────────────────────────── */
export const cartAPI = {
  get:       ()                     => api.get('/cart'),
  addItem:   (productId, quantity)  => api.post(`/cart/items?productId=${productId}&quantity=${quantity}`),
  updateItem:(itemId, quantity)     => api.put(`/cart/items/${itemId}?quantity=${quantity}`),
  removeItem:(itemId)               => api.delete(`/cart/items/${itemId}`),
};

/* ─── Orders (customer) ─────────────────────────────────────── */
export const orderAPI = {
  place:    (data)        => api.post('/orders', data),
  list:     (page = 0)    => api.get(`/orders?page=${page}&size=10`),
  getById:  (id)          => api.get(`/orders/${id}`),
  cancel:   (id)          => api.put(`/orders/${id}/cancel`),
};

/* ─── Seller orders ─────────────────────────────────────────── */
export const sellerOrderAPI = {
  list:         (page = 0)         => api.get(`/seller/orders?page=${page}&size=10`),
  getById:      (id)               => api.get(`/seller/orders/${id}`),
  updateStatus: (id, data)         => api.put(`/seller/orders/${id}/status`, data),
  cancel:       (id, data)         => api.put(`/seller/orders/${id}/cancel`, data),
};

/* ─── Seller profile ────────────────────────────────────────── */
export const sellerAPI = {
  create:   (data) => api.post('/sellers', data),
  update:   (data) => api.put('/sellers/me', data),
  getMe:    ()     => api.get('/sellers/me'),
  getById:  (id)   => api.get(`/sellers/${id}`),
};

/* ─── Reviews ───────────────────────────────────────────────── */
export const reviewAPI = {
  list:   (productId, page = 0) => api.get(`/products/${productId}/reviews?page=${page}&size=10`),
  add:    (productId, data)     => api.post(`/products/${productId}/reviews`, data),
  update: (reviewId, data)      => api.put(`/reviews/${reviewId}`, data),
  delete: (reviewId)            => api.delete(`/reviews/${reviewId}`),
};

/* ─── Wishlist ──────────────────────────────────────────────── */
export const wishlistAPI = {
  get:    ()           => api.get('/wishlist'),
  add:    (productId)  => api.post(`/wishlist/${productId}`),
  remove: (productId)  => api.delete(`/wishlist/${productId}`),
};

/* ─── AI Chat ───────────────────────────────────────────────── */
export const chatAPI = {
  send: (userId, message) => api.post('/chat', { userId, message }),
};

export default api;

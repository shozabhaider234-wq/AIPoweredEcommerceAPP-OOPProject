import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import Navbar from './components/common/Navbar';
import ChatWidget from './components/common/ChatWidget';
import { Spinner } from './components/common/UI';

// Pages
import HomePage              from './pages/HomePage';
import { LoginPage, RegisterPage } from './pages/AuthPages';
import ShopPage              from './pages/ShopPage';
import ProductDetailPage     from './pages/ProductDetailPage';
import { CartPage, CheckoutPage } from './pages/CartCheckoutPages';
import { OrdersPage, OrderDetailPage } from './pages/OrderPages';
import WishlistPage          from './pages/WishlistPage';
import ProfilePage           from './pages/ProfilePage';
import SellerProductsPage    from './pages/SellerProductsPage';
import SellerOrdersPage      from './pages/SellerOrdersPage';
import SellerProfilePage     from './pages/SellerProfilePage';

/* ─── Auth Guards ───────────────────────────────────────────── */
function RequireAuth({ children, role }) {
  const { isLoggedIn, user, loading } = useAuth();
  const location = useLocation();

  if (loading) return (
    <div style={{ minHeight: '60vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <Spinner size={32} />
    </div>
  );

  if (!isLoggedIn) return <Navigate to="/login" state={{ from: location }} replace />;
  if (role && user?.role !== role) return <Navigate to="/" replace />;
  return children;
}

function GuestOnly({ children }) {
  const { isLoggedIn, loading, user } = useAuth();
  if (loading) return null;
  if (isLoggedIn) return <Navigate to={user?.role === 'SELLER' ? '/seller/products' : '/shop'} replace />;
  return children;
}

/* ─── Shell (Navbar + routes) ────────────────────────────────── */
function AppShell() {
  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar />
      <div style={{ flex: 1 }}>
        <Routes>

          {/* Public */}
          <Route path="/"        element={<HomePage />} />
          <Route path="/shop"    element={<ShopPage />} />
          <Route path="/product/:id" element={<ProductDetailPage />} />

          {/* Auth */}
          <Route path="/login"    element={<GuestOnly><LoginPage /></GuestOnly>} />
          <Route path="/register" element={<GuestOnly><RegisterPage /></GuestOnly>} />

          {/* Customer */}
          <Route path="/cart"     element={<RequireAuth role="CUSTOMER"><CartPage /></RequireAuth>} />
          <Route path="/checkout" element={<RequireAuth role="CUSTOMER"><CheckoutPage /></RequireAuth>} />
          <Route path="/orders"   element={<RequireAuth role="CUSTOMER"><OrdersPage /></RequireAuth>} />
          <Route path="/orders/:id" element={<RequireAuth role="CUSTOMER"><OrderDetailPage /></RequireAuth>} />
          <Route path="/wishlist" element={<RequireAuth role="CUSTOMER"><WishlistPage /></RequireAuth>} />
          <Route path="/profile"  element={<RequireAuth role="CUSTOMER"><ProfilePage /></RequireAuth>} />

          {/* Seller */}
          <Route path="/seller/products" element={<RequireAuth role="SELLER"><SellerProductsPage /></RequireAuth>} />
          <Route path="/seller/orders"   element={<RequireAuth role="SELLER"><SellerOrdersPage /></RequireAuth>} />
          <Route path="/seller/profile"  element={<RequireAuth role="SELLER"><SellerProfilePage /></RequireAuth>} />

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
      <ChatWidget />
    </div>
  );
}

/* ─── Root App ───────────────────────────────────────────────── */
export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <AppShell />
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 3500,
              style: {
                fontFamily: 'var(--font-body)',
                fontSize: 14,
                background: 'var(--surface)',
                color: 'var(--ink)',
                border: '1px solid var(--border)',
                borderRadius: 8,
                boxShadow: 'var(--shadow-md)',
              },
              success: { iconTheme: { primary: '#2d7a4f', secondary: '#fff' } },
              error:   { iconTheme: { primary: '#b91c1c', secondary: '#fff' } },
            }}
          />
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

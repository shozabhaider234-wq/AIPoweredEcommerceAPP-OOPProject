import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { ShoppingCart, Heart, User, LogOut, Package, Store, Menu, X, Search } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';

export default function Navbar() {
  const { user, isLoggedIn, logout, isCustomer, isSeller } = useAuth();
  const { itemCount } = useCart() || {};
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => { logout(); navigate('/'); };

  const navLinks = isCustomer ? [
    { to: '/shop', label: 'Shop' },
    { to: '/wishlist', label: 'Wishlist' },
    { to: '/orders', label: 'Orders' },
  ] : isSeller ? [
    { to: '/seller/products', label: 'Products' },
    { to: '/seller/orders', label: 'Orders' },
  ] : [
    { to: '/shop', label: 'Shop' },
  ];

  return (
    <nav style={{
      background: 'var(--green-bg)',
      borderBottom: '1px solid var(--border)',
      position: 'sticky',
      top: 0,
      zIndex: 200,
    }}>
      <div className="container" style={{ display: 'flex', alignItems: 'center', height: 64, gap: 24 }}>

        {/* Logo */}
        <Link to="/" style={{
          fontFamily: 'var(--font-display)',
          fontWeight: 800,
          fontSize: 20,
          color: 'var(--ink)',
          letterSpacing: '-0.03em',
          flexShrink: 0,
        }}>
          BAZAAR<span style={{ color: 'var(--accent)' }}>.</span>
        </Link>

        {/* Desktop nav */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 4, flex: 1 }} className="desktop-nav">
          {navLinks.map(l => (
            <Link key={l.to} to={l.to} style={{
              padding: '6px 14px',
              borderRadius: 'var(--radius-md)',
              fontSize: 14,
              fontWeight: 500,
              color: location.pathname.startsWith(l.to) ? 'var(--ink)' : 'var(--ink-muted)',
              background: location.pathname.startsWith(l.to) ? 'var(--surface-3)' : 'transparent',
              transition: 'all var(--transition)',
            }}>
              {l.label}
            </Link>
          ))}
        </div>

        {/* Right actions */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginLeft: 'auto' }}>
          {isCustomer && (
            <>
              <Link to="/cart" style={{ position: 'relative' }}>
                <button className="btn btn-ghost" style={{ padding: 8 }}>
                  <ShoppingCart size={20} />
                  {itemCount > 0 && (
                    <span style={{
                      position: 'absolute', top: 2, right: 2,
                      background: 'var(--accent)', color: '#fff',
                      borderRadius: '50%', width: 17, height: 17,
                      fontSize: 10, fontWeight: 700,
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                    }}>{itemCount > 9 ? '9+' : itemCount}</span>
                  )}
                </button>
              </Link>
              <Link to="/wishlist">
                <button className="btn btn-ghost" style={{ padding: 8 }}><Heart size={20} /></button>
              </Link>
            </>
          )}

          {isLoggedIn ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Link to={isSeller ? '/seller/profile' : '/profile'}>
                <button className="btn btn-ghost" style={{ padding: '6px 12px', gap: 8 }}>
                  <div style={{
                    width: 28, height: 28, borderRadius: '50%',
                    background: 'var(--accent)', color: 'var(--surface-2)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 12, fontWeight: 700, fontFamily: 'var(--font-display)',
                    flexShrink: 0,
                  }}>
                    {user?.name?.[0]?.toUpperCase()}
                  </div>
                  <span style={{ fontSize: 14, fontWeight: 500, maxWidth: 100, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {user?.name?.split(' ')[0]}
                  </span>
                </button>
              </Link>
              <button className="btn btn-ghost" style={{ padding: 8 }} onClick={handleLogout} title="Logout">
                <LogOut size={18} />
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', gap: 8 }}>
              <Link to="/login"><button className="btn btn-outline btn-sm">Login</button></Link>
              <Link to="/register"><button className="btn btn-primary btn-sm">Sign up</button></Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}
